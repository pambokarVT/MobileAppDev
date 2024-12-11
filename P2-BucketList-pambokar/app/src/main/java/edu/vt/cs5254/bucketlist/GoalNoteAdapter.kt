package edu.vt.cs5254.bucketlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.bucketlist.databinding.ListItemGoalNoteBinding

class GoalNoteHolder(private val binding: ListItemGoalNoteBinding) :
    RecyclerView.ViewHolder(binding.root) {

        lateinit var boundNote: GoalNote
            private set

    fun bind(note: GoalNote) {
        val btn = binding.goalNoteButton
        btn.visibility = View.VISIBLE
        btn.isEnabled = false
        btn.text = when (note.type) {
            GoalNoteType.PAUSED -> "PAUSED"
            GoalNoteType.COMPLETED -> "COMPLETED"
            GoalNoteType.PROGRESS -> note.text
        }

        when (note.type) {
            GoalNoteType.PAUSED -> {
                btn.setBackgroundWithContrastingText("yellow")
            }
            GoalNoteType.COMPLETED -> {
                btn.setBackgroundWithContrastingText("green")
            }
            GoalNoteType.PROGRESS -> {
                btn.setBackgroundWithContrastingText("purple")
            }
        }
    }
}

class GoalNoteAdapter(private val notes: List<GoalNote>) :
    RecyclerView.Adapter<GoalNoteHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalNoteHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemGoalNoteBinding.inflate(inflater, parent, false)
        return GoalNoteHolder(binding)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: GoalNoteHolder, position: Int) {
        holder.bind(notes[position])
    }
}
