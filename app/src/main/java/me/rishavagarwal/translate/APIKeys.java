package me.rishavagarwal.translate;

public class APIKeys {
    private static final String TRANSLATE_API_KEY = "trnsl.1.1.20180605T144143Z.dada7ec0531f347e.ecb6e9b556490320dd437b2bb67f9d656b75f294";
    private static final String GET_TRANSLATION_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate";

    private static final String ADMOB_APP_ID = "ca-app-pub-2751644825615773~4441331797";
    private static final String TRANSLATE_BANNER_AD_ID = "ca-app-pub-2751644825615773/1723669116";
    private static final String HISTORY_BANNER_AD_ID = "ca-app-pub-2751644825615773/2197192274";
    private static final String BOOKMARK_BANNER_AD_ID = "ca-app-pub-2751644825615773/6684776785";

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