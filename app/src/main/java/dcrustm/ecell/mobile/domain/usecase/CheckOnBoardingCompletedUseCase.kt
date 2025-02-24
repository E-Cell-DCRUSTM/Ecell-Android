package dcrustm.ecell.mobile.domain.usecase

import dcrustm.ecell.mobile.domain.dummy.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// UseCase to check if the on-boarding process has been completed
class CheckOnBoardingCompletedUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {

    operator fun invoke(): Flow<Boolean> = repository.isOnBoardingCompleted

}