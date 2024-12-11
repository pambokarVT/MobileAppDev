package edu.vt.cs5254.multiquiz

import androidx.lifecycle.ViewModel

class QuizViewModel: ViewModel() {

    // List of questions
    val questions = listOf(
        Question(
            questionResId = R.string.question_1,
            answerList = listOf(
                Answer(textResId = R.string.answer_1_0, isCorrect = true),
                Answer(textResId = R.string.answer_1_1, isCorrect = false),
                Answer(textResId = R.string.answer_1_2, isCorrect = false),
                Answer(textResId = R.string.answer_1_3, isCorrect = false)
            )
        ),
        Question(
            questionResId = R.string.question_2,
            answerList = listOf(
                Answer(textResId = R.string.answer_2_0, isCorrect = false),
                Answer(textResId = R.string.answer_2_1, isCorrect = true),
                Answer(textResId = R.string.answer_2_2, isCorrect = false),
                Answer(textResId = R.string.answer_2_3, isCorrect = false)
            )
        ),
        Question(
            questionResId = R.string.question_3,
            answerList = listOf(
                Answer(textResId = R.string.answer_3_0, isCorrect = false),
                Answer(textResId = R.string.answer_3_1, isCorrect = false),
                Answer(textResId = R.string.answer_3_2, isCorrect = true),
                Answer(textResId = R.string.answer_3_3, isCorrect = false)
            )
        ),
        Question(
            questionResId = R.string.question_4,
            answerList = listOf(
                Answer(textResId = R.string.answer_4_0, isCorrect = false),
                Answer(textResId = R.string.answer_4_1, isCorrect = false),
                Answer(textResId = R.string.answer_4_2, isCorrect = false),
                Answer(textResId = R.string.answer_4_3, isCorrect = true)
            )
        )
    )

    // Track the current question index
    var currentQuestionIndex = 0

    val questionTestId
            get() = questions[currentQuestionIndex].questionResId

    val answerList
            get() = questions[currentQuestionIndex].answerList

    fun nextQuestion() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size
    }

    val isLastQuestion
        get() = (currentQuestionIndex == questions.size - 1)

    fun resetQuiz() {
        currentQuestionIndex = 0
        questions.forEach { question ->
            question.answerList.forEach {
                it.isSelected = false
                it.isEnabled = true
            }
        }
    }
}
