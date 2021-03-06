package com.proxerme.app.fragment.media

import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.proxerme.app.R
import com.proxerme.app.activity.MediaActivity
import com.proxerme.app.adapter.media.MediaAdapter
import com.proxerme.app.adapter.media.MediaAdapter.MediaAdapterCallback
import com.proxerme.app.dialog.HentaiConfirmationDialog
import com.proxerme.app.event.HentaiConfirmationEvent
import com.proxerme.app.fragment.framework.EasyPagingFragment
import com.proxerme.app.helper.PreferenceHelper
import com.proxerme.app.manager.SectionManager.Section
import com.proxerme.app.util.Utils
import com.proxerme.library.connection.list.entity.MediaListEntry
import com.proxerme.library.connection.list.request.MediaSearchRequest
import com.proxerme.library.parameters.CategoryParameter
import com.proxerme.library.parameters.MediaSortParameter
import com.proxerme.library.parameters.TypeParameter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class MediaListFragment : EasyPagingFragment<MediaListEntry, MediaAdapterCallback>() {

    companion object {

        const val ITEMS_ON_PAGE = 30

        private const val ARGUMENT_CATEGORY = "category"
        private const val STATE_SORT_CRITERIA = "state_sort_criteria"
        private const val STATE_TYPE = "state_type"
        private const val STATE_SEARCH_QUERY = "state_search_query"
        private const val STATE_HAS_SEARCHED = "state_has_searched"

        fun newInstance(@CategoryParameter.Category category: String): MediaListFragment {
            return MediaListFragment().apply {
                this.arguments = Bundle().apply {
                    this.putString(ARGUMENT_CATEGORY, category)
                }
            }
        }
    }

    override val section = Section.MEDIA_LIST
    override val itemsOnPage = ITEMS_ON_PAGE
    override val isSwipeToRefreshEnabled = false

    override val canLoad: Boolean
        get() {
            return checkHentai() && super.canLoad
        }

    @CategoryParameter.Category
    private lateinit var category: String

    @MediaSortParameter.MediaSortCriteria
    private lateinit var sortCriteria: String

    @TypeParameter.Type
    private lateinit var type: String

    private var searchQuery: String? = null
    private var hasSearched = false

    override lateinit var adapter: MediaAdapter
    override lateinit var layoutManager: StaggeredGridLayoutManager

    private lateinit var searchItem: MenuItem
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        category = arguments.getString(ARGUMENT_CATEGORY)

        if (savedInstanceState != null) {
            sortCriteria = savedInstanceState.getString(STATE_SORT_CRITERIA)
            type = savedInstanceState.getString(STATE_TYPE)
            searchQuery = savedInstanceState.getString(STATE_SEARCH_QUERY)
            hasSearched = savedInstanceState.getBoolean(STATE_HAS_SEARCHED)
        } else {
            sortCriteria = MediaSortParameter.RATING
            type = when (category) {
                CategoryParameter.ANIME -> TypeParameter.ALL_ANIME
                CategoryParameter.MANGA -> TypeParameter.ALL_MANGA
                else -> throw IllegalArgumentException("Unknown value for category")
            }
        }

        adapter = MediaAdapter(savedInstanceState, category)
        adapter.callback = object : MediaAdapterCallback() {
            override fun onItemClick(v: View, item: MediaListEntry) {
                MediaActivity.navigateTo(activity, item.id, item.name)
            }
        }

        layoutManager = StaggeredGridLayoutManager(Utils.calculateSpanAmount(activity) + 1,
                StaggeredGridLayoutManager.VERTICAL)

        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)

        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_media_list, menu)

        when (sortCriteria) {
            MediaSortParameter.RATING -> menu.findItem(R.id.rating).isChecked = true
            MediaSortParameter.CLICKS -> menu.findItem(R.id.clicks).isChecked = true
            MediaSortParameter.COUNT -> menu.findItem(R.id.count).isChecked = true
            MediaSortParameter.NAME -> menu.findItem(R.id.name).isChecked = true
        }

        val filterSubMenu = menu.findItem(R.id.filter).subMenu

        when (category) {
            CategoryParameter.ANIME -> filterSubMenu.setGroupVisible(R.id.filterManga, false)
            CategoryParameter.MANGA -> filterSubMenu.setGroupVisible(R.id.filterAnime, false)
        }

        when (type) {
            TypeParameter.ALL_ANIME -> filterSubMenu.findItem(R.id.all_anime).isChecked = true
            TypeParameter.ANIMESERIES -> filterSubMenu.findItem(R.id.animeseries).isChecked = true
            TypeParameter.MOVIE -> filterSubMenu.findItem(R.id.movies).isChecked = true
            TypeParameter.OVA -> filterSubMenu.findItem(R.id.ova).isChecked = true
            TypeParameter.HENTAI -> filterSubMenu.findItem(R.id.hentai).isChecked = true
            TypeParameter.ALL_MANGA -> filterSubMenu.findItem(R.id.all_manga).isChecked = true
            TypeParameter.ONESHOT -> filterSubMenu.findItem(R.id.oneshot).isChecked = true
            TypeParameter.DOUJIN -> filterSubMenu.findItem(R.id.doujin).isChecked = true
            TypeParameter.HMANGA -> filterSubMenu.findItem(R.id.hmanga).isChecked = true
        }

        searchItem = menu.findItem(R.id.search)
        searchView = searchItem.actionView as SearchView

        if (searchQuery != null) {
            searchItem.expandActionView()
            searchView.setQuery(searchQuery, false)
            searchView.clearFocus()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hasSearched = true

                searchView.clearFocus()
                reset()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText

                return false
            }
        })

        MenuItemCompat.setOnActionExpandListener(searchItem,
                object : MenuItemCompat.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                        return true
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                        searchQuery = null

                        if (hasSearched) {
                            reset()

                            hasSearched = false
                        }

                        return true
                    }
                })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val previousCriteria = sortCriteria
        val previousType = type

        when (item.itemId) {
            R.id.rating -> sortCriteria = MediaSortParameter.RATING
            R.id.clicks -> sortCriteria = MediaSortParameter.CLICKS
            R.id.count -> sortCriteria = MediaSortParameter.COUNT
            R.id.name -> sortCriteria = MediaSortParameter.NAME
            R.id.all_anime -> type = TypeParameter.ALL_ANIME
            R.id.animeseries -> type = TypeParameter.ANIMESERIES
            R.id.movies -> type = TypeParameter.MOVIE
            R.id.ova -> type = TypeParameter.OVA
            R.id.hentai -> type = TypeParameter.HENTAI
            R.id.all_manga -> type = TypeParameter.ALL_MANGA
            R.id.oneshot -> type = TypeParameter.ONESHOT
            R.id.doujin -> type = TypeParameter.DOUJIN
            R.id.hmanga -> type = TypeParameter.HMANGA
            else -> return false
        }

        if (previousCriteria != sortCriteria || previousType != type) {
            reset()

            item.isChecked = true
        }

        return true
    }

    override fun onDestroyOptionsMenu() {
        searchView.setOnQueryTextListener(null)
        MenuItemCompat.setOnActionExpandListener(searchItem, null)

        super.onDestroyOptionsMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkHentai()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        adapter.saveInstanceState(outState)
        outState.putString(STATE_SORT_CRITERIA, sortCriteria)
        outState.putString(STATE_TYPE, type)
        outState.putString(STATE_SEARCH_QUERY, searchQuery)
        outState.putBoolean(STATE_HAS_SEARCHED, hasSearched)
    }

    override fun constructPagedLoadingRequest(page: Int): LoadingRequest<Array<MediaListEntry>> {
        return LoadingRequest(MediaSearchRequest(page)
                .withName(searchQuery)
                .withType(type)
                .withSortCriteria(sortCriteria)
                .withLimit(ITEMS_ON_PAGE))
    }

    /**
     * ( ͡° ͜ʖ ͡°)
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onHentaiConfirmation(@Suppress("UNUSED_PARAMETER") event: HentaiConfirmationEvent) {
        if (type == TypeParameter.HENTAI || type == TypeParameter.HMANGA) {
            reset()
        }
    }

    private fun checkHentai(): Boolean {
        if (type == TypeParameter.HENTAI || type == TypeParameter.HMANGA) {
            if (!PreferenceHelper.isHentaiAllowed(context)) {
                doShowError(getString(R.string.error_hentai_confirmation_needed),
                        getString(R.string.error_confirm),
                        onButtonClickListener = View.OnClickListener {
                            HentaiConfirmationDialog.show(activity as AppCompatActivity)
                        })

                return false
            }
        }

        return true
    }
}