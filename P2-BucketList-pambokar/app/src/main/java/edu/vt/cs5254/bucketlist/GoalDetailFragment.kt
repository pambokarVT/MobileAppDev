package edu.vt.cs5254.bucketlist

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.bucketlist.databinding.FragmentGoalDetailBinding
import kotlinx.coroutines.launch
import java.io.File

class GoalDetailFragment : Fragment() {

    private val arg: GoalDetailFragmentArgs by navArgs()
    private val vm: GoalDetailViewModel by viewModels {
        GoalDetailViewModelFactory(arg.goalId)
    }

    private var _binding: FragmentGoalDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) { "FragmentGoalDetailBinding is NULL!!!" }

    private val photoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { tookPicture ->
        if (tookPicture) {
            vm.goal.value?.let {
                binding.goalPhoto.tag = null
                updatePhoto(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_goal_detail, menu)

                val photoIntent = photoLauncher.contract.createIntent(
                    requireContext(),
                    Uri.EMPTY
                )
                menu.findItem(R.id.take_photo_menu).isVisible =
                    canResolveIntent(photoIntent)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.share_goal_menu -> {
                        vm.goal.value?.let { shareGoal(it) }
                        true
                    }

                    R.id.take_photo_menu -> {
                        vm.goal.value?.let {
                            val photoFile = File(
                                requireContext().applicationContext.filesDir,
                                it.photoFileName
                            )
                            val photoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "edu.vt.cs5254.bucketlist.fileprovider",
                                photoFile
                            )
                            photoLauncher.launch(photoUri)
                        }
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner)

        _binding = FragmentGoalDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getItemTouchHelper().attachToRecyclerView(binding.goalNoteRecyclerView)
        binding.goalNoteRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.addProgressButton.setOnClickListener {
            findNavController().navigate(GoalDetailFragmentDirections.addProgress())
        }

        setFragmentResultListener(ProgressDialogFragment.REQUEST_KEY) { _, bundle ->
            val progressText = bundle.getString(ProgressDialogFragment.BUNDLE_KEY) ?: return@setFragmentResultListener
            vm.updateGoal { oldGoal ->
                oldGoal.copy().apply {
                    notes = oldGoal.notes + GoalNote(
                        text = progressText,
                        type = GoalNoteType.PROGRESS,
                        goalId = oldGoal.id
                    )
                }
            }
        }

        binding.goalPhoto.setOnClickListener {
            vm.goal.value?.let {
                findNavController().navigate(
                    GoalDetailFragmentDirections.showImageDetail(it.photoFileName)
                )
            }
        }

        binding.pausedCheckbox.setOnClickListener {
            vm.updateGoal { oldGoal ->
                oldGoal.copy().apply {
                    notes = if (oldGoal.isPaused) {
                        oldGoal.notes.filter { it.type != GoalNoteType.PAUSED }
                    } else {
                        oldGoal.notes + GoalNote(
                            type = GoalNoteType.PAUSED,
                            goalId = oldGoal.id
                        )
                    }
                }
            }
        }

        binding.completedCheckbox.setOnClickListener {
            vm.updateGoal { oldGoal ->
                oldGoal.copy().apply {
                    notes = if (oldGoal.isCompleted) {
                        oldGoal.notes.filter { it.type != GoalNoteType.COMPLETED }
                    } else {
                        oldGoal.notes + GoalNote(
                            type = GoalNoteType.COMPLETED,
                            goalId = oldGoal.id
                        )
                    }
                }
            }
        }

        binding.titleText.addTextChangedListener { text ->
            vm.updateGoal { oldGoal ->
                oldGoal.copy(title = text.toString()).apply { notes = oldGoal.notes }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.goal.collect { goal ->
                    goal?.let {
                        updateView(it)
                        binding.goalNoteRecyclerView.adapter = GoalNoteAdapter(it.notes)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateView(goal: Goal) {
        if (binding.titleText.text.toString() != goal.title) {
            binding.titleText.setText(goal.title)
        }

        binding.lastUpdatedText.text =
            DateFormat.format(
                "'Last updated' yyyy-MM-dd 'at' hh:mm:ss a",
                goal.lastUpdated
            )

        binding.completedCheckbox.isChecked = goal.isCompleted
        binding.pausedCheckbox.isChecked = goal.isPaused

        binding.completedCheckbox.isEnabled = !binding.pausedCheckbox.isChecked
        binding.pausedCheckbox.isEnabled = !binding.completedCheckbox.isChecked

        if (goal.isCompleted) {
            binding.addProgressButton.hide()
        } else {
            binding.addProgressButton.show()
        }

        // Configure RecyclerView for goal notes
        binding.goalNoteRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.goalNoteRecyclerView.adapter = GoalNoteAdapter(goal.notes)

        updatePhoto(goal)
    }

    private fun updatePhoto(goal: Goal) {
        if (binding.goalPhoto.tag != goal.photoFileName) {
            val photoFile = File(
                requireContext().applicationContext.filesDir,
                goal.photoFileName
            )
            if (photoFile.exists()) {
                binding.goalPhoto.doOnLayout { imageView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        imageView.width,
                        imageView.height
                    )
                    binding.goalPhoto.setImageBitmap(scaledBitmap)
                    binding.goalPhoto.tag = goal.photoFileName
                    binding.goalPhoto.isEnabled = true
                }
            } else {
                binding.goalPhoto.setImageBitmap(null)
                binding.goalPhoto.tag = null
                binding.goalPhoto.isEnabled = false
            }
        }
    }

    private fun shareGoal(goal: Goal) {
        val goalSummary = createGoalSummary(goal)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, goal.title)
            putExtra(Intent.EXTRA_TEXT, goalSummary)
        }
        val chooser = Intent.createChooser(intent, getString(R.string.share_goal))
        startActivity(chooser)
    }

    private fun getItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val noteHolder = viewHolder as GoalNoteHolder
                val swipedNote = noteHolder.boundNote
                vm.updateGoal { goal ->
                    goal.copy().apply {
                        notes = notes.filter { it != swipedNote }
                    }
                }
            }
        })
    }

    private fun createGoalSummary(goal: Goal): String {
        val dateFormatted = DateFormat.format("'Last updated' yyyy-MM-dd 'at' hh:mm:ss a", goal.lastUpdated)
        val progressNotes = goal.notes.filter { it.type == GoalNoteType.PROGRESS }
        return buildString {
            append("${goal.title}\n$dateFormatted\n")
            if (progressNotes.isNotEmpty()) {
                append("Progress:\n")
                append(progressNotes.joinToString("\n") { " * ${it.text}" })
                append("\n")
            }
            when {
                goal.isPaused -> append("This goal has been Paused.\n")
                goal.isCompleted -> append("This goal has been Completed.\n")
            }
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        return requireActivity().packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ) != null
    }
}
