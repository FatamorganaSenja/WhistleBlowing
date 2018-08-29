package senja.fatamorgana.whistleblowing;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Splashscreen extends AppCompatActivity {

    private static int splashInterval = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent ez = new Intent(Splashscreen.this, GuideActivity.class);
                startActivity(ez);

                this.finish();
            }
            private void finish() {
                // TODO Auto-generated method stub

            }
        },splashInterval);
    }
}
