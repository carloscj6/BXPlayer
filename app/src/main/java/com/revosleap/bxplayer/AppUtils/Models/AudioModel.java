package com.revosleap.bxplayer.AppUtils.Models;

public class AudioModel {
    String path;
    String title;
    String album;
    String artist;
    String albumItems;

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
