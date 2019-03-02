package com.revosleap.proxima.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.revosleap.proxima.R
import com.revosleap.proxima.models.Album
import com.revosleap.proxima.ui.activities.PlayerActivity
import com.revosleap.proxima.utils.utils.Universal
import com.revosleap.simpleadapter.SimpleAdapter
import com.revosleap.simpleadapter.SimpleCallbacks
import kotlinx.android.synthetic.main.fragment_fragment_album.*
import kotlinx.android.synthetic.main.track.view.*
import java.lang.reflect.Type

class FragmentArtistAlbum : Fragment(), SimpleCallbacks {
    private var simpleAdapter: SimpleAdapter? = null
    private var albums = mutableListOf<Album>()
    private var playerActivity: PlayerActivity? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        playerActivity = activity as PlayerActivity

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? {
        getAlbums()
        simpleAdapter = SimpleAdapter(R.layout.track, this)
        return inflater.inflate(R.layout.fragment_fragment_album, container, false)
    }

    override fun bindView(view: View, item: Any, position: Int) {
        item as Album
        val title = view.textViewArtistTrack
        val subTitle = view.textViewTitleTrack
        title.text = item.title
        subTitle.text = item.year.toString()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewAlbum.apply {
            adapter = simpleAdapter
            layoutManager = LinearLayoutManager(playerActivity)
            hasFixedSize()
        }
        simpleAdapter?.addManyItems(albums.toMutableList())
        buttonListSortAlbums.visibility= View.GONE
    }

    private fun getAlbums() {
        val albumString = arguments?.getString(Universal.ALBUMS_BUNDLE)
        val gson = Gson()
        val albumType: Type = object : TypeToken<MutableList<Album>>() {}.type
        val artAlbums = gson.fromJson<MutableList<Album>>(albumString, albumType)
        if (artAlbums != null && artAlbums.size > 0) {
            albums = artAlbums
        }
    }

    private fun goToInfo(position: Int) {
        val gson = Gson()
        val gsonString = gson.toJson(albums[position].songs)
        val albumInfo = FragmentAlbumInfo()
        val bundle = Bundle()
        bundle.putString(Universal.ALBUM_BUNDLE, gsonString)
        albumInfo.arguments = bundle
        playerActivity?.supportFragmentManager!!
                .beginTransaction()
                .replace(R.id.frame_current, albumInfo, Universal.ALBUM_INFO_TAG)
                .addToBackStack(null)
                .commit()
        playerActivity?.replaceFragment()
    }

    override fun onViewClicked(view: View, item: Any, position: Int) {
        goToInfo(position)
    }

    override fun onViewLongClicked(it: View?, item: Any, position: Int) {

    }
}