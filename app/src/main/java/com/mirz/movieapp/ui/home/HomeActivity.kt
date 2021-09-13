package com.mirz.movieapp.ui.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mirz.movieapp.R
import com.mirz.movieapp.ui.adapter.CustomAdapterMovies
import com.mirz.movieapp.ui.moviedetail.MovieDetailScrollingActivity
import com.mirz.movieapp.util.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import androidx.core.view.MenuItemCompat
import com.mirz.movieapp.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity(), KodeinAware {

    companion object {
        const val ANIMATION_DURATION = 1000.toLong()
    }

    override val kodein by kodein()
    private lateinit var dataBind: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private val factory: HomeViewModelFactory by instance()
    private lateinit var customAdapterMovies: CustomAdapterMovies
    private lateinit var searchView: SearchView
    private lateinit var searchMenuItem : MenuItem

    private var searchType: String = AppConstant.MOVIE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBind = DataBindingUtil.setContentView(this, R.layout.activity_home)
        setHomeTitle()
        setupViewModel()
        setupUI()
        initializeObserver()
        handleNetworkChanges()
        setupAPICall()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        searchMenuItem = menu.findItem(R.id.search)
        searchView = searchMenuItem.actionView as SearchView
        searchView.apply {
            queryHint = "Search"
            isSubmitButtonEnabled = true
            onActionViewExpanded()
        }
        search(searchView, searchMenuItem)
        return true
    }

    /*private fun initializeView() {
        dataBind.searchRadioRoup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { _, checkedId ->
            run {
                when (checkedId) {
                    R.id.radio_movies -> searchType = AppConstant.MOVIE
                    R.id.radio_series -> searchType = AppConstant.SERIES
                }
            }

        })
    }*/

    private fun setupUI() {
        dataBind.searchRadioRoup.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { _, checkedId ->
            run {
                when (checkedId) {
                    R.id.radio_movies -> {
                        searchType = AppConstant.MOVIE
                        dataBind.radioMovies.isChecked = true
                        dataBind.radioSeries.isChecked = false
                        setHomeTitle()
                    }
                    R.id.radio_series -> {
                        searchType = AppConstant.SERIES
                        dataBind.radioSeries.isChecked = true
                        dataBind.radioMovies.isChecked = false
                        setHomeTitle()
                    }
                }
            }

        })
        customAdapterMovies = CustomAdapterMovies()
        dataBind.recyclerViewMovies.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = customAdapterMovies
            addOnItemTouchListener(
                RecyclerItemClickListener(
                    applicationContext,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
                            if (customAdapterMovies.getData().isNotEmpty()) {
                                val searchItem = customAdapterMovies.getData()[position]
                                searchItem?.let {
                                    val intent =
                                        Intent(
                                            applicationContext,
                                            MovieDetailScrollingActivity::class.java
                                        )
                                    intent.putExtra(AppConstant.INTENT_POSTER, it.poster)
                                    intent.putExtra(AppConstant.INTENT_TITLE, it.title)
                                    startActivity(intent)
                                }

                            }
                        }

                    })
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    val visibleItemCount = layoutManager!!.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    viewModel.checkForLoadMoreItems(
                        visibleItemCount,
                        totalItemCount,
                        firstVisibleItemPosition
                    )
                }

            })
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

    }

    private fun initializeObserver() {
        viewModel.movieNameLiveData.observe(this, Observer {
            Log.i("Info", "Movie Name = $it")
        })
        viewModel.loadMoreListLiveData.observe(this, Observer {
            if (it) {
                customAdapterMovies.setData(null)
                Handler().postDelayed({
                    viewModel.loadMore()
                }, 2000)
            }
        })
    }

    private fun setupAPICall() {
        viewModel.moviesLiveData.observe(this, Observer { state ->
            when (state) {
                is State.Loading -> {
                    dataBind.rlError.hide()
                    dataBind.recyclerViewMovies.hide()
                    dataBind.linearLayoutSearch.hide()
                    dataBind.shimmerProgress.show()
                    setHomeTitle()
                }
                is State.Success -> {
                    dataBind.rlError.hide()
                    dataBind.recyclerViewMovies.show()
                    dataBind.linearLayoutSearch.hide()
                    dataBind.shimmerProgress.hide()
                    customAdapterMovies.setData(state.data)
                }
                is State.Error -> {
                    dataBind.shimmerProgress.hide()
                    dataBind.rlError.show()
                    dataBind.textError.text = state.message
                }
            }
        })

    }

    private fun setHomeTitle(){
        if (searchType.equals(AppConstant.MOVIE)){
            setTitle(R.string.movie)
        } else{
            setTitle(R.string.series)
        }
    }

    private fun handleNetworkChanges() {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this, Observer { isConnected ->
            if (!isConnected) {
                dataBind.textViewNetworkStatus.text = getString(R.string.text_no_connectivity)
                dataBind.networkStatusLayout.apply {
                    show()
                    setBackgroundColor(getColorRes(R.color.colorStatusNotConnected))
                }
            } else {
                if (viewModel.moviesLiveData.value is State.Error || customAdapterMovies.itemCount == 0) {
                    viewModel.getMovies()
                }
                dataBind.textViewNetworkStatus.text = getString(R.string.text_connectivity)
                dataBind.networkStatusLayout.apply {
                    setBackgroundColor(getColorRes(R.color.colorStatusConnected))

                    animate()
                        .alpha(1f)
                        .setStartDelay(ANIMATION_DURATION)
                        .setDuration(ANIMATION_DURATION)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                hide()
                            }
                        })
                }
            }
        })
    }

    private fun search(searchView: SearchView, searchMenuItem: MenuItem) {

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                dismissKeyboard(searchView)
                searchView.clearFocus()
                viewModel.searchMovie(query, searchType)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.trim().length >= 3) {
                    //searchView.clearFocus()
                    viewModel.searchMovie(newText, searchType)
                } else if (newText.trim().length < 3 && newText.trim().length > 0){

                } else {
                    dismissKeyboard(searchView)
                    searchView.clearFocus()
                    dataBind.recyclerViewMovies.hide()
                    dataBind.linearLayoutSearch.show()
                }
                return true
            }
        })

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, object : MenuItemCompat.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView.setQuery("", true)
                searchView.isIconified = false
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                dismissKeyboard(searchView)
                searchView.setQuery("", false)
                searchView.isIconified = true
                dataBind.recyclerViewMovies.hide()
                dataBind.linearLayoutSearch.show()
                return true
            }

        })
    }


}