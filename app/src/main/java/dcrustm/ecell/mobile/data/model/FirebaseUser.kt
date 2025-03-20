package dcrustm.ecell.mobile.data.model

import com.google.firebase.auth.FirebaseUser
import dcrustm.ecell.mobile.domain.model.User

//fun FirebaseUser.toDomainUser(): User {
//    return User(
//        id = this.uid,
//        name = displayName ?: email?.substringBefore('@') ?: "Unknown",
//        email = email ?: "",
//        photoUrl = photoUrl?.toString()
//    )
//}