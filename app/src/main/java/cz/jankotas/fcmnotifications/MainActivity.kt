package cz.jankotas.fcmnotifications

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import cz.jankotas.fcmnotifications.ui.theme.FCMNotificationsTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    // TODO add activity result for handling permission result
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FCMNotificationsTheme {
                MainScreen(
                    onRegisterButtonClick = ::onRegisterButtonClick,
                )
            }
        }
    }

    private fun askNotificationPermission() {
        TODO("Implement asking for sending notification permission")
    }

    private fun onRegisterButtonClick() {
        lifecycleScope.launch {
            val token = getAndStoreToken()
            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(this::class.simpleName, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun getAndStoreToken(): String {
        val token = Firebase.messaging.token.await()

        // Check whether the retrieved token matches the one on your server for this user's device
        val preferences = this@MainActivity.getPreferences(Context.MODE_PRIVATE)
        val tokenStored = preferences.getString(getString(R.string.preferences_key_fcm_token), "")
        if (tokenStored.isNullOrBlank() || tokenStored != token) {
            val deviceToken = hashMapOf(
                "token" to token,
                "timestamp" to FieldValue.serverTimestamp(),
            )

            // Get user ID from Firebase Auth or your own server
            Firebase.firestore
                .collection(Constants.fcmTokenCollection)
                .document("myuserid") // should be an unique user ID
                .set(deviceToken)
                .addOnSuccessListener {
                    Log.d(this::class.simpleName, "Token registration succeed")
                    with(preferences.edit()) {
                        putString(getString(R.string.preferences_key_fcm_token), token)
                        apply()
                    }
                }.addOnFailureListener {
                    Log.d(this::class.simpleName, "Token registration failed")
                }
        }
        return token
    }
}
