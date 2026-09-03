package com.example.data.network

import com.example.data.model.CalendarDataResponse
import com.example.data.model.FinancesResponse
import com.example.data.model.LoginRequest
import com.example.data.model.LoginResponse
import com.example.data.model.MessagesResponse
import com.example.data.model.NeptunRequest
import com.example.data.model.SubjectGradesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NeptunApiService {

    @POST("Login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("GetCalendarData")
    suspend fun getCalendarData(
        @Body request: NeptunRequest
    ): Response<CalendarDataResponse>

    @POST("GetSubjectGrades")
    suspend fun getSubjectGrades(
        @Body request: NeptunRequest
    ): Response<SubjectGradesResponse>

    @POST("GetMessages")
    suspend fun getMessages(
        @Body request: NeptunRequest
    ): Response<MessagesResponse>

    @POST("GetFinances")
    suspend fun getFinances(
        @Body request: NeptunRequest
    ): Response<FinancesResponse>
}
