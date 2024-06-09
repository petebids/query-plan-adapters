package io.petebids.cerbos.queryplan.jpa.adapter

import dev.cerbos.sdk.builders.Principal

fun interface PrincipalRepository {

    fun retrievePrincipalById(id: String): Principal?
}
