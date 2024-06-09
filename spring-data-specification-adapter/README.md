# Why does this exist

Query plan adapters convert the output of the Cerbos Plan API into something ORM-specific so the decision made by
the Cerbos PDP can be enforced in your database
In the case of JPA & Spring, I've chosen the ```org.springframework.data.jpa.domain.Specification<T>```
interface to build around 


# Should I use this in production?

absolutely not - yet



# How do I use it? 

Given a JPA entity 
```kotlin
package io.petebids.cerbos.queryplan.jpa.adapter

import jakarta.persistence.CascadeType
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
data class Resource(
    @Id val id: String,
    val aBool: Boolean,
    val name: String,
    val aString: String,
    val aNumber: Int,
    val createdBy: String,
    @ElementCollection(fetch = FetchType.EAGER)
    val ownedBy: Set<String>,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    val nested: Nested,
)

@Entity
data class Nested(
    @Id val id: String,
    val aBool: Boolean,
)

```

and it's corresponding repository that extends ```JpaSpecificationExecutor```
```kotlin
@Repository
interface ResourceRepository : CrudRepository<Resource, String>, JpaSpecificationExecutor<Resource>
```
You declare a Specification Adapter by providing the resource as a type parameter 


```kotlin
@Component
class ResourceSpecificationAdapter(
    cerbos: CerbosBlockingClient,
    principalRepository: PrincipalRepository,
    policyPathToType: Map<String, Class<*>>
) : JpaSpecificationAdapter<Resource>(cerbos, principalRepository, policyPathToType)

```

and implement the ```io.petebids.cerbos.queryplan.jpa.adapter.PrincipalRepository```  to fetch your principal objects for your Source of truth, cache - etc 

```kotlin
fun interface PrincipalRepository {

    fun retrievePrincipalById(id: String): dev.cerbos.sdk.builders.Principal?
}


@Repository
class SpringSecurityPrincipalRepository  : PrincipalRepository{
    override fun retrievePrincipalById(id: String): Principal? {
        val jwt = SecurityContextHolder.getContext().authentication.principal as Jwt
        Assert.isTrue(id == jwt.subject, "Principal & JWT subject must match")

        return Principal.newInstance(id).withRoles(customRoleProcessor(jwt))
    }
}




@Repository
class JpaPrincipalRepository (private val userRepository: UserRepository): PrincipalRepository {
    override fun retrievePrincipalById(id: String): Principal? {
        return userRepository.findByIdOrNull(id)?.let {
            Principal.newInstance(it.id).withRoles()
        }
    }

}




@Entity
data class User(
    @Id val id: String,

    )

interface UserRepository : CrudRepository<User, String>

```

then use the specification adapter to provide a Specification to filter your data 

```kotlin
    
    val specification: Specification<Resource> = resourceSpecificationAdapter.specificationFor(id = principalId, resource = "resource", action = "view")
            
    val resources: List<Resource> = resourceRepository.findAll(specification)

```

Once you have your secure filter, you can then add user-supplied parameters as well

```kotlin

    val specification: Specification<Resource> = resourceSpecificationAdapter.specificationFor(id = principalId, resource = "resource", action = "view")

    
    specification.and { root, _, criteriaBuilder ->
            criteriaBuilder.equal(root.get<String>("aString"), "aUserSuppliedQueryParameter")
        }
        
    val resources: List<Resource> = resourceRepository.findAll(specification)

```
