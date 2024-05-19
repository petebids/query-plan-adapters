package dev.cerbos.springdataspecificationadapter

import dev.cerbos.sdk.CerbosBlockingClient

class ResourceSpecificationGenerator(
    cerbos: CerbosBlockingClient,
    principalRepository: PrincipalRepository,
    policyPathToType: Map<String, Class<*>>
) : BaseCerbosAuthZSpecificationGeneratorV2<Resource>(cerbos, principalRepository, policyPathToType)
