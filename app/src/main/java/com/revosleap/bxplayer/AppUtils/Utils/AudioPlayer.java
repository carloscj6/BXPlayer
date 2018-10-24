package com.revosleap.bxplayer.AppUtils.Utils;

import android.media.MediaPlayer;
import android.util.Log;

public class AudioPlayer {
   public static MediaPlayer player= new MediaPlayer();

    public void play(String path){
            if (player.isPlaying()){
                player.stop();

            }
        try {
            player.setDataSource(path);
            player.prepare();
        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
            Log.e("Exception type: ",e.toString());
        }
        player.start();

    }
    public void pause(){

        player.pause();

    }
    public boolean isPLaying(){
        return player.isPlaying();
    }
    public void start(){
        player.start();
    }

}
