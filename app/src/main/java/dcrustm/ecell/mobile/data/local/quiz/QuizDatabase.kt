package dcrustm.ecell.mobile.data.local.quiz

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [QuizEntity::class, QuestionEntity::class], version = 1)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
}