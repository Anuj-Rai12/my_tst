package com.pos10.di
import com.pos10.helper.SharedPreference
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject


/*class AuthInterceptor @Inject constructor(
    private val authEventManager: AuthEventManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
          SharedPreference.getToken()
        }

        val request = chain.request().newBuilder()
        request.addHeader("Authorization", "Bearer $token")
//        if (networkManger.isConnected()) {
            val response = chain.proceed(request.build())

            if (response.code == 401) {
                runBlocking {
                    authEventManager.send(AuthEvent.Unauthorized)
                }
                return response
            } else return response
//        } else
//            throw NoConnectivityException()
    }

}*/

class AuthInterceptor @Inject constructor(
    private val authEventManager: AuthEventManager
) : Interceptor {

    private val maxRetry = 1

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { SharedPreference.getToken() }
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        var lastException: IOException? = null

        repeat(maxRetry + 1) { attempt ->
            try {
                val response = chain.proceed(request)

                // Handle 401 Unauthorized
                if (response.code == 401) {
                    runBlocking {
                        authEventManager.send(AuthEvent.Unauthorized)
                    }
                }

                return response
            } catch (e: IOException) {
                lastException = e
                if (attempt == maxRetry) throw e
            }
        }

        // Should never reach here, but Kotlin requires return
        throw lastException ?: IOException("Unknown network error")
    }
}
