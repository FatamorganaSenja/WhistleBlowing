package senja.fatamorgana.whistleblowing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import bg.devlabs.fullscreenvideoview.FullscreenVideoView;

public class VideoActivity extends AppCompatActivity {

    FullscreenVideoView fullscreenVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        fullscreenVideoView = findViewById(R.id.fullscreenVideoView);
        String videoUrl = "http://zestiria.me/mine.mp4";
        fullscreenVideoView.toggleFullscreen();
        fullscreenVideoView.videoUrl(videoUrl)
                .enableAutoStart()
                .canSeekBackward(false)
                .canSeekForward(false);

    }
}
