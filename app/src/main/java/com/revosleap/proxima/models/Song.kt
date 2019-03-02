package com.revosleap.proxima.models

import java.io.Serializable

class Song : Serializable {
    var path: String? = ""
    var title: String? = ""
    var artist: String? = ""
    var albumName: String? = ""
    var artistId: Int = 0
    var songYear: Int = 0
    var duration: Long = 0L
    var trackNumber:Int=0
}
