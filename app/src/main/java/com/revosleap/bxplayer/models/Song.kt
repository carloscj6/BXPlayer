package com.revosleap.bxplayer.models

import java.io.Serializable

class Song : Serializable {
    var path: String? = ""
    var title: String? = ""
    var artist: String? = ""
    var albumName: String? = ""
    var artistId: Int = 0
    var songYear: Int = 0
    var duration: Int = 0
    var trackNumber:Int=0
}
