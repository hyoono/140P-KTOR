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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapp1.ui.theme.MCLDark
import com.example.myapp1.ui.theme.MCLDarkGray
import com.example.myapp1.ui.theme.MCLLight
import com.example.myapp1.ui.theme.MCLRed
import com.example.myapp1.ui.theme.MCLWhite
import com.example.myapp1.ui.theme.MyApp1Theme
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    var nameState by mutableStateOf("")
    var ageState by mutableStateOf("")
    var countryState by mutableStateOf("")
    var genderState by mutableStateOf("")

    var isAgeError by mutableStateOf(false)
    var originalNameForUpdate by mutableStateOf("")

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiService = ApiService(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApp1Theme {
                MyLayoutApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        apiService.close()
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @Preview(showBackground = true)
    @Composable
    fun MyLayoutApp() {
        var isLoading by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Larawan()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "User Management Utility",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MCLWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MyInputFields()
                        AddButton(onIsLoadingChange = { isLoading = it })
                        SearchButton(onIsLoadingChange = { isLoading = it })
                        EditButton(onIsLoadingChange = { isLoading = it })
                        DeleteButton(onIsLoadingChange = { isLoading = it })
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    @Composable
    fun MyInputFields() {
        OutlinedTextField(
            value = nameState,
            onValueChange = { nameState = it },
            label = { Text("Name") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ageState,
            onValueChange = {
                ageState = it
                isAgeError = it.isNotBlank() && it.any { char -> !char.isDigit() }},
            label = { Text("Age") },

            leadingIcon = { Icon(Icons.Filled.Pin, contentDescription = "Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = isAgeError,
            supportingText = {
                if (isAgeError) {
                    Text(
                        text = "Age must contain only numbers.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
        val countries = Locale.getISOCountries().mapNotNull { iso ->
            Locale("", iso).getDisplayCountry(Locale.ENGLISH)
        }.distinct().sorted()
        var countryExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = countryExpanded,
            onExpandedChange = { countryExpanded = !countryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = countryState,
                onValueChange = {},
                readOnly = true,
                label = { Text("Country") },
                leadingIcon = { Icon(Icons.Filled.Public, contentDescription = "Country") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = countryExpanded,
                onDismissRequest = { countryExpanded = false }
            ) {
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country) },
                        onClick = {
                            countryState = country
                            countryExpanded = false
                        }
                    )
                }
            }
        }
        val genders = listOf(
            "Male", "Female", "Bisexual", "Gay", "Lesbian",
            "Transgender Male", "Transgender Female", "Non-binary", "Queer", "Prefer not to say"
        )
        var genderExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = genderState,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                leadingIcon = { Icon(Icons.Filled.Wc, contentDescription = "Gender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genders.forEach { gender ->
                    DropdownMenuItem(
                        text = { Text(gender) },
                        onClick = {
                            genderState = gender
                            genderExpanded = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun AddButton(onIsLoadingChange: (Boolean) -> Unit) {
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        Button(
            onClick = {
                if (isAgeError) {
                    showToast(context, "Please enter a valid age.")
                    return@Button
                }
                if (nameState.isNotBlank() && ageState.isNotBlank() && countryState.isNotBlank() && genderState.isNotBlank()) {
                    coroutineScope.launch {
                        onIsLoadingChange(true)
                        try {
                            val responseMessage = apiService.addRecord(nameState, ageState, countryState, genderState)
                            showToast(context, responseMessage)
                            if (responseMessage.contains("successfully", ignoreCase = true)) {
                                nameState = ""
                                ageState = ""
                                countryState = ""
                                genderState = ""
                            }
                        } catch (e: Exception) {
                            showToast(context, "Client Error: ${e.message}")
                        } finally {
                            onIsLoadingChange(false)
                        }
                    }
                } else {
                    showToast(context, "All fields are required.")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MCLDark),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Record", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Add Record")
        }
    }

    @Composable
    fun SearchButton(onIsLoadingChange: (Boolean) -> Unit) {
        val coroutineScope = rememberCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }
        var searchName by rememberSaveable { mutableStateOf("") }
        val context = LocalContext.current

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MCLLight),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Filled.Search, contentDescription = "Search", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Search by Name")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Search Record") },
                text = {
                    OutlinedTextField(
                        value = searchName,
                        onValueChange = { searchName = it },
                        label = { Text("Name to search") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if(searchName.isNotBlank()){
                            showDialog = false
                            coroutineScope.launch {
                                onIsLoadingChange(true)
                                try {
                                    val result = apiService.searchRecord(searchName)
                                    if (!result.startsWith("Error", ignoreCase = true)) {
                                        val parts = result.split(",")
                                        if (parts.size >= 4) {
                                            nameState = parts[0]
                                            ageState = parts[1]
                                            countryState = parts[2]
                                            genderState = parts[3]
                                            originalNameForUpdate = parts[0]
                                            showToast(context, "Record found!")
                                        } else {
                                            showToast(context, "Invalid data from server.")
                                        }
                                    } else {
                                        showToast(context, result)
                                    }
                                } catch (e: Exception) {
                                    showToast(context, "Client Error: ${e.message}")
                                } finally {
                                    onIsLoadingChange(false)
                                    searchName = ""
                                }
                            }
                        }
                    }) { Text("Search") }
                },
                dismissButton = { Button(onClick = { showDialog = false }) { Text("Cancel") } }
            )
        }
    }

    @Composable
    fun EditButton(onIsLoadingChange: (Boolean) -> Unit) {
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        Button(
            onClick = {
                if (isAgeError) {
                    showToast(context, "Please enter a valid age.")
                    return@Button
                }
                if (originalNameForUpdate.isNotBlank()) {
                    if (nameState.isNotBlank() && ageState.isNotBlank() && countryState.isNotBlank() && genderState.isNotBlank()) {
                        coroutineScope.launch {
                            onIsLoadingChange(true)
                            try {
                                val responseMessage = apiService.updateRecord(originalNameForUpdate, nameState, ageState, countryState, genderState)
                                showToast(context, responseMessage)
                                if (responseMessage.contains("successfully", ignoreCase = true)) {
                                    originalNameForUpdate = nameState
                                }
                            } catch (e: Exception) {
                                showToast(context, "Client Error: ${e.message}")
                            } finally {
                                onIsLoadingChange(false)
                            }
                        }
                    } else {
                        showToast(context, "Fields cannot be empty for an update.")
                    }
                } else {
                    showToast(context, "Search for a record first to edit.")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MCLDarkGray),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Record", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Save Edits")
        }
    }

    @Composable
    fun DeleteButton(onIsLoadingChange: (Boolean) -> Unit) {
        val coroutineScope = rememberCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }
        var nameToDelete by rememberSaveable { mutableStateOf("") }
        val context = LocalContext.current

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(MCLRed),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete Record", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Delete Record")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Delete Record") },
                text = {
                    OutlinedTextField(
                        value = nameToDelete,
                        onValueChange = { nameToDelete = it },
                        label = { Text("Name to delete") }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (nameToDelete.isNotBlank()) {
                                showDialog = false
                                coroutineScope.launch {
                                    onIsLoadingChange(true)
                                    try {
                                        val responseMessage = apiService.deleteRecord(nameToDelete)
                                        showToast(context, responseMessage)
                                        if (responseMessage.contains("successfully", ignoreCase = true)) {
                                            if (nameState == nameToDelete) {
                                                nameState = ""
                                                ageState = ""
                                                countryState = ""
                                                genderState = ""
                                                originalNameForUpdate = ""
                                            }
                                        }
                                    } catch (e: Exception) {
                                        showToast(context, "Client Error: ${e.message}")
                                    } finally {
                                        onIsLoadingChange(false)
                                        nameToDelete = ""
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                },
                dismissButton = { Button(onClick = { showDialog = false }) { Text("Cancel") } }
            )
        }
    }

    @Composable
    fun Larawan() {
        Image(
            painter = painterResource(id = R.drawable.cat),
            contentDescription = "background image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.2f
        )
    }
}