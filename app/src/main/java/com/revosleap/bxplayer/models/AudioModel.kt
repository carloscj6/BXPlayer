package com.revosleap.bxplayer.models

import java.io.Serializable

class AudioModel : Serializable {
    var path: String? = null
    var title: String? = null
    public var album: String? = null
    var artist: String? = null
    var albumName: String? = null
    var artistId: Int = 0
    var songYear: Int = 0
    var duration: Int = 0
}
