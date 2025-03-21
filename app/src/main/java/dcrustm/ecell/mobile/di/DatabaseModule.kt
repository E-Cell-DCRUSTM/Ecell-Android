package dcrustm.ecell.mobile.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dcrustm.ecell.mobile.data.local.image.ImageDao
import dcrustm.ecell.mobile.data.local.image.ImageDatabase
import dcrustm.ecell.mobile.data.local.image.ImageMetadataDao
import dcrustm.ecell.mobile.data.local.profile.ProfileDao
import dcrustm.ecell.mobile.data.local.profile.ProfileDatabase
import dcrustm.ecell.mobile.data.remote.image.ImagesApiService
import dcrustm.ecell.mobile.data.repository.ImageRepositoryImpl
import dcrustm.ecell.mobile.domain.dummy.ImageRepository
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
    fun provideProfileDatabase(@ApplicationContext context: Context): ProfileDatabase {
        return Room.databaseBuilder(context, ProfileDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideImageDatabase(@ApplicationContext context: Context): ImageDatabase =
        Room.databaseBuilder(context, ImageDatabase::class.java, "images_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideImageDao(database: ImageDatabase): ImageDao = database.imageDao()

    @Provides
    fun provideImageMetadataDao(database: ImageDatabase): ImageMetadataDao =
        database.metadataDao()

    @Provides
    @Singleton
    fun provideImageRepository(
        imageDao: ImageDao,
        metadataDao: ImageMetadataDao,
        apiService: ImagesApiService
    ): ImageRepository {
        return ImageRepositoryImpl(imageDao, metadataDao, apiService)
    }

    @Provides
    fun provideUserDao(database: ProfileDatabase): ProfileDao {
        return database.profileDao()
    }

}