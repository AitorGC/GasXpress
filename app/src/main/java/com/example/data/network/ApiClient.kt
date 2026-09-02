package com.example.data.network

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/"
    
    private var applicationContext: Context? = null
    
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }
    
    private val networkPolicyInterceptor = Interceptor { chain ->
        val context = applicationContext ?: return@Interceptor chain.proceed(chain.request())
        val prefs = context.getSharedPreferences("api_policy", Context.MODE_PRIVATE)
        
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val dateStr = "${now.get(Calendar.YEAR)}-${now.get(Calendar.MONTH)}-${now.get(Calendar.DAY_OF_MONTH)}"
        
        val lastDate = prefs.getString("last_date", "")
        if (dateStr != lastDate) {
            prefs.edit()
                .putString("last_date", dateStr)
                .putInt("count_8_20", 0)
                .putBoolean("has_6_fetch", false)
                .putBoolean("has_18_fetch", false)
                .apply()
        }
        
        val count = prefs.getInt("count_8_20", 0)
        val has6 = prefs.getBoolean("has_6_fetch", false)
        val has18 = prefs.getBoolean("has_18_fetch", false)
        
        var shouldFetchNetwork = false
        if (!has6 && hour >= 6) {
            shouldFetchNetwork = true
        } else if (!has18 && hour >= 18) {
            shouldFetchNetwork = true
        } else if (hour in 8..19) {
            if (count < 3) {
                shouldFetchNetwork = true
            }
        } else {
            // Outside 8:00 and 20:00 (e.g. 21:00 or 5:00), we allow fetching if needed, 
            // but the prompt only limits between 8:00 and 20:00. Let's allow it.
            shouldFetchNetwork = true
        }
        
        val request = if (shouldFetchNetwork) {
            chain.request().newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build()
        } else {
            chain.request().newBuilder().cacheControl(CacheControl.FORCE_CACHE).build()
        }
        
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            // If FORCE_CACHE fails (no cache), and we really need data, we might want to fallback to network?
            // Wait, if it fails, it throws an exception. We'll just let it throw and fallback to mock data in Repo.
            throw e
        }
        
        if (shouldFetchNetwork && response.isSuccessful && response.networkResponse != null) {
            val edit = prefs.edit()
            if (hour in 6..17) edit.putBoolean("has_6_fetch", true)
            if (hour >= 18) edit.putBoolean("has_18_fetch", true)
            if (hour in 8..19) edit.putInt("count_8_20", count + 1)
            edit.apply()
        }
        
        // Force the response to be cached for a long time since we control when to fetch
        if (response.isSuccessful) {
            return@Interceptor response.newBuilder()
                .header("Cache-Control", "public, max-age=${60 * 60 * 24}")
                .removeHeader("Pragma")
                .build()
        }
        
        response
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val builder = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(networkPolicyInterceptor)
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            
        applicationContext?.let { ctx ->
            val cacheSize = 15L * 1024 * 1024 // 15 MB
            val cache = Cache(File(ctx.cacheDir, "miteco_cache"), cacheSize)
            builder.cache(cache)
        }
        
        builder.build()
    }

    val mitecoApi: MitecoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MitecoApiService::class.java)
    }
}
