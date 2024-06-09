package io.petebids.cerbos.queryplan.jpa.adapter

import dev.cerbos.sdk.CerbosBlockingClient

class ResourceSpecificationAdapter(
    cerbos: CerbosBlockingClient,
    principalRepository: PrincipalRepository,
    policyPathToType: Map<String, Class<*>>
) : JpaSpecificationAdapter<Resource>(cerbos, principalRepository, policyPathToType)
