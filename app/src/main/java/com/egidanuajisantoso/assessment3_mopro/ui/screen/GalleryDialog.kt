package com.egidanuajisantoso.assessment3_mopro.ui.screen

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.graphics.Bitmap
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.egidanuajisantoso.assessment3_mopro.R
import com.egidanuajisantoso.assessment3_mopro.ui.theme.Assessment3_moproTheme
import java.util.Calendar

@Composable
fun GalleryDialog(
    bitmap: Bitmap?,
    onDismissRequest: () -> Unit,
    onConfirmation: (String, String, String) -> Unit
){
    var lokasi by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var tanggal by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
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

    datePickerDialog.setOnDismissListener {
        showDatePicker = false
    }

    if (showDatePicker) {
        datePickerDialog.show()
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
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = stringResource(id = R.string.gambar_terpilih),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }

                OutlinedTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    label = { Text(text = stringResource(id = R.string.lokasi)) },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text(text = stringResource(id = R.string.deskripsi)) },
                    maxLines = 2,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = tanggal,
                        onValueChange = { }, // Biarkan kosong agar tidak bisa diubah manual
                        label = { Text(text = stringResource(id = R.string.tanggal)) },
                        readOnly = true, // Membuatnya hanya bisa dibaca
                        trailingIcon = {
                            // Tambahkan ikon kalender untuk UX yang lebih baik
                            Image(
                                painter = painterResource(id = R.drawable.baseline_calendar_month_24), // Ganti dengan ikon kalender Anda
                                contentDescription = "Pilih Tanggal"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Box transparan di atasnya untuk menangkap klik
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                showDatePicker = true // Tampilkan dialog saat diklik
                            }
                    )
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = stringResource(id = R.string.batal))
                    }
                    OutlinedButton(
                        onClick = { onConfirmation(lokasi, deskripsi, tanggal) },
                        enabled = lokasi.isNotEmpty() && deskripsi.isNotEmpty() && tanggal.isNotEmpty(),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = stringResource(R.string.simpan))
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun AddDialogPreview() {
    Assessment3_moproTheme {
        GalleryDialog(
            bitmap = null,
            onDismissRequest = { },
            onConfirmation = { _, _, _ -> }
        )
    }
}