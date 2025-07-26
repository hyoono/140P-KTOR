package com.example.myapp1

import android.content.Context
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.body
import io.ktor.client.statement.*
import io.ktor.client.plugins.*
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters


class ApiService(private val context: Context) {

    private val client = HttpClient(CIO) {
        expectSuccess = false
    }
    private val baseUrl = "http://10.0.2.2/IT140P/REST/"
    private suspend fun makePostRequest(endpoint: String, params: Parameters): String {
        return try {
            val response: HttpResponse = client.submitForm(
                url = baseUrl + endpoint,
                formParameters = params
            )
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()

            "Client-side error: ${e.message}"
        }
    }

    suspend fun addRecord(name: String, age: String, country: String, gender: String): String {
        val formParams = Parameters.build {
            append("name", name)
            append("age", age)
            append("country", country)
            append("gender", gender)
        }
        return makePostRequest("add_record.php", formParams)
    }

    suspend fun searchRecord(name: String): String {
        return try {
            val response: HttpResponse = client.get("${baseUrl}search_record.php") {
                parameter("name", name)
            }
            response.body()
        } catch (e: Exception) {
            e.printStackTrace()
            "Client-side error: ${e.message}"
        }
    }

    suspend fun updateRecord(originalName: String, newName: String, newAge: String, newCountry: String, newGender: String): String {
        val formParams = Parameters.build {
            append("original_name", originalName)
            append("name", newName)
            append("age", newAge)
            append("country", newCountry)
            append("gender", newGender)
        }
        return makePostRequest("update_record.php", formParams)
    }

    suspend fun deleteRecord(name: String): String {
        val formParams = Parameters.build {
            append("name", name)
        }
        return makePostRequest("delete_record.php", formParams)
    }

    fun close() {
        client.close()
    }
}