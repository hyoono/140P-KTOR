package com.example.myapp1

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp1.ui.theme.MyApp1Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // State for input fields - hoisted to be accessible by multiple dialogs/functions if needed
    var nameState by mutableStateOf("")
    var ageState by mutableStateOf("")
    var countryState by mutableStateOf("")
    var genderState by mutableStateOf("")

    // For update, we need to know the original name to identify the record
    var originalNameForUpdate by mutableStateOf("")

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiService = ApiService(applicationContext) // Initialize ApiService
        enableEdgeToEdge()
        setContent {
            MyApp1Theme {
                MyLayoutApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        apiService.close() // Close the Ktor client
    }

    @Preview(widthDp = 300, heightDp = 600)
    @Composable
    fun MyLayoutApp() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Larawan()
            Column(
                modifier = Modifier
                    .padding(16.dp) // Added more padding
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between items
            ) {
                MyInputFields() // Renamed for clarity
                AddButton()
                SearchButton()
                EditButton()
                DeleteButton()
            }
        }
    }

    @Composable
    fun MyInputFields() {
        // Use the hoisted state variables
        TextField(
            value = nameState,
            onValueChange = { nameState = it },
            label = { Text("Name") }, // Added labels for better UX
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = ageState,
            onValueChange = { ageState = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = countryState,
            onValueChange = { countryState = it },
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = genderState,
            onValueChange = { genderState = it },
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    fun AddButton() {
        val coroutineScope = rememberCoroutineScope()
        Button(
            onClick = {
                coroutineScope.launch {
                    apiService.addRecord(nameState, ageState, countryState, genderState)
                    // Optionally clear fields after adding
                    nameState = ""
                    ageState = ""
                    countryState = ""
                    genderState = ""
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color(0XFF0F9D58),
                containerColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Add Record", fontSize = 14.sp)
        }
    }

    @Composable
    fun SearchButton() {
        val coroutineScope = rememberCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }
        var searchName by rememberSaveable { mutableStateOf("") }

        Button(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color(0XFF0F9D58),
                containerColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Search by Name")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("SEARCH RECORD") },
                text = {
                    Column {
                        Text("Enter name to search:")
                        TextField(
                            value = searchName,
                            onValueChange = { searchName = it },
                            placeholder = { Text("Type name here") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        coroutineScope.launch {
                            val result = apiService.searchRecord(searchName)
                            // Assuming your search_record.php returns data that you might want to
                            // populate into the fields for editing.
                            // This part depends heavily on your API's response format.
                            // For simplicity, we're just showing a toast here.
                            // If it returns JSON, you'd parse it and update nameState, ageState, etc.
                            // For example, if your PHP script returns "Name:John,Age:30,..."
                            // you'll need to parse this string.
                            // For now, let's assume if a record is found, you might want to allow editing it.
                            // So we can populate the fields if the search is "successful" (you need to define this)
                            // Example: (VERY SIMPLIFIED - REPLACE WITH ACTUAL PARSING)
                            if (!result.contains("Error") && !result.contains("not found")) { // Adjust this condition
                                // This is a placeholder. You'll need to parse the 'result'
                                // and set nameState, ageState, etc.
                                // For example, if result is "John,30,USA,M"
                                val parts = result.split(",")
                                if (parts.size >= 4) {
                                    nameState = parts[0]
                                    ageState = parts[1]
                                    countryState = parts[2]
                                    genderState = parts[3]
                                    originalNameForUpdate = parts[0] // Store for potential update
                                }
                            }
                        }
                        searchName = "" // Clear search field
                    }) { Text("Search") }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }

    @Composable
    fun EditButton() {
        val coroutineScope = rememberCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }

        // This button is more meaningful if the fields are populated (e.g., after a search)
        Button(
            onClick = {
                if (nameState.isNotBlank()) { // Only show dialog if there's a name (likely from a search)
                    originalNameForUpdate = nameState // Capture the current name as the one to update
                    showDialog = true
                } else {
                    // Optionally show a toast that the user should search first
                    Toast.makeText(LocalContext.current, "Search for a record first to edit.", Toast.LENGTH_SHORT).show()
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.Yellow, // Different color for edit
                containerColor = Color.DarkGray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Edit Current Record")
        }

        if (showDialog) {
            // Re-use the input fields from the main screen, but pre-filled
            // The user edits them directly, then clicks "Save Changes"
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("EDIT RECORD") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Editing record for: $originalNameForUpdate")
                        Text("Modify the details below:")
                        // The TextFields for name, age, country, gender are already bound
                        // to nameState, ageState, etc. So, changes in the main UI
                        // will be reflected here if the dialog is opened after a search populates them.
                        // You might want to have separate state for the dialog if the UX feels clunky.
                        // For this example, we'll assume the main fields are used.
                        MyInputFields() // Display the current input fields for editing
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        coroutineScope.launch {
                            apiService.updateRecord(
                                originalNameForUpdate, // The name of the record to update
                                nameState,             // New name
                                ageState,              // New age
                                countryState,          // New country
                                genderState            // New gender
                            )
                            // Optionally clear fields or re-fetch after update
                        }
                    }) { Text("Save Changes") }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }

    @Composable
    fun DeleteButton() {
        val coroutineScope = rememberCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }
        var nameToDelete by rememberSaveable { mutableStateOf("") }

        Button(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = Color.Red // Destructive action color
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Delete Record")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("DELETE RECORD") },
                text = {
                    Column {
                        Text("Enter name of the record to delete:")
                        TextField(
                            value = nameToDelete,
                            onValueChange = { nameToDelete = it },
                            placeholder = { Text("Type name here") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        if (nameToDelete.isNotBlank()) {
                            coroutineScope.launch {
                                apiService.deleteRecord(nameToDelete)
                                // Optionally clear fields if the deleted record was displayed
                                if (nameState == nameToDelete) {
                                    nameState = ""
                                    ageState = ""
                                    countryState = ""
                                    genderState = ""
                                }
                            }
                        }
                        nameToDelete = "" // Clear delete field
                    }) { Text("Delete") }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }


    @Composable
    fun Larawan() {
        Image(
            painter = painterResource(id = R.drawable.cat), // Make sure this drawable exists
            contentDescription = "background image",
            contentScale = ContentScale.Crop, // Crop might be better for a full background
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f // Make it a bit transparent if it's a background
        )
    }
}
