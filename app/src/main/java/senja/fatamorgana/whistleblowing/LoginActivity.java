package senja.fatamorgana.whistleblowing;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import senja.fatamorgana.whistleblowing.Config.Link;
import senja.fatamorgana.whistleblowing.Config.SharedPrefManager;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    EditText etNIM;
    String NIM, data_result;
    Boolean connect_status;
    SharedPrefManager SP_Help;
    JSONArray resultJson = null;
    RelativeLayout rl_prosesLogin, rl_login;
    LottieAnimationView anim_login;
    Handler handler;
    Animation fadeout, fadein;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        SP_Help = new SharedPrefManager(this);

        if (SP_Help.getSPSudahLogin()){
            Intent i = new Intent(LoginActivity.this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
            finish();
        }

        anim_login.setScale(0.3f);
        anim_login.setSpeed(1f);

        fadeout = AnimationUtils.loadAnimation(this, R.anim.fade_out_animation);
        fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (etNIM.getText().toString().length() < 1){
                   Toast.makeText(LoginActivity.this, R.string.loginKosong, Toast.LENGTH_SHORT).show();
                   etNIM.requestFocus();
               }else {
                   rl_login.setAnimation(fadeout);
                   rl_prosesLogin.animate().alpha(1.0f);
                   rl_prosesLogin.setVisibility(View.VISIBLE);
                   rl_prosesLogin.setAnimation(fadein);
                   handler = new Handler();
                   delayLogin();
               }
            }
        });
    }

    void init(){
        btnLogin = (Button)findViewById(R.id.btnLogin);
        etNIM = (EditText)findViewById(R.id.etNIM);
        rl_prosesLogin = (RelativeLayout)findViewById(R.id.rl_prosesLogin);
        rl_login = (RelativeLayout)findViewById(R.id.rl_login);
        anim_login = (LottieAnimationView)findViewById(R.id.anim_login);
    }

    void delayLogin(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkConnection();
            }
        }, 1000);

    }

    void Login(final String NIM){
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("nim", NIM));

                String result = null;
                InputStream is = null;
                String line;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(Link.getData);
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
                Log.e("DATA LOGIN => ", data_result+"\n\n");
                loginResult();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

    void loginResult(){
        String a = Character.toString(data_result.charAt(0));

        if (a.equals("0")) {
            Toast.makeText(this, R.string.akun_false, Toast.LENGTH_SHORT).show();
            rl_prosesLogin.animate().alpha(0.0f).setDuration(1000);
            rl_login.setAnimation(fadein);

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
                    SP_Help.saveSPBoolean(SharedPrefManager.SP_SUDAH_LOGIN, true);
                    Intent i = new Intent(LoginActivity.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
                    finish();
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
                    NIM = etNIM.getText().toString();
                    Login(NIM);
                }else {
                    Toast.makeText(LoginActivity.this, R.string.koneksi_error, Toast.LENGTH_SHORT).show();
                    rl_prosesLogin.animate().alpha(0.0f).setDuration(1000);
                    rl_login.setAnimation(fadein);
                }
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();

        return connect_status;
    }
}
