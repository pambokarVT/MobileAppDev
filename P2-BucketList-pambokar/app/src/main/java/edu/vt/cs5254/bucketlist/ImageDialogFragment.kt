package edu.vt.cs5254.bucketlist

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import edu.vt.cs5254.bucketlist.databinding.FragmentImageDialogBinding
import java.io.File

class ImageDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = FragmentImageDialogBinding.inflate(layoutInflater)

        val args: ImageDialogFragmentArgs by navArgs()

        val photoFile = File(
            requireActivity().applicationContext.filesDir,
            args.goalImageFilename
        )

        if (photoFile.exists()) {
            binding.root.doOnLayout { dialog ->
                val bitmap = getScaledBitmap(
                    photoFile.path,
                    dialog.width,
                    dialog.height
                )
                binding.imageDetail.setImageBitmap(bitmap)
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .show()
    }
}