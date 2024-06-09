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


@Entity
data class User(
    @Id val id: String,

    )

