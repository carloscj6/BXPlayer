package com.revosleap.bxplayer.ui.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.revosleap.bxplayer.R
import com.revosleap.bxplayer.models.Artist
import com.revosleap.bxplayer.utils.utils.ArtistProvider
import com.revosleap.simpleadapter.SimpleAdapter
import com.revosleap.simpleadapter.SimpleCallbacks
import kotlinx.android.synthetic.main.artist_item.view.*
import kotlinx.android.synthetic.main.fragment_artists.*
import org.jetbrains.anko.AnkoLogger


class FragmentArtists : Fragment(), SimpleCallbacks, AnkoLogger {

    private var simpleAdapter: SimpleAdapter? = null
    private var artistList = mutableListOf<Artist>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        simpleAdapter = SimpleAdapter(R.layout.artist_item, this)
        artistList = ArtistProvider.getAllArtists(activity!!)
        return inflater.inflate(R.layout.fragment_artists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        simpleAdapter?.clearItems()
        simpleAdapter?.addManyItems(artistList.toMutableList())
        artistrecycler.apply {
            adapter = simpleAdapter
            layoutManager = LinearLayoutManager(activity)
            hasFixedSize()
        }

    }

    override fun bindView(view: View, item: Any, position: Int) {
        item as Artist
        val artistName = view.textViewArtistName
        val artistInfo = view.textViewArtistInfo
        artistName.text = item.name
        var album="Album"
        var track="Track"
        if (item.albums.size>1){
            album="Albums"
        }
        if (item.songCount>1){
            track="Tracks"
        }
        val info = item.songCount.toString() + " $track | " + item.albums.size + " $album"
        artistInfo.text = info
    }

    override fun onViewClicked(view: View, item: Any, position: Int) {

    }

    override fun onViewLongClicked(it: View?, item: Any, position: Int) {

    }

}
