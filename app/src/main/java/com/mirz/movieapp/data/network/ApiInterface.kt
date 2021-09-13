package com.mirz.movieapp.data.network

import com.mirz.movieapp.data.model.MovieDetail
import com.mirz.movieapp.data.model.SearchResults
import com.mirz.movieapp.util.AppConstant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiInterface {

    @GET("?apiKey=${AppConstant.API_KEY}")
    suspend fun getSearchResultData(
        @Query(value = "s") searchTitle: String,
        @Query(value = "type" ) type: String,
        @Query(value = "page") pageIndex: Int
    ): Response<SearchResults>

    @GET("?plot=full")
    suspend fun getMovieDetailData(
        @Query(value = "t") title: String,
        @Query(value = "apiKey") apiKey: String
    ): Response<MovieDetail>

    companion object {
        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ): ApiInterface {

            val okkHttpclient = OkHttpClient.Builder()
                .addInterceptor(networkConnectionInterceptor)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .client(okkHttpclient)
                .baseUrl("https://www.omdbapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiInterface::class.java)
        }
    }
}