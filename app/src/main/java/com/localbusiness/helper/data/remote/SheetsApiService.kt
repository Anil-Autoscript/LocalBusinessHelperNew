package com.localbusiness.helper.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Google Sheets API v4 - Read access (public sheets with API key)
interface SheetsApiService {

    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getSheetValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("key") apiKey: String
    ): Response<SheetResponse>
}

data class SheetResponse(
    val range: String,
    val majorDimension: String,
    val values: List<List<String>>?
)

data class SheetRow(
    val customerName: String,
    val phone: String,
    val product: String,
    val quantity: String,
    val price: String,
    val orderDate: String,
    val deliveryDate: String,
    val paymentStatus: String,
    val followUpDate: String
)
