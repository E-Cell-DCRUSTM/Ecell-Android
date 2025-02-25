package dcrustm.ecell.mobile.domain.dummy


// Repository interface that defines the contract for DataStore operations
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    // Flow to observe the on-boarding completion status
    val isOnBoardingCompleted: Flow<Boolean>

    // Function to update the on-boarding completion value
    suspend fun updateOnBoardingCompleted(completed: Boolean)

}