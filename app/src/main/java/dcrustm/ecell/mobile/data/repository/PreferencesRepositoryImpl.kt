package dcrustm.ecell.mobile.data.repository


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dcrustm.ecell.mobile.domain.dummy.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create the DataStore instance
private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private object PreferencesKeys {
        val ON_BOARDING_COMPLETED = booleanPreferencesKey("is_on_boarding_completed")
    }

    // Retrieve the on-boarding status with a default of false
    override val isOnBoardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[PreferencesKeys.ON_BOARDING_COMPLETED] ?: false
        }

    // Update the on-boarding status value in DataStore
    override suspend fun updateOnBoardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ON_BOARDING_COMPLETED] = completed
        }
    }

}