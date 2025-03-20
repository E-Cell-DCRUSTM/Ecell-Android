//package dcrustm.ecell.mobile.ui.quiz
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.ExperimentalLayoutApi
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.Button
//import androidx.compose.material.ButtonDefaults
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.OutlinedTextField
//import androidx.compose.material.RadioButton
//import androidx.compose.material.RadioButtonDefaults
//import androidx.compose.material.Text
//import androidx.compose.material.TextFieldDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.google.accompanist.flowlayout.FlowRow
//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//fun QuestionAdditionScreen(
//    viewModel: QuizViewModel = hiltViewModel(),
//    onFinish: () -> Unit
//) {
//// Root column with vertical scroll and padding; background is set to provide a minimal look.
//    Column(
//        modifier = Modifier
//            .verticalScroll(rememberScrollState())
//            .background(MaterialTheme.colors.background)
//            .padding(16.dp)
//    ) {
//// Header text with a bolder style.
//        Text(
//            text = "Add Question",
//            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
//            color = MaterialTheme.colors.primary
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Question text field with custom rounded corners and border colors.
//        OutlinedTextField(
//            value = viewModel.currentQuestionText,
//            onValueChange = { viewModel.currentQuestionText = it },
//            label = { Text("Question Text") },
//            modifier = Modifier.fillMaxWidth(),
//            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
//            colors = TextFieldDefaults.outlinedTextFieldColors(
//                focusedBorderColor = MaterialTheme.colors.primary,
//                unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
//            )
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Question type selector.
//        Text(
//            text = "Select Question Type",
//            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
//        )
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            RadioButton(
//                selected = viewModel.currentQuestionType == "MCQ",
//                onClick = { viewModel.currentQuestionType = "MCQ" },
//                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
//            )
//            Text(
//                text = "MCQ",
//                modifier = Modifier.clickable { viewModel.currentQuestionType = "MCQ" }
//            )
//            Spacer(modifier = Modifier.width(16.dp))
//            RadioButton(
//                selected = viewModel.currentQuestionType == "SHORT",
//                onClick = { viewModel.currentQuestionType = "SHORT" },
//                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
//            )
//            Text(
//                text = "Short Answer",
//                modifier = Modifier.clickable { viewModel.currentQuestionType = "SHORT" }
//            )
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//
//        if (viewModel.currentQuestionType == "MCQ") {
//            // Four option text fields, each with the same custom styling.
//            OutlinedTextField(
//                value = viewModel.option1,
//                onValueChange = { viewModel.option1 = it },
//                label = { Text("Option 1") },
//                modifier = Modifier.fillMaxWidth(),
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = MaterialTheme.colors.primary,
//                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
//                )
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            OutlinedTextField(
//                value = viewModel.option2,
//                onValueChange = { viewModel.option2 = it },
//                label = { Text("Option 2") },
//                modifier = Modifier.fillMaxWidth(),
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = MaterialTheme.colors.primary,
//                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
//                )
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            OutlinedTextField(
//                value = viewModel.option3,
//                onValueChange = { viewModel.option3 = it },
//                label = { Text("Option 3") },
//                modifier = Modifier.fillMaxWidth(),
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = MaterialTheme.colors.primary,
//                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
//                )
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            OutlinedTextField(
//                value = viewModel.option4,
//                onValueChange = { viewModel.option4 = it },
//                label = { Text("Option 4") },
//                modifier = Modifier.fillMaxWidth(),
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = MaterialTheme.colors.primary,
//                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
//                )
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Label for the correct option selector.
//            Text(
//                text = "Select Correct Option",
//                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
//            )
//            // Using FlowRow lets items wrap onto multiple lines if needed.
//            FlowRow(
//                mainAxisSpacing = 8.dp,
//                crossAxisSpacing = 8.dp,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                listOf("1", "2", "3", "4").forEachIndexed { index, option ->
//                    // Each option is clickable to toggle its radio button.
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.clickable { viewModel.correctOptionIndex = index }
//                    ) {
//                        RadioButton(
//                            selected = viewModel.correctOptionIndex == index,
//                            onClick = { viewModel.correctOptionIndex = index },
//                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
//                        )
//                        Text(text = "Option $option")
//                    }
//                }
//            }
//        } else {
//            // For short answers, limit the input to a single word.
//            OutlinedTextField(
//                value = viewModel.shortAnswer,
//                onValueChange = {
//                    if (!it.contains(" ")) {
//                        viewModel.shortAnswer = it
//                    }
//                },
//                label = { Text("Answer (Single word)") },
//                modifier = Modifier.fillMaxWidth(),
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = MaterialTheme.colors.primary,
//                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
//                )
//            )
//        }
//
//        if (viewModel.questionError.isNotEmpty()) {
//            Text(text = viewModel.questionError, color = MaterialTheme.colors.error)
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Two buttons for adding a question and finishing the quiz.
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Button(
//                onClick = {
//                    viewModel.addCurrentQuestion()
//                },
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
//                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
//            ) {
//                Text("Add Question", color = Color.White)
//            }
//            Button(
//                onClick = {
//                    viewModel.saveQuiz(
//                        onSuccess = { onFinish() },
//                        onError = { message ->
//                            // In a real app, you might display a Snackbar or Toast here.
//                        }
//                    )
//                },
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
//                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
//            ) {
//                Text("Finish Quiz", color = Color.White)
//            }
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "Added Questions: ${viewModel.questions.size}",
//            style = MaterialTheme.typography.body2
//        )
//    }
//}