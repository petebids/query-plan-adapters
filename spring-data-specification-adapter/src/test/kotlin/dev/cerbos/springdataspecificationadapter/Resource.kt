package dev.cerbos.springdataspecificationadapter

import jakarta.persistence.CascadeType
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import java.util.UUID

@Entity
data class Resource(
    @Id val id: UUID,
    val aBool: Boolean,
    val aString: String,
    val aNumber: Number,
    val createdBy: String,
    @ElementCollection
    val ownedBy: Set<String>,
    @OneToOne(cascade = [CascadeType.ALL])
    val nested: Nested,
)

@Entity
data class Nested(
    @Id val id: UUID,
    val aBool: Boolean,
)