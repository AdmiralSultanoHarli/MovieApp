package com.mirz.movieapp.data.repositories

import com.mirz.movieapp.data.model.MovieDetail
import com.mirz.movieapp.data.network.ApiInterface
import com.mirz.movieapp.data.network.SafeApiRequest

class MovieDetailRepository(
    private val api: ApiInterface
) : SafeApiRequest() {

    suspend fun getMovieDetail(
        title: String,
        apiKey: String
    ): MovieDetail {

        return apiRequest { api.getMovieDetailData(title, apiKey) }
    }


}