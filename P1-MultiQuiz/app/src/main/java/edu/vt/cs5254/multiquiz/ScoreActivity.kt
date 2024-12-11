package edu.vt.cs5254.multiquiz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.vt.cs5254.multiquiz.databinding.ActivityScoreBinding

private const val EXTRA_SCORE = "edu.vt.cs5254.multiquiz.score"
private const val EXTRA_RESET = "edu.vt.cs5254.multiquiz.reset"

class ScoreActivity : AppCompatActivity() {

    // CREATE A NEW VIEWMODEL CLASS WITH SINGLE BOOLEAN SHOWING WHETHER RESET
    // BUTTON HAS EVER BEEN CLICKED OR NOT

    private lateinit var binding: ActivityScoreBinding

    private val scoreViewModel: ScoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the score passed from MainActivity
        val score = intent.getIntExtra(EXTRA_SCORE, 0)

        // Update the initial view with the score or "???" if reset was clicked
        updateView(score)

        // Set click listener for the Reset button
        binding.resetButton.setOnClickListener {
            if (!scoreViewModel.isResetClicked) {
                scoreViewModel.isResetClicked = true
                binding.resetButton.isEnabled = false
                binding.scoreText.text = "?"

                // Send the result back to MainActivity to reset the quiz
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_RESET, true)
                }
                setResult(RESULT_OK, resultIntent)
            }
        }
}
    private fun updateView(score: Int) {
        if (scoreViewModel.isResetClicked) {
            // If the reset button has been clicked, show "???"
            binding.scoreText.text = "?"
            binding.resetButton.isEnabled = false
        } else {
            // Show the actual score
            binding.scoreText.text = score.toString()
            binding.resetButton.isEnabled = true
        }
    }

    companion object {
        fun newIntent(context: Context, score: Int): Intent {
            return Intent(context, ScoreActivity::class.java).apply {
                putExtra(EXTRA_SCORE, score)
            }
        }
    }
}