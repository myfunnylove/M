package locidnet.com.marvarid.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.Log;
import android.widget.RemoteViews;

import locidnet.com.marvarid.BuildConfig;
import locidnet.com.marvarid.R;
import locidnet.com.marvarid.base.Base;
import locidnet.com.marvarid.model.Audio;
import locidnet.com.marvarid.pattern.MControlObserver.MusicControlObserver;
import locidnet.com.marvarid.resources.utils.log;
import locidnet.com.marvarid.ui.activity.MainActivity;

import java.util.ArrayList;
import java.util.Random;

/*
 * This is demo code to accompany the Mobiletuts+ series:
 * Android SDK: Creating a Music Player
 *
 * Sue Smith - February 2014
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MusicControlObserver {

    public static int CONTROL_PRESSED = -1;
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Audio> songs;
    //current position
    private int songPosn;
    //binder
    private final IBinder musicBind = new MusicBinder();
    //title of current song
    private String songTitle="";
    //notification id
    private static final int NOTIFY_ID = 1;
    //shuffle flag and random
    private boolean shuffle=false;
    private Random rand;
    public static String PLAYING_SONG_URL = "";
    public static int PLAY_STATUS = -1;
    public static final int PLAYING = 1;
    public static final int PAUSED = 0;

//    public static final String TAG = "MEDIAPLAYER_CONTROLLER";

    public static final String ACTION_PLAY_TOGGLE = "locidnet.com.marvarid.ACTION.PLAY_TOGGLE";
    public static final String ACTION_PLAY_LAST = "locidnet.com.marvarid.ACTION.PLAY_LAST";
    public static final String ACTION_PLAY_NEXT = "locidnet.com.marvarid.ACTION.PLAY_NEXT";
    public static final String ACTION_STOP_SERVICE = "locidnet.com.marvarid.ACTION.STOP_SERVICE";
    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        songPosn=0;
        //random
        rand=new Random();
        //create player
        player = new MediaPlayer();
        //initialize
        initMusicPlayer();
        MainActivity.MyPostOffset.getMusicSubject().subscribe(this);

    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    //pass song list
    public void setList(ArrayList<Audio> theSongs){
        songs=theSongs;
    }
    public ArrayList<Audio> getSongs(){
        return songs;
    }

    @Override
    public void playPause(String id) {
        log.INSTANCE.d("PATTERN OBSERVER CALLED MY PROFILE MUSICSERVICE");

        createAndShowNotification();
    }

    //binder
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    //activity will bind to service
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    //play a song
    public void playSong(){
        //play
        player.reset();
        //get song
        Audio playSong = songs.get(songPosn);
        //get title
        songTitle=playSong.getTitle();
        //get id
        //set uri

        //set the data source
        try{
            player.setDataSource("http://api.maydon.net/new/"+playSong.getMiddlePath());
            MusicService.PLAYING_SONG_URL = playSong.getMiddlePath();
            PLAY_STATUS = PLAYING;
            log.INSTANCE.d("PLAYIN SONG -> "+MusicService.PLAYING_SONG_URL);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                player.start();
                createAndShowNotification();
                Intent intent = new Intent(ACTION_PLAY_TOGGLE);
                LocalBroadcastManager.getInstance(MusicService.this).sendBroadcast(intent);
            }
        });
    }

    //set the song
    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //check if playback has reached the end of a track
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        //notification

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null){
            String action = intent.getAction();
            if (ACTION_PLAY_TOGGLE.equals(action)) {
                CONTROL_PRESSED = 1;
                if (isPng()) {
                    pausePlayer();
                } else {
                    go();
                }
            } else if (ACTION_PLAY_NEXT.equals(action)) {
                CONTROL_PRESSED = 1;

                playNext();
            } else if (ACTION_PLAY_LAST.equals(action)) {
                CONTROL_PRESSED = 1;

                playPrev();
            } else if (ACTION_STOP_SERVICE.equals(action)) {
                CONTROL_PRESSED = 1;

                if (isPng()) {
                    pausePlayer();
                }
                stopForeground(true);
            }
        }

        return START_STICKY;
    }

    private void createAndShowNotification(){


       /*
       *
       *  WHEN NOTIIFACTION CLICK
       *
       * */
       PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0);

        Notification not = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setCustomContentView(getSmallContentView())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .build();

        startForeground(NOTIFY_ID, not);
    }


    /*
    *
    *  NOTIFICATION VIEWS
    *
    *
    * */
    private RemoteViews mContentViewBig, mContentViewSmall;
    private RemoteViews getSmallContentView() {
        if (mContentViewSmall == null) {
            mContentViewSmall = new RemoteViews(getPackageName(), R.layout.res_notification_view);
            setUpRemoteView(mContentViewSmall);
        }
        updateRemoteViews(mContentViewSmall);
        return mContentViewSmall;
    }


    private void setUpRemoteView(RemoteViews remoteView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            remoteView.setImageViewResource(R.id.close, R.drawable.notif_close);
            remoteView.setImageViewResource(R.id.prev, R.drawable.notif_prev);
            remoteView.setImageViewResource(R.id.next, R.drawable.notif_next);
            remoteView.setImageViewResource(R.id.image_view_play_toggle, isPng()
                    ? R.drawable.notif_pause : R.drawable.notif_play);
        }else{
            remoteView.setImageViewResource(R.id.close, R.drawable.png_close);
            remoteView.setImageViewResource(R.id.prev, R.drawable.png_prev);
            remoteView.setImageViewResource(R.id.next, R.drawable.png_next);
            remoteView.setImageViewResource(R.id.image_view_play_toggle, isPng()
                    ? R.drawable.png_pause : R.drawable.png_play);
        }
        remoteView.setOnClickPendingIntent(R.id.btn_close, getPendingIntent(ACTION_STOP_SERVICE));
        remoteView.setOnClickPendingIntent(R.id.btn_prev, getPendingIntent(ACTION_PLAY_LAST));
        remoteView.setOnClickPendingIntent(R.id.btn_next, getPendingIntent(ACTION_PLAY_NEXT));
        remoteView.setOnClickPendingIntent(R.id.btn_play, getPendingIntent(ACTION_PLAY_TOGGLE));
    }

    private void updateRemoteViews(RemoteViews remoteView) {
        log.INSTANCE.d("notification update " + isPng());
        Audio playSong = songs.get(songPosn);
        if (playSong != null) {
            remoteView.setTextColor(R.id.title, Base.Companion.getGet().getResources().getColor(R.color.headerTextColor));
            remoteView.setTextColor(R.id.artist, Base.Companion.getGet().getResources().getColor(R.color.normalTextColor));
            remoteView.setTextViewText(R.id.title, (playSong.getTitle().isEmpty()) ? Base.Companion.getGet().getResources().getString(R.string.unknown) : playSong.getTitle());
            remoteView.setTextViewText(R.id.artist,(playSong.getArtist().isEmpty()) ? Base.Companion.getGet().getResources().getString(R.string.unknown) : playSong.getArtist());
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            remoteView.setImageViewResource(R.id.image_view_play_toggle, isPng()
                    ? R.drawable.notif_pause : R.drawable.notif_play);
        }else{
            remoteView.setImageViewResource(R.id.image_view_play_toggle, isPng()
                    ? R.drawable.png_pause : R.drawable.png_play);
        }
//        Bitmap album = AlbumUtils.parseAlbum(getPlayingSong());

            remoteView.setImageViewResource(R.id.album, R.mipmap.ic_launcher);
    }

    private PendingIntent getPendingIntent(String action) {
        return PendingIntent.getService(this, 0, new Intent(action), 0);
    }
    //playback methods
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void  pausePlayer(){
        log.INSTANCE.d("PLAYING SONG PAUSED");

        player.pause();
        //player.release();
        PLAY_STATUS = PAUSED;
        MainActivity.MyPostOffset.getMusicSubject().playMeause("");
//        Intent intent = new Intent(ACTION_PLAY_TOGGLE);
//        LocalBroadcastManager.getInstance(MusicService.this).sendBroadcast(intent);
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        log.INSTANCE.d("PLAYING SONG GO");

        player.start();
//        Intent intent = new Intent(ACTION_PLAY_TOGGLE);
//        LocalBroadcastManager.getInstance(MusicService.this).sendBroadcast(intent);
        PLAY_STATUS = PLAYING;
        MainActivity.MyPostOffset.getMusicSubject().playMeause("");
    }

    //skip to previous track
    public void playPrev(){
        songPosn--;
        if(songPosn<0) songPosn=songs.size()-1;
        playSong();
//        Intent intent = new Intent(ACTION_PLAY_LAST);
//        LocalBroadcastManager.getInstance(MusicService.this).sendBroadcast(intent);
        MainActivity.MyPostOffset.getMusicSubject().playMeause("");

    }

    //skip to next
    public void playNext(){
        if(shuffle){
            int newSong = songPosn;

            while(newSong==songPosn){
                newSong=rand.nextInt(songs.size());
            }
            songPosn=newSong;
        }
        else{

            songPosn++;
            if(songPosn>=songs.size()) songPosn=0;
        }

        playSong();
        MainActivity.MyPostOffset.getMusicSubject().playMeause("");

//        Intent intent = new Intent(ACTION_PLAY_NEXT);
//        LocalBroadcastManager.getInstance(MusicService.this).sendBroadcast(intent);

    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    //toggle shuffle
    public void setShuffle(){
        shuffle = !shuffle;
    }


}