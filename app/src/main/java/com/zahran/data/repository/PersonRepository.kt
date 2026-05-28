package com.zahran.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zahran.data.mapper.toSerializableObject
import com.zahran.data.model.Person
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val familyCollection = firestore.collection("family")

    /**
     * Fetches members of a generation belonging to the specified parent.
     * Uses snapshotListener to serve from cache immediately and then update from network.
     */
    fun getFamilyMembers(parentId: String): Flow<List<Person>> = callbackFlow {
        val listener = familyCollection
            .whereEqualTo("parentId", parentId)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val members = snapshot?.documents?.mapNotNull { doc ->
                    doc.toSerializableObject<Person>()
                } ?: emptyList()
                
                trySend(members)
            }
            
        awaitClose { listener.remove() }
    }

    /**
     * Returns the total count of family members registered in the tree.
     */
    fun getFamilyCount(): Flow<Int> = callbackFlow {
        val listener = familyCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val count = snapshot?.size() ?: 0
            trySend(count)
        }
        awaitClose { listener.remove() }
    }
}
