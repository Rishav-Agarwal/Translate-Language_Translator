package me.rishavagarwal.translate;

import android.support.v4.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.support.v4.content.AsyncTaskLoader;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static me.rishavagarwal.translate.MainActivity.firebaseAuth;

public class Translate extends Fragment implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    HistoryDatabaseHandler historyDatabaseHandler;
    BookmarkDatabaseHandler bookmarkDatabaseHandler;

    Translation translation;

    static final String PARAM_TEXT = "text";
    static final String PARAM_KEY = "key";
    static final String PARAM_LANG = "lang";
    static final String TRANSLATE_API_KEY = APIKeys.getTranslateApiKey();
    static final String GET_TRANSLATION_URL = APIKeys.getGetTranslationUrl();
    static View layout;
    static String TRANSLATION_URL;
    static ArrayList<String[]> languagesWithCode;
    static LayoutInflater inflater;
    static String[][] langWithCodeOff;
    String LANGUAGE;
    SearchableSpinner spinFromLang, spinToLang;
    ArrayAdapter<String> langsTo, langsFrom;
    ArrayList<String> langTo, langFrom, langCodes;
    Button butMicInput, butTranslate, butListenInput, butListenOutput, butClear, butCopy, butShare, butZoom, butBookmark;
    EditText etInput, etOutput;
    Toolbar myToolbar;
    TextToSpeech tts;
    ProgressBar progressBar;
    LoaderManager.LoaderCallbacks<String[]> translateLoader;

    //Function which displays customized toast message
    public static void showToast(String message, Context context) {
        TextView tv = layout.findViewById(R.id.tv_toast);
        tv.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 0, 185);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_translate, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        historyDatabaseHandler = new HistoryDatabaseHandler(getContext());
        bookmarkDatabaseHandler = new BookmarkDatabaseHandler(getContext());

        setLangsetAdapterSpinner();         //Mainly set adapters to the spinners

        setupSharedPreferences();

        setLangCode();

        initializeReferences();             //Calls function which initializes reference to the widgets
        setOnClickListeners();              //Calls function which sets OnClickListener for them
        setAdapters();                      //Set empty adapters to spinners for languages

        setFilledAdapter();

        inflater = getActivity().getLayoutInflater();     //Inflating layout for custom toast
        layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) getView().findViewById(R.id.toast_view_group));

        initLoaderTranslator();             //Just initializes the translator loader[1]

        editTextPropsAndListener();         //Function to set properties and listener for better user-experience

        spinnerChangeListener();             //Function to respond to spinner change
    }

    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        setAppLanguage(sharedPreferences);

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
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

    //When spinner is changed, set visibility of mic_input and listen_input buttons according to availability
    private void spinnerChangeListener() {
        spinFromLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //If spinner selection is changed, perform these actions...
                Log.i("Item selected - ", i + "");
                if (languagesWithCode.get(i).length < 4)            //Voice recognition is available for the language
                    butMicInput.setVisibility(View.INVISIBLE);
                else                                                //Voice recognition is NOT available for the language
                    butMicInput.setVisibility(View.VISIBLE);

                //TTS for input text
                if (languagesWithCode.get(i).length >= 3)           //Text-to-Speech is NOT available for the language
                    butListenInput.setVisibility(View.VISIBLE);
                else
                    butListenInput.setVisibility(View.INVISIBLE);   //TTS is available for the language
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    //Function to set properties and listener for better user-experience
    private void editTextPropsAndListener() {
        //Clear focus for EditTexts and restrict user to edit the output text
        etOutput.clearFocus();
        etInput.clearFocus();
        etOutput.setKeyListener(null);

        //Add EditText(input) change listener for better user-experience
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().equals("") || charSequence.toString().trim().equals("\0")) {
                    butListenInput.setVisibility(View.INVISIBLE);
                    butClear.setVisibility(View.INVISIBLE);
                    butCopy.setVisibility(View.INVISIBLE);
                    butShare.setVisibility(View.INVISIBLE);
                    etOutput.setVisibility(View.INVISIBLE);
                    butListenOutput.setVisibility(View.INVISIBLE);
                    butZoom.setVisibility(View.INVISIBLE);
                    butBookmark.setVisibility(View.INVISIBLE);
                } else {
                    butListenInput.setVisibility(View.VISIBLE);
                    butClear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    //When activity is destroyed, clear the spinner adapters
    @Override
    public void onDestroy() {
        Log.i("onDestroy", "Destroyed!");
        langsFrom.clear();
        langsTo.clear();
        langCodes.clear();
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    //When activity is paused, stop tts engine and destroy the loader
    @Override
    public void onPause() {
        Log.i("onPause-", "Pausing...");
        //If TTS engine is initialized, stop it
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        //If Translator loader is initialized, destroy it
        if (getLoaderManager().getLoader(1) != null)
            getLoaderManager().destroyLoader(1);
        super.onPause();
    }

    /* **************************************************************************************************
     * Function to store languages in ArrayList and set languages adapters to the spinners
     * First column specifies the language code in Yandex servers
     * Second column specifies the language name
     * Third column specifies the language code in Google servers if TTS for that language is avialble,
     *          else is left null/blank
     * Fourth column contains either "1" or is not present -
     *          "1" --> Voice recognition is available for that language
     *          else --> Not available
     ****************************************************************************************************/
    private void setLangsetAdapterSpinner() {
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
    }

    private void setLangCode() {
        //Now put it into ArrayList and ArrayAdapter
        languagesWithCode = new ArrayList<>();
        langFrom = new ArrayList<>();
        langTo = new ArrayList<>();
        for (int i = 0; i < langWithCodeOff.length; ++i) {
            langCodes.add(i, langWithCodeOff[i][0]);
            languagesWithCode.add(i, langWithCodeOff[i]);
            langFrom.add(i, languagesWithCode.get(i)[1]);
            if (i > 0) {
                langTo.add(i - 1, languagesWithCode.get(i)[1]);
            }
        }
    }

    private void setFilledAdapter() {
        langsFrom.addAll(langFrom);
        spinFromLang.setAdapter(langsFrom);
        langsTo.addAll(langTo);
        spinToLang.setAdapter(langsTo);
    }

    //Start listening to voice input
    private void startListening() {
        String lang = languagesWithCode.get(spinFromLang.getSelectedItemPosition())[2];
        Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        voice.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, lang);
        voice.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!");
        try {
            startActivityForResult(voice, 100);
        } catch (ActivityNotFoundException anfe) {
            showToast(getString(R.string.not_supported), getContext());
        }
    }

    //Handle result from voice recognition
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Result(" + requestCode + ")", String.valueOf(resultCode));
        if (requestCode == 100 && resultCode == RESULT_OK) {
            //When we get result display it inside the input text (EditText)
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            etInput.setText(results.get(0));
            etInput.setSelection(etInput.getText().length());
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //Returns language to be translated (from_lang)
    private String getFromLang() {
        return languagesWithCode.get(spinFromLang.getSelectedItemPosition())[2];
    }

    //Returns language to which input is converted (to_lang)
    private String getToLangCode() {
        return languagesWithCode.get(spinToLang.getSelectedItemPosition() + 1)[2];
    }

    //Function to load and display banner ad
    public void loadBannerAd() {
        LinearLayout adLayout = (LinearLayout) getView().findViewById(R.id.ad_layout);
        if (adLayout.getChildCount() == 0) {
            MobileAds.initialize(getContext(), APIKeys.getAdmobAppId());
            AdView adView = new AdView(getActivity());
            adView.setVisibility(View.VISIBLE);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(APIKeys.getTranslateBannerAdId());
            adLayout.addView(adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
        }
    }

    //Function to initialize and start loader to Translate
    private void initLoaderTranslator() {
        translateLoader =
                new LoaderManager.LoaderCallbacks<String[]>() {

                    @Override
                    public Loader<String[]> onCreateLoader(int i, Bundle bundle) {
                        return new TranslateLoader(getContext());
                    }

                    @Override
                    public void onLoadFinished(Loader<String[]> loader, String s[]) {
                        progressBar.setVisibility(View.INVISIBLE);

                        //When loading is finished (We get our translation), print the translation
                        Log.i("onLoadFinished", "Loading Finished!");
                        if (s == null) {
                            showToast(getString(R.string.internet_error), getContext());
                            return;
                        }

                        switch (s[0]) {
                            case "200":            //If no error occurred (Code=200), proceed

                                //If no error, there must be some output
                                etOutput.setVisibility(View.VISIBLE);
                                etOutput.setText(s[2]);

                                butZoom.setVisibility(View.VISIBLE);
                                butBookmark.setVisibility(View.VISIBLE);

                                //If text-to-speech is available for the output language, show listen output button
                                if (languagesWithCode.get(spinToLang.getSelectedItemPosition() + 1).length >= 3)
                                    butListenOutput.setVisibility(View.VISIBLE);
                                else
                                    butListenOutput.setVisibility(View.INVISIBLE);

                                //If no error, show copy and share button to allow user to copy and share the translated data
                                butCopy.setVisibility(View.VISIBLE);
                                butShare.setVisibility(View.VISIBLE);

                                //If speech for the output language is available, set tts language to it and speak
                                if (languagesWithCode.get(spinToLang.getSelectedItemPosition() + 1).length >= 3) {
                                    //Set speaking language to the selected output language
                                    tts.setLanguage(new Locale(getToLangCode()));
                                    Log.i("Speaking output-", etOutput.getText().toString());
                                    if (Build.VERSION.SDK_INT >= 21)
                                        tts.speak(etOutput.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                                    else
                                        tts.speak(etOutput.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                                }

                                if (spinFromLang.getSelectedItemPosition() == 0) {
                                    String autoLang = s[1].split("-")[0];
                                    for (int i = 1; i < languagesWithCode.size(); ++i) {
                                        if (languagesWithCode.get(i)[0].equals(autoLang)) {
                                            Log.i("Found -->", i + "");
                                            spinFromLang.setSelection(i);
                                            break;
                                        }
                                    }
                                }

                                translation = new Translation(etInput.getText().toString(), spinFromLang.getSelectedItem().toString(), etOutput.getText().toString(), spinToLang.getSelectedItem().toString(), Calendar.getInstance().getTime().toString());

                                historyDatabaseHandler.addTranslation(translation);
                                HistoryFragment.historyAdapter.clear();
                                HistoryFragment.historyAdapter.addAll(historyDatabaseHandler.getAllTranslations());

                                if (firebaseAuth.getCurrentUser() != null)
                                    MainActivity.databaseReference.push().setValue(translation);
                                break;
                            case "413":         //When text is too big (code=413)
                                showToast(getString(R.string.too_big), getContext());
                                break;
                            case "422":         //When the text couldn't be translated (code=422)
                                showToast(getString(R.string.couldnt_translate), getContext());
                                break;
                            case "501":         //When translation direction is not supported (code=501)
                                showToast(getString(R.string.dir_no_support), getContext());
                                break;
                            default:
                                /* ***************************************************************
                                 * 4 possibilities -
                                 * -> code=401 --> Invalid API key
                                 * -> code=402 --> API key blocked
                                 * -> code=404 --> Daily limit exceeded
                                 * -> Other code --> Code is unknown as of Sunday, June 25, 2017
                                 *****************************************************************/
                                showToast(getString(R.string.error_report) + s[0], getContext());
                        }
                    }

                    @Override
                    public void onLoaderReset(Loader<String[]> loader) {
                        Log.i("onLoaderReset", "Resetting Loader...");
                    }
                };
    }

    //Setting adapters to spinners for languages
    private void setAdapters() {
        langsTo = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1);        //ArrayList of result languages
        langsFrom = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1);      //ArrayList of input languages
        spinFromLang.setAdapter(langsFrom);                 //Set ArrayAdapter for input lang (Spinner)
        spinToLang.setAdapter(langsTo);                     //Set ArrayAdapter for resultant lang (Spinner)
    }

    //Initialize reference to widgets
    private void initializeReferences() {
        spinFromLang = (SearchableSpinner) getView().findViewById(R.id.spinner_from_lang);
        spinToLang = (SearchableSpinner) getView().findViewById(R.id.spinner_to_lang);

        //Set title for Searchable Spinners
        spinFromLang.setTitle("From");
        spinToLang.setTitle("To");

        butMicInput = (Button) getView().findViewById(R.id.but_mic_input);
        etInput = (EditText) getView().findViewById(R.id.et_input_text);
        myToolbar = (Toolbar) getView().findViewById(R.id.my_toolbar);
        butTranslate = (Button) getView().findViewById(R.id.but_translate);
        butListenOutput = (Button) getView().findViewById(R.id.but_listen_output);
        etOutput = (EditText) getView().findViewById(R.id.et_output);
        butZoom = (Button) getView().findViewById(R.id.but_zoom);
        butClear = (Button) getView().findViewById(R.id.but_clear);
        butListenInput = (Button) getView().findViewById(R.id.but_listen_input);
        butCopy = (Button) getView().findViewById(R.id.but_copy);
        butShare = (Button) getView().findViewById(R.id.but_share);
        progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
        butBookmark = (Button) getView().findViewById(R.id.but_bookmark);
    }

    //Function to set OnClickListeners
    private void setOnClickListeners() {
        butMicInput.setOnClickListener(this);
        butTranslate.setOnClickListener(this);
        butListenInput.setOnClickListener(this);
        butListenOutput.setOnClickListener(this);
        butClear.setOnClickListener(this);
        butCopy.setOnClickListener(this);
        butShare.setOnClickListener(this);
        butZoom.setOnClickListener(this);
        butBookmark.setOnClickListener(this);
    }

    //Function which responds on click
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //When mic is pressed for voice input
            case R.id.but_mic_input:
                etInput.setText("");
                etOutput.setText("");
                startListening();
                break;
            case R.id.but_translate:
                //Animate translate button
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.touch_release);
                butTranslate.startAnimation(animation);

                etOutput.setText("");
                //Start translation
                String input = etInput.getText().toString();
                if (input.equals("") || input.equals("\0")) {
                    showToast(getString(R.string.invalid_input), getContext());
                    break;
                }
                etOutput.setVisibility(View.INVISIBLE);
                butListenOutput.setVisibility(View.INVISIBLE);
                butCopy.setVisibility(View.INVISIBLE);
                butShare.setVisibility(View.INVISIBLE);
                butZoom.setVisibility(View.INVISIBLE);
                butBookmark.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                //Build "lang"(URL parameter)
                String fromLangCode = languagesWithCode.get(spinFromLang.getSelectedItemPosition())[0];
                String toLangCode = languagesWithCode.get(spinToLang.getSelectedItemPosition() + 1)[0];
                if (fromLangCode.equals("**")) {
                    LANGUAGE = toLangCode;
                } else {
                    LANGUAGE = fromLangCode + "-" + toLangCode;
                }

                Log.i("LangCode", LANGUAGE);

                //Build Uri
                Uri baseUri = Uri.parse(GET_TRANSLATION_URL);
                Uri uriBuilder = baseUri.buildUpon()
                        .appendQueryParameter(PARAM_KEY, TRANSLATE_API_KEY)
                        .appendQueryParameter(PARAM_TEXT, input)
                        .appendQueryParameter(PARAM_LANG, LANGUAGE)
                        .build();
                TRANSLATION_URL = uriBuilder.toString();
                if (getLoaderManager().getLoader(1) == null)
                    getLoaderManager().initLoader(1, null, translateLoader);       //Initializing loader[1]
                else
                    getLoaderManager().restartLoader(1, null, translateLoader);     //Restart if already present
                break;
            case R.id.but_listen_input:     //Listen input
                tts.setLanguage(new Locale(getFromLang()));
                Log.i("Speaking input-", etInput.getText().toString());
                if (Build.VERSION.SDK_INT >= 21)
                    tts.speak(etInput.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                else
                    tts.speak(etInput.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                break;
            case R.id.but_listen_output:    //Listen output
                tts.setLanguage(new Locale(getToLangCode()));
                Log.i("Speaking output-", etOutput.getText().toString());
                if (Build.VERSION.SDK_INT >= 21)
                    tts.speak(etOutput.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                else
                    tts.speak(etOutput.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                break;
            case R.id.but_clear:    //Clear input and output text
                etInput.setText("");
                etOutput.setText("");
                break;
            case R.id.but_copy:     //Copy output text
                String output = etOutput.getText().toString();
                if (output.equals("") || output.equals("\0")) {
                    showToast(getString(R.string.copy_no_text), getContext());
                    break;
                }
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Translated text", etOutput.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                showToast(getString(R.string.copied), getContext());
                break;
            case R.id.but_share:    //Share output text
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_TEXT, etOutput.getText().toString());
                share.setType("text/plain");
                startActivity(share);
                break;
            case R.id.but_zoom:             //Zoom the text to look bigger
                Intent zoom = new Intent(getContext(), ZoomedText.class);
                zoom.putExtra("TEXT", etOutput.getText().toString());
                startActivity(zoom);
                break;
            case R.id.but_bookmark:
                List<Translation> translationList = bookmarkDatabaseHandler.getAllBookmarks();

                for (int i = 0; i < translationList.size(); ++i) {
                    if (translation.getFrom_lang().equals(translationList.get(i).getFrom_lang()) && translation.getTo_lang().equals(translationList.get(i).getTo_lang()) && translation.getFrom_text().equals(translationList.get(i).getFrom_text()) && translation.getTo_text().equals(translationList.get(i).getTo_text())) {
                        bookmarkDatabaseHandler.deleteBookmark(translationList.get(i));
                    }
                }

                bookmarkDatabaseHandler.addBookmark(translation);
                BookmarkFragment.bookmarkAdapter.clear();
                BookmarkFragment.bookmarkAdapter.addAll(bookmarkDatabaseHandler.getAllBookmarks());
                showToast(getString(R.string.bookmark_added), getContext());
                break;
        }
    }

    @Override
    public void onResume() {
        Log.i("onResume", "Resuming...");
        super.onResume();
        //Initialize tts engine as soon as activity resumes/starts/is created
        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    tts.setLanguage(new Locale("en_IN"));
                    Log.i("Initializing TTS...", "TTS initialized!");
                }
            }
        });

        setupSharedPreferences();

        //Load ads as soon as the activity starts or resumes if network is available
        if (isNetworkAvailable()) {
            Log.d("Loading Ads", "Loading ads!");
            loadBannerAd();
        }
    }

    //Function to return to return status of network connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        setAppLanguage(sharedPreferences);
        getActivity().finish();
        Intent i = getActivity().getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }

    private static class TranslateLoader extends AsyncTaskLoader<String[]> {

        String mUrl;
        Context mContext;
        ParseJsonTranslate parseJsonTranslate = new ParseJsonTranslate(getContext());
        private String[] translated;

        TranslateLoader(Context context) {
            super(context);
            mContext = context;
            Log.i("Constructor", "Translate Constructor");
        }

        @Override
        public String[] loadInBackground() {
            Log.i("loadInBackground", "Loading in background...");
            if (mUrl == null)
                return null;

            URL url = createUrl(mUrl);      //Create URL

            String jsonResponse;
            try {
                jsonResponse = makeHttpRequest(url);    //Try making Http request
            } catch (IOException e) {           //If error iin making request, show error
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(mContext.getString(R.string.internet_error), mContext);
                    }
                });
                return translated;
            }

            translated = parseJsonTranslate.getTranslated(jsonResponse);    //Parse the JSON response received from the URL
            return translated;
        }

        @Override
        protected void onStartLoading() {
            Log.i("onStartLoading", "Loading started...");
            mUrl = TRANSLATION_URL;
            forceLoad();
        }

        //Function to make Http request
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(10000);
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
                if (inputStream != null)
                    inputStream.close();
            }
            return jsonResponse;
        }

        //Function to read the response
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        //Function to create the URL
        private URL createUrl(String mUrl) {
            URL url = null;
            try {
                url = new URL(mUrl);
            } catch (MalformedURLException e) {
                showToast(getContext().getString(R.string.couldnt_find_info), mContext);
            }
            return url;
        }
    }
}