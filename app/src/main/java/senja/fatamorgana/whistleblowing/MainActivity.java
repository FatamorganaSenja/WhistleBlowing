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
import android.widget.Button;
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

import cn.pedant.SweetAlert.SweetAlertDialog;
import senja.fatamorgana.whistleblowing.Config.Link;
import senja.fatamorgana.whistleblowing.Config.SharedPrefManager;
import senja.fatamorgana.whistleblowing.Config.UpdateApp;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static senja.fatamorgana.whistleblowing.Config.Link.AppFolder;

public class MainActivity extends AppCompatActivity {

    Button bt_logout, bt_profile;
    SharedPrefManager SP_Help;
    Boolean connect_status;
    JSONArray resultJson = null;
    String data_result, app_version, update_version;
    Handler handler;
    UpdateApp UpdateApps;
    private static final int REQUEST_WRITE_PERMISSION = 786;

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

        bt_logout = (Button)findViewById(R.id.bt_logout);
        bt_profile = (Button)findViewById(R.id.bt_profile);

        bt_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SP_Help.saveSPBoolean(SharedPrefManager.SP_SUDAH_LOGIN, false);
                Intent i = new Intent(MainActivity.this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in_animation, R.anim.fade_out_animation);
                finish();
            }
        });

        bt_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
                pDialog.setTitleText("Oops...");
                pDialog.setContentText("Aplikasi Anda \nKudet");
                pDialog.setConfirmText("Update");
                pDialog.setCancelable(false);
                pDialog.show();
            }
        });

        handler = new Handler();
        delayCheck();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
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
            if (update_version.equals(app_version)){
                Log.e("VERSION ==>",update_version+" ==== "+app_version);
            }else {
                updateAlert();
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
//                    noConnection();
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
                update();
                pDialog.dismissWithAnimation();
            }
        });
        pDialog.show();
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
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("\nDownloading");
        pDialog.setCancelable(false);
        pDialog.show();
        downloadUpdate();
    }

    void downloadUpdate(){

        int check = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (check == PackageManager.PERMISSION_GRANTED) {

//            UpdateApps = new UpdateApp();
//            UpdateApps.setContext(getApplicationContext());
//            UpdateApps.execute(Link.Update);
            InstallUpdate();

        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1024);
            downloadUpdate();
        }
    }

    void InstallUpdate(){
        String destination = Environment.getExternalStoragePublicDirectory(AppFolder) + "/";
        final String fileName = "Update.apk";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists())
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();

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
                requestPermission();
                canReadWriteExternal();
//                File toInstall = new File(Environment.getExternalStoragePublicDirectory(Link.AppFolder),"/Update.apk");
//                Uri fileUri = FileProvider.getUriForFile(ctxt,getApplicationContext().getPackageName() + ".provider", toInstall);
//
//                Intent install = new Intent(Intent.ACTION_VIEW);
//                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                install.setDataAndType(fileUri,
//                        manager.getMimeTypeForDownloadedFile(downloadId));
//                startActivity(install);
//
//                unregisterReceiver(this);
//                finish();

//                File toInstall = new File(Environment.getExternalStoragePublicDirectory(AppFolder),"/Update.apk");
//                Log.e("PATH =>>",""+toInstall);
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
}