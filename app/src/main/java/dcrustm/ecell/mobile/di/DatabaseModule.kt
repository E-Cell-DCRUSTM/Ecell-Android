package dcrustm.ecell.mobile.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dcrustm.ecell.mobile.data.local.quiz.QuizDao
import dcrustm.ecell.mobile.data.local.quiz.QuizDatabase
import dcrustm.ecell.mobile.data.local.quiz.QuizRepository
import dcrustm.ecell.mobile.data.local.quiz.QuizRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQuizDatabase(@ApplicationContext context: Context): QuizDatabase =
        Room.databaseBuilder(context, QuizDatabase::class.java, "quiz_database").build()

    @Provides
    fun provideQuizDao(database: QuizDatabase): QuizDao = database.quizDao()

    @Provides
    @Singleton
    fun provideQuizRepository(dao: QuizDao): QuizRepository = QuizRepositoryImpl(dao)

}