package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.SpecificNeedChip
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.*
import com.example.viewmodel.NatacaoViewModel
import java.io.File
import java.util.UUID

@Composable
fun StudentsScreen(
    viewModel: NatacaoViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTag by remember { mutableStateOf("Todos") }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var studentForHistoryDetail by remember { mutableStateOf<Student?>(null) }

    val filterTags = listOf("Todos", "Autismo", "Deficiência Visual", "Síndrome de Down", "Paralisia Cerebral")

    val filteredStudents = students.filter { std ->
        val matchesSearch = std.name.contains(searchQuery, ignoreCase = true) ||
                std.specificNeeds.any { it.contains(searchQuery, ignoreCase = true) }
        val matchesTag = if (selectedFilterTag == "Todos") true else {
            std.specificNeeds.any { it.contains(selectedFilterTag, ignoreCase = true) }
        }
        matchesSearch && matchesTag
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Novo Aluno", fontWeight = FontWeight.Bold) },
                containerColor = OceanBluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_student")
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Banner de Privacidade
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = OceanBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Dados protegidos sob sigilo pedagógico. Acesso restrito ao corpo docente autorizado.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                }
            }

            // Barra de Busca
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar aluno por nome ou necessidade...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_input")
                )
            }

            // Chips de Filtro
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterTags) { tag ->
                        FilterChip(
                            selected = selectedFilterTag == tag,
                            onClick = { selectedFilterTag = tag },
                            label = { Text(tag) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Alunos Cadastrados (${filteredStudents.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Lista de Alunos
            items(filteredStudents, key = { it.id }) { std ->
                StudentCard(
                    student = std,
                    isSelected = std.id == selectedStudent.id,
                    onSelect = { viewModel.selectStudent(std.id) },
                    onEdit = { studentToEdit = std },
                    onViewHistory = { studentForHistoryDetail = std },
                    onDelete = { viewModel.deleteStudent(std.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Diálogo Adicionar / Editar Aluno
    if (showAddDialog || studentToEdit != null) {
        val editing = studentToEdit
        StudentFormDialog(
            initialStudent = editing,
            onDismiss = {
                showAddDialog = false
                studentToEdit = null
            },
            onSave = { updated ->
                viewModel.saveStudent(updated)
                showAddDialog = false
                studentToEdit = null
            }
        )
    }

    // Modal de Histórico e Evolução do Aluno
    if (studentForHistoryDetail != null) {
        StudentHistoryModal(
            student = studentForHistoryDetail!!,
            onDismiss = { studentForHistoryDetail = null }
        )
    }
}

@Composable
private fun StudentCard(
    student: Student,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onViewHistory: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) OceanBluePrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar com Foto do Aluno ou Emoji
                StudentAvatar(
                    student = student,
                    size = 52.dp,
                    borderColor = OceanBluePrimary
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${student.age} anos",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Adaptação: ${student.adaptationLevel}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OceanBluePrimary
                    ) {
                        Text(
                            text = "EM AULA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Chips de Necessidades Específicas
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                student.specificNeeds.forEach { need ->
                    SpecificNeedChip(text = need)
                }
            }

            // Preferência de Comunicação
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Comunicação: ${student.communicationPreference}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Observações Importantes
            if (student.importantNotes.isNotBlank()) {
                Text(
                    text = "⚠️ Obs: ${student.importantNotes}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewHistory,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Histórico (${student.history.size})", style = MaterialTheme.typography.labelSmall)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                    if (!isSelected) {
                        Button(
                            onClick = onSelect,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Selecionar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentFormDialog(
    initialStudent: Student?,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var age by remember { mutableStateOf(initialStudent?.age?.toString() ?: "9") }
    var avatarEmoji by remember { mutableStateOf(initialStudent?.avatarEmoji ?: "👦🏻") }
    var photoUri by remember { mutableStateOf(initialStudent?.photoUri) }
    var specificNeedsStr by remember { mutableStateOf(initialStudent?.specificNeeds?.joinToString(", ") ?: "Autismo (TEA)") }
    var communicationPref by remember { mutableStateOf(initialStudent?.communicationPreference ?: "Sinais visuais + Vibrações") }
    var adaptationLevel by remember { mutableStateOf(initialStudent?.adaptationLevel ?: "Flutuação e Respiração") }
    var notes by remember { mutableStateOf(initialStudent?.importantNotes ?: "") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val studentId = initialStudent?.id ?: UUID.randomUUID().toString()
                val photosDir = File(context.filesDir, "student_photos").apply { mkdirs() }
                val destFile = File(photosDir, "student_${studentId}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                photoUri = android.net.Uri.fromFile(destFile).toString()
            } catch (e: Exception) {
                photoUri = uri.toString()
            }
        }
    }

    // Configurações de Acessibilidade
    var highContrast by remember { mutableStateOf(initialStudent?.accessibility?.highContrast ?: false) }
    var reducedStimuli by remember { mutableStateOf(initialStudent?.accessibility?.reducedStimuli ?: false) }
    var wallAlerts by remember { mutableStateOf(initialStudent?.accessibility?.wallProximityAlerts ?: true) }
    var voiceAnnouncements by remember { mutableStateOf(initialStudent?.accessibility?.voiceAnnouncements ?: true) }
    var poolLength by remember { mutableStateOf(initialStudent?.accessibility?.poolLengthMeters ?: 25) }

    val avatarOptions = listOf("👦🏻", "👧🏽", "🧒🏼", "👧🏻", "👦🏾", "🏊‍♂️", "🏊‍♀️", "⭐")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialStudent != null) "Editar Cadastro do Aluno" else "Cadastrar Novo Aluno",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Foto do Aluno (Galeria do dispositivo ou Presets)
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, TurquoisePrimary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Foto do Aluno",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBluePrimary
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Preview com StudentAvatar
                                StudentAvatar(
                                    name = name.ifBlank { "Aluno" },
                                    avatarEmoji = avatarEmoji,
                                    photoUri = photoUri,
                                    size = 70.dp,
                                    borderColor = TurquoisePrimary,
                                    borderWidth = 3.dp
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OceanBluePrimary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_pick_student_photo")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (photoUri.isNullOrBlank()) "Escolher Foto" else "Trocar Foto",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (!photoUri.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = { photoUri = null },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("btn_remove_student_photo")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Remover Foto",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }

                            // Modelos Rápidos para Testar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Modelos prontos:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                )
                                SuggestionChip(
                                    onClick = {
                                        photoUri = "android.resource://${context.packageName}/drawable/img_student_swimmer_boy"
                                    },
                                    label = { Text("🏊‍♂️ Menino", fontSize = 11.sp) }
                                )
                                SuggestionChip(
                                    onClick = {
                                        photoUri = "android.resource://${context.packageName}/drawable/img_student_swimmer_girl"
                                    },
                                    label = { Text("🏊‍♀️ Menina", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
                // Nome e Idade
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo do Aluno") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Idade (anos)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Column(modifier = Modifier.weight(1.5f)) {
                            Text("Avatar:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                avatarOptions.take(4).forEach { av ->
                                    Surface(
                                        shape = CircleShape,
                                        color = if (avatarEmoji == av) TurquoiseSoft else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (avatarEmoji == av) BorderStroke(2.dp, TurquoisePrimary) else null,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clickable { avatarEmoji = av }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(av, fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = specificNeedsStr,
                        onValueChange = { specificNeedsStr = it },
                        label = { Text("Necessidades Específicas (separar por vírgula)") },
                        placeholder = { Text("Ex: Autismo (TEA), Hipersensibilidade Auditiva") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = communicationPref,
                        onValueChange = { communicationPref = it },
                        label = { Text("Preferência de Comunicação") },
                        placeholder = { Text("Ex: Pictogramas + 2 vibrações curtas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = adaptationLevel,
                        onValueChange = { adaptationLevel = it },
                        label = { Text("Nível de Adaptação / Experiência") },
                        placeholder = { Text("Ex: Flutuação e Pernada com Prancha") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações Pedagógicas Importantes") },
                        placeholder = { Text("Gatilhos de estresse, sensibilidades, cuidados") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Toggles de Acessibilidade
                item {
                    Text(
                        text = "Recursos de Acessibilidade:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = highContrast, onCheckedChange = { highContrast = it })
                            Text("Alto Contraste no Relógio e Tela", style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = reducedStimuli, onCheckedChange = { reducedStimuli = it })
                            Text("Redução de Estímulos Visuais (TEA)", style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = wallAlerts, onCheckedChange = { wallAlerts = it })
                            Text("Avisos de Virada e Aproximação de Parede", style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = voiceAnnouncements, onCheckedChange = { voiceAnnouncements = it })
                            Text("Instruções por Voz / Fala no Relógio", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val parsedNeeds = specificNeedsStr.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        val student = Student(
                            id = initialStudent?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            age = age.toIntOrNull() ?: 9,
                            avatarEmoji = avatarEmoji,
                            specificNeeds = parsedNeeds.ifEmpty { listOf("Adaptação Geral") },
                            communicationPreference = communicationPref.trim().ifBlank { "Sinais Hápticos" },
                            accessibility = StudentAccessibility(
                                highContrast = highContrast,
                                reducedStimuli = reducedStimuli,
                                largeFonts = true,
                                vibrationIntensity = initialStudent?.accessibility?.vibrationIntensity ?: 1.0f,
                                wallProximityAlerts = wallAlerts,
                                poolLengthMeters = poolLength,
                                voiceAnnouncements = voiceAnnouncements
                            ),
                            adaptationLevel = adaptationLevel.trim().ifBlank { "Adaptação Aquática" },
                            importantNotes = notes.trim(),
                            photoUri = photoUri,
                            history = initialStudent?.history ?: emptyList()
                        )
                        onSave(student)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Salvar Cadastro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun StudentHistoryModal(
    student: Student,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = student.avatarEmoji, fontSize = 24.sp)
                Column {
                    Text(
                        text = "Evolução: ${student.name}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${student.history.size} aulas registradas",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        text = {
            if (student.history.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pool,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Nenhum histórico registrado ainda",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Ao finalizar uma aula na aba Acompanhamento, o registro de progresso e notas pedagógicas aparecerão aqui.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(student.history, key = { it.id }) { h ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = h.date,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = OceanBluePrimary)
                                    )
                                    Row {
                                        repeat(h.engagementRating) {
                                            Text("⭐", fontSize = 12.sp)
                                        }
                                    }
                                }

                                Text(
                                    text = h.lessonTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )

                                Text(
                                    text = "Duração: ${h.durationMinutes} min • ${h.completedExercises}/${h.totalExercises} exercícios • ${h.commandsSent} sinais emitidos",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "“${h.evolutionNotes}”",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}
