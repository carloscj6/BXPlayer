package com.revosleap.bxplayer.AppUtils.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.revosleap.bxplayer.AppUtils.Models.AudioModel;
import com.revosleap.bxplayer.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GetAudio {
    public List<AudioModel> geAllAudio(Context context){

        List<AudioModel> temAudioList= new ArrayList<>();
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String order= MediaStore.Audio.Media.TITLE;

        String[] projection={MediaStore.Audio.AudioColumns.DATA,MediaStore.Audio.AudioColumns.ALBUM,
        MediaStore.Audio.ArtistColumns.ARTIST,MediaStore.Audio.AudioColumns.TITLE,MediaStore.Audio.AudioColumns.DURATION};
        Cursor cursor= context.getContentResolver().query(uri,projection,null,
                null,order);
        if (cursor!=null){
           Log.v("Count","Songs "+cursor.getCount());
            while (cursor.moveToNext()){

                AudioModel model= new AudioModel();
                String path= cursor.getString(0);
                String album= cursor.getString(1);
                String artist= cursor.getString(2);
                String name= cursor.getString(3);
                String duration=cursor.getString(4);

                model.setTitle(name);
                model.setAlbum(album);
                model.setArtist(artist);
                model.setPath(path);
                model.setDuration(Integer.parseInt(duration));


                temAudioList.add(model);
            }
            cursor.close();
        }
        else Toast.makeText(context, "Null", Toast.LENGTH_SHORT).show();
        return temAudioList;

    }
}
