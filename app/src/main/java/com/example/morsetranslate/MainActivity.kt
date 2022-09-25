package com.example.morsetranslate

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doOnTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.time.toDuration


class MainActivity : AppCompatActivity() {

    // PLAYER AUDIO
    private lateinit var mMorsePlayer : MorsePlayer
    // MORSE WRITER DIALOG
    private lateinit var mMorseWriterDialog : MorseWriter

    // PERMISION
    private var REQUEST_CAMERA_PERMISSION = 1
    private var REQUEST_GALLERY_PERMISSION = 2
    private var IMAGE_CAPTURE_CODE = 100
    private var IMAGE_GALLERY_CODE = 200

    // IMAGE TO TRANSLATE
    private lateinit var mImgVToTranslate: ImageView
    private lateinit var mCImgVCloseImage: CircleImageView
    private lateinit var mRlImageContainer : RelativeLayout


    private lateinit var mImgVTelegraph : ImageView
    private lateinit var mImgVDeleteText: ImageView
    private lateinit var mImgVPasteText: ImageView
    private lateinit var mImgVChangeText: ImageView
    private lateinit var mImgVCopyText: ImageView
    private lateinit var mImgVPlayAudio: ImageView
    private lateinit var mImgVLight: ImageView

    private lateinit var mEtText: TextInputEditText
    private lateinit var mtxtResult: TextView
    private lateinit var mtxtTop: TextView
    private lateinit var mtxtBottom: TextView

    // FLOAT BUTTON ANIMATIONS
    private lateinit var mFloatButtonManager: FloatButtonManager
    // FLOAT BUTTONS
    private lateinit var mFabPlus: FloatingActionButton
    private lateinit var mFabGallery: FloatingActionButton
    private lateinit var mFabCamera: FloatingActionButton

    private lateinit var mToConvert: String
    private lateinit var mResult: String

    // AUDIO
    //private lateinit var mDot: MediaPlayer
    //private lateinit var mStripe: MediaPlayer
    private lateinit var mBeepSound1: MediaPlayer

    //
    private var mSelected = ""

    private var mMorse = MorseCode()
    private lateinit var mCameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMorseWriterDialog = MorseWriter(this)

        mFloatButtonManager = FloatButtonManager(this)

        mRlImageContainer = findViewById(R.id.rlImageContainer)
        mCImgVCloseImage = findViewById(R.id.cImgVCloseImage)
        mCImgVCloseImage.setOnClickListener {
            mRlImageContainer.visibility = View.GONE
        }

        mImgVTelegraph = findViewById(R.id.imgVTelegraph)
        mImgVToTranslate = findViewById(R.id.imgVToTranslate)
        mImgVDeleteText = findViewById(R.id.imgVDeleteText)
        mImgVPasteText = findViewById(R.id.imgVPasteText)
        mImgVChangeText = findViewById(R.id.imgVChangeText)
        mImgVCopyText = findViewById(R.id.imgVCopyText)
        mImgVPlayAudio = findViewById(R.id.imgVPlayAudio)
        mImgVLight = findViewById(R.id.imgVLight)

        mCameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        //mDot = MediaPlayer.create(this, R.raw.dot)
        //mStripe = MediaPlayer.create(this, R.raw.stripe)
        mBeepSound1 = MediaPlayer.create(this, R.raw.beepaudio)

        mEtText = findViewById(R.id.etText)
        mtxtResult = findViewById(R.id.txtResult)

        mtxtTop = findViewById(R.id.txtTop)
        mtxtBottom = findViewById(R.id.txtBottom)


        // si el texto cambia traduce en tiempo real
        mEtText.doOnTextChanged { text, start, before, count -> translate() }

        mImgVTelegraph.setOnClickListener {
            mMorseWriterDialog.alertDialogOpen()
        }

        mImgVDeleteText.setOnClickListener {
            deleteText()
        }
        mImgVPasteText.setOnClickListener {
            pasteText()
        }

        mImgVChangeText.setOnClickListener {
            changeText()
        }

        mImgVCopyText.setOnClickListener {
            copyText()
        }

        // mMorsePlayer = MorsePlayer(900, 25)
        mImgVPlayAudio.setOnClickListener {
            val txtToPlay = mtxtResult.text.toString()
            if (txtToPlay.isNotEmpty()) {
                // playAudio(txtToPlay)
                // mMorsePlayer.playMorse(txtToPlay)

            } else Toast.makeText(this, "Cannot be empty!", Toast.LENGTH_LONG).show()
        }

        mImgVLight.setOnClickListener {
            val txtToLight = mtxtResult.text.toString()
            if (txtToLight.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    playLight(txtToLight)
                }
            } else Toast.makeText(this, "Cannot be empty!", Toast.LENGTH_LONG).show()
        }

        mFabGallery = findViewById(R.id.fabGallery)
        mFabGallery.setOnClickListener {
            mSelected = "Gallery"
            validatePermission(mSelected)
        }

        mFabCamera = findViewById(R.id.fabCamera)
        mFabCamera.setOnClickListener {
            mSelected = "Camera"
            validatePermission(mSelected)
        }

        mFabPlus = findViewById(R.id.fabPlus)
        mFabPlus.setOnClickListener {
            mFloatButtonManager.onPlusButtonClicked(mFabPlus, mFabCamera, mFabGallery)
        }
    }

    /**
     *
     * CAMERA || IMAGE || PERMISSIONS
     *
     */


    private fun validatePermission(mSelected: String) {
        if (mSelected.contentEquals("Camera")){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // no se ha dado el permiso a la camara todavia
                requestPermission()
            } else{
                // se dio el permiso y abrimos la camara
                openCamera()
            }
        }else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // no se ha dado el permiso a la camara todavia
                requestPermission()
            } else{
                // se dio el permiso y abrimos la galeria
                openGallery()
            }
        }

    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)
            && ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            // el usuario rechazo los permisos
            Toast.makeText(this, "You need to enable permissions for this task!", Toast.LENGTH_SHORT).show()
        }else{
            // pedir permisos
            if (mSelected.contentEquals("Camera")){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA) , REQUEST_CAMERA_PERMISSION)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE) , REQUEST_GALLERY_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                // PERMISO DE CAMARA ACEPTADO
                openCamera()
            }else{
                // PERMISO SIN ACEPTAR
                Toast.makeText(this, "Permissions denied!", Toast.LENGTH_SHORT).show()
            }
        }else if (requestCode == REQUEST_GALLERY_PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                // PERMISO DE CAMARA ACEPTADO
                openGallery()
            }else{
                // PERMISO SIN ACEPTAR
                Toast.makeText(this, "Permissions denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGE_CAPTURE_CODE)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)

    }

    private fun openGallery(){
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, IMAGE_GALLERY_CODE)
    }

    // recibe la foto
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IMAGE_CAPTURE_CODE -> {
                if (resultCode == Activity.RESULT_OK) {

                    val bitmap = data!!.extras!!.get("data") as Bitmap
                    mRlImageContainer.visibility = View.VISIBLE
                    mImgVToTranslate.setImageBitmap(bitmap)
                    translateImageToText()

                } else {
                    Toast.makeText(this, "Error with photo...", Toast.LENGTH_SHORT).show()
                }
            }

            IMAGE_GALLERY_CODE -> {
                if (resultCode == RESULT_OK){

                    var uri = data!!.data
                    mImgVToTranslate.setImageURI(uri)
                    mRlImageContainer.visibility = View.VISIBLE


                    // detectar texto de img
                    translateImageToText()

                }else {
                    Toast.makeText(this, "Error with image...", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun translateImageToText() {
        // obtenemos bitmap del imgV
        val bitmapDrawable = mImgVToTranslate.drawable
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmapDrawable.toBitmap(), 0)

        // pasamos la img
        recognizer.process(image)
            .addOnSuccessListener {

                val resultTxt = it.text
                mEtText.setText(resultTxt)

            }.addOnFailureListener {
                Toast.makeText(this, "No text detected.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     *
     * TEXT || TRANSLATION || AUDIO || FLASHLIGHT
     *
     */

    // cambia el resultado de posicion
    private fun changeText() {
        if (mtxtResult.text.toString().isNotEmpty()){
            mEtText.setText(mResult)
        }
    }

    // activa mensaje por linterna
    @RequiresApi(Build.VERSION_CODES.M)
    private fun playLight(txtToLight: String) {


        var isMorse = false
        val alpha = mMorse.mAlpha

        for(i in alpha.indices){
            if (txtToLight.contains(alpha[i])){
                isMorse = true
                break
            }
        }

        var characters = txtToLight.trim()

        if (!isMorse){
            Toast.makeText(this, "Starting Message!", Toast.LENGTH_LONG).show()

            for (i in characters.indices){
                if (characters[i].toString() == "."){
                    mCameraManager.setTorchMode(mCameraManager.cameraIdList[0], true)
                    Thread.sleep(500)
                    mCameraManager.setTorchMode(mCameraManager.cameraIdList[0], false)
                }else if (characters[i].toString() == "-"){
                    mCameraManager.setTorchMode(mCameraManager.cameraIdList[0], true)
                    Thread.sleep(1000)
                    mCameraManager.setTorchMode(mCameraManager.cameraIdList[0], false)
                }else if (characters[i].toString() == " "){
                    Thread.sleep(1000)
                }
            }

        }else{
            Toast.makeText(this, "Need Morse code!", Toast.LENGTH_LONG).show()
        }
    }

    // activa mensaje por audio
    private fun playAudio(txtToPlay: String) {

        val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 90)

        var isMorse = true
        val alpha = mMorse.mAlpha

        for(i in alpha.indices){
            if (txtToPlay.contains(alpha[i])){
                isMorse = false
                break
            }
        }

            val characters = txtToPlay.trim()

            if (!isMorse){
                Toast.makeText(this, "Playing audio...", Toast.LENGTH_LONG).show()

                MediaPlayer.OnCompletionListener {  }

                for (i in characters.indices){

                }

                //mDot.stop()
                //mStripe.stop()
            }else{
                Toast.makeText(this, "Need Morse code!", Toast.LENGTH_LONG).show()
            }


    }

    override fun onDestroy() {
        super.onDestroy()
        mBeepSound1.release()

    }

    // copiar texto de portapapeles al textview
    private fun copyText() {
        if (mtxtResult.text.toString().isNotEmpty()){
            val txtToCopy = mtxtResult.text.toString()
            val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(null, txtToCopy)
            clipBoard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
        }
    }

    // pegar texto de portapapeles al textview
    private fun pasteText() {
        val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val pasteData = clipBoard.primaryClip?.getItemAt(0)?.text
        mEtText.setText(pasteData)
    }

    // borra el campo de input edit text top
    private fun deleteText() {
        mEtText.setText("")
    }

    // traduccion del texto
    private fun translate() {
        mToConvert = mEtText.text.toString().toLowerCase(Locale.ROOT)
        var alpha = mMorse.mAlpha
        var isNotMorse = false

        if (mToConvert.isNotEmpty()){

            // validando que sea morse
            for(i in alpha.indices){
                if (mToConvert.contains(alpha[i])){
                    isNotMorse = true
                    break
                }
            }
            // si es morse
            if (!isNotMorse){

                mResult = mMorse.morseToAlpha(mToConvert)

            }else{

                mResult = mMorse.alphaToMorse(mToConvert)

            }

            mtxtResult.text = mResult

        }else{
            mtxtResult.text = ""
        }

    }

}