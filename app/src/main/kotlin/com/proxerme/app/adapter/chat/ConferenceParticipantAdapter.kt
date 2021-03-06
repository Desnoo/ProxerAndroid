package com.proxerme.app.adapter.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.TouchableMovementMethod
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.proxerme.app.R
import com.proxerme.app.adapter.chat.ConferenceParticipantAdapter.ConferenceParticipantAdapterCallback
import com.proxerme.app.adapter.framework.PagingAdapter
import com.proxerme.app.util.Utils
import com.proxerme.library.connection.messenger.entity.ConferenceInfoUser
import com.proxerme.library.info.ProxerUrlHolder

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class ConferenceParticipantAdapter(val savedInstanceState: Bundle?) :
        PagingAdapter<ConferenceInfoUser, ConferenceParticipantAdapterCallback>() {

    private companion object {
        private const val ITEMS_STATE = "adapter_conference_participant_state_items"
        private const val LEADER_STATE = "adapter_conference_participant_state_leader"
    }

    var leader: String? = null

    init {
        savedInstanceState?.let {
            list.addAll(it.getParcelableArrayList(ITEMS_STATE))
            leader = it.getString(LEADER_STATE)
        }

        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            PagingViewHolder<ConferenceInfoUser, ConferenceParticipantAdapterCallback> {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_conference_participant, parent, false))
    }

    override fun saveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(ITEMS_STATE, list)
        outState.putString(LEADER_STATE, leader)
    }

    inner class ViewHolder(itemView: View) :
            PagingViewHolder<ConferenceInfoUser, ConferenceParticipantAdapterCallback>(itemView) {

        override val adapterList: List<ConferenceInfoUser>
            get() = list
        override val adapterCallback: ConferenceParticipantAdapterCallback?
            get() = callback

        private val image: ImageView by bindView(R.id.image)
        private val username: TextView by bindView(R.id.username)
        private val status: TextView by bindView(R.id.status)

        init {
            status.movementMethod = TouchableMovementMethod.getInstance()
        }

        override fun bind(item: ConferenceInfoUser) {
            username.text = item.username

            if (item.id == leader) {
                username.setCompoundDrawables(null, null, IconicsDrawable(username.context)
                        .icon(CommunityMaterial.Icon.cmd_star)
                        .sizeDp(32)
                        .paddingDp(8)
                        .colorRes(R.color.colorAccent), null)
            } else {

                username.setCompoundDrawables(null, null, null, null)
            }

            if (item.status.isBlank()) {
                status.visibility = View.GONE
            } else {
                status.visibility = View.VISIBLE
                status.text = Utils.buildClickableText(status.context, item.status,
                        onWebClickListener = Link.OnClickListener { link ->
                            Utils.viewLink(status.context, link)
                        })
            }

            if (item.imageId.isBlank()) {
                image.setImageDrawable(IconicsDrawable(image.context)
                        .icon(CommunityMaterial.Icon.cmd_account)
                        .sizeDp(96)
                        .paddingDp(16)
                        .colorRes(R.color.colorAccent))
            } else {
                Glide.with(image.context)
                        .load(ProxerUrlHolder.getUserImageUrl(item.imageId).toString())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(image)
            }
        }
    }

    abstract class ConferenceParticipantAdapterCallback :
            PagingAdapterCallback<ConferenceInfoUser>()
}