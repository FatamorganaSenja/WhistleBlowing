package senja.fatamorgana.whistleblowing.Config;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    public static final String SP_USER_APP = "spUserApp";

    public static final String SP_NIM = "user_id";
    public static final String SP_NAMA = "nama_user";
    public static final String SP_USERNAME = "username";
    public static final String SP_PASSWORD = "password";
    public static final String SP_SUDAH_LOGIN = "spSudahLogin";
    public static final String SP_FIRST = "pertama";
    public static final String SP_CHANCE = "chance";
    public static final String SP_QUESTION = "question";
    public static final String SP_QUESTION_TAKE = "izi";
    public static final String SP_FILENAME = "mp4";
    public static final String SP_ANSWER = "answer";
    public static final String SP_SEARCH_KEY = "key";
    public static final String SP_APPID = "app_id";
    public static final String SP_APPVERSION = "app_version";
    public static final String SP_APPSTATUS = "app_status";
    public static final String SP_APPPASSWORD = "app_password";

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    public SharedPrefManager(Context context) {
        sp = context.getSharedPreferences(SP_USER_APP, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public void saveSPString(String keySP, String value) {
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public void saveSPInt(String keySP, int value) {
        spEditor.putInt(keySP, value);
        spEditor.commit();
    }

    public void saveSPBoolean(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public String getSPNama() {
        return sp.getString(SP_NAMA, "");
    }

    public String getSpSearchKey() {
        return sp.getString(SP_SEARCH_KEY, "");
    }

    public String getSPUsername() {
        return sp.getString(SP_USERNAME, "");
    }

    public String getSPPassword() {
        return sp.getString(SP_PASSWORD, "");
    }

    public String getSPNIM() {
        return sp.getString(SP_NIM, "");
    }

    public String getSPChance() {
        return sp.getString(SP_CHANCE, "");
    }

    public String getSpQuestion() {
        return sp.getString(SP_QUESTION, "");
    }

    public String getSpQuestionTake() {
        return sp.getString(SP_QUESTION_TAKE, "");
    }

    public String getSpFilename() {
        return sp.getString(SP_FILENAME, "");
    }

    public String getSpAppid() {
        return sp.getString(SP_APPID, "");
    }

    public String getSpAppversion() {
        return sp.getString(SP_APPVERSION, "");
    }

    public String getSpAppstatus() {
        return sp.getString(SP_APPSTATUS, "");
    }

    public String getSpApppassword() {
        return sp.getString(SP_APPPASSWORD, "");
    }

    public String getSpAnswer() {
        return sp.getString(SP_ANSWER, "");
    }

    public Boolean getSPSudahLogin() {
        return sp.getBoolean(SP_SUDAH_LOGIN, false);
    }

    public Boolean getSPFirst() {
        return sp.getBoolean(SP_FIRST, false);
    }

}
