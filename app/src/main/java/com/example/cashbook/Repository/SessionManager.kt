package com.example.cashbook.Repository

import android.content.Context

object SessionManager {

    private const val PREFS_NAME = "cashbook_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"

    fun saveSession(context: Context, userId: Int, username: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getUserId(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_USER_ID, -1)
        return if (id == -1) null else id
    }

    fun getUsername(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, null)
    }

    fun isLoggedIn(context: Context): Boolean = getUserId(context) != null

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}