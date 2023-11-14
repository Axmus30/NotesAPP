package com.example.notes


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notes.ui.theme.NotesTheme

//notes app
//citation
// https://chat.openai.com/
// https://www.youtube.com/watch?v=glyqjzkc4fk&t=629s&ab_channel=Stevdza-San
class MainActivity : ComponentActivity() {
    private val notes = mutableStateListOf(
        Note(1, "Test 1", "argh"),
        Note(2, "Test 2", "argh"),
        Note(3, "Test 3", "argh"),
        Note(4, "Test 4", "argh")
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Route.list) {
                    composable(route = Route.list) {
                        ListScreen(
                            navigateToAdd = { navController.navigate(Route.add){} },
                            navigateToEdit = { noteId -> navController.navigate(Route.edit + "/$noteId") },
                            notes = notes,
                            removeNote = { note -> removeNote(note) }
                        )
                    }
                    composable(route = Route.add) {
                        AddScreen(
                            onAddNote = { note -> addNote(note) },
                            navController = navController,
                            notes = notes
                        )
                    }
                    composable(route = Route.edit + "/{noteId}",
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId")
                        val noteToEdit = notes.find { it.id == noteId }
                        if (noteToEdit != null) {
                            EditScreen(noteToEdit, onEditNote = { editedNote ->
                                editNote(editedNote)
                            }, navController = navController)
                        }
                    }
                }
            }
        }
    }
    private fun addNote(note: Note) {
        notes.add(note)
    }
    private fun editNote(editedNote: Note) {
        val index = notes.indexOfFirst { it.id == editedNote.id }
        if (index != -1) {
            notes[index].title = editedNote.title
            notes[index].text = editedNote.text
        }
    }
    private fun removeNote(note: Note) {
        notes.remove(note)
    }
}


object Route {
    const val list = "ListScreen()"
    const val edit = "EditScreen()"
    const val add = "AddScreen()"
}


data class Note(var id: Int, var title: String, var text: String)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    onAddNote: (Note) -> Unit,
    navController: NavHostController,
    notes: List<Note>,
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var checkLongTitle by remember { mutableStateOf(false) }
    var checkShortTitle by remember { mutableStateOf(false) }
    var checkText by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.width(350.dp)
        )
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Text") },
            modifier = Modifier.width(350.dp)
        )
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    navController.navigateUp()
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val newNote = Note(notes.size+1, title, text)
                    if (newNote.title.length < 3) {
                        checkShortTitle = true
                    } else if (newNote.title.length > 50) {
                        checkLongTitle = true
                    } else if (newNote.text.length > 150) {
                        checkText = true
                    } else {
                        onAddNote(newNote)
                        navController.navigateUp()
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Add Note")
            }
            if (checkShortTitle) {
                ValNote("Title needs to be 3 characters or longer"){
                    checkShortTitle = false
                }
            }
            if (checkLongTitle) {
                ValNote("Title cant be longer the 50"){
                    checkLongTitle = false
                }
            }
            if (checkText) {
                ValNote("Text to long, max 150"){
                    checkText= false
                }
            }
        }
    }
}
@Composable
fun ValNote(
    errText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = { Text(errText) },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        }
    )
}


@Composable
fun ListScreen(
    navigateToAdd: () -> Unit,
    navigateToEdit: (Int) -> Unit,
    notes: List<Note>,
    removeNote: (Note) -> Unit
) {
    val reversedNotes = notes.reversed()
    val removeNote: (Note) -> Unit = { noteToRemove ->
        notes.find { it.id == noteToRemove.id }?.let {
            removeNote(it)
        }
    }
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = {
                    navigateToAdd()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        reversedNotes.forEach { note ->
            NoteItem(note, removeNote) {
                navigateToEdit(note.id)
            }
        }
    }
}


@Composable
fun NoteItem(note: Note, onRemove: (Note) -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(all = 10.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(all = 10.dp)) {
            var confirmDelete by remember { mutableStateOf(false) }
            Text(
                note.title,
                fontSize = 25.sp,
                fontWeight = FontWeight.W700,
                modifier = Modifier.padding(10.dp)
            )
            Text(
                note.text,
                color = Color.Black,
                modifier = Modifier.padding(12.dp)
            )
            Row {
                IconButton(
                    {
                        confirmDelete = true
                    }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "delete")
                }
                if (confirmDelete) {
                    AlertDialog(
                        onDismissRequest = {
                            confirmDelete = false
                        },
                        title = { Text("Confirm Delete") },
                        text = { Text("Confirm deletion of the selected note") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    confirmDelete = false
                                    onRemove(note)
                                }
                            ) {
                                Text("Delete")
                            }
                        }
                    )
                }
                IconButton(
                    {
                        onEdit()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "edit")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    note: Note,
    onEditNote: (Note) -> Unit,
    navController: NavHostController,
) {
    var editedTitle by remember { mutableStateOf(note.title) }
    var editedText by remember { mutableStateOf(note.text) }
    var checkLongTitle by remember { mutableStateOf(false) }
    var checkShortTitle by remember { mutableStateOf(false) }
    var checkText by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        TextField(
            value = editedTitle,
            onValueChange = { editedTitle = it },
            label = { Text("Title") },
            modifier = Modifier.width(350.dp)
        )
        TextField(
            value = editedText,
            onValueChange = { editedText = it },
            label = { Text("Text") },
            modifier = Modifier.width(350.dp)
        )
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    navController.navigateUp()
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val editedNote = Note(note.id, editedTitle, editedText)
                    if (editedNote.title.length < 3) {
                        checkShortTitle = true
                    } else if (editedNote.title.length > 50) {
                        checkLongTitle = true
                    } else if (editedNote.text.length > 150) {
                        checkText = true
                    } else {
                        onEditNote(editedNote)
                        navController.navigateUp()
                    }

                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Save")
            }
            if (checkShortTitle) {
                ValNote("Title needs to be 3 characters or longer"){
                    checkShortTitle = false
                }
            }
            if (checkLongTitle) {
                ValNote("Title cant be longer the 50"){
                    checkLongTitle = false
                }
            }
            if (checkText) {
                ValNote("Text to long, max 150"){
                    checkText= false
                }
            }
        }
    }
}