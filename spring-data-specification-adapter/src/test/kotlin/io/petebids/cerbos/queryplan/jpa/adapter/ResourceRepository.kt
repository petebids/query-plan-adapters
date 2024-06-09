package io.petebids.cerbos.queryplan.jpa.adapter

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ResourceRepository : CrudRepository<Resource, String>, JpaSpecificationExecutor<Resource>
