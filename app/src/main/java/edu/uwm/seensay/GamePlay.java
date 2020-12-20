package edu.uwm.seensay;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Sampler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GamePlay extends AppCompatActivity {
    public static final String FINAL_SCORE = "edu.uwm.seensay.FINAL_SCORE";
    public static final String LAST_MUTE = "edu.uwm.seensay.LAST_MUTE";
    public static final String GAME_MUTE = "edu.uwm.seensay.GAME_MUTE";
    Animal[] animalArr;
    private Button image1;
    private Button image2;
    private Button image3;
    private Button image4;
    private MediaPlayer sound1;
    private MediaPlayer sound2;
    private MediaPlayer sound3;
    private MediaPlayer sound4;
    private MediaPlayer currentAnimalSound;

    private Button musicButton;
    private Button soundText;
    private Button answer;
    private Button done;
    private Button homeTitle;
    private Button aviButton;
    private ImageView underCam;
    private View backColor;
    private int score;
    private int rightOrWrong;
    private boolean muted;
    Dialog dialog;
    Button yes;
    Button no;

    PhoneStateListener phoneStateListener;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels + getNavigationBarHeight();
        int width = displayMetrics.widthPixels;

        dialog = new Dialog(GamePlay.this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.stroke_dialog));
        dialog.getWindow().setLayout((int) (width*.9), (int) (height/2.5));
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        yes = dialog.findViewById(R.id.yesButton);
        no = dialog.findViewById(R.id.noButton);

        Intent intent = getIntent();
        muted = intent.getBooleanExtra(MainActivity.MUTED_OR_NOT, false);
        musicButton = findViewById(R.id.volume);

        if (!muted) {
            startService(new Intent(this, BackgroundMusic.class));
        } else {
            musicButton.setBackgroundResource(R.drawable.muted);
        }

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

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.title_layout);
        done = findViewById(R.id.doneButton);
        homeTitle = findViewById(R.id.title);
        aviButton = findViewById(R.id.camera);
        underCam = findViewById(R.id.underCamera);

        aviButton.setBackground(null);
        if (BitmapHelper.getInstance().getBitmap() != null) {
            underCam.setImageBitmap(BitmapHelper.getInstance().getBitmap());
        }

        MediaPlayer catSoundMP = MediaPlayer.create(this, R.raw.catsound);
        MediaPlayer cowSoundMP = MediaPlayer.create(this, R.raw.cowsound);
        MediaPlayer dogSoundMP = MediaPlayer.create(this, R.raw.dogsound);
        MediaPlayer donkeySoundMP = MediaPlayer.create(this, R.raw.donkeysound);
        MediaPlayer duckSoundMP = MediaPlayer.create(this, R.raw.ducksound);
        MediaPlayer elephantSoundMP = MediaPlayer.create(this, R.raw.elephantsound);
        MediaPlayer lionSoundMP = MediaPlayer.create(this, R.raw.lionsound);
        MediaPlayer monkeySoundMP = MediaPlayer.create(this, R.raw.monkeysound);
        MediaPlayer pigSoundMP = MediaPlayer.create(this, R.raw.pigsound);
        MediaPlayer mouseSoundMP = MediaPlayer.create(this, R.raw.mousesound);
        MediaPlayer sheepSoundMP = MediaPlayer.create(this, R.raw.sheepsound);

        Animal an1 = new Animal(R.drawable.cat, "Meow!", catSoundMP);
        Animal an2 = new Animal(R.drawable.cow, "Moo!", cowSoundMP);
        Animal an3 = new Animal(R.drawable.dog, "Roof!", dogSoundMP);
        Animal an4 = new Animal(R.drawable.donkey, "Hee-Haw!", donkeySoundMP);
        Animal an5 = new Animal(R.drawable.duck, "Quack!", duckSoundMP);
        Animal an6 = new Animal(R.drawable.elephant, "Baraag!", elephantSoundMP);
        Animal an7 = new Animal(R.drawable.lion, "Roar!", lionSoundMP);
        Animal an8 = new Animal(R.drawable.monkey, "Oo-Oo-Ah-Ah!", monkeySoundMP);
        Animal an9 = new Animal(R.drawable.pig, "Oink Oink!", pigSoundMP);
        Animal an10 = new Animal(R.drawable.mouse, "Squeak-Squeak!", mouseSoundMP);
        Animal an11 = new Animal(R.drawable.sheep, "Baaaa!", sheepSoundMP);

        animalArr = new Animal[]{
                an1, an2, an3, an4, an5, an6, an7, an8, an9, an10, an11
        };

        final ImageView correct = new ImageView(getApplicationContext());
        correct.setScaleX((float) .75);
        correct.setScaleY((float) .75);
        correct.setImageResource(R.drawable.correct);

        final ImageView wrong = new ImageView(getApplicationContext());
        wrong.setScaleX((float) .75);
        wrong.setScaleY((float) .75);
        wrong.setImageResource(R.drawable.wrong);

        backColor = findViewById(R.id.backLayer);


        image1 = findViewById(R.id.button1);
        image2 = findViewById(R.id.button2);
        image3 = findViewById(R.id.button3);
        image4 =  findViewById(R.id.button4);
        final Button goHome = findViewById(R.id.home);
        soundText = findViewById(R.id.soundID);
        score = 0;

        final Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);


        chooseRandomAnimal();
        homeTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moreGames(rightOrWrong);
                    }
                }, 200);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bouncetwo(v, done);
                moreGames(rightOrWrong);
            }
        });

        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fade(v);
                muteSwitcher();
            }
        });

        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bounce(v, goHome);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moreGames(rightOrWrong);
                    }
                }, 600);
            }
        });

        soundText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentAnimalSound.start();
                bounce(v, soundText);
            }
        });


        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound1.start();

                if (image1 == answer) {
                    bouncetwo(v, image1);
                    score++;
                    rightOrWrong = 1;

                    toast.setView(correct);
                } else {
                    backColor.setBackgroundColor(Color.rgb(234, 153, 153));
                    rightOrWrong = -1;

                    toast.setView(wrong);
                }
                toast.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (rightOrWrong < 0) {
                            backColor.setBackgroundResource(R.drawable.background_transition_fade);
                            TransitionDrawable transition = (TransitionDrawable) backColor.getBackground();
                            transition.startTransition(250);
                        } else {
                            chooseRandomAnimal();
                        }
                    }
                }, 2500);
            }
        });

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound2.start();

                if (image2 == answer) {
                    bouncetwo(v, image2);
                    score++;
                    rightOrWrong = 1;

                    toast.setView(correct);
                } else {
                    backColor.setBackgroundColor(Color.rgb(234, 153, 153));
                    toast.setView(wrong);
                    rightOrWrong = -1;
                }
                toast.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (rightOrWrong < 0) {
                            backColor.setBackgroundResource(R.drawable.background_transition_fade);
                            TransitionDrawable transition = (TransitionDrawable) backColor.getBackground();
                            transition.startTransition(250);
                        } else {
                            chooseRandomAnimal();
                        }
                    }
                }, 2500);
            }
        });

        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound3.start();

                if (image3 == answer) {
                    bouncetwo(v, image3);
                    score++;
                    rightOrWrong = 1;

                    toast.setView(correct);
                } else {
                    backColor.setBackgroundColor(Color.rgb(234, 153, 153));
                    toast.setView(wrong);
                    rightOrWrong = -1;
                }
                toast.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (rightOrWrong < 0) {
                            backColor.setBackgroundResource(R.drawable.background_transition_fade);
                            TransitionDrawable transition = (TransitionDrawable) backColor.getBackground();
                            transition.startTransition(250);
                        } else {
                            chooseRandomAnimal();
                        }
                    }
                }, 2500);
            }
        });

        image4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sound4.start();

                if (image4 == answer) {
                    bouncetwo(v, image4);
                    score++;
                    rightOrWrong = 1;

                    toast.setView(correct);
                } else {
                    backColor.setBackgroundColor(Color.rgb(234, 153, 153));
                    toast.setView(wrong);
                    rightOrWrong = -1;
                }
                toast.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (rightOrWrong < 0) {
                            backColor.setBackgroundResource(R.drawable.background_transition_fade);
                            TransitionDrawable transition = (TransitionDrawable) backColor.getBackground();
                            transition.startTransition(250);
                        } else {
                            chooseRandomAnimal();
                        }
                    }
                }, 2500);
            }
        });
    }


    public void chooseRandomAnimal() {
        int soundPick = (int) Math.floor(Math.random()*4);

        shuffleAnimals();
        image1.setBackgroundResource(animalArr[0].getImageNum());
        image2.setBackgroundResource(animalArr[1].getImageNum());
        image3.setBackgroundResource(animalArr[2].getImageNum());
        image4.setBackgroundResource(animalArr[3].getImageNum());
        sound1 = animalArr[0].getAnimalSoundMP();
        sound2 = animalArr[1].getAnimalSoundMP();
        sound3 = animalArr[2].getAnimalSoundMP();
        sound4 = animalArr[3].getAnimalSoundMP();
        currentAnimalSound = animalArr[soundPick].getAnimalSoundMP();
        soundText.setText(animalArr[soundPick].getSound());

        if (soundPick == 0) {
            answer = image1;
        } else if (soundPick == 1) {
            answer = image2;
        } else if (soundPick == 2) {
            answer = image3;
        } else {
            answer = image4;
        }
    }

    public void shuffleAnimals() {
        Collections.shuffle(Arrays.asList(animalArr));
    }

    private void moreGames(final int rightOrWrong) {
        if (rightOrWrong != 0) {
            dialog.show();

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (score < 1) {
                        backToStart();
                    } else {
                        openResults();
                    }
                }
            });
        } else {
            if (score < 1) {
                backToStart();
            } else {
                openResults();
            }
        }
    }

    public void openResults() {
        Intent intent = new Intent(this, Results.class);
        intent.putExtra(FINAL_SCORE, score);
        intent.putExtra(LAST_MUTE, muted);

        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        startActivity(intent);
        finish();
    }

    public void backToStart() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(GAME_MUTE, muted);
        startActivity(intent);
        stopMusic();
        finish();
    }

    public void stopMusic() {
        stopService(new Intent(this, BackgroundMusic.class));
    }

    public void startMusic() {
        startService(new Intent(this, BackgroundMusic.class));
    }



    public void muteSwitcher() {
        if (muted) {
            musicButton.setBackgroundResource(R.drawable.volume);
            startService(new Intent(this, BackgroundMusic.class));
        } else {
            musicButton.setBackgroundResource(R.drawable.muted);
            stopMusic();
        }
        muted = !muted;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moreGames(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!muted) {
            stopMusic();
            startMusic();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isApplicationSentToBackground(this)) {
            stopMusic();
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

    public void bouncetwo(View view, Button b) {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.bouncetwo);
        MyBounce bouncy = new MyBounce(.2, 40);
        animation.setInterpolator(bouncy);
        b.startAnimation(animation);
    }

    public void fade(View view) {
        Button button = musicButton;
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade);
        button.startAnimation(animation);
    }

    private int getNavigationBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight)
            return realHeight - usableHeight;
        else
            return 0;
    }
}