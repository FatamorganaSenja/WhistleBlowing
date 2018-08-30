package senja.fatamorgana.whistleblowing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import bg.devlabs.fullscreenvideoview.FullscreenVideoView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import senja.fatamorgana.whistleblowing.Config.SharedPrefManager;

public class VideoActivity extends AppCompatActivity {

    FullscreenVideoView fullscreenVideoView;
    Button btn_submit;


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

        btn_submit = (Button)findViewById(R.id.btn_submit);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog pDialog = new SweetAlertDialog(VideoActivity.this);
                pDialog.setTitleText("Anda yakin ingin dengan jawaban ini?");
                pDialog.setConfirmText("Ya");
                pDialog.setCancelText("Tidak");
                pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        Intent i = new Intent(VideoActivity.this, SuccessActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
                        finish();
                    }
                });
                pDialog.show();
            }
        });
    }


}
