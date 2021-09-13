package com.mirz.movieapp

import android.app.Application
import com.mirz.movieapp.data.network.ApiInterface
import com.mirz.movieapp.data.network.NetworkConnectionInterceptor
import com.mirz.movieapp.data.repositories.HomeRepository
import com.mirz.movieapp.data.repositories.MovieDetailRepository
import com.mirz.movieapp.ui.home.HomeViewModelFactory
import com.mirz.movieapp.ui.moviedetail.MovieDetailViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class MFApplication : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@MFApplication))

        bind() from this.singleton { NetworkConnectionInterceptor(this.instance()) }
        bind() from this.singleton { ApiInterface(this.instance()) }
        bind() from this.singleton { HomeRepository(this.instance()) }
        bind() from this.provider { HomeViewModelFactory(this.instance()) }
        bind() from this.singleton { MovieDetailRepository(this.instance()) }
        bind() from this.provider { MovieDetailViewModelFactory(this.instance()) }


    }

}