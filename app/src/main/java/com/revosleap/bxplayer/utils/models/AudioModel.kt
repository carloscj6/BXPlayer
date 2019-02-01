package com.revosleap.bxplayer.utils.models

import java.io.InputStream

class AudioModel {
    var path: String? = null
    var title: String? = null
    var album: String? = null
    var artist: String? = null
    var albumItems: String? = null
    var cover: InputStream? = null
    var artistId: Int = 0
    var songYear: Int = 0
    var duration: Int = 0
}
