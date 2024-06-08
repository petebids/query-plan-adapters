package dev.cerbos.springdataspecificationadapter


import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.domain.Specification
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.MountableFile
import java.time.Duration
import java.time.temporal.ChronoUnit

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig::class)
class SpringDataSpecificationAdapterIntegrationTests {

    // refactor this to  https://java.testcontainers.org/modules/docker_compose/
    companion object {
        @Container
        val cerbos: GenericContainer<*> = GenericContainer("cerbos/cerbos:0.36.0").apply {
            this.withExposedPorts(3593)
            this.withCopyToContainer(MountableFile.forClasspathResource("resource.yaml"), "/policies/resource.yaml")
            this.withCopyToContainer(MountableFile.forClasspathResource("cerbos_config.yaml"), "config.yaml")
            this.setCommand("server", "--config=config.yaml")
            this.waitingFor(
                LogMessageWaitStrategy()
                    .withRegEx(".*Starting gRPC server.*")
                    .withStartupTimeout(
                        Duration.of(10L, ChronoUnit.SECONDS)
                    )
            )
        }

        @Container
        val database: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15.2").apply {
            this.withPassword("postgres").withUsername("postgres")
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", database::getJdbcUrl)
            registry.add("spring.datasource.username", database::getUsername)
            registry.add("spring.datasource.password", database::getPassword)
            registry.add("spring.datasource.driver-class-name", database::getDriverClassName)
            registry.add("spring.jpa.databasePlatform") { "org.hibernate.dialect.PostgreSQLDialect" }
            val cerbosAddress : String = cerbos.host + ":" +cerbos.getMappedPort(3593)

            registry.add("cerbos.address") { cerbosAddress}
        }


        @BeforeAll
        @JvmStatic
        fun start() {
            database.start()
            cerbos.start()
        }
    }


    @Autowired
    private lateinit var resourceRepository: ResourceRepository

    @Autowired
    private lateinit var resourceSpecificationGenerator: ResourceSpecificationGenerator


    @ParameterizedTest
    @CsvSource(value = ["always-allow", "explicit-deny", "and", "equal", "ne", "and", "or", "nand", "nor", "in", "gt", "gte", "lt", "lte", "equal-nested", "relation-some", "relation-is-not"])
    fun `smoke test with all actions`(action: String) {


        val specification: Specification<Resource> = resourceSpecificationGenerator.specificationFor(
            id = "principal", resource = "resource", action = action

        )
        resourceRepository.findAll(specification)


    }


    @Test
    fun `always-deny should throw a runtime exception`() {

        assertThrows<RuntimeException> {
            resourceSpecificationGenerator.specificationFor(
                id = "principal", resource = "resource", action = "always-deny"

            )
        }

    }

}
