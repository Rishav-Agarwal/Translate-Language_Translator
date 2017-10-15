package com.appsbyrishavagarwal.translate;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class Welcome extends Activity implements View.OnTouchListener {

    public LinearLayout llTranslate;
    public ImageView ivMultiLang;
    public TextView ivMyName;
    ArrayList<String> langCodes;
    String[][] langWithCodeOff;
    Handler handler = new Handler();
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        initializeWidgets();        //Linking variables to widgets created in xml

        setOnTouchListeners();      //Setting onTouch listeners to respond to touches

        multiMapAnimation();        //Animate the multi-language map!

        myNameAnimation();          //Animate my name at bottom!

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent translate = new Intent(Welcome.this, MainActivity.class);
                startActivity(translate);
                finish();
            }
        };
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 4000);
    }

    //As soon as the activity starts, make it fullscreen
    @Override
    protected void onStart() {
        super.onStart();
        makeFullscreen();

        changeLanguage();
    }

    private void changeLanguage() {
        langWithCodeOff = new String[][]{
                {"**", getString(R.string.auto_detect), "", "1"},
                {"af", getString(R.string.Afrikaans)},
                {"sq", getString(R.string.Albanian)},
                {"am", getString(R.string.Amharic)},
                {"ar", getString(R.string.Arabic)},
                {"hy", getString(R.string.Armenian)},
                {"az", getString(R.string.Azerbaijani)},
                {"ba", getString(R.string.Bashkir)},
                {"eu", getString(R.string.Basque)},
                {"be", getString(R.string.Belarusian)},
                {"bn", getString(R.string.Bengali), "bn_IN"},     //NO VOICE INPUT
                {"bs", getString(R.string.Bosnian)},
                {"bg", getString(R.string.Bulgarian)},
                {"my", getString(R.string.Burmese)},
                {"ca", getString(R.string.Catalan)},
                {"ceb", getString(R.string.Cebuano)},
                {"zh", getString(R.string.Chinese), "zh_CN", "1"},
                {"hr", getString(R.string.Croatian)},
                {"cs", getString(R.string.Czech), "cs_CZ", "1"},
                {"da", getString(R.string.Danish), "da_DK", "1"},
                {"nl", getString(R.string.Dutch), "nl_NL", "1"},
                {"en", getString(R.string.English), "en_IN", "1"},
                {"eo", getString(R.string.Esperanto)},
                {"et", getString(R.string.Estonian)},
                {"fi", getString(R.string.Finnish), "fi_FI", "1"},
                {"fr", getString(R.string.French), "fr_FR", "1"},
                {"gl", getString(R.string.Galician)},
                {"ka", getString(R.string.Georgian)},
                {"de", getString(R.string.German), "de_DE", "1"},
                {"el", getString(R.string.Greek)},
                {"gu", getString(R.string.Gujarati)},
                {"ht", getString(R.string.Haitian)},
                {"he", getString(R.string.Hebrew)},
                {"mrj", getString(R.string.Hill_Mari)},
                {"hi", getString(R.string.Hindi), "hi_IN", "1"},
                {"hu", getString(R.string.Hungarian), "hu_HU", "1"},
                {"is", getString(R.string.Icelandic)},
                {"id", getString(R.string.Indonesian), "in_ID", "1"},
                {"ga", getString(R.string.Irish)},
                {"it", getString(R.string.Italian), "it_IT", "1"},
                {"ja", getString(R.string.Japanese), "ja_JP", "1"},
                {"jv", getString(R.string.Javanese)},
                {"kn", getString(R.string.Kannada)},
                {"kk", getString(R.string.Kazakh)},
                {"km", getString(R.string.Khmer), "km_KH"},           //NO VOICE INPUT
                {"ko", getString(R.string.Korean), "ko_KR", "1"},
                {"ky", getString(R.string.Kyrgyz)},
                {"lo", getString(R.string.Lao)},
                {"la", getString(R.string.Latin)},
                {"lv", getString(R.string.Latvian)},
                {"lt", getString(R.string.Lithuanian)},
                {"lb", getString(R.string.Luxembourgish)},
                {"mk", getString(R.string.Macedonian)},
                {"mg", getString(R.string.Malagasy)},
                {"ms", getString(R.string.Malay)},
                {"ml", getString(R.string.Malayalam)},
                {"mt", getString(R.string.Maltese)},
                {"mi", getString(R.string.Maori)},
                {"mr", getString(R.string.Marathi)},
                {"mhr", getString(R.string.Mari)},
                {"mn", getString(R.string.Mongolian)},
                {"ne", getString(R.string.Nepali), "ne_NP"},          //NO VOICE INPUT
                {"no", getString(R.string.Norwegian), "nn_NO"},       //NO VOICE INPUT
                {"pap", getString(R.string.Papiamento)},
                {"fa", getString(R.string.Persian)},
                {"pl", getString(R.string.Polish), "pl_PL", "1"},
                {"pt", getString(R.string.Portuguese), "pt_BR", "1"},
                {"pa", getString(R.string.Punjabi)},
                {"ro", getString(R.string.Romanian)},
                {"ru", getString(R.string.Russian), "ru_RU", "1"},
                {"gd", getString(R.string.Scottish_Gaelic)},
                {"sr", getString(R.string.Serbian)},
                {"si", getString(R.string.Sinhala), "si_LK"},         //NO VOICE INPUT
                {"sk", getString(R.string.Slovak)},
                {"sl", getString(R.string.Slovenian)},
                {"es", getString(R.string.Spanish), "es_ES", "1"},
                {"su", getString(R.string.Sundanese)},
                {"sw", getString(R.string.Swahili)},
                {"sv", getString(R.string.Swedish), "sv_SE", "1"},
                {"tl", getString(R.string.Tagalog)},
                {"tg", getString(R.string.Tajik)},
                {"ta", getString(R.string.Tamil)},
                {"tt", getString(R.string.Tatar)},
                {"te", getString(R.string.Telugu)},
                {"th", getString(R.string.Thai), "th_TH", "1"},
                {"tr", getString(R.string.Turkish), "tr_TR", "1"},
                {"udm", getString(R.string.Udmurt)},
                {"uk", getString(R.string.Ukrainian), "uk_UA", "1"},
                {"ur", getString(R.string.Urdu)},
                {"uz", getString(R.string.Uzbek)},
                {"vi", getString(R.string.Vietnamese), "vi_VN", "1"},
                {"cy", getString(R.string.Welsh)},
                {"xh", getString(R.string.Xhosa)},
                {"yi", getString(R.string.Yiddish)}};


        langCodes = new ArrayList<>();
        for (int i = 0; i < langWithCodeOff.length; ++i) {
            langCodes.add(i, langWithCodeOff[i][0]);
        }

        setupSharedPreferences();
    }

    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setAppLanguage(sharedPreferences);
    }

    private void setAppLanguage(SharedPreferences sharedPreferences) {
        int langCode = langCodes.indexOf(sharedPreferences.getString(getString(R.string.key_pref_lang), getString(R.string.en)));

        Log.v(sharedPreferences.getString(getString(R.string.key_pref_lang), getString(R.string.en)), langCode + "");

        Resources res = this.getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= 17)
            conf.setLocale(new Locale(langCodes.get(langCode))); // API 17+ only.
        else
            conf.locale = new Locale(langCodes.get(langCode)); //if targeting lower versions
        res.updateConfiguration(conf, dm);
    }


    //Animating translate image and text
    private void translateAnimation() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_translate);
        llTranslate.setVisibility(View.VISIBLE);
        llTranslate.startAnimation(anim);
    }

    //Animating the multi-language map
    private void multiMapAnimation() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in_map);
        ivMultiLang.startAnimation(anim);

        //Animate translate image and text after all animations end
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Animate translate image and text after all animations end
                translateAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    //Animating my name!
    private void myNameAnimation() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.my_name_entry);
        ivMyName.startAnimation(anim);
    }

    //Function to make the activity fullscreen each time it starts
    public void makeFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION       //hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN            //hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    //Function to initialize the variable to store the ids of respective widgets
    public void initializeWidgets() {
        llTranslate = findViewById(R.id.ll_translate);
        ivMyName = findViewById(R.id.iv_my_name);
        ivMultiLang = findViewById(R.id.iv_multi_lang);
    }

    //Setting OnTouch Listeners to respond to touch
    public void setOnTouchListeners() {
        llTranslate.setOnTouchListener(this);
        ivMyName.setOnTouchListener(this);
    }

    //Implementing touch events
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id = view.getId();

        switch (id) {
            case R.id.ll_translate:
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {       //When translate icon is pressed
                    //If translate icon is pressed, scale to 0.8
                    llTranslatePressed();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {         //When translate icon is released
                    //If translate icon is released, scale to 1.0
                    handler.removeCallbacks(runnable);
                    llTranslateReleased();
                    Intent translate = new Intent(Welcome.this, MainActivity.class);
                    startActivity(translate);
                    finish();
                }
                return true;
            case R.id.iv_my_name:
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {       //When my name is pressed
                    //If name is pressed, scale to 0.8
                    ivMyNamePressed();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {         //When my name is released
                    //If name is released, scale to 1.0
                    ivMyNameReleased();
                }
                return true;
        }

        return false;
    }

    //Act when Translate icon is pressed
    private void llTranslatePressed() {
        Animation animTouch = AnimationUtils.loadAnimation(this, R.anim.touch);     //Loading the animation(touch)
        llTranslate.startAnimation(animTouch);          //Starting the animation
    }

    //Act when Translate icon is released
    private void llTranslateReleased() {
        Animation animTouch = AnimationUtils.loadAnimation(this, R.anim.touch_release);
        llTranslate.startAnimation(animTouch);
    }

    //Act when my name is pressed
    private void ivMyNamePressed() {
        Animation animTouch = AnimationUtils.loadAnimation(this, R.anim.touch);
        ivMyName.startAnimation(animTouch);
    }

    //Act when my name is released
    private void ivMyNameReleased() {
        Animation animTouch = AnimationUtils.loadAnimation(this, R.anim.touch_release);
        ivMyName.startAnimation(animTouch);
    }
}