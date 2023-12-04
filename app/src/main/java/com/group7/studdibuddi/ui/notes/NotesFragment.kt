package com.group7.studdibuddi.ui.notes

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.group7.studdibuddi.Util
import com.group7.studdibuddi.databinding.FragmentNotesBinding
import java.io.IOException

class NotesFragment : Fragment() {
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    private var _binding: FragmentNotesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this)[NotesViewModel::class.java]

        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        binding.buttonInput.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.buttonGenerate.setOnClickListener {
            selectedImageUri?.let { uri ->
                processImage(uri)
            }
        }
        Util.checkPermissions(requireActivity())
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
        }
    }

    private fun processImage(imageUri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
            val image = InputImage.fromBitmap(bitmap, 0)

            val options = TextRecognizerOptions.Builder().build()
            val recognizer = TextRecognition.getClient(options)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    binding.textViewResult.text = visionText.text

                    val clipboard: ClipboardManager? =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("note", visionText.text)
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(requireContext(), "Note Saved to Clipboard", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    println(e)
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}