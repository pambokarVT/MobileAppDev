package edu.vt.cs5254.multiquiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.vt.cs5254.multiquiz.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Binding object to access views via ViewBinding
    private lateinit var binding: ActivityMainBinding

    private lateinit var answerButtons: List<Button>

    // ViewModel initialization via "by viewModels()" for lifecycle-aware ViewModel
    private val quizViewModel: QuizViewModel by viewModels()

    private lateinit var scoreActivityLauncher: ActivityResultLauncher<Intent>

    //eventually create a launcher to start the score activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register the launcher for ScoreActivity
        scoreActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // Check if the user reset the quiz
                val shouldResetQuiz = result.data?.getBooleanExtra("edu.vt.cs5254.multiquiz.reset", false) ?: false
                if (shouldResetQuiz) {
                    quizViewModel.resetQuiz()
                    updateView(true)  // Reset to the first question and update the view
                } else {
                    // Return to the first question and restore the previous state
                    quizViewModel.currentQuestionIndex = 0
                }
            }
        }

        // List of buttons corresponding to answer buttons
        answerButtons = listOf(
            binding.answer0Button,
            binding.answer1Button,
            binding.answer2Button,
            binding.answer3Button
        )

        binding.hintButton.setOnClickListener {
            quizViewModel.answerList.filter{ it.isEnabled && !it.isCorrect}
                .random()
                .let {
                    it.isEnabled = false
                    it.isSelected = false
                }
            updateView()
        }

        binding.submitButton.setOnClickListener {
            if (quizViewModel.isLastQuestion) {
                // Calculate the score at the end of the quiz
                val correctAnswers = quizViewModel.questions.flatMap { it.answerList }
                    .count { it.isCorrect && it.isSelected }  // Count the correct answers
                val hintsUsed = quizViewModel.questions.flatMap { it.answerList }
                    .count { !it.isCorrect && !it.isEnabled }  // Count the hints used (disabled incorrect answers)

                // Score formula: 25 points per correct answer, minus 8 points per hint used
                val score = correctAnswers * 25 - hintsUsed * 8

                // Launch the ScoreActivity using the activity result launcher
                scoreActivityLauncher.launch(ScoreActivity.newIntent(this, score))
            } else {
                quizViewModel.nextQuestion()
                updateView(true)
            }
        }
        updateView(true)
    }

    // Update the view based on the current state
    private fun updateView(fullUpdate: Boolean = false) {
        if (fullUpdate) {
            binding.questionTextView.setText(quizViewModel.questionTestId)
            quizViewModel.answerList.zip(answerButtons)
                .forEach { (answer, button) ->
                    button.setText(answer.textResId)
                    button.setOnClickListener {
                        answer.isSelected = !answer.isSelected
                        quizViewModel.answerList.filter { it != answer }
                            .forEach { it.isSelected = false }
                        updateView()
                    }
                }

        }

        quizViewModel.answerList.zip(answerButtons)
            .forEach { (answer, button) ->
                button.isSelected = answer.isSelected
                button.isEnabled = answer.isEnabled
                button.updateColor()
            }


        // Enable/disable the Hint button
        binding.hintButton.isEnabled = quizViewModel.answerList.any { !it.isCorrect && it.isEnabled }

        // Enable the Submit button only if an answer is selected
        binding.submitButton.isEnabled = quizViewModel.answerList.any { it.isSelected }
    }
}
