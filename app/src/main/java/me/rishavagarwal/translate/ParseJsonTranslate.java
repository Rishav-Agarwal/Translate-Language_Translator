package me.rishavagarwal.translate;

/*
Class to parse JSON object received on translation
*/

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

class ParseJsonTranslate {

    private Context mContext;

    ParseJsonTranslate(Context context) {
        mContext = context;
    }

    //Function to return parsed(JSON) data
    String[] getTranslated(String JSON) {
        String result = "";
        String code = "";
        String lang = "";
        try {
            JSONObject translatedObject = new JSONObject(JSON);
            result = translatedObject.getJSONArray("text").get(0).toString();
            Log.i("Result -", result);
            lang = translatedObject.getString("lang");
            Log.i("Lang", lang);
            code = translatedObject.getString("code");
        } catch (JSONException e) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Translate.showToast(mContext.getString(R.string.internal_error), mContext);
                }
            });
        }
        return new String[]{code, lang, result};
    }
}