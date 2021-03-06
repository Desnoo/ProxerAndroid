package com.proxerme.app.adapter.chat

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import cn.nekocode.badge.BadgeDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.proxerme.app.R
import com.proxerme.app.adapter.framework.PagingAdapter
import com.proxerme.app.entitiy.LocalConference
import com.proxerme.app.util.TimeUtil
import com.proxerme.library.connection.messenger.entity.Conference
import com.proxerme.library.info.ProxerUrlHolder

/**
 * An Adapter for [Conference]s, used in a RecyclerView.

 * @author Ruben Gees
 */
class ConferenceAdapter(savedInstanceState: Bundle? = null) :
        PagingAdapter<LocalConference, ConferenceAdapter.ConferenceAdapterCallback>() {

    private companion object {
        private const val ITEMS_STATE = "adapter_conference_state_items"
    }

    init {
        savedInstanceState?.let {
            list.addAll(it.getParcelableArrayList(ITEMS_STATE))
        }

        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_conference, parent, false))
    }

    override fun getItemId(position: Int): Long = list[position].localId

    override fun saveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(ITEMS_STATE, list)
    }

    inner class ViewHolder(itemView: View) :
            PagingViewHolder<LocalConference, ConferenceAdapterCallback>(itemView) {

        override val adapterList: List<LocalConference>
            get() = list
        override val adapterCallback: ConferenceAdapterCallback?
            get() = callback

        private val image: ImageView by bindView(R.id.image)
        private val topic: TextView by bindView(R.id.topic)
        private val newMessages: ImageView by bindView(R.id.newMessages)
        private val time: TextView by bindView(R.id.time)
        private val participants: TextView by bindView(R.id.participants)

        override fun bind(item: LocalConference) {
            topic.text = item.topic
            time.text = TimeUtil.convertToRelativeReadableTime(time.context,
                    item.time)
            participants.text = participants.context.resources.getQuantityString(
                    R.plurals.item_conferences_participants, item.participantAmount,
                    item.participantAmount)

            if (item.isReadLocal) {
                newMessages.setImageDrawable(null)
                newMessages.visibility = View.GONE
            } else {
                newMessages.setImageDrawable(BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_NUMBER)
                        .number(item.unreadMessageAmount)
                        .badgeColor(ContextCompat.getColor(topic.context, R.color.accent))
                        .build())
                newMessages.visibility = View.VISIBLE
            }

            if (item.imageId.isBlank()) {
                val icon = IconicsDrawable(image.context)
                        .sizeDp(96)
                        .paddingDp(16)
                        .colorRes(R.color.colorAccent)

                if (item.isGroup) {
                    icon.icon(CommunityMaterial.Icon.cmd_account_multiple)
                } else {
                    icon.icon(CommunityMaterial.Icon.cmd_account)
                }

                image.setImageDrawable(icon)
            } else {
                Glide.with(image.context)
                        .load(ProxerUrlHolder.getUserImageUrl(item.imageId).toString())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(image)
            }
        }
    }

    abstract class ConferenceAdapterCallback : PagingAdapterCallback<LocalConference>()
}
