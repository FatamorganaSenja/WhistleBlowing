package senja.fatamorgana.whistleblowing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

import bg.devlabs.fullscreenvideoview.FullscreenVideoView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import senja.fatamorgana.whistleblowing.Config.Link;
import senja.fatamorgana.whistleblowing.Config.SharedPrefManager;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class FullVideoActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private VideoView mVV;
    private String videoPath, videoid, videoName;
    SharedPrefManager SP_Help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullvideo);

        SP_Help = new SharedPrefManager(this);

        videoid = SP_Help.getSpQuestionTake();
        videoName = SP_Help.getSpFilename();

        Log.e("LOKASI V",videoName);
        if (videoid.length() == 0){
            Intent a = new Intent(FullVideoActivity.this, MainActivity.class);
            startActivity(a);
            finish();
        }else {
//            String Lokasi = Link.AppFolder;
//            File sdcard = getExternalStoragePublicDirectory(Lokasi);
//            File file = new File(sdcard, SP_Help.getSpFilename());
//            videoPath = file.getPath();
            videoPath = "http://fatamorgana-senja.000webhostapp.com/coba/video/Main.mp4";
            Log.e("LOKASI", videoPath);
        }

        mVV = (VideoView)findViewById(R.id.myvideoview);
        mVV.setOnCompletionListener(this);

        final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Buffering...");
        pDialog.setCancelable(false);
        pDialog.show();

        try {
            // Start the MediaController
//            MediaController mediacontroller = new MediaController(this);
//            mediacontroller.setAnchorView(mVV);

            Uri videoUri = Uri.parse(videoPath);
//            mVV.setMediaController(mediacontroller);
            mVV.setVideoURI(videoUri);

        } catch (Exception e) {

            e.printStackTrace();
        }
//        mVV.requestFocus();
        mVV.setOnPreparedListener( new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(false);
//                if (!playFileRes(videoPath)) return;
                mVV.start();
//                pDialog.dismiss();
            }
        });

        mVV.setOnTouchListener( new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if( ((VideoView)v).isPlaying() )
                    ((VideoView)v).pause();
                else
                    ((VideoView)v).start();
                return true;
            }
        });

        final MediaPlayer.OnInfoListener onInfoToPlayStateListener = new MediaPlayer.OnInfoListener() {

            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
                        pDialog.dismiss();
                        return true;
                    }
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                        pDialog.show();
                        return true;
                    }
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                        pDialog.show();
                        return true;
                    }
                }
                return false;
            }

        };

        mVV.setOnInfoListener(onInfoToPlayStateListener);
    }

    private boolean playFileRes(String videoPath) {
        if (videoPath==null || "".equals(videoPath)) {
            stopPlaying();
            return false;
        } else {
            mVV.setVideoURI( Uri.parse(videoPath) );
            return true;
        }
    }

    public void stopPlaying() {
        mVV.stopPlayback();
        this.finish();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Intent ea = new Intent(FullVideoActivity.this, VideoActivity.class);
        ea.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(ea);
        overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
