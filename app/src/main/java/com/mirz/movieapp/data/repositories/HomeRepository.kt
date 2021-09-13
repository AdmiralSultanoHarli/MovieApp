package com.mirz.movieapp.data.repositories

import com.mirz.movieapp.data.model.SearchResults
import com.mirz.movieapp.data.network.ApiInterface
import com.mirz.movieapp.data.network.SafeApiRequest

class HomeRepository(
    private val api: ApiInterface
) : SafeApiRequest() {

    suspend fun getMovies(
        searchTitle: String,
        type: String,
        pageIndex: Int
    ): SearchResults {

        return apiRequest { api.getSearchResultData(searchTitle, type, pageIndex)}
    }


}