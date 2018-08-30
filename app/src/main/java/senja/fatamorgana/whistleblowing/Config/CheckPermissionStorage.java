package senja.fatamorgana.whistleblowing.Config;

import android.os.Environment;

public class CheckPermissionStorage {
    public static boolean isSDCardPresent() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
}
