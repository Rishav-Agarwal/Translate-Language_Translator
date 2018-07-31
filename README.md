# Translate-Language_Translator
Translate allows you to translate text and phrases between 90+ languages. It supports voice input and speech output features.

### To use the app, you need to add file named `APIKeys.java` defined as below:

```java
package me.rishavagarwal.translate;

public class APIKeys {
    private static final String TRANSLATE_API_KEY = <YOUR_YANDEX_TRANSLATE_API_KEY>;
    private static final String GET_TRANSLATION_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate";

    private static final String ADMOB_APP_ID = <YOUR_ADMOD_PUBLISHER_ID>;
    private static final String TRANSLATE_BANNER_AD_ID = <YOUR_ADMOD_TRANSLATE_BANNER_ID>;
    private static final String HISTORY_BANNER_AD_ID = <YOUR_ADMOD_HISORY_BANNER_ID>;
    private static final String BOOKMARK_BANNER_AD_ID = <YOUR_ADMOD_BOOKMARK_BANNER_ID>;

    public static String getTranslateApiKey() {
        return TRANSLATE_API_KEY;
    }

    public static String getGetTranslationUrl() {
        return GET_TRANSLATION_URL;
    }

    public static String getAdmobAppId() {
        return ADMOB_APP_ID;
    }

    public static String getTranslateBannerAdId() {
        return TRANSLATE_BANNER_AD_ID;
    }

    public static String getHistoryBannerAdId() {
        return HISTORY_BANNER_AD_ID;
    }

    public static String getBookmarkBannerAdId() {
        return BOOKMARK_BANNER_AD_ID;
    }
}
```
