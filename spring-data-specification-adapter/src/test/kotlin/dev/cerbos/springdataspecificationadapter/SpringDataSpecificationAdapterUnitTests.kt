package dev.cerbos.springdataspecificationadapter

import com.jayway.jsonpath.Criteria
import dev.cerbos.api.v1.engine.Engine
import dev.cerbos.api.v1.engine.Engine.PlanResourcesFilter.Expression.Operand
import dev.cerbos.api.v1.response.Response
import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.PlanResourcesResult
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SpringDataSpecificationAdapterUnitTests : SpringDataSpecificationAdapterTests {


    @Mock
    lateinit var r : Root<Resource>

    @Mock
    lateinit var cq: CriteriaQuery<Resource>

    @Mock
    lateinit var cb: CriteriaBuilder

    @Mock
    lateinit var cerbos : CerbosBlockingClient

    @Mock
    lateinit var principalRepository: MockPrincipalRepository

    val resourceSpecificationGenerator = ResourceSpecificationGenerator(cerbos, principalRepository, emptyMap())

    override fun `always-allow`() {

        `when`(
            cerbos.plan(any(), any(), any())
        ).then {

        }


    }

    override fun `always-deny`() {
        TODO("Not yet implemented")
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