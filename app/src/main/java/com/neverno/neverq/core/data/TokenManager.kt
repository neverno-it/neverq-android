package com.neverno.neverq.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "neverq_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val USER_TYPE = stringPreferencesKey("user_type")
    private val USER_ROLE = stringPreferencesKey("user_role")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_EMAIL = stringPreferencesKey("user_email")
    private val COMPANY_ID = intPreferencesKey("company_id")

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN] }
    val userType: Flow<String?> = context.dataStore.data.map { it[USER_TYPE] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[USER_ROLE] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL] }
    val companyId: Flow<Int?> = context.dataStore.data.map { it[COMPANY_ID] }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userType: String,
        role: String,
        name: String,
        email: String,
        companyId: Int?,
    ) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
            prefs[USER_TYPE] = userType
            prefs[USER_ROLE] = role
            prefs[USER_NAME] = name
            prefs[USER_EMAIL] = email
            if (companyId != null) prefs[COMPANY_ID] = companyId
        }
    }

    suspend fun updateAccessToken(token: String) {
        context.dataStore.edit { it[ACCESS_TOKEN] = token }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun isLoggedIn(): Boolean = accessToken.first() != null

    suspend fun getAccessTokenNow(): String? = accessToken.first()
    suspend fun getRefreshTokenNow(): String? = refreshToken.first()
    suspend fun getUserTypeNow(): String? = userType.first()
    suspend fun getUserRoleNow(): String? = userRole.first()
}
