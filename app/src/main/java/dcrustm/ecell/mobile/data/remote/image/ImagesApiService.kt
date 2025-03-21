package dcrustm.ecell.mobile.data.remote.image

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.io.File

class ImagesApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {

    // POST /api/images/upload – requires ADMIN/SUPERUSER token; uploads an image as multipart/form-data.
    suspend fun uploadImage(file: File, caption: String, authToken: String): ImageResponse {
        return client.post("$baseUrl/api/images/upload") {
            header("Authorization", "Bearer $authToken")
            // Set multipart request
            setBody(MultiPartFormDataContent(formData {
                append("file", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg") // (adjust accordingly)
                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                })
                append("caption", caption)
            }))
        }.body()
    }

    // GET /api/images – fetch all images.
    suspend fun fetchAllImages(): List<ImageResponse> {
        return client.get("$baseUrl/api/images").body()
    }

    // GET /api/images?after=ISO-8601 formatted value – fetch images after a given timestamp.
    suspend fun fetchImagesAfter(after: String): List<ImageResponse> {
        return client.get("$baseUrl/api/images") {
            parameter("after", after)
        }.body()
    }

}