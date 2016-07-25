package com.proxerme.app.adapter

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.proxerme.app.R
import com.proxerme.app.util.TimeUtil
import com.proxerme.library.connection.experimental.chat.entity.Conference
import com.proxerme.library.info.ProxerUrlHolder
import java.util.*

/**
 * An Adapter for [Conference]s, used in a RecyclerView.

 * @author Ruben Gees
 */
class ConferenceAdapter : RecyclerView.Adapter<ConferenceAdapter.ViewHolder> {

    private companion object {
        const val STATE_ITEMS = "adapter_conferences_state_items"
    }

    private val list = ArrayList<Conference>()
    var callback: OnConferenceInteractionListener? = null

    constructor(savedInstanceState: Bundle?) : super() {
        setHasStableIds(true)

        savedInstanceState?.let {
            list.addAll(it.getParcelableArrayList(STATE_ITEMS))
        }
    }

    override fun getItemCount(): Int = list.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_conference, parent, false))
    }

    override fun getItemId(position: Int): Long = list[position].id.toLong()

    fun addItems(newItems: Collection<Conference>) {
        newItems.forEach { newConference ->
            val index = list.indexOfFirst { oldConference -> oldConference.id == newConference.id }

            if (index >= 0) {
                list.removeAt(index)
            }

            list.add(newConference)
        }

        list.sortByDescending { it.time }

        notifyDataSetChanged()
    }

    fun clear() {
        list.clear()

        notifyDataSetChanged()
    }

    fun saveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(STATE_ITEMS, list)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val image: ImageView by bindView(R.id.image)
        private val container: ViewGroup by bindView(R.id.container)
        private val topic: TextView by bindView(R.id.topic)
        private val time: TextView by bindView(R.id.time)
        private val participants: TextView by bindView(R.id.participants)

        init {
            image.setOnClickListener {
                callback?.onConferenceImageClick(it, list[adapterPosition])
            }

            container.setOnClickListener {
                callback?.onConferenceClick(it, list[adapterPosition])
            }

            topic.setOnClickListener {
                callback?.onConferenceTopicClick(it, list[adapterPosition])
            }
        }

        fun bind(conference: Conference) {
            topic.text = conference.topic
            time.text = TimeUtil.convertToRelativeReadableTime(time.context,
                    conference.time)
            participants.text = participants.context.resources.getQuantityString(
                    R.plurals.item_conferences_participants, conference.participantAmount,
                    conference.participantAmount)

            if (conference.isRead) {
                topic.setCompoundDrawables(null, null, null, null)
            } else {
                topic.setCompoundDrawables(null, null,
                        IconicsDrawable(topic.context)
                                .icon(CommunityMaterial.Icon.cmd_message_alert)
                                .sizeDp(32)
                                .paddingDp(8)
                                .colorRes(R.color.colorPrimary), null)
            }

            if (conference.imageId.isBlank()) {
                val icon = IconicsDrawable(image.context)
                        .sizeDp(96)
                        .paddingDp(16)
                        .colorRes(R.color.colorPrimary)

                if (conference.isConference) {
                    icon.icon(CommunityMaterial.Icon.cmd_account_multiple)
                } else {
                    icon.icon(CommunityMaterial.Icon.cmd_account)
                }

                image.setImageDrawable(icon)
            } else {
                Glide.with(image.context)
                        .load(ProxerUrlHolder.getUserImageUrl(conference.imageId))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(image)
            }
        }
    }

    abstract class OnConferenceInteractionListener {
        open fun onConferenceClick(v: View, conference: Conference) {

        }

        open fun onConferenceImageClick(v: View, conference: Conference) {

        }

        open fun onConferenceTopicClick(v: View, conference: Conference) {

        }
    }
}