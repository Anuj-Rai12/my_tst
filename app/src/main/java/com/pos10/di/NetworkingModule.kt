package com.pos10.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pos10.BuildConfig
import com.pos10.BuildConfig.BASE_URL
import com.pos10.MyApplication
import com.pos10.helper.CommonConstants
import com.pos10.helper.CommonConstants.SEA_POS_DOMAIN
import com.pos10.helper.CustomHttpClient
import com.pos10.network.ApiServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkingModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideRetryInterceptor(): Interceptor = Interceptor { chain ->
        var response: okhttp3.Response? = null
        var tryCount = 0
        val maxRetry = 1

        while (tryCount <= maxRetry) {
            try {
                response = chain.proceed(chain.request())
                if (response.isSuccessful) break
            } catch (e: IOException) {
                if (tryCount == maxRetry) throw e
            }
            tryCount++
        }
        response!!
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        retryInterceptor: Interceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val baseUrl = CommonConstants.BASE_URL
        return if (baseUrl.contains(SEA_POS_DOMAIN, ignoreCase = true)) {
            CustomHttpClient.okHttpClientWith_Sectigo46_CA(
                MyApplication.appContext,
                logging,
                retryInterceptor,
                authInterceptor
            )
        } else {
            OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(CommonConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiServices =
        retrofit.create(ApiServices::class.java)

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        authEventManager: AuthEventManager
    ): AuthInterceptor {
        return AuthInterceptor(authEventManager)
    }
}
