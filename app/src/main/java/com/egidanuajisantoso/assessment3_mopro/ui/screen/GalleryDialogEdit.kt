package com.egidanuajisantoso.assessment3_mopro.ui.screen

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.egidanuajisantoso.assessment3_mopro.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryDialogEdit(
    initialBitmap: Bitmap,
    initialLokasi: String,
    initialDeskripsi: String,
    initialTanggal: String,
    onDismissRequest: () -> Unit,
    onConfirmation: (String, String, String, Bitmap) -> Unit,
    onDeleteConfirmation: () -> Unit
){

    var lokasi by remember { mutableStateOf(initialLokasi) }
    var deskripsi by remember { mutableStateOf(initialDeskripsi) }
    var tanggal by remember { mutableStateOf(initialTanggal) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> newImageUri = uri }
    )


    val newBitmap by remember(newImageUri) {
        derivedStateOf {
            newImageUri?.let { uri -> uriToBitmap(context, uri) }
        }
    }


    val displayBitmap = newBitmap ?: initialBitmap

    var showDatePicker by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            tanggal = "$dayOfMonth-${month + 1}-$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    datePickerDialog.setOnDismissListener { showDatePicker = false }

    if (showDatePicker) {
        datePickerDialog.show()
    }


    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "Konfirmasi Hapus") },
            text = { Text(text = "Apakah Anda yakin ingin menghapus data ini?") },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteConfirmation()
                    }
                ) { Text("Hapus") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { galleryLauncher.launch("image/*") }
                ) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = stringResource(id = R.string.gambar_terpilih),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "Ketuk untuk mengubah",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                OutlinedTextField(value = lokasi, onValueChange = { lokasi = it }, label = { Text(stringResource(id = R.string.lokasi)) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = deskripsi, onValueChange = { deskripsi = it }, label = { Text(stringResource(id = R.string.deskripsi)) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))


                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = tanggal,
                        onValueChange = {},
                        label = { Text(text = stringResource(id = R.string.tanggal)) },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                                contentDescription = "Pilih Tanggal"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = stringResource(id = R.string.batal))
                    }
                    OutlinedButton(
                        onClick = { onConfirmation(lokasi, deskripsi, tanggal, displayBitmap) },
                        enabled = lokasi.isNotEmpty() && deskripsi.isNotEmpty() && tanggal.isNotEmpty(),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = stringResource(R.string.simpan_perubahan))
                    }
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color(0f, 0f, 0f, 0.5f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Hewan",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun uriToBitmap(context: Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}