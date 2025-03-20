package dcrustm.ecell.mobile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            // Other engines/settings such as timeouts or logging can be added here if needed.
        }
    }

    @Provides
    @Singleton
    @Named("base_url")
    fun provideBaseUrl(): String {
        // For Android emulator, use "10.0.2.2" to access your host machine.
        // For a physical device, replace this with your machine's IP address (e.g., "http://192.168.x.x:8080")
        return "http://192.168.69.65:8080"
    }
}
