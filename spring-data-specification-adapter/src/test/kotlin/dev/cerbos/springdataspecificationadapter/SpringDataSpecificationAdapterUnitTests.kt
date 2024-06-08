package dev.cerbos.springdataspecificationadapter

import com.google.protobuf.util.JsonFormat
import dev.cerbos.api.v1.engine.Engine.PlanResourcesFilter.Expression.Operand
import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.CerbosClientBuilder
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension


@ExtendWith(MockitoExtension::class)
class SpringDataSpecificationAdapterUnitTests : SpringDataSpecificationAdapterTests {


    @Mock
    lateinit var r : Root<Resource>

    @Mock
    lateinit var cq: CriteriaQuery<Resource>

    @Mock
    lateinit var cb: CriteriaBuilder

    @Captor
    lateinit var predicateCaptor: ArgumentCaptor<Predicate>

    val cerbos: CerbosBlockingClient =
        CerbosClientBuilder("localhost:3593").withPlaintext().withInsecure().buildBlockingClient()

    val principalRepository = MockPrincipalRepository()

    val resourceSpecificationGenerator = ResourceSpecificationGenerator(cerbos, principalRepository, emptyMap())

    @Disabled

    @Test
    override fun `always-allow`() {

        val specificationFor = resourceSpecificationGenerator.specificationFor(
            id = "principal",
            resource = "resource",
            action = "always-allow"
        )

        val predicate = specificationFor.toPredicate(r, cq, cb)

        verify(cb).literal("1")

    }


    @Disabled
    @Test
    override fun `always-deny`() {

        val opBuilder = Operand.newBuilder()
        JsonFormat.parser().ignoringUnknownFields().merge(
            """
                "expression": {
    "operator": "eq",
    "operands": [{
      "variable": "request.resource.attr.aBool"
    }, {
      "value": true
    }]
  }
}
          """, opBuilder
        )

        opBuilder.build()


    }

    override fun `explicit-deny`() {
        TODO("Not yet implemented")
    }

    override fun equals() {
        TODO("Not yet implemented")
    }

    override fun ne() {
        TODO("Not yet implemented")
    }

    override fun and() {
        TODO("Not yet implemented")
    }

    override fun or() {
        TODO("Not yet implemented")
    }

    override fun nand() {
        TODO("Not yet implemented")
    }

    override fun nor() {
        TODO("Not yet implemented")
    }

    override fun `in`() {
        TODO("Not yet implemented")
    }

    override fun gt() {
        TODO("Not yet implemented")
    }

    override fun lt() {
        TODO("Not yet implemented")
    }

    override fun gte() {
        TODO("Not yet implemented")
    }

    override fun lte() {
        TODO("Not yet implemented")
    }

    override fun `equal-nested`() {
        TODO("Not yet implemented")
    }

    override fun `relation-is-not`() {
        TODO("Not yet implemented")
    }

    override fun `relation-some`() {
        TODO("Not yet implemented")
    }
}