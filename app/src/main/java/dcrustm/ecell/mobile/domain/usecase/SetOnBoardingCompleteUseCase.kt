package dcrustm.ecell.mobile.domain.usecase

import dcrustm.ecell.mobile.domain.dummy.PreferencesRepository
import javax.inject.Inject


class SetOnBoardingCompleteUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {

    suspend operator fun invoke(completed: Boolean) {
        repository.updateOnBoardingCompleted(completed)
    }

}