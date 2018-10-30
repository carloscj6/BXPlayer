package com.revosleap.bxplayer.AppUtils.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AudioUtils {
    public static Bitmap cover (String path){
        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        InputStream inputStream = null;
        retriever.setDataSource(path);
        if (retriever.getEmbeddedPicture() != null) {
            inputStream = new ByteArrayInputStream(retriever.getEmbeddedPicture());


        }
        Bitmap image=BitmapFactory.decodeStream(inputStream);
        retriever.release();
        return image;
    }
    public static String formatDuration(final int duration) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

}
