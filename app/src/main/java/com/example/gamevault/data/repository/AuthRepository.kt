package com.example.gamevault.data.repository

import com.example.gamevault.data.local.db.dao.UserDao
import com.example.gamevault.data.local.entity.UserEntity
import com.example.gamevault.data.local.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.security.MessageDigest

class AuthRepository(
    private val userDao: UserDao,
    private val appPreferences: AppPreferences
) {

    val isLoggedIn: Flow<Boolean> = appPreferences.isLoggedIn
    val loggedInUserId: Flow<Int> = appPreferences.loggedInUserId

    fun getUserById(id: Int): Flow<UserEntity?> = userDao.getUserById(id)

    suspend fun register(
        username: String,
        email: String,
        password: String
    ): RegisterResult {
        if (userDao.emailExists(email) > 0) {
            return RegisterResult.EmailAlreadyExists
        }
        if (userDao.usernameExists(username) > 0) {
            return RegisterResult.UsernameAlreadyExists
        }

        val passwordHash = hashPassword(password)
        val user = UserEntity(
            username = username,
            email = email,
            passwordHash = passwordHash
        )

        val userId = userDao.insertUser(user)
        appPreferences.saveLoggedInUser(userId.toInt())
        return RegisterResult.Success(userId.toInt())
    }

    suspend fun login(
        emailOrUsername: String,
        password: String
    ): LoginResult {
        val passwordHash = hashPassword(password)
        val user = userDao.login(emailOrUsername, passwordHash)
            ?: userDao.getUserByEmailOrUsername(emailOrUsername)?.let {
                if (it.passwordHash == passwordHash) it else null
            }

        return if (user != null) {
            appPreferences.saveLoggedInUser(user.id)
            LoginResult.Success(user)
        } else {
            LoginResult.InvalidCredentials
        }
    }

    suspend fun logout() {
        appPreferences.clearLoggedInUser()
    }

    suspend fun updateProfile(
        userId: Int,
        username: String,
        profilePictureUri: String?
    ): UpdateProfileResult {
        val currentUser = userDao.getUserByEmailOrUsername(username)
        if (currentUser != null && currentUser.id != userId) {
            return UpdateProfileResult.UsernameAlreadyExists
        }

        val userToUpdate = userDao.getUserById(userId).first()
            ?: return UpdateProfileResult.UserNotFound

        userDao.updateUser(
            userToUpdate.copy(
                username = username,
                profilePictureUri = profilePictureUri
            )
        )
        return UpdateProfileResult.Success
    }

    suspend fun updatePassword(
        userId: Int,
        currentPassword: String,
        newPassword: String
    ): UpdatePasswordResult {
        val currentHash = hashPassword(currentPassword)
        val newHash = hashPassword(newPassword)

        val userToUpdate = userDao.getUserById(userId).first()
            ?: return UpdatePasswordResult.UserNotFound

        if (userToUpdate.passwordHash != currentHash) {
            return UpdatePasswordResult.WrongCurrentPassword
        }

        userDao.updateUser(userToUpdate.copy(passwordHash = newHash))
        return UpdatePasswordResult.Success
    }

    suspend fun deleteAccount(userId: Int) {
        userDao.deleteUserById(userId)
        appPreferences.clearLoggedInUser()
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // --- Result sealed classes ---
    sealed class RegisterResult {
        data class Success(val userId: Int) : RegisterResult()
        object EmailAlreadyExists : RegisterResult()
        object UsernameAlreadyExists : RegisterResult()
    }

    sealed class LoginResult {
        data class Success(val user: UserEntity) : LoginResult()
        object InvalidCredentials : LoginResult()
    }

    sealed class UpdateProfileResult {
        object Success : UpdateProfileResult()
        object UsernameAlreadyExists : UpdateProfileResult()
        object UserNotFound : UpdateProfileResult()
    }

    sealed class UpdatePasswordResult {
        object Success : UpdatePasswordResult()
        object WrongCurrentPassword : UpdatePasswordResult()
        object UserNotFound : UpdatePasswordResult()
    }
}