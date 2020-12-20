package edu.uwm.seensay;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static String MUTED_OR_NOT = "edu.uwm.seensay.MUTED_OR_NOT";
    MediaPlayer homeScreenMP;
    ImageView floatingPic;
    ImageView underCam;
    Button aviButton;
    Button startGameButton;
    Button soundButton;
    boolean muted;
    boolean resultsMuted;
    boolean gameMuted;

    PhoneStateListener phoneStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build();
        StrictMode.setThreadPolicy(policy);

        floatingPic = findViewById(R.id.profile_image);
        soundButton = findViewById(R.id.volume);

        Intent intent = getIntent();
        resultsMuted = intent.getBooleanExtra(Results.RESULTS_MUTE, false);

        Intent intent1 = getIntent();
        gameMuted = intent1.getBooleanExtra(GamePlay.GAME_MUTE, false);

        muted = (resultsMuted || gameMuted);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.title_layout);

        aviButton = findViewById(R.id.camera);
        underCam = findViewById(R.id.underCamera);

        if (BitmapHelper.getInstance().getBitmap() != null) {
            aviButton.setBackground(null);
            underCam.setImageBitmap(BitmapHelper.getInstance().getBitmap());
        }

        startMusic();
        if (!muted) {
            homeScreenMP.setVolume(1,1);
        } else {
            soundButton.setBackgroundResource(R.drawable.muted);
            homeScreenMP.setVolume(0, 0);
        }

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //Incoming call: Pause music
                    homeScreenMP.pause();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                    startMusic();
                }
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muted = !muted;

                if (muted) {
                    soundButton.setBackgroundResource(R.drawable.muted);
                    homeScreenMP.setVolume(0, 0);
                } else {
                    soundButton.setBackgroundResource(R.drawable.volume);
                    homeScreenMP.setVolume(1,1);
                }
                fade(v);
            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA
                    }, 100);
        }

        aviButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(v, aviButton);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent2, 100);
                    }
                }, 350);
            }
        });

        startGameButton = findViewById(R.id.startButton);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(v, startGameButton);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openGamePlay();
                    }
                }, 600);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (data == null) {
                return;
            }
            Bitmap captureImage = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            //floatingPic.setImageBitmap(captureImage);

            assert captureImage != null;
            Bitmap cropped = cropToSquare(captureImage);
            BitmapHelper.getInstance().setBitmap(cropped);
            BitmapDrawable newProf = new BitmapDrawable(getResources(), cropped);
            aviButton.setBackground(null);
            underCam.setImageBitmap(cropped);
        }
    }

    public static Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = Math.min(height, width);
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = Math.max(cropW, 0);
        int cropH = (height - width) / 2;
        cropH = Math.max(cropH, 0);

        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
    }

    public void openGamePlay() {
        Intent intent = new Intent(this, GamePlay.class);
        intent.putExtra(MUTED_OR_NOT, muted);

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        startActivity(intent);
        stopMusic();
        finish();
    }

    public void startMusic() {
        if (homeScreenMP == null) {
            homeScreenMP = MediaPlayer.create(this, R.raw.homemusic);
            homeScreenMP.setLooping(true);
        }

        if (!homeScreenMP.isPlaying()) {
            homeScreenMP.start();
        }
    }

    public void stopMusic() {
        if (homeScreenMP.isPlaying()) {
            homeScreenMP.stop();
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        stopMusic();
        finish();
        System.exit(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        startMusic();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isApplicationSentToBackground(this)) {
            homeScreenMP.pause();
        }

    }

    public boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public void bounce(View view, Button b) {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        MyBounce bouncy = new MyBounce(.2, 10);
        animation.setInterpolator(bouncy);
        b.startAnimation(animation);
    }


    public void fade(View view) {
        Button button = soundButton;
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade);
        button.startAnimation(animation);
    }
}