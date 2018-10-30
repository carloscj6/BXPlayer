package com.revosleap.bxplayer.AppUtils.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public  class ImageCover extends AsyncTask<String,Void,Bitmap> {

    @Override
    protected Bitmap doInBackground(String... strings) {
        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        InputStream inputStream = null;
        retriever.setDataSource(strings[0]);

        if (retriever.getEmbeddedPicture() != null) {
            inputStream = new ByteArrayInputStream(retriever.getEmbeddedPicture());


        }
        Bitmap image=BitmapFactory.decodeStream(inputStream);
        retriever.release();
        return image;
    }
}
