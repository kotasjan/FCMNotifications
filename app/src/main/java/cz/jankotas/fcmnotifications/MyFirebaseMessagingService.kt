package cz.jankotas.fcmnotifications

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(this::class.simpleName, "From: ${message.from}")

        // Check if message contains a data payload.
        if (message.data.isNotEmpty()) {
            Log.d(this::class.simpleName, "Message data payload: ${message.data}")
        }

        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d(this::class.simpleName, "Message Notification Body: ${it.body}")
            // sendNotification(it)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(this::class.simpleName, "Refreshed token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String?) {
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
                Log.d(this::class.simpleName, "Token refresh succeed")
            }.addOnFailureListener {
                Log.d(this::class.simpleName, "Token refresh failed")
            }
    }
}
