package com.egidanuajisantoso.assessment3_mopro.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.egidanuajisantoso.assessment3_mopro.BuildConfig
import com.egidanuajisantoso.assessment3_mopro.R
import com.egidanuajisantoso.assessment3_mopro.model.Gallery
import com.egidanuajisantoso.assessment3_mopro.model.User
import com.egidanuajisantoso.assessment3_mopro.network.GalleryApi
import com.egidanuajisantoso.assessment3_mopro.network.GalleryApi.ApiStatus
import com.egidanuajisantoso.assessment3_mopro.network.UserDataStore
import com.egidanuajisantoso.assessment3_mopro.ui.theme.Assessment3_moproTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(){
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialog by remember { mutableStateOf(false) }
    var showHewanDialog by remember { mutableStateOf(false) }

    var itemToEdit by remember { mutableStateOf<Gallery?>(null) }
    var bitmapToEdit by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var bitmap: Bitmap? by remember { mutableStateOf(null) }

    var isDatasetLinked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var dataSet = 0

    val launcher = rememberLauncherForActivityResult(CropImageContract()){
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null){
            showHewanDialog = true
        }
    }


    LaunchedEffect(itemToEdit) {
        itemToEdit?.let { gallery ->
            coroutineScope.launch(Dispatchers.IO) {
                bitmapToEdit = loadBitmapFromUrl(context, GalleryApi.getGalleryUrl(gallery.gambar))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    if (user.email.isNotEmpty()){
                        IconButton(onClick = {
                            isDatasetLinked = !isDatasetLinked

                            if (isDatasetLinked) {
                                viewModel.retrieveDataUser(user.email)
                            }else{
                                viewModel.retrieveData(user.email)

                            }
                        }) {
                            if (isDatasetLinked) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_dataset_linked_24),
                                    contentDescription = "Dataset Terhubung",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_dataset_24),
                                    contentDescription = "Dataset Tidak Terhubung",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }


                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            scope.launch {
                                signIn(context, dataStore)
                            }
                        } else {
                            showDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_account_circle_24),
                            contentDescription = stringResource(id = R.string.profil),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },

        floatingActionButton = {
            if (user.email.isNotEmpty()){
                FloatingActionButton(onClick = {
                    val options = CropImageContractOptions(
                        null, CropImageOptions(
                            imageSourceIncludeGallery = true,
                            imageSourceIncludeCamera = true,
                            fixAspectRatio = true,
                        )
                    )
                    launcher.launch(options)
                }){
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.tambah_gallery)
                    )
                }
            }

        }

    ){ padding ->
        ScreenContent(
            viewModel = viewModel,
            userId = user.email,
            modifier = Modifier.padding(padding),
            onEditClick = { gallery ->
                if (user.email.isNotEmpty()) {
                    itemToEdit = gallery
                }
            }
        )

        if (showHewanDialog){
            GalleryDialog(
                bitmap = bitmap,
                onDismissRequest = { showHewanDialog = false }){ lokasi, deskripsi,tanggal ->
                viewModel.saveData(user.email, lokasi, deskripsi,tanggal, bitmap!!)
                showHewanDialog = false
            }
        }

        if (showDialog){
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }){
                CoroutineScope(Dispatchers.IO).launch { signOut(context , dataStore) }
                showDialog = false
            }
        }

        // Dialog untuk mengedit atau menghapus data
        if (itemToEdit != null && bitmapToEdit != null) {
            GalleryDialogEdit(
                initialBitmap = bitmapToEdit!!,
                initialLokasi = itemToEdit!!.lokasi,
                initialDeskripsi = itemToEdit!!.deskripsi,
                initialTanggal = itemToEdit!!.tanggal,
                onDismissRequest = {
                    itemToEdit = null
                    bitmapToEdit = null
                },
                onConfirmation = { newLokasi, newDeskripsi, newTanggal, newBitmap ->
                    viewModel.updateData(
                        id = itemToEdit!!.id,
                        userId = user.email,
                        lokasi = newLokasi,
                        deskripsi = newDeskripsi,
                        tanggal = newTanggal,
                        bitmap = newBitmap
                    )
                    itemToEdit = null
                    bitmapToEdit = null
                },
                onDeleteConfirmation = {
                    viewModel.deleteData(itemToEdit!!.id, user.email)
                    itemToEdit = null
                    bitmapToEdit = null
                }
            )
        }

        if (errorMessage != null){
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }


}

@Composable
fun ScreenContent(
    viewModel: MainViewModel,
    userId: String,
    modifier: Modifier,
    onEditClick: (Gallery) -> Unit
) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    LaunchedEffect(userId){
        viewModel.retrieveData(userId)
    }

    when(status){
        ApiStatus.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator()
            }
        }

        ApiStatus.SUCCESS -> {
            LazyVerticalGrid(
                modifier = modifier
                    .fillMaxSize()
                    .padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp)
            ){
                items(data) { galleryItem ->
                    if (galleryItem.Authorization == userId){
                        ListItem(
                            user = userId,
                            gallery = galleryItem,
                            onItemClick = onEditClick
                        )
                    }else{
                        ListItem(
                            gallery = galleryItem,
                            onItemClick = { },
                            user = userId
                        )
                    }

                }
            }
        }

        ApiStatus.FAILED -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = { viewModel.retrieveData(userId) },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }
}

@Composable
fun ListItem(
    user : String,
    gallery: Gallery,
    onItemClick: (Gallery) -> Unit
){
//    Log.d("ListItem", "Email: ${gallery.Authorization}")
    Box(
        modifier = Modifier
            .padding(4.dp)
            .border(1.dp, Color.Gray)
            .clickable { onItemClick(gallery) },
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(GalleryApi.getGalleryUrl(gallery.gambar))
                .crossfade(true)
                .build(),
            contentDescription = stringResource(id = R.string.gambar),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.baseline_broken_image_24),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f))
                .padding(4.dp)
        ) {
            // Column ini sekarang hanya berisi elemen teks
            Column {
                Text(
                    text = gallery.lokasi,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = gallery.deskripsi,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = Color.White
                )

                Text(
                    text = gallery.tanggal,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            if (gallery.Authorization == user) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_edit_document_24),
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

private suspend fun signIn(context: Context , dataStore: UserDataStore){
    val googleOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    }catch (e: GetCredentialException){
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(
    result: GetCredentialResponse,
    dataStore: UserDataStore
){
    val credential = result.credential
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri?.toString() ?: ""
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException){
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    }
    else{
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context , dataStore: UserDataStore){
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    }catch (e: ClearCredentialException){
        Log.e("SIGN-OUT", "Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver : ContentResolver,
    result: CropImageView.CropResult
): Bitmap?{
    if (!result.isSuccessful){
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }
    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
        MediaStore.Images.Media.getBitmap(resolver, uri)
    }else{
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

private suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? {
    val loader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false)
        .build()
    return try {
        val result = (loader.execute(request) as SuccessResult).drawable
        (result as BitmapDrawable).bitmap
    } catch (e: Exception) {
        Log.e("LoadBitmap", "Failed to load bitmap from URL: $url", e)
        null
    }
}



@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    Assessment3_moproTheme {
        MainScreen()
    }
}