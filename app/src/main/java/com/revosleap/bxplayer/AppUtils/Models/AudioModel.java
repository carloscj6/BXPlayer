package com.revosleap.bxplayer.AppUtils.Models;

import android.graphics.Bitmap;

import java.io.InputStream;

public class AudioModel {
    private String path;
    private String title;
    private String album;
    private String artist;
    private String albumItems;
    private InputStream cover;
    private int artistId;
    private int songYear;
    private int duration;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSongYear() {
        return songYear;
    }

    public void setSongYear(int songYear) {
        this.songYear = songYear;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public InputStream getCover() {
        return cover;
    }

    public void setCover(InputStream cover) {
        this.cover = cover;
    }

    public String getAlbumItems() {
        return albumItems;
    }

    public void setAlbumItems(String albumItems) {
        this.albumItems = albumItems;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
