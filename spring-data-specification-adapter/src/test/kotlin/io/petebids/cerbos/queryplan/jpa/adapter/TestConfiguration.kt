package io.petebids.cerbos.queryplan.jpa.adapter

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.CerbosClientBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfig {


    @Bean
    fun mockPrincipalRepo(): MockPrincipalRepository = MockPrincipalRepository()

    @Bean
    fun cerbos(cerbosConfigProps: CerbosConfigProps): CerbosBlockingClient =
        CerbosClientBuilder(cerbosConfigProps.address).withPlaintext().withInsecure().buildBlockingClient()

    @Bean
    fun resourceSpecificationGenerator(
        cerbos: CerbosBlockingClient,
        principalRepository: MockPrincipalRepository
    ): ResourceSpecificationAdapter = ResourceSpecificationAdapter(
        cerbos, principalRepository, mapOf(
            "request.resource.attr.createdBy" to String::class.java,
            "request.resource.attr.aBool" to Boolean::class.java,
            "request.resource.attr.nested.aBool" to Boolean::class.java,
            "request.resource.attr.aString" to String::class.java,
            "request.resource.attr.aNumber" to Number::class.java
        )
    )
}