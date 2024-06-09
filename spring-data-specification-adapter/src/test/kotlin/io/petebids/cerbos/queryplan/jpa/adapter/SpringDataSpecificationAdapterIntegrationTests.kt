package io.petebids.cerbos.queryplan.jpa.adapter


import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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
import java.util.stream.Stream

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig::class)
class SpringDataSpecificationAdapterIntegrationTests {

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
            val cerbosAddress : String = cerbos.host + ":" + cerbos.getMappedPort(3593)

            registry.add("cerbos.address") { cerbosAddress}
        }


        @BeforeAll
        @JvmStatic
        fun start() {
            database.start()
            cerbos.start()

        }

        @JvmStatic
        fun testData(): Stream<TestCase> =
            Stream.of(
                TestCase(action = "always-allow", expectedResultCount = 3, validator = { _ -> true }),
                TestCase(action = "always-deny", expectedResultCount = 0),
                TestCase(
                    "explicit-deny",
                    expectedResultCount = 1,
                    validator = { resources -> resources.count { !it.aBool } == 1 }),
                TestCase(
                    "and",
                    expectedResultCount = 1,
                    validator = { resources -> resources.count { it.name == "resource3" } == 1 }),
                TestCase(
                    "equal",
                    expectedResultCount = 2,
                    validator = { resources -> resources.count { it.name == "resource1" || it.name == "resource3" } == 2 }),
                TestCase(
                    "ne",
                    expectedResultCount = 2,
                    validator = { resources -> resources.count { it.name == "resource2" || it.name == "resource3" } == 2 }),
                TestCase("or", expectedResultCount = 3),
                TestCase("nand", expectedResultCount = 2),
                TestCase("nor", expectedResultCount = 0),
                TestCase("in", expectedResultCount = 2),
                TestCase(
                    "gt",
                    expectedResultCount = 2,
                    validator = { resources -> resources.count { it.name == "resource2" || it.name == "resource3" } == 2 }),
                TestCase("gte", expectedResultCount = 3),
                TestCase("lt", expectedResultCount = 1),
                TestCase(
                    "lte",
                    expectedResultCount = 2,
                    validator = { resources -> resources.count { it.name == "resource1" || it.name == "resource2" } == 2 }),
                TestCase("equal-nested", expectedResultCount = 2,
                    validator = { resources -> resources.count { it.name == "resource1" || it.name == "resource3" } == 2 }),
                TestCase("relation-some", expectedResultCount = 2,
                    validator = { resources -> resources.count { it.name == "resource1" || it.name == "resource2" } == 2 }),
                TestCase(
                    "relation-none",
                    expectedResultCount = 1,
                    validator = { resources -> resources.count { it.name == "resource3" } == 1 }),
                TestCase("relation-is-not", expectedResultCount = 2,
                    validator = { resources -> resources.count { it.name == "resource2" || it.name == "resource3" } == 2 }),
            )

    }


    @Autowired
    private lateinit var resourceRepository: ResourceRepository

    @Autowired
    private lateinit var resourceSpecificationAdapter: ResourceSpecificationAdapter

    @BeforeEach
    fun setup() {
        resourceRepository.saveAll(
            listOf(
                Resource(
                    id = "1",
                    aBool = true,
                    name = "resource1",
                    aString = "string",
                    aNumber = 1,
                    createdBy = "1",
                    ownedBy = setOf("1"),
                    nested = Nested(id = "nested1", aBool = true)
                ),
                Resource(
                    id = "2",
                    aBool = false,
                    name = "resource2",
                    aString = "amIAString?",
                    aNumber = 2,
                    createdBy = "2",
                    ownedBy = setOf("1"),
                    nested = Nested(id = "nested2", aBool = false)
                ),
                Resource(
                    id = "3",
                    aBool = true,
                    name = "resource3",
                    aString = "anotherString",
                    aNumber = 3,
                    createdBy = "2",
                    ownedBy = setOf("2"),
                    nested = Nested(id = "nested3", aBool = true)
                )
            )
        )
    }


    @AfterEach
    fun cleanup() {
        resourceRepository.deleteAll()
    }


    @ParameterizedTest
    @MethodSource("testData")
    fun `smoke test with all actions`(testCase: TestCase) {

        val specification: Specification<Resource> = resourceSpecificationAdapter.specificationFor(
            id = testCase.principal, resource = "resource", action = testCase.action

        )

        val resources: List<Resource> = resourceRepository.findAll(specification)

        assertEquals(testCase.expectedResultCount, resources.size)

        testCase.validator?.let {
            assert(it.invoke(resources))
        }
    }

    // use this class as a short dev loop for iterating on a new case before pulling it up into io.petebids.cerbos.queryplan.jpa.adapter.SpringDataSpecificationAdapterIntegrationTests.Companion.testData
    @Test
    fun `test one`() {

        val testCase = TestCase("relation-some", expectedResultCount = 2,
            validator = { resources -> resources.count { it.name == "resource1" || it.name == "resource2" } == 2 })


        val specification: Specification<Resource> = resourceSpecificationAdapter.specificationFor(
            id = testCase.principal, resource = "resource", action = testCase.action

        )
        val resources: List<Resource> = resourceRepository.findAll(specification)

        assertEquals(testCase.expectedResultCount, resources.size)

        testCase.validator?.let {
            assert(it.invoke(resources))
        }
    }


}

fun printingValidator(): PostTestValidator = { resources -> resources.forEach { println(it) }; true }
typealias PostTestValidator = (List<Resource>) -> Boolean

data class TestCase(
    val action: String,
    val principal: String = "1",
    val expectedResultCount: Int,
    val validator: PostTestValidator? = printingValidator(),
)