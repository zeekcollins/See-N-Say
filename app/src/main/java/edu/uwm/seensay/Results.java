package edu.uwm.seensay;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.*;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

public class Results extends AppCompatActivity {
    public static final String RESULTS_MUTE = "edu.uwm.seensay.RESULTS_MUTE";
    ImageView displayProf;
    Handler handler;
    boolean muted;

    PhoneStateListener phoneStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Objects.requireNonNull(getSupportActionBar()).hide();

        Intent intent = getIntent();
        int score = intent.getIntExtra(GamePlay.FINAL_SCORE, 0);
        muted = intent.getBooleanExtra(GamePlay.LAST_MUTE, false);

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //Incoming call: Pause music
                    stopMusic();
                }
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        displayProf = findViewById(R.id.profile_image);
        if (BitmapHelper.getInstance().getBitmap() != null) {
            displayProf.setImageBitmap(BitmapHelper.getInstance().getBitmap());
        }

        TextView tv1 = (TextView) findViewById(R.id.resultScore);
        tv1.setText(String.valueOf(score));

        TextView tv2 = (TextView) findViewById(R.id.pointText);
        if (score == 1) {
            tv2.setText("Point!");
        } else if (score > 9) {
            tv1.setTextSize((float) (tv1.getTextSize()/4));
        }

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                backToStart();
            }
        }, 7000);


    }
    public void stopMusic() {
        stopService(new Intent(this, BackgroundMusic.class));
    }

    public void backToStart() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(RESULTS_MUTE, muted);
        startActivity(intent);

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        stopMusic();
        finish();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        handler.removeCallbacksAndMessages(null);
        backToStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isApplicationSentToBackground(this)) {
            stopMusic();
            System.exit(0);
        }

    }

    public boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            return !topActivity.getPackageName().equals(context.getPackageName());
        }
        return false;
    }
}