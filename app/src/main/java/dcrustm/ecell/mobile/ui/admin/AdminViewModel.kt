package dcrustm.ecell.mobile.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
): ViewModel() {

    fun onInfoClick() {
        Log.d("ViewModel", "Info Button Clicked!")
    }

    fun onProfileClick() {
        Log.d("ViewModel", "Profile Button Clicked!")
    }

    fun onCardClick(cardClicked: String) {
        Log.d("ViewModel", "Feature Card Clicked!")
    }

}