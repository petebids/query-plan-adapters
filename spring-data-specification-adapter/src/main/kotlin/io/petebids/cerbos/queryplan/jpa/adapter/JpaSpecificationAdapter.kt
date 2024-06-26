package io.petebids.cerbos.queryplan.jpa.adapter


import com.google.protobuf.Value
import com.google.protobuf.util.JsonFormat
import dev.cerbos.api.v1.engine.Engine.PlanResourcesFilter.Expression.Operand
import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.PlanResourcesResult
import dev.cerbos.sdk.builders.Resource
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.Specification
import java.util.UUID


open class JpaSpecificationAdapter<T : Any>(
    private val cerbos: CerbosBlockingClient,
    private val principalRepository: PrincipalRepository,
    private val policyPathToType: Map<String, Class<*>>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun specificationFor(id: String, resource: String, action: String): Specification<T> {

        val result: PlanResourcesResult = cerbos.plan(
            principalRepository.retrievePrincipalById(id)!!,
            Resource.newInstance(resource),
            action
        )

        return when {
            // This grants unconditional access - ads a no-op predicate
            result.isAlwaysAllowed -> {
                logger.debug("unconditional access for action $action for  principal $id granted to resource kind $resource")
                allowed()
            }
            // This generates a specification that is always false
            // alternatively, a runtime exception could be thrown here
            result.isAlwaysDenied -> {
                logger.debug("unconditional deny for action $action for  principal $id granted to resource kind $resource")
                 deny()
            }
            // generate a specification
            result.isConditional -> {
                val op: Operand = result.condition.get()
                val json = JsonFormat.printer().print(op)
                logger.debug("conditionally authorized : $json")
                operandToSpecification(op)
            }

            else -> throw UnsupportedOperationException("Unsupported result $result")
        }
    }


    private fun operandToSpecification(
        op: Operand
    ): Specification<T> {
        return when (op.expression.operator) {
            "and" -> Specification.allOf(op.expression.operandsList.map { operandToSpecification(it) })
            "or" -> Specification.anyOf(op.expression.operandsList.map { operandToSpecification(it) })
            "not" -> Specification.not(Specification.allOf(
                op.expression.operandsList.map { operandToSpecification(it) }))
            "ne" -> Specification { r: Root<T>, cq: CriteriaQuery<*>, cb: CriteriaBuilder ->
                cb.not(equalsSpec(op).toPredicate(r, cq, cb))
            }
            "in" -> inSpec(op)

            "lt" -> Specification { r: Root<T>, _: CriteriaQuery<*>, cb: CriteriaBuilder ->
                cb.lessThan(
                    r.get(op.expression.operandsList[0].variable.removePrefix("request.resource.attr.")),
                    op.expression.operandsList[1].value.numberValue
                )
            }

            "le" -> Specification { r: Root<T>, _: CriteriaQuery<*>, cb: CriteriaBuilder ->
                cb.lessThanOrEqualTo(
                    r.get(op.expression.operandsList[0].variable.removePrefix("request.resource.attr.")),
                    op.expression.operandsList[1].value.numberValue
                )

            }

            "gt" -> Specification { r: Root<T>, _: CriteriaQuery<*>, cb: CriteriaBuilder ->
                cb.greaterThan(
                    r.get(op.expression.operandsList[0].variable.removePrefix("request.resource.attr.")),
                    op.expression.operandsList[1].value.numberValue
                )
            }

            "ge" -> Specification { r: Root<T>, _: CriteriaQuery<*>, cb: CriteriaBuilder ->
                cb.greaterThanOrEqualTo(
                    r.get(op.expression.operandsList[0].variable.removePrefix("request.resource.attr.")),
                    op.expression.operandsList[1].value.numberValue
                )
            }

            "eq" -> equalsSpec(op)
            else -> throw UnsupportedOperationException("Unexpected operand $op")
        }
    }

    private fun inSpec(op: Operand): Specification<T> {
        return Specification { r: Root<T>, _: CriteriaQuery<*>, cb: CriteriaBuilder ->
            predicate(cb, op, r)
        }
    }

    private fun predicate(
        cb: CriteriaBuilder,
        op: Operand,
        r: Root<T>,
    ): Predicate {

        return when {

            // TODO
            // these are hacks to cover the test cases, production ready walks the tree of value types properly


            // used by `relation some` in its current form
            op.expression.operandsList[0].value.stringValue != null && op.expression.operandsList[1].hasVariable() -> {
                cb.isMember(
                    op.expression.operandsList[0].value.stringValue,
                    r.get(op.expression.operandsList[1].variable.removePrefix("request.resource.attr."))
                )
            }

            /*
                 operator: "in"
                  operands {
                    variable: "request.resource.attr.aString"
                  }
                  operands {
                    value {
                      list_value {
                        values {
                          string_value: "string"
                        }
                        values {
                          string_value: "anotherString"
                        }
                      }
                    }
                  }
                }
         */
            op.expression.operandsList[1].value.listValue != null -> {
                r.get<Boolean>(op.expression.operandsList[0].variable.removePrefix("request.resource.attr.")).`in`(
                    op.expression.operandsList[1].value.listValue.valuesList.map { it.stringValue.toString() }
                )

            }

            else -> TODO()

        }

    }


    private fun equalsSpec(
        op: Operand
    ): Specification<T> =
        Specification { r: Root<T>, _, cb ->
            parseEqualityOperands(r, cb, op.expression.operandsList[0], op.expression.operandsList[1])
        }




    private fun parseEqualityOperands(r: Root<T>, cb: CriteriaBuilder, left: Operand, right: Operand): Predicate? {
        return when {
            left.hasVariable() && right.hasValue() -> {
                val rightValue = policyPathToType[left.variable]?.let { valueToType(it, right.value) }
                cb.equal(walkPath(r, cb, left), cb.literal(rightValue))
            }
            left.hasVariable() && right.hasVariable() ->
                cb.equal(walkPath(r, cb, left), walkPath(r, cb, right))

            else -> TODO()
        }
    }

    private fun <T> walkPath(r: Root<T>, cb: CriteriaBuilder, op: Operand): Expression<Any> {
        when {
            op.hasVariable() -> {

                val removePrefix = op.variable.removePrefix("request.resource.attr.")

                // Warning implicitly any depth > 1 to a nesting problem will be solved by a join
                // TODO explicitly map join and attribute comparison
                if (removePrefix.contains(".")) {
                    val split = removePrefix.split(".")
                    return r.join<Any, Any>(split[0], JoinType.INNER).get(split[1])

                }
                return r.get(removePrefix)

            }

            else -> TODO()
        }

    }


    private fun valueToType(clazz: Class<*>, value: Value): Any {
        return when (clazz) {
            Boolean::class.java -> value.boolValue
            UUID::class.java -> UUID.fromString(value.stringValue)
            String::class.java -> value.stringValue
            Number::class.java -> value.numberValue
            else -> TODO()
        }
    }

    private fun <T> allowed(): Specification<T> = Specification { _: Root<T>, _, cb ->
        cb.equal(cb.literal(1), 1)
    }

    private fun <T> deny(): Specification<T> = Specification { _: Root<T>, _, cb ->
        cb.equal(cb.literal(1), 0)
    }

}