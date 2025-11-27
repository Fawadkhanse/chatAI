package com.example.chatai.domain.repository


import com.example.chatai.data.Resource
import com.example.chatai.data.remote.HttpMethod
import kotlinx.coroutines.flow.Flow


import okhttp3.MultipartBody
import okhttp3.RequestBody

interface RemoteRepository {
    suspend fun makeApiRequest(
        requestModel: Any? = null,
        endpoint: String,
        httpMethod: HttpMethod = HttpMethod.POST,
        returnErrorBody: Boolean = false,
        isMockResponse: Boolean = false
    ): Flow<Resource<String>>

    suspend fun makeMultipartRequest(
        params: Map<String, RequestBody>,
        image: MultipartBody.Part?,
        endpoint: String,
        httpMethod: HttpMethod = HttpMethod.POST,
    ): Flow<Resource<String>>
}