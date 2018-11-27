package senja.fatamorgana.whistleblowing;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import bg.devlabs.fullscreenvideoview.FullscreenVideoView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import senja.fatamorgana.whistleblowing.Config.Link;
import senja.fatamorgana.whistleblowing.Config.SharedPrefManager;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class VideoActivity extends AppCompatActivity {

    Button btn_submit, bt_setuju, bt_tsetuju;
    String video_id, jawaban = "25", data_result, question;
    IndicatorSeekBar sb_indicator;
    Boolean connect_status;
    JSONArray resultJson = null;
    SharedPrefManager SP_Help;
    SweetAlertDialog ConnectionDialog, QuizDialog, FailedDialog, checkConnectionDialog, emptyDialog;
    LinearLayout ll_ulangi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        SP_Help = new SharedPrefManager(this);

        btn_submit = (Button)findViewById(R.id.btn_submit);
        bt_setuju = (Button)findViewById(R.id.bt_setuju);
        bt_tsetuju = (Button)findViewById(R.id.bt_tsetuju);
        sb_indicator = (IndicatorSeekBar) findViewById(R.id.sb_indicator);
        ll_ulangi = (LinearLayout)findViewById(R.id.ll_ulangi);

        ll_ulangi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ea = new Intent(VideoActivity.this, FullVideoActivity.class);
                ea.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(ea);
                overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
                finish();
            }
        });

        video_id = SP_Help.getSpQuestionTake();
        question = video_id;

//        Bundle b = this.getIntent().getExtras();
//        video_id = b.getString("file");
//        question = b.getString("id");

//        String Lokasi = Link.AppFolder;
//        File sdcard = getExternalStoragePublicDirectory(Lokasi);
//        File file = new File(sdcard, video_id);
//        Uri fileUri = FileProvider.getUriForFile(VideoActivity.this,  getResources().getString(R.string.authority_provider), file);
//        File path = new File(file.getPath());
//        fullscreenVideoView.toggleFullscreen();
//        fullscreenVideoView.videoFile(path)
//                .enableAutoStart()
//                .canSeekBackward(false)
//                .canSeekForward(false);


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnectionDialog = new SweetAlertDialog(VideoActivity.this);
                checkConnectionDialog.setTitleText("Anda yakin ingin dengan jawaban ini?");
                checkConnectionDialog.setConfirmText("Ya");
                checkConnectionDialog.setCancelText("Tidak");
                checkConnectionDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        ConnectionDialog();
//                        parseIndicator();
                        checkJawabanUser();
                        checkConnectionDialog.dismissWithAnimation();
                    }
                });
                checkConnectionDialog.show();
            }
        });

        bt_setuju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bt_tsetuju.getVisibility() == View.VISIBLE){
                    bt_tsetuju.setVisibility(View.INVISIBLE);
                    bt_setuju.setTextColor(Color.GREEN);
                }else if (bt_tsetuju.getVisibility() == View.INVISIBLE){
                    bt_tsetuju.setVisibility(View.VISIBLE);
                    bt_setuju.setTextColor(Color.WHITE);
                }
            }
        });

        bt_tsetuju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bt_setuju.getVisibility() == View.VISIBLE){
                    bt_setuju.setVisibility(View.INVISIBLE);
                    bt_tsetuju.setTextColor(Color.GREEN);
                }else if (bt_setuju.getVisibility() == View.INVISIBLE){
                    bt_setuju.setVisibility(View.VISIBLE);
                    bt_tsetuju.setTextColor(Color.WHITE);
                }
            }
        });

        sb_indicator.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                jawaban = Integer.toString(seekParams.progress);
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        final MediaPlayer question = MediaPlayer.create(this, R.raw.question);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                question.start();
            }
        }, 500);
    }

    void checkJawabanUser(){
        if (bt_setuju.getVisibility() == View.VISIBLE && bt_tsetuju.getVisibility() == View.INVISIBLE){
            jawaban = "Setuju";
            Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkConnection();
                }
            }, 1000);
        }else if (bt_tsetuju.getVisibility() == View.VISIBLE && bt_setuju.getVisibility() == View.INVISIBLE){
            jawaban = "Tidak Setuju";
            Handler handler1 = new Handler();
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkConnection();
                }
            }, 1000);
        }else {
            emptyResponse();
        }
    }

    void emptyResponse(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ConnectionDialog.dismissWithAnimation();
                Handler handler1 = new Handler();
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        emptyDialog();
                    }
                }, 500);
            }
        }, 2000);
    }

    void parseIndicator(){
        final Integer p = Integer.parseInt(jawaban);
        if (p >= 0 && p <= 10){
            jawaban = "Tidak Setuju";
            checkConnection();
        }
        if (p >= 20 && p <= 25){
            jawaban = "Kurang Setuju";
            checkConnection();
        }
        if (p >= 45 && p <= 55){
            jawaban = "Setuju";
            checkConnection();
        }
        if (p >= 70 && p <= 80){
            jawaban = "Setuju Saja";
            checkConnection();
        }
        if (p >= 95 && p <= 100){
            jawaban = "Sangat Setuju";
            checkConnection();
        }
    }

    void jawabQuiz(final String nim, final String question, final String jawaban){
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("nim", nim));
                nameValuePairs.add(new BasicNameValuePair("question", question));
                nameValuePairs.add(new BasicNameValuePair("answer", jawaban));

                String result = null;
                InputStream is = null;
                String line;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(Link.postQuiz);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                    Log.e("pass 1", "connection success ");
                } catch (Exception e) {
                    Log.e("Fail 1", e.toString());
                }
                try {
                    BufferedReader reader = new BufferedReader
                            (new InputStreamReader(is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                } catch (Exception e) {
                    Log.e("Fail 2", e.toString());
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                data_result = result;
                Log.e("DATA SETELAH KIRIM => ", data_result+"\n\n");
                answerQuiz();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

    void answerQuiz(){
        ConnectionDialog.dismissWithAnimation();
        String a = Character.toString(data_result.charAt(0));

        if (a.equals("0")) {
            Toast.makeText(this, R.string.post_false, Toast.LENGTH_SHORT).show();

        }else if(data_result.length() > 5){
            try {
                JSONObject jsonObj = new JSONObject(data_result);
                resultJson = jsonObj.getJSONArray("result");

                for (int i = 0; i < resultJson.length(); i++) {
                    JSONObject c = resultJson.getJSONObject(i);

                    SP_Help.saveSPString(SharedPrefManager.SP_NAMA, c.getString("nama"));
                    SP_Help.saveSPString(SharedPrefManager.SP_NIM, c.getString("nim"));
                    SP_Help.saveSPString(SharedPrefManager.SP_CHANCE, c.getString("chance"));
                    SP_Help.saveSPString(SharedPrefManager.SP_QUESTION, c.getString("question"));
                    SP_Help.saveSPString(SharedPrefManager.SP_ANSWER, c.getString("answer"));

                }
                QuizDialog();
                // Stop refresh animation
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private Boolean checkConnection() {
        class GetDataJSON extends AsyncTask<Boolean, Void, Boolean> {
            @Override
            protected Boolean doInBackground(Boolean... params) {
                HttpClient httpclient = new DefaultHttpClient();

                Boolean responseString = null;
                HttpResponse response = null;
                try {
                    response = httpclient.execute(new HttpGet(Link.checkLink));

                    StatusLine statusline = response.getStatusLine();

                    if (statusline.getStatusCode() == HttpStatus.SC_OK) {
                        responseString = true;
                        return responseString;
                    } else {
                        responseString = false;
                    }
                } catch (IOException e) {
                    responseString = false;
                } finally {
                    if (response != null) {
//                        response.getEntity().consumeContent();
                        responseString = false;
                    }
                }
                return responseString;
            }

            @Override
            protected void onPostExecute(Boolean responseString) {
                connect_status = responseString;
                Log.e("Internet", " => "+connect_status);
                if (connect_status){
                    ConnectionDialog.dismissWithAnimation();
                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            jawabQuiz(SP_Help.getSPNIM(), question, jawaban);
                        }
                    }, 500);
                }else {
                    Toast.makeText(VideoActivity.this, R.string.koneksi_error, Toast.LENGTH_SHORT).show();
                    ConnectionDialog.dismissWithAnimation();
                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FailedDialog();
                        }
                    }, 500);
//                    rl_prosesLogin.animate().alpha(0.0f).setDuration(1000);
//                    rl_login.setAnimation(fadein);
                }
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();

        return connect_status;
    }

    void ConnectionDialog(){
        ConnectionDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        ConnectionDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        ConnectionDialog.setTitleText("\nMemeriksa Koneksi");
        ConnectionDialog.setCancelable(false);
        ConnectionDialog.show();
    }

    void FailedDialog(){
        FailedDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        FailedDialog.setTitleText("Oops...");
        FailedDialog.setContentText("Koneksi Bermasalah");
        FailedDialog.setConfirmText("Ok");
        FailedDialog.show();
    }

    void emptyDialog(){
        emptyDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        emptyDialog.setTitleText("Oops...");
        emptyDialog.setContentText("Kamu Harus Memilih Jawaban !");
        emptyDialog.setConfirmText("Ok");
        emptyDialog.show();
    }

    void QuizDialog(){
        SP_Help.saveSPString(SharedPrefManager.SP_QUESTION_TAKE,"");
        SP_Help.saveSPString(SharedPrefManager.SP_FILENAME,"");
        QuizDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        QuizDialog.setTitleText("Terimakasih");
        QuizDialog.setContentText("Atas Partisipasi\n Anda");
        QuizDialog.setConfirmText("Ok");
        QuizDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                Intent i = new Intent(VideoActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
                finish();
            }
        });
        QuizDialog.setCancelable(false);
        QuizDialog.show();
    }

    @Override
    public void onBackPressed(){
        SweetAlertDialog pDialog = new SweetAlertDialog(VideoActivity.this, SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText("Oops...");
        pDialog.setContentText("Tombol Kembali\n Tidak Diperbolehkan \n Disini");
        pDialog.setConfirmText("Ok");
        pDialog.show();
    }
}
