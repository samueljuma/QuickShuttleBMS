package com.buupass.quickshuttle.data.session

import android.content.Context
import com.buupass.quickshuttle.data.models.auth.UserDTO
import com.buupass.quickshuttle.domain.auth.UserDomain
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "token"
        private const val USER_FULL_NAME = "full_name"
        private const val USER_NAME = "username"
        private const val USER_EMAIL = "email"
        private const val CAN_REPRINT = "can_reprint"
        private const val CURRENCY = "currency"
    }

    fun saveLoggedInUserDetails(user: UserDTO) {
        sharedPreferences.edit()
            .putInt(KEY_USER_ID, user.user_id ?: 0)
            .putString(KEY_TOKEN, user.token)
            .putString(USER_FULL_NAME, user.full_name)
            .putString(USER_NAME, user.username)
            .putString(USER_EMAIL, user.email)
            .putInt(CAN_REPRINT, user.can_reprint ?: 0)
            .putString(CURRENCY, user.currency)
            .apply()
    }
    fun getAuthToken(): String {
        val token = sharedPreferences.getString(KEY_TOKEN, null)
        return if (!token.isNullOrEmpty()) {
            "Token $token"
        } else {
            ""
        }
    }

    fun getUserDetails(): UserDomain {
        return UserDomain(
            id = sharedPreferences.getInt(KEY_USER_ID, 0),
            username = sharedPreferences.getString(USER_NAME, "") ?: "",
            full_name = sharedPreferences.getString(USER_FULL_NAME, "") ?: "",
            email = sharedPreferences.getString(USER_EMAIL, "") ?: "",
            can_reprint = sharedPreferences.getInt(CAN_REPRINT, 0),
            currency = sharedPreferences.getString(CURRENCY, "") ?: ""
        )
    }

    fun clearAllUserDetails() {
        sharedPreferences.edit { clear() }
    }
}