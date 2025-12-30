package com.freshly.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshly.app.ui.theme.Primary500
import com.freshly.app.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val isAnswerRevealed by viewModel.isAnswerRevealed.collectAsState()
    val score by viewModel.score.collectAsState()
    val isQuizComplete by viewModel.isQuizComplete.collectAsState()
    
    val currentQuestion = remember(questions, currentIndex) {
        questions.getOrNull(currentIndex)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Quiz", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Primary500.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            if (isQuizComplete) {
                QuizCompletionScreen(
                    score = score,
                    total = questions.size,
                    onRetake = { viewModel.startNewQuiz() },
                    onExit = onNavigateBack
                )
            } else {
                currentQuestion?.let { question ->
                    QuizQuestionScreen(
                        question = question,
                        questionNumber = currentIndex + 1,
                        totalQuestions = questions.size,
                        selectedAnswer = selectedAnswer,
                        isAnswerRevealed = isAnswerRevealed,
                        onAnswerSelected = { viewModel.selectAnswer(it) },
                        onRevealAnswer = { viewModel.revealAnswer() },
                        onNextQuestion = { viewModel.nextQuestion() },
                        currentScore = score
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizQuestionScreen(
    question: com.freshly.app.data.model.QuizQuestion,
    questionNumber: Int,
    totalQuestions: Int,
    selectedAnswer: Int?,
    isAnswerRevealed: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onRevealAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    currentScore: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Progress and Score
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Question Counter
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Primary500.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Question $questionNumber/$totalQuestions",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = Primary500
                    )
                }
                
                // Score Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "$currentScore",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = questionNumber.toFloat() / totalQuestions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Primary500,
                trackColor = Primary500.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Category Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (question.category) {
                    com.freshly.app.data.model.QuizCategory.EXPIRATION -> Color(0xFFE8F5E9)
                    com.freshly.app.data.model.QuizCategory.STORAGE -> Color(0xFFE3F2FD)
                    com.freshly.app.data.model.QuizCategory.NUTRITION -> Color(0xFFFFF3E0)
                    com.freshly.app.data.model.QuizCategory.FOOD_SAFETY -> Color(0xFFFCE4EC)
                }
            ) {
                Text(
                    text = question.category.name.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (question.category) {
                        com.freshly.app.data.model.QuizCategory.EXPIRATION -> Color(0xFF2E7D32)
                        com.freshly.app.data.model.QuizCategory.STORAGE -> Color(0xFF1565C0)
                        com.freshly.app.data.model.QuizCategory.NUTRITION -> Color(0xFFE65100)
                        com.freshly.app.data.model.QuizCategory.FOOD_SAFETY -> Color(0xFFC2185B)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Question
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Answer Options
            question.options.forEachIndexed { index, option ->
                AnswerOption(
                    text = option,
                    isSelected = selectedAnswer == index,
                    isCorrect = index == question.correctAnswer,
                    isRevealed = isAnswerRevealed,
                    onClick = { onAnswerSelected(index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Explanation (shown after reveal)
            AnimatedVisibility(
                visible = isAnswerRevealed,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF9C4)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFF57F17),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = question.explanation,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF33691E)
                        )
                    }
                }
            }
        }
        
        // Bottom Button
        Button(
            onClick = {
                if (!isAnswerRevealed && selectedAnswer != null) {
                    onRevealAnswer()
                } else if (isAnswerRevealed) {
                    onNextQuestion()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedAnswer != null,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary500
            )
        ) {
            Text(
                text = if (!isAnswerRevealed) "Check Answer" else "Next Question",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isRevealed: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isRevealed && isCorrect -> Color(0xFFE8F5E9)
        isRevealed && isSelected && !isCorrect -> Color(0xFFFFEBEE)
        isSelected -> Primary500.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        isRevealed && isCorrect -> Color(0xFF4CAF50)
        isRevealed && isSelected && !isCorrect -> Color(0xFFF44336)
        isSelected -> Primary500
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    
    Surface(
        onClick = if (!isRevealed) onClick else {{}},
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        tonalElevation = if (isSelected && !isRevealed) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            
            if (isRevealed) {
                if (isCorrect) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF4CAF50), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF44336), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizCompletionScreen(
    score: Int,
    total: Int,
    onRetake: () -> Unit,
    onExit: () -> Unit
) {
    val percentage = ((score.toFloat() / total) * 100).toInt()
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Trophy Icon with animation
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale.value)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(80.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Quiz Complete!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when {
                percentage == 100 -> "Perfect Score! 🎉"
                percentage >= 80 -> "Excellent Work! 🌟"
                percentage >= 60 -> "Good Job! 👍"
                else -> "Keep Learning! 📚"
            },
            style = MaterialTheme.typography.titleLarge,
            color = Primary500
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Score Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Primary500.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Score",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$score / $total",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary500
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Action Buttons
        Button(
            onClick = onRetake,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary500)
        ) {
            Text(
                text = "Take Quiz Again",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Primary500
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, Primary500)
        ) {
            Text(
                text = "Exit Quiz",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
