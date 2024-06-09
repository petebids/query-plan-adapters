package io.petebids.cerbos.queryplan.jpa.adapter

import dev.cerbos.sdk.builders.Principal
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull

class MockPrincipalRepository : PrincipalRepository {
    override fun retrievePrincipalById(id: String): Principal? {
        return Principal.newInstance(id).withRoles("USER")
    }
}


class JpaPrincipalRepository (private val userRepository: UserRepository): PrincipalRepository {
    override fun retrievePrincipalById(id: String): Principal? {
        return userRepository.findByIdOrNull(id)?.let {
            Principal.newInstance(it.id).withRoles()
        }
    }

}


interface UserRepository : CrudRepository<User, String>