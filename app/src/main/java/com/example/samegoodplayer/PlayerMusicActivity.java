package com.example.samegoodplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerMusicActivity extends AppCompatActivity {

    Button playBt,prevBt,nextBt,ffBt,frBt, retryBt;
    TextView txtname, txtstart,txtduration;
    SeekBar seekTrack,volumeSeekbar;
    BarVisualizer visualizer;
    ImageView imageView;
    Thread updateSeekbar;
    AudioManager audioManager;
    boolean retrytrack=false;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer !=null)
        {
            visualizer.release();
        }
        super.onDestroy();
    }

    String sname;
    public static  final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList <File> mySongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_music);

        getSupportActionBar().setTitle("Now Playing!");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        playBt = findViewById(R.id.playButt);
        prevBt = findViewById(R.id.prevBt);
        nextBt = findViewById(R.id.nextBt);
        ffBt = findViewById(R.id.ffBt);
        frBt = findViewById(R.id.frBt);
        txtname = findViewById(R.id.txtsong);
        txtstart = findViewById(R.id.textCurrPos);
        txtduration=findViewById(R.id.textDurat);
        seekTrack = findViewById(R.id.seekbarTrack);
        visualizer = findViewById(R.id.blast);
        imageView = findViewById(R.id.imageView);
        retryBt = findViewById(R.id.retrybtn);

        volumeSeekbar = findViewById(R.id.volumeSeekbar);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        volumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = intent.getStringExtra("songName");
        position = bundle.getInt("position",0);
        txtname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname  = mySongs.get(position).getName();
        txtname.setText(sname);
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        updateSeekbar = new Thread()
        {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currPosition = 0;
                while (currPosition<totalDuration)
                {
                    try {
                        sleep(500);
                        currPosition = mediaPlayer.getCurrentPosition();
                        seekTrack.setProgress(currPosition);
                    } catch (InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekTrack.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekTrack.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_500), PorterDuff.Mode.MULTIPLY);
        seekTrack.getThumb().setColorFilter(getResources().getColor(R.color.purple_700),PorterDuff.Mode.SRC_IN);
        seekTrack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String timeEnd = createTime(mediaPlayer.getDuration());
        txtduration.setText(timeEnd);

        final Handler handler = new Handler();
        final int delay =1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currTime = createTime(mediaPlayer.getCurrentPosition());
                txtstart.setText(currTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (retrytrack)
                {
                    mediaPlayer.seekTo(0);
                    seekTrack.setProgress(0);
                    mediaPlayer.start();
                }
                else {
                nextBt.performClick();
                Toast.makeText(PlayerMusicActivity.this,"Next track!",Toast.LENGTH_SHORT).show();}

            }
        });

        int audiosessionID=mediaPlayer.getAudioSessionId();
        if (audiosessionID !=-1)
        {
            visualizer.setAudioSessionId(audiosessionID);
        }

    }


    public void playMusic(View view) {
        if (mediaPlayer.isPlaying())
        {
            playBt.setBackgroundResource(R.drawable.ic_play);
            mediaPlayer.pause();
        }
        else
            {
                playBt.setBackgroundResource(R.drawable.ic_pause);
                mediaPlayer.start();
            }
    }


    public String createTime (int duration)
    {
        String time="";
        int min = duration/1000/60;
        int sek = duration/1000 % 60;
        time+=min+":";
        if (sek<10)
        {
            time +="0";
        }
        time+=sek;
        return  time;
    }


    public void  startAnimation(View view)
    {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public void nextPlayingButt(View view) {
        mediaPlayer.stop();
        mediaPlayer.release();
        position= ((position+1)%mySongs.size());
        Uri u = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
        txtduration.setText(createTime(mediaPlayer.getDuration()));
        seekTrack.setMax(mediaPlayer.getDuration());
        sname = mySongs.get(position).getName();
        txtname.setText(sname);
        seekTrack.setProgress(0);
        mediaPlayer.start();
        playBt.setBackgroundResource(R.drawable.ic_pause);
        startAnimation(imageView);
        int audiosessionID=mediaPlayer.getAudioSessionId();
        if (audiosessionID !=-1)
        {
            visualizer.setAudioSessionId(audiosessionID);
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (retrytrack)
                {
                    mediaPlayer.seekTo(0);
                    seekTrack.setProgress(0);
                    mediaPlayer.start();
                }
                else {
                    nextBt.performClick();
                    Toast.makeText(PlayerMusicActivity.this,"Next track!",Toast.LENGTH_SHORT).show();}

            }
        });
    }

    public void prevPlayingButt(View view) {
        mediaPlayer.stop();
        mediaPlayer.release();
        position = ((position-1)<0)?(mySongs.size()-1):(position-1);
        Uri u = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
        txtduration.setText(createTime(mediaPlayer.getDuration()));
        sname = mySongs.get(position).getName();
        txtname.setText(sname);
        seekTrack.setProgress(0);
        seekTrack.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        playBt.setBackgroundResource(R.drawable.ic_pause);
        startAnimation(imageView);
        int audiosessionID=mediaPlayer.getAudioSessionId();
        if (audiosessionID !=-1)
        {
            visualizer.setAudioSessionId(audiosessionID);
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (retrytrack)
                {
                    mediaPlayer.seekTo(0);
                    seekTrack.setProgress(0);
                    mediaPlayer.start();
                }
                else {
                    nextBt.performClick();
                    Toast.makeText(PlayerMusicActivity.this,"Next track!",Toast.LENGTH_SHORT).show();}

            }
        });
    }

    public void playingffBtn(View view) {
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
        }
    }

    public void playingfrBtn(View view) {
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
        }
    }


    public void retryPlayingTrack(View view) {
        retrytrack = !retrytrack;
        if (retrytrack)
        {retryBt.setBackgroundResource(R.drawable.ic_disabled);
        Toast.makeText(PlayerMusicActivity.this,"Retry on!",Toast.LENGTH_SHORT).show();}
        else {retryBt.setBackgroundResource(R.drawable.ic_cached);
        Toast.makeText(PlayerMusicActivity.this,"Retry off!",Toast.LENGTH_SHORT).show();}
    }
}