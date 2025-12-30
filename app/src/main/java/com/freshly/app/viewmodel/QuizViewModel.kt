package com.freshly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.freshly.app.data.model.QuizQuestion
import com.freshly.app.data.model.QuizQuestions
import com.freshly.app.data.repository.UserRepository

class QuizViewModel : ViewModel() {
    
    private val userRepository = UserRepository()
    
    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions: StateFlow<List<QuizQuestion>> = _questions.asStateFlow()
    
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()
    
    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer.asStateFlow()
    
    private val _isAnswerRevealed = MutableStateFlow(false)
    val isAnswerRevealed: StateFlow<Boolean> = _isAnswerRevealed.asStateFlow()
    
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()
    
    private val _isQuizComplete = MutableStateFlow(false)
    val isQuizComplete: StateFlow<Boolean> = _isQuizComplete.asStateFlow()
    
    init {
        startNewQuiz()
    }
    
    fun startNewQuiz() {
        _questions.value = QuizQuestions.getRandomQuestions(5)
        _currentQuestionIndex.value = 0
        _selectedAnswer.value = null
        _isAnswerRevealed.value = false
        _score.value = 0
        _isQuizComplete.value = false
    }
    
    fun selectAnswer(answerIndex: Int) {
        if (!_isAnswerRevealed.value) {
            _selectedAnswer.value = answerIndex
        }
    }
    
    fun revealAnswer() {
        if (_selectedAnswer.value != null && !_isAnswerRevealed.value) {
            _isAnswerRevealed.value = true
            
            val currentQuestion = _questions.value[_currentQuestionIndex.value]
            if (_selectedAnswer.value == currentQuestion.correctAnswer) {
                _score.value += 1
            }
        }
    }
    
    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value += 1
            _selectedAnswer.value = null
            _isAnswerRevealed.value = false
        } else {
            _isQuizComplete.value = true
            // Award XP based on quiz performance
            val percentage = getScorePercentage()
            val xpReward = when {
                percentage >= 80 -> 50 // Perfect/Excellent
                percentage >= 60 -> 30 // Good
                percentage >= 40 -> 20 // Average
                else -> 10 // Participation
            }
            viewModelScope.launch {
                userRepository.addXP(xpReward)
            }
        }
    }
    
    fun getCurrentQuestion(): QuizQuestion? {
        return _questions.value.getOrNull(_currentQuestionIndex.value)
    }
    
    fun getScorePercentage(): Int {
        return if (_questions.value.isNotEmpty()) {
            ((_score.value.toFloat() / _questions.value.size) * 100).toInt()
        } else 0
    }
}
