package dev.cerbos.springdataspecificationadapter

import org.junit.jupiter.api.Test

interface SpringDataSpecificationAdapterTests {
    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `always-allow`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `always-deny`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `explicit-deny`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `equals`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `ne`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `and`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `or`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `nand`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `nor`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `in`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `gt`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `lt`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `gte`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `lte`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `equal-nested`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `relation-is-not`()

    @Suppress("RemoveRedundantBackticks")
    @Test
    fun `relation-some`()
}