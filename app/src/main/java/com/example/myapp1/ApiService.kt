package com.example.myapp1

import android.content.Context
import android.widget.Toast
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.call.body
import io.ktor.client.statement.*

class ApiService(private val context: Context) {

    private val client = HttpClient(CIO)
    private val baseUrl = "http://192.168.0.103/IT123P/REST/" // Your base URL

    suspend fun addRecord(name: String, age: String, country: String, gender: String): String {
        return try {
            val response: HttpResponse = client.get("${baseUrl}add_record.php") {
                parameter("name", name)
                parameter("age", age)
                parameter("country", country)
                parameter("gender", gender)
            }
            val responseBody: String = response.body()
            showToast(responseBody)
            responseBody
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = "Error adding record: ${e.message}"
            showToast(errorMessage)
            errorMessage
        }
    }

    suspend fun searchRecord(name: String): String {
        return try {
            val response: HttpResponse = client.get("${baseUrl}search_record.php") {
                parameter("name", name)
            }
            val responseBody: String = response.body()
            showToast(responseBody) // Assuming the response is a direct message or JSON
            responseBody
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = "Error searching record: ${e.message}"
            showToast(errorMessage)
            errorMessage
        }
    }

    suspend fun updateRecord(originalName: String, newName: String, newAge: String, newCountry: String, newGender: String): String {
        // Assuming your update_record.php script takes the original name to identify the record
        // and then the new values. Adjust parameters as per your backend script.
        return try {
            val response: HttpResponse = client.get("${baseUrl}update_record.php") {
                parameter("original_name", originalName) // To identify the record
                parameter("new_name", newName)
                parameter("new_age", newAge)
                parameter("new_country", newCountry)
                parameter("new_gender", newGender)
            }
            val responseBody: String = response.body()
            showToast(responseBody)
            responseBody
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = "Error updating record: ${e.message}"
            showToast(errorMessage)
            errorMessage
        }
    }

    suspend fun deleteRecord(name: String): String {
        return try {
            val response: HttpResponse = client.get("${baseUrl}delete_record.php") {
                parameter("name", name) // Assuming deletion is by name
            }
            val responseBody: String = response.body()
            showToast(responseBody)
            responseBody
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = "Error deleting record: ${e.message}"
            showToast(errorMessage)
            errorMessage
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Call this when your activity/application is destroyed to clean up resources
    fun close() {
        client.close()
    }
}
