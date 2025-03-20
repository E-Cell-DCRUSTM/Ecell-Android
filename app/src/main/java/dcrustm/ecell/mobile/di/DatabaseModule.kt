package dcrustm.ecell.mobile.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dcrustm.ecell.mobile.data.local.user.UserDao
import dcrustm.ecell.mobile.data.local.user.UserDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

//    @Provides
//    @Singleton
//    fun provideQuizDatabase(@ApplicationContext context: Context): QuizDatabase =
//        Room.databaseBuilder(context, QuizDatabase::class.java, "quiz_database").build()
//
//    @Provides
//    fun provideQuizDao(database: QuizDatabase): QuizDao = database.quizDao()
//
//    @Provides
//    @Singleton
//    fun provideQuizRepository(dao: QuizDao): QuizRepository = QuizRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideUserDatabase(@ApplicationContext context: Context): UserDatabase {
        return Room.databaseBuilder(context, UserDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: UserDatabase): UserDao {
        return database.userDao()
    }

}