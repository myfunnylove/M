package locidnet.com.marvarid.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * Created by myfunnylove on 12.10.17.
 */
@Entity(tableName = "audio")
public class Audio {
    @PrimaryKey
    @SerializedName("audio_id")
    private String audioId;
    @SerializedName("post_id")
    private String postId;
    @SerializedName("duration")
    private String  duration;
    @SerializedName("size")
    private String  size;
    @SerializedName("middle_path")
    private String middlePath;
    @SerializedName("bitrate")
    private String bitrate;
    @SerializedName("title")
    private String title;
    @SerializedName("artist")
    private String artist;
    @SerializedName("isFeatured")
    private int isFeatured =  -1;

    public Audio(String audioId, String postId, String duration, String size, String middlePath, String bitrate, String title, String artist, int isFeatured) {
        this.audioId = audioId;
        this.postId = postId;
        this.duration = duration;
        this.size = size;
        this.middlePath = middlePath;
        this.bitrate = bitrate;
        this.title = title;
        this.artist = artist;
        this.isFeatured = isFeatured;
    }

    public String getAudioId() {
        return audioId;
    }

    public void setAudioId(String audioId) {
        this.audioId = audioId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMiddlePath() {
        return middlePath;
    }

    public void setMiddlePath(String middlePath) {
        this.middlePath = middlePath;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(int isFeatured) {
        this.isFeatured = isFeatured;
    }
}
