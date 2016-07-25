package com.proxerme.app.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import butterknife.bindView
import com.proxerme.app.R
import com.proxerme.app.customtabs.CustomTabActivityHelper
import com.proxerme.app.dialog.LoginDialog
import com.proxerme.app.dialog.LogoutDialog
import com.proxerme.app.fragment.ConferencesFragment
import com.proxerme.app.fragment.NewsFragment
import com.proxerme.app.fragment.SettingsFragment
import com.proxerme.app.helper.IntroductionHelper
import com.proxerme.app.helper.MaterialDrawerHelper
import com.proxerme.app.helper.MaterialDrawerHelper.Companion.ACCOUNT_GUEST
import com.proxerme.app.helper.MaterialDrawerHelper.Companion.ACCOUNT_LOGIN
import com.proxerme.app.helper.MaterialDrawerHelper.Companion.ACCOUNT_LOGOUT
import com.proxerme.app.helper.MaterialDrawerHelper.Companion.ACCOUNT_USER
import com.proxerme.app.helper.MaterialDrawerHelper.Companion.ITEM_CHAT
import com.proxerme.app.helper.MaterialDrawerHelper.Companion.ITEM_DONATE
import com.proxerme.app.helper.MaterialDrawerHelper.Companion.ITEM_NEWS
import com.proxerme.app.helper.MaterialDrawerHelper.Companion.ITEM_SETTINGS
import com.proxerme.app.helper.PreferenceHelper
import com.proxerme.app.helper.StorageHelper
import com.proxerme.app.interfaces.OnActivityListener
import com.proxerme.app.manager.UserManager
import com.proxerme.app.module.CustomTabsModule
import com.proxerme.library.info.ProxerUrlHolder
import com.rubengees.introduction.IntroductionActivity.OPTION_RESULT
import com.rubengees.introduction.IntroductionBuilder
import com.rubengees.introduction.entity.Option
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class DashboardActivity : AppCompatActivity(), CustomTabsModule {

    companion object {
        private const val STATE_TITLE = "activity_dashboard_title"
        private const val EXTRA_DRAWER_ITEM = "extra_drawer_item"

        fun getSectionIntent(context: Context,
                             @MaterialDrawerHelper.Companion.DrawerItem itemId: Long): Intent {
            return Intent(context, DashboardActivity::class.java)
                    .apply { this.putExtra(EXTRA_DRAWER_ITEM, itemId) }
        }
    }

    override val customTabActivityHelper: CustomTabActivityHelper = CustomTabActivityHelper()

    private lateinit var drawer: MaterialDrawerHelper
    private var onActivityListener: OnActivityListener? = null

    private var title: String? = null

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val appbar: AppBarLayout by bindView(R.id.appbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(toolbar)

        drawer = MaterialDrawerHelper(this, toolbar, savedInstanceState,
                { id -> onDrawerItemClick(id) }, { id -> onAccountItemClick(id) })

        displayFirstPage(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        customTabActivityHelper.bindCustomTabsService(this)
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        customTabActivityHelper.unbindCustomTabsService(this)

        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(STATE_TITLE, title)
        drawer.saveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        if (savedInstanceState != null) {
            val lastFragment = supportFragmentManager.findFragmentById(R.id.contentContainer)

            when (lastFragment) {
                is OnActivityListener -> onActivityListener = lastFragment
                else -> onActivityListener = null
            }

            title = savedInstanceState.getString(STATE_TITLE)

            setTitle(title)
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.onBackPressed()
        } else {
            if (!(onActivityListener?.onBackPressed() ?: false)) {
                if (!drawer.onBackPressed()) {
                    super.onBackPressed()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IntroductionBuilder.INTRODUCTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                for (option in data.getParcelableArrayListExtra<Option>(OPTION_RESULT)) {
                    when (option.position) {
                        1 -> {
                            PreferenceHelper.setNewsNotificationsEnabled(this, option.isActivated)
                            PreferenceHelper.setChatNotificationsEnabled(this, option.isActivated)
                        }
                    }
                }
            }

            StorageHelper.firstStart = true
            drawer.select(ITEM_NEWS)
        }
    }

    private fun setFragment(fragment: Fragment, title: String) {
        this.title = title

        setTitle(title)
        setFragment(fragment)
    }

    private fun setFragment(fragment: Fragment): Unit {
        when (fragment) {
            is OnActivityListener -> onActivityListener = fragment
            else -> onActivityListener = null
        }

        appbar.setExpanded(true, true)
        supportFragmentManager.beginTransaction().replace(R.id.contentContainer, fragment)
                .commitNow()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginStateChanged(newState: UserManager.LoginState) {
        drawer.refreshHeader(this)
    }

    private fun displayFirstPage(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            if (StorageHelper.firstStart) {
                IntroductionHelper(this)
            } else {
                drawer.select(getItemToLoad())
            }
        }
    }

    @MaterialDrawerHelper.Companion.DrawerItem
    private fun getItemToLoad(): Long {
        return intent.getLongExtra(EXTRA_DRAWER_ITEM, MaterialDrawerHelper.ITEM_NEWS)
    }

    private fun onDrawerItemClick(id: Long): Boolean {
        when (id) {
            ITEM_NEWS -> {
                setFragment(NewsFragment.newInstance(), "News")

                return false
            }

            ITEM_CHAT -> {
                setFragment(ConferencesFragment.newInstance(), "Nachrichten")

                return false
            }

            ITEM_DONATE -> {
                showPage(ProxerUrlHolder.getDonateUrl("default"))

                return true
            }

            ITEM_SETTINGS -> {
                setFragment(SettingsFragment.newInstance(), "Einstellungen")

                return false
            }

            else -> return true
        }
    }

    private fun onAccountItemClick(id: Long): Boolean {
        when (id) {
            ACCOUNT_GUEST -> {
                LoginDialog.show(this)

                return false
            }

            ACCOUNT_LOGIN -> {
                LoginDialog.show(this)

                return false
            }

            ACCOUNT_LOGOUT -> {
                LogoutDialog.show(this)

                return false
            }

            ACCOUNT_USER -> {
                UserManager.user?.let {
                    UserActivity.navigateTo(this, it.id, it.username, it.imageId)
                }

                return false
            }

            else -> return false
        }
    }
}