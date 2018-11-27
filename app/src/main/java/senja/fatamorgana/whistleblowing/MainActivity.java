package senja.fatamorgana.whistleblowing;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pub.devrel.easypermissions.EasyPermissions;
import senja.fatamorgana.whistleblowing.Config.CheckPermissionStorage;
import senja.fatamorgana.whistleblowing.Config.Link;
import senja.fatamorgana.whistleblowing.Config.SharedPrefManager;
import senja.fatamorgana.whistleblowing.Config.UpdateApp;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static senja.fatamorgana.whistleblowing.Config.Link.AppFolder;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    ImageView iv_logout, iv_profile, iv_mulai;
    TextView tv_nmMahasiswa, tv_nim;
    SharedPrefManager SP_Help;
    Boolean connect_status, update = false, video = false;
    JSONArray resultJson = null;
    String data_result, app_version, update_version, status_server, trigger;
    Handler handler;
    UpdateApp UpdateApps;
    Integer r1, max = 4, min = 1;
    String n1;
    private static final int REQUEST_WRITE_PERMISSION = 786;
    private static final int WRITE_REQUEST_CODE = 300;
    SweetAlertDialog updateDialog, noChance, videoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//        builder.detectFileUriExposure();

//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder(); StrictMode.setVmPolicy(builder.build());
        SP_Help = new SharedPrefManager(this);
        app_version = BuildConfig.VERSION_NAME;
        trigger = SP_Help.getSpQuestionTake();
        Log.e("TRIG", trigger);
        if (trigger.length() > 0){
            Intent i = new Intent(MainActivity.this, VideoActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
            finish();
        }else {
            handler = new Handler();
            delayCheck();
        }



        iv_logout = (ImageView) findViewById(R.id.iv_logout);
        iv_profile = (ImageView) findViewById(R.id.iv_profile);
        iv_mulai = (ImageView) findViewById(R.id.iv_mulai);
        tv_nmMahasiswa = (TextView)findViewById(R.id.tv_nmMahasiswa);
        tv_nim = (TextView)findViewById(R.id.tv_nim);

        tv_nmMahasiswa.setText(SP_Help.getSPNama());
        tv_nim.setText(SP_Help.getSPNIM());

        iv_mulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(SP_Help.getSPChance()) > 0 ){
                    randomNumber();
                    downloadVideo("1");
                }else {
                    noChance();
                }
//                Intent ee = new Intent(MainActivity.this, VideoActivity.class);
//                startActivity(ee);
//                overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
            }
        });

        iv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               logout();
            }
        });

//        iv_profile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
//                pDialog.setTitleText("Oops...");
//                pDialog.setContentText("Aplikasi Anda \nKudet");
//                pDialog.setConfirmText("Update");
//                pDialog.setCancelable(false);
//                pDialog.show();
//            }
//        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {

        if (update){
            downloadUpdate();
        }

        if (video){
            downloadVideo("1");
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        final SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText("Oops...");
        pDialog.setContentText("Permission Ditolak");
        pDialog.setConfirmText("Ok");
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                delayCheck();
                pDialog.dismissWithAnimation();
            }
        });
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
    }

    private boolean canReadWriteExternal() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    void delayCheck(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkConnection();
            }
        }, 500);
    }

    void CheckApp(final String ID){
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("id", ID));

                String result = null;
                InputStream is = null;
                String line;
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(Link.getBase);
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
                Log.e("DATA STARTUP => ", data_result+"\n\n");
                compareApp();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

    void compareApp(){
        try {
            JSONObject jsonObj = new JSONObject(data_result);
            resultJson = jsonObj.getJSONArray("result");

            for (int i = 0; i < resultJson.length(); i++) {
                JSONObject c = resultJson.getJSONObject(i);

                SP_Help.saveSPString(SharedPrefManager.SP_APPID, c.getString("id"));
                SP_Help.saveSPString(SharedPrefManager.SP_APPVERSION, c.getString("version"));
                SP_Help.saveSPString(SharedPrefManager.SP_APPSTATUS, c.getString("status"));
                SP_Help.saveSPString(SharedPrefManager.SP_APPPASSWORD, c.getString("password"));

            }
            update_version = SP_Help.getSpAppversion();
            status_server = SP_Help.getSpAppstatus();

            if (status_server.equals("online")){
                if (update_version.equals(app_version)){
                    Log.e("VERSION ==>",update_version+" ==== "+app_version);
                }else {
                    updateAlert();
                }
            }else {
                statusAlert();
            }



            // Stop refresh animation
        } catch (JSONException e) {
            e.printStackTrace();
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
                    CheckApp("1");
                }else {
                    noConnection();
                }
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute();

        return connect_status;
    }

    void updateAlert(){
        final SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText("Oops...");
        pDialog.setContentText("Aplikasi Anda \nKadaluarsa");
        pDialog.setConfirmText("Update");
        pDialog.setCancelable(false);
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                downloadUpdate();
                pDialog.dismissWithAnimation();
            }
        });
        pDialog.show();
    }

    void statusAlert(){
        final SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText("Oops...");
        pDialog.setContentText("Server Sedang \nMaintenance");
        pDialog.setConfirmText("Ok");
        pDialog.setCancelable(false);
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                pDialog.dismissWithAnimation();
                finish();
            }
        });
        pDialog.show();
    }

    void noChance(){
        noChance = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
        noChance.setTitleText("Terimakasih");
        noChance.setContentText("Anda Sudah\n Menjawab Quiz");
        noChance.setConfirmText("Ok");
        noChance.show();
    }

    void videoDownload(final String id){
        videoDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        videoDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        videoDialog.setTitleText("\nLoading\n Video");
        videoDialog.setCancelable(false);
        videoDialog.show();
//        checkVideo(id);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                urlVideo(id);
            }
        }, 3000);
    }

    void noConnection(){
        SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText("Oops...");
        pDialog.setContentText("Koneksi Bermasalah");
        pDialog.setConfirmText("Ok");
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                delayCheck();
            }
        });
        pDialog.setCancelable(false);
        pDialog.show();
    }

    void update(){
        updateDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        updateDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        updateDialog.setTitleText("\nDownloading");
        updateDialog.setCancelable(false);
        updateDialog.show();
        InstallUpdate();
    }

    void downloadUpdate(){
        if (CheckPermissionStorage.isSDCardPresent()) {
            if (EasyPermissions.hasPermissions(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                update();
            }else {
                EasyPermissions.requestPermissions(MainActivity.this, getString(R.string.write_file), WRITE_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
                update = true;
            }
        } else {
            EasyPermissions.requestPermissions(MainActivity.this, getString(R.string.write_file), WRITE_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
            update = true;
        }
    }

    void downloadVideo(final String id){
        if (CheckPermissionStorage.isSDCardPresent()) {
            if (EasyPermissions.hasPermissions(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                videoDownload(id);
            }else {
                video = true;
                EasyPermissions.requestPermissions(MainActivity.this, getString(R.string.write_file), WRITE_REQUEST_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        } else {
            video = true;
            EasyPermissions.requestPermissions(MainActivity.this, getString(R.string.write_file), WRITE_REQUEST_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    void InstallUpdate(){
        String destination = Environment.getExternalStoragePublicDirectory(AppFolder) + "/";
        final String fileName = "Update.apk";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(uri.getPath());
        if(file.exists()){
            file.delete();
            if(file.exists()){
                getApplicationContext().deleteFile(file.getName());
            }
        }

        //get url of app on server
        String url = Link.Update;

        //set downloadmanager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Whistleblowing");
        request.setTitle("Downloading Update...");

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                updateDialog.dismissWithAnimation();
                requestPermission();
                canReadWriteExternal();

                String Lokasi = Link.AppFolder;
                File sdcard = getExternalStoragePublicDirectory(Lokasi);
                File file = new File(sdcard, fileName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    Uri fileUri = FileProvider.getUriForFile(MainActivity.this,  getResources().getString(R.string.authority_provider), file);
                    intent = new Intent(Intent.ACTION_VIEW, fileUri);
                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    intent.setDataAndType(fileUri, "application/vnd.android" + ".package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                    finish();
                } else {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                    finish();
                }
            }
        };
        //register receiver for when .apk download is compete
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    void logout(){
        SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this);
        pDialog.setTitleText("Anda yakin ingin logout?");
        pDialog.setConfirmText("Ya");
        pDialog.setCancelText("Tidak");
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                SP_Help.saveSPBoolean(SharedPrefManager.SP_SUDAH_LOGIN, false);
                Intent i = new Intent(MainActivity.this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
                finish();
            }
        });
        pDialog.show();
    }

    void randomNumber() {
        Random ran1 = new Random();
        r1 = ran1.nextInt(max - min + 1) + min;
        n1 = Integer.toString(r1);
    }

    void urlVideo(final String id){
        SP_Help.saveSPString(SharedPrefManager.SP_QUESTION_TAKE, id);
        Intent intent = new Intent(MainActivity.this, FullVideoActivity.class);
        Bundle c = new Bundle();

        c.putString("file", id);
        intent.putExtras(c);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
    }

    void checkVideo(final String id){
//        requestPermission();
//        canReadWriteExternal();
        String destination = Environment.getExternalStoragePublicDirectory(AppFolder) + "/";
        final String fileName = id+".mp4";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(uri.getPath());
        if(file.exists()){
//            file.delete();
                SP_Help.saveSPString(SharedPrefManager.SP_FILENAME, fileName);
                Log.e("MAIN FILE", fileName);
                SP_Help.saveSPString(SharedPrefManager.SP_QUESTION_TAKE, id);
                Intent intent = new Intent(MainActivity.this, FullVideoActivity.class);
                Bundle c = new Bundle();

                c.putString("file", fileName);
                c.putString("id", id);
                intent.putExtras(c);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);

        }else{
            //get url of app on server
            String url = Link.getVideo+fileName;

            //set downloadmanager
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("Whistleblowing");
            request.setTitle("Downloading Video");

            //set destination
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
//                    updateDialog.dismissWithAnimation();

                    SP_Help.saveSPString(SharedPrefManager.SP_FILENAME, fileName);
                    SP_Help.saveSPString(SharedPrefManager.SP_QUESTION_TAKE, id);
                    intent = new Intent(MainActivity.this, FullVideoActivity.class);
                    Bundle c = new Bundle();

                    c.putString("file", fileName);
                    intent.putExtras(c);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
//                    String Lokasi = Link.AppFolder;
//                    File sdcard = getExternalStoragePublicDirectory(Lokasi);
//                    File file = new File(sdcard, fileName);

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//
//                        Uri fileUri = FileProvider.getUriForFile(MainActivity.this,  getResources().getString(R.string.authority_provider), file);
//                        intent = new Intent(Intent.ACTION_VIEW, fileUri);
//                        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
//                        intent.setDataAndType(fileUri, "application/vnd.android" + ".package-archive");
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        startActivity(intent);
//                        finish();
//                    } else {
//                        intent = new Intent(Intent.ACTION_VIEW);
//                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        startActivity(intent);
//                        finish();
//                    }
                }
            };
            //register receiver for when .apk download is compete
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    @Override
    public void onBackPressed(){
        final SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this);
        pDialog.setTitleText("Anda Mau Keluar Aplikasi ?");
        pDialog.setConfirmText("Ya");
        pDialog.setCancelText("Tidak");
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                pDialog.dismissWithAnimation();
                finish();
            }
        });
        pDialog.show();
    }
}

