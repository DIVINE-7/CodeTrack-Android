package com.example.codetrack.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton factory and HTTP client configuration for CodeTrack REST services.
 */
object RetrofitClient {

    /**
     * Default base URL for standard Android Studio emulator routing to host PC loopback.
     */
    const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/api/"

    @Volatile
    var baseUrl: String = DEFAULT_BASE_URL
        set(value) {
            field = if (value.endsWith("/")) value else "$value/"
            resetClient()
        }

    /**
     * Logging interceptor configured strictly at BASIC level.
     * Logs only HTTP method, request URL, response status code, and execution time.
     * Deliberately avoids logging request/response bodies or sensitive headers (passwords, tokens).
     */
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
            redactHeader("Cookie")
        }
    }

    /**
     * Interceptor that provides seamless emulator loopback support.
     * On modern Android emulators with Wi-Fi device simulation, untrusted app UIDs are isolated
     * from the virtual router gateway 10.0.2.2. When targeting 10.0.2.2, this interceptor transparently
     * attempts the ADB reverse tunnel (127.0.0.1) first, falling back to 10.0.2.2 directly.
     */
    private val emulatorHostInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url
        if (originalUrl.host == "10.0.2.2") {
            val reversedUrl = originalUrl.newBuilder()
                .host("127.0.0.1")
                .build()
            val reversedRequest = originalRequest.newBuilder()
                .url(reversedUrl)
                .header("Host", "${originalUrl.host}:${originalUrl.port}")
                .build()
            try {
                chain.proceed(reversedRequest)
            } catch (_: Exception) {
                chain.proceed(originalRequest)
            }
        } else {
            chain.proceed(originalRequest)
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        buildOkHttpClient()
    }

    private fun buildOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(emulatorHostInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Volatile
    private var retrofitInstance: Retrofit? = null

    @Volatile
    private var authApiServiceInstance: AuthApiService? = null

    fun getRetrofit(): Retrofit {
        return retrofitInstance ?: synchronized(this) {
            retrofitInstance ?: Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { retrofitInstance = it }
        }
    }

    val authApiService: AuthApiService
        get() = authApiServiceInstance ?: synchronized(this) {
            authApiServiceInstance ?: getRetrofit().create(AuthApiService::class.java).also {
                authApiServiceInstance = it
            }
        }

    private fun resetClient() {
        synchronized(this) {
            retrofitInstance = null
            authApiServiceInstance = null
        }
    }
}
