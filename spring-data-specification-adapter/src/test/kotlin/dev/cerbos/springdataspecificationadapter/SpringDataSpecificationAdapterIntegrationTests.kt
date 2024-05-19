package dev.cerbos.springdataspecificationadapter


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.domain.Specification
import org.testcontainers.containers.GenericContainer

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig::class)
class SpringDataSpecificationAdapterIntegrationTests : PostgresJpaTestcontainers(),
    SpringDataSpecificationAdapterTests {

    companion object {

        @JvmField
        val cerbos = GenericContainer("ghcr.io/cerbos/cerbos:dev")
            .withExposedPorts(3592)
            .setCommand("server")

    }


    @Autowired
    private lateinit var resourceRepository: ResourceRepository

    @Autowired
    private lateinit var resourceSpecificationGenerator: ResourceSpecificationGenerator

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `always-allow`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "always-allow"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `always-deny`() {

        assertThrows<RuntimeException> {
            resourceSpecificationGenerator.specificationFor(
                id = "principal", resource = "resource", action = "always-deny"

            )
        }

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `explicit-deny`() {


        val specification = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "explicit-deny"

        )
        /*
        assertEquals(not(Specification<Resource> { root, query, criteriaBuilder ->
            criteriaBuilder.equal(criteriaBuilder.literal("1"), criteriaBuilder.literal("1"))
        }), specification)


         */
        resourceRepository.findAll(specification)
    }


    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `equals`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "equal"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `ne`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "ne"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `and`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "and"
        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `or`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "or"

        )
        resourceRepository.findAll(specification)

    }

    /*
    * operator: "not"
  operands {
    expression {
      operator: "and"
      operands {
        expression {
          operator: "eq"
          operands {
            variable: "request.resource.attr.aBool"
          }
          operands {
            value {
              bool_value: true
            }
          }
        }
      }
      operands {
        expression {
          operator: "ne"
          operands {
            variable: "request.resource.attr.aString"
          }
          operands {
            value {
              string_value: "string"
            }
          }
        }
      }
    }
  }
}
    select
        r1_0.id,
        r1_0.a_bool,
        r1_0.a_number,
        r1_0.a_string,
        r1_0.created_by,
        r1_0.nested_id
    from
        resource r1_0
    where
        not(r1_0.a_bool=true
        and r1_0.a_string<>'string')
*
    *
     */
    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `nand`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "nand"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `nor`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "nor"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `in`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "in"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `gt`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "gt"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `lt`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "lt"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `gte`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "gte"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `lte`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "lte"

        )
        resourceRepository.findAll(specification)

    }


    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `equal-nested`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "equal-nested"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `relation-is-not`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "relation-is-not"

        )
        resourceRepository.findAll(specification)

    }

    @Suppress("RemoveRedundantBackticks")
    @Test
    override fun `relation-some`() {

        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = "relation-some"

        )
        resourceRepository.findAll(specification)

    }


}
