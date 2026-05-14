package com.pos10.helper

import android.content.Context
import com.pos10.R
import com.pos10.di.AuthInterceptor

import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object CustomHttpClient {

    fun okHttpClientWith_Sectigo46_CA(context: Context, logging: Interceptor,retryInterceptor: Interceptor,authInterceptor: AuthInterceptor): OkHttpClient {
        // Load CAs from res/raw
        val cf = CertificateFactory.getInstance("X.509")
        val caInput  = context?.resources?.openRawResource(R.raw.rootsectigo)
        val ca = caInput.use { cf.generateCertificate(it) }

        // Create a KeyStore containing our trusted CA
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null, null)
        keyStore.setCertificateEntry("sectigo_r46", ca)

        // Create a TrustManager that trusts the CAs in our KeyStore
        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmf.init(keyStore)

        // Get the first X509TrustManager
        val trustManager = tmf.trustManagers
            .filterIsInstance<X509TrustManager>()
            .first()

        // Create an SSLContext that uses our TrustManager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), null)

        // Build your OkHttpClient with this custom SSL context
        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .protocols(listOf(Protocol.HTTP_1_1))
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .dns(Dns.SYSTEM)
            .build()
    }

    object ImageOkHttpProvider {
        fun getUnsafeOkHttpClient(): OkHttpClient {

            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            return OkHttpClient.Builder()
                .sslSocketFactory(
                    sslContext.socketFactory,
                    trustAllCerts[0] as X509TrustManager
                )
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        }
    }

}