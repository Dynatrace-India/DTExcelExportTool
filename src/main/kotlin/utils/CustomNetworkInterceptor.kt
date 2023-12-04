package utils

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response

class CustomNetworkInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        println("Sending request: ${request.url}")

        val response = chain.proceed(request)
        println("Received response: ${response.code} - ${response.message}")

        return response
    }
}