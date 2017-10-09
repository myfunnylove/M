package locidnet.com.marvarid.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import locidnet.com.marvarid.R;
import locidnet.com.marvarid.base.Base;
import locidnet.com.marvarid.base.BaseActivity;
import locidnet.com.marvarid.model.Audio;
import locidnet.com.marvarid.pattern.MControlObserver.MusicControlObserver;
import locidnet.com.marvarid.resources.utils.log;
import locidnet.com.marvarid.ui.activity.MainActivity;
import okhttp3.OkHttpClient;

/**
 *
 * Created by myfunnylove on 07.10.17.
 *
 */

public class PlayerService extends Service implements MusicControlObserver {

    private final int NOTIFICATION_ID = 404;

    public static String PLAYING_SONG_URL = "";
    public static int PLAY_STATUS = -1;
    public static final int PLAYING = 1;
    public static final int PAUSED = 0;


    public static ArrayList<Audio> songs;
    public static int songPosn = 0;
    private int pressPauseFromControl = 1;
    private RemoteViews  mContentViewSmall;
    private Audio currentAudio;

    public void setActivity(BaseActivity activity) {
        this.activity = activity;
    }

    private BaseActivity activity;
    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    private final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(
                      PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    );

    private MediaSessionCompat mediaSession;

    private AudioManager audioManager;

    private SimpleExoPlayer exoPlayer;
    private ExtractorsFactory extractorsFactory;
    private DataSource.Factory dataSourceFactory;


    @Override
    public void onCreate() {
        super.onCreate();
        if (MainActivity.MyPostOffset.getMusicSubject() != null) {
            MainActivity.MyPostOffset.getMusicSubject().subscribe(this);
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);

        Context appContext = getApplicationContext();

        Intent activityIntent = new Intent(appContext, MainActivity.class);
        mediaSession.setSessionActivity(PendingIntent.getActivity(appContext, 0, activityIntent, 0));

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, appContext, MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(appContext, 0, mediaButtonIntent, 0));

        exoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());
        exoPlayer.addListener(exoPlayerListener);

        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        log.INSTANCE.d("repeat mode" + exoPlayer.getRepeatMode());

        DataSource.Factory httpDataSourceFactory = new OkHttpDataSourceFactory(new OkHttpClient(), Util.getUserAgent(this, getString(R.string.app_name)), null);

        String folderName ="marvarid";
        File f = new File(Environment.getExternalStorageDirectory(),folderName);
        if (!f.exists()) f.mkdirs();

        File inner = new File(Environment.getExternalStorageDirectory() +"/" + folderName,"music");
        if (!inner.exists()) inner.mkdirs();

        Cache cache = new SimpleCache(inner, new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100)); // 100 Mb max
        this.dataSourceFactory = new CacheDataSourceFactory(cache, httpDataSourceFactory, CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
        this.extractorsFactory = new DefaultExtractorsFactory();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }


    public int getPressPauseFromControl() {
        return pressPauseFromControl;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pressPauseFromControl = 1;

        mediaSession.release();
        exoPlayer.release();
        songs = null;
    }
    int currentState = PlaybackStateCompat.STATE_STOPPED;

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        private Uri currentUri;

        @Override
        public void onPlay() {
            pressPauseFromControl = -1;

            log.INSTANCE.d("PLAY "+exoPlayer.getPlayWhenReady());
            if (!exoPlayer.getPlayWhenReady()) {
                if (songs.size() > 0){
                    PLAY_STATUS = PLAYING;

                    startService(new Intent(getApplicationContext(), PlayerService.class));
                    Audio playSong = songs.get(songPosn);

                    PlayerService.PLAYING_SONG_URL = playSong.getMiddlePath();

                    prepareToPlay(Uri.parse("http://api.maydon.net/new/"+playSong.getMiddlePath()));

                    int audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                        return;

                    mediaSession.setActive(true); // Сразу после получения фокуса
                    mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                    currentState = PlaybackStateCompat.STATE_PLAYING;
                    updateMetadataFromTrack(playSong);

                    refreshNotificationAndForegroundStatus(currentState);

                    registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

                    exoPlayer.setPlayWhenReady(true);

                    if (MainActivity.MyPostOffset.getMusicSubject() != null) {
                        MainActivity.MyPostOffset.getMusicSubject().playMeause("");
                    }
                }else{
                    onStop();
                }
            }else{
                if (currentState == PlaybackStateCompat.STATE_PLAYING){
                    log.INSTANCE.d("is equal" +songs.get(songPosn).getMiddlePath()+" "+PLAYING_SONG_URL);
                    if (songs.get(songPosn).getMiddlePath().equals(PLAYING_SONG_URL))
                        onPause();
                    else{
                        exoPlayer.setPlayWhenReady(false);
                        onPlay();
                    }
                }

            }
        }

        @Override
        public void onPause() {
            pressPauseFromControl = 1;

            if (exoPlayer.getPlayWhenReady()) {
                exoPlayer.setPlayWhenReady(false);
                PLAY_STATUS = PAUSED;

                mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                currentState = PlaybackStateCompat.STATE_PAUSED;

                refreshNotificationAndForegroundStatus(currentState);
                MainActivity.MyPostOffset.getMusicSubject().playMeause("");

                unregisterReceiver(becomingNoisyReceiver);
            }
        }

        @Override
        public void onStop() {
            pressPauseFromControl = 1;

            exoPlayer.setPlayWhenReady(false);
            PLAY_STATUS = PAUSED;

            audioManager.abandonAudioFocus(audioFocusChangeListener);

            mediaSession.setActive(false);

            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            currentState = PlaybackStateCompat.STATE_STOPPED;

            refreshNotificationAndForegroundStatus(currentState);
            MainActivity.MyPostOffset.getMusicSubject().playMeause("");

            stopSelf();
        }

        @Override
        public void onSkipToNext() {
            pressPauseFromControl = -1;
            log.INSTANCE.d("onSkipToNext");
            songPosn++;
            if (songPosn >= songs.size()) songPosn = 0;

            PLAY_STATUS = PLAYING;

            Audio playSong = songs.get(songPosn);

            PlayerService.PLAYING_SONG_URL = playSong.getMiddlePath();

            updateMetadataFromTrack(playSong);

            refreshNotificationAndForegroundStatus(currentState);
            MainActivity.MyPostOffset.getMusicSubject().playMeause("");

            prepareToPlay(Uri.parse("http://api.maydon.net/new/"+playSong.getMiddlePath()));
        }







        @Override
        public void onSkipToPrevious() {


            pressPauseFromControl = -1;

            songPosn--;
            if (songPosn < 0) songPosn = songs.size() - 1;

            PLAY_STATUS = PLAYING;
            Audio playSong = songs.get(songPosn);

            PlayerService.PLAYING_SONG_URL = playSong.getMiddlePath();

            updateMetadataFromTrack(playSong);

            refreshNotificationAndForegroundStatus(currentState);
            MainActivity.MyPostOffset.getMusicSubject().playMeause("");

            prepareToPlay(Uri.parse("http://api.maydon.net/new/"+playSong.getMiddlePath()));
        }



        private void prepareToPlay(Uri uri) {
            if (!uri.equals(currentUri)) {
                currentUri = uri;
                ExtractorMediaSource mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
                exoPlayer.prepare(mediaSource);
            }
        }

        private void updateMetadataFromTrack(Audio track) {
            currentAudio = track;
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.drawable.bg));
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, (track.getTitle().isEmpty()) ? Base.Companion.getGet().getResources().getString(R.string.unknown) : track.getTitle());
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM,(track.getArtist().isEmpty()) ? Base.Companion.getGet().getResources().getString(R.string.unknown) : track.getArtist());
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, (track.getArtist().isEmpty()) ? Base.Companion.getGet().getResources().getString(R.string.unknown) : track.getArtist());
//            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DURATION,track.getDuration());
            mediaSession.setMetadata(metadataBuilder.build());
        }
    };

    public Audio getCurrentAudio() {
        return currentAudio;
    }




    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (pressPauseFromControl == -1)
                    mediaSessionCallback.onPlay();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (pressPauseFromControl == -1)
                    mediaSessionCallback.onPause();
                    break;
                default:
                    if (pressPauseFromControl == -1)
                    mediaSessionCallback.onPause();

                    break;
            }
        }
    };

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Disconnecting headphones - stop playback
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mediaSessionCallback.onPause();
            }
        }
    };



    private Player.EventListener exoPlayerListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            log.INSTANCE.d("onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {


        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            log.INSTANCE.d("onLoadingChanged");

        }



        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            log.INSTANCE.d("onPlayerStateChanged + "+playWhenReady + " "+playbackState);
            if (playWhenReady && playbackState == Player.STATE_ENDED) {
                mediaSessionCallback.onSkipToNext();
            }else if (playWhenReady && playbackState == Player.STATE_READY){
                activity.hideLoading();
            }
        }



        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
        }

        @Override
        public void onPositionDiscontinuity() {
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            log.INSTANCE.d("onPlaybackParametersChanged");

        }


    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceBinder();
    }

    @Override
    public void playPause(@NotNull String id) {
        refreshNotificationAndForegroundStatus(currentState);

    }


    public void setPressPauseFromControl(int pressPauseFromControl) {
        this.pressPauseFromControl = pressPauseFromControl;
    }

    public class PlayerServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }
        public PlayerService getService() {
            return PlayerService.this;
        }

    }

    public int getCurrentState() {
        return currentState;
    }

    private void refreshNotificationAndForegroundStatus(int playbackState) {
        switch (playbackState) {
            case PlaybackStateCompat.STATE_PLAYING: {
                startForeground(NOTIFICATION_ID, getNotification(playbackState));
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED: {
                NotificationManagerCompat.from(PlayerService.this).notify(NOTIFICATION_ID, getNotification(playbackState));
                stopForeground(false);
                break;
            }
            default: {
                stopForeground(true);
                break;
            }
        }
    }

    private Notification getNotification(int playbackState) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        Notification not = new android.support.v4.app.NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setCustomContentView(getSmallContentView(playbackState))
                .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)

                .build();

        return not;
    }

    private RemoteViews getSmallContentView(int state) {
        if (mContentViewSmall == null) {
            mContentViewSmall = new RemoteViews(getPackageName(), R.layout.res_notification_view);
            setUpRemoteView(mContentViewSmall,state);
        }
        updateRemoteViews(mContentViewSmall,state);
        return mContentViewSmall;
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    private void setUpRemoteView(RemoteViews remoteView, int state) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            remoteView.setImageViewResource(R.id.close, R.drawable.notif_close);
            remoteView.setImageViewResource(R.id.prev, R.drawable.notif_prev);
            remoteView.setImageViewResource(R.id.next, R.drawable.notif_next);
            remoteView.setImageViewResource(R.id.image_view_play_toggle, (state == PlaybackStateCompat.STATE_PLAYING)
                    ? R.drawable.notif_pause : R.drawable.notif_play);
        } else {
            remoteView.setImageViewResource(R.id.close, R.drawable.png_close);
            remoteView.setImageViewResource(R.id.prev, R.drawable.png_prev);
            remoteView.setImageViewResource(R.id.next, R.drawable.png_next);
            remoteView.setImageViewResource(R.id.image_view_play_toggle, (state == PlaybackStateCompat.STATE_PLAYING)
                    ? R.drawable.png_pause : R.drawable.png_play);
        }
        remoteView.setOnClickPendingIntent(R.id.btn_close, getPendingIntent(PlaybackStateCompat.ACTION_STOP));
        remoteView.setOnClickPendingIntent(R.id.btn_prev, getPendingIntent(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
        remoteView.setOnClickPendingIntent(R.id.btn_next, getPendingIntent(PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        remoteView.setOnClickPendingIntent(R.id.btn_play, getPendingIntent(PlaybackStateCompat.ACTION_PLAY_PAUSE));
    }

    private void updateRemoteViews(RemoteViews remoteView,int state) {
        Audio playSong = songs.get(songPosn);
        if (playSong != null) {
            remoteView.setTextColor(R.id.title, Base.Companion.getGet().getResources().getColor(R.color.headerTextColor));
            remoteView.setTextColor(R.id.artist, Base.Companion.getGet().getResources().getColor(R.color.normalTextColor));
            remoteView.setTextViewText(R.id.title, (playSong.getTitle().isEmpty()) ? Base.Companion.getGet().getResources().getString(R.string.unknown) : playSong.getTitle());
            remoteView.setTextViewText(R.id.artist, (playSong.getArtist().isEmpty()) ? Base.Companion.getGet().getResources().getString(R.string.unknown) : playSong.getArtist());
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            remoteView.setImageViewResource(R.id.image_view_play_toggle, (state == PlaybackStateCompat.STATE_PLAYING)
                    ? R.drawable.notif_pause : R.drawable.notif_play);
        } else {
            remoteView.setImageViewResource(R.id.image_view_play_toggle, (state == PlaybackStateCompat.STATE_PLAYING)
                    ? R.drawable.png_pause : R.drawable.png_play);
        }
//        Bitmap album = AlbumUtils.parseAlbum(getPlayingSong());

        remoteView.setImageViewResource(R.id.album, R.mipmap.ic_launcher);
    }

    private PendingIntent getPendingIntent(long state) {
        return  MediaButtonReceiver.buildMediaButtonPendingIntent(this, state);
    }
}
