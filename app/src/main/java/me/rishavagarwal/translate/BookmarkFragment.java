package me.rishavagarwal.translate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;

public class BookmarkFragment extends Fragment {

    ListView lvTranslations;

    public static BookmarkAdapter bookmarkAdapter;
    BookmarkDatabaseHandler bookmarkDatabaseHandler;

    List<Translation> translationList;

    AdView adView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lvTranslations = (ListView) getView().findViewById(R.id.lv_bookmarks);

        bookmarkDatabaseHandler = new BookmarkDatabaseHandler(getContext());

        translationList = bookmarkDatabaseHandler.getAllBookmarks();

        bookmarkAdapter = new BookmarkAdapter(getContext(), android.R.layout.activity_list_item, translationList);
        lvTranslations.setAdapter(bookmarkAdapter);
        lvTranslations.setEmptyView(getView().findViewById(R.id.empty_bookmark));
    }

    //Function to load and display banner ad and load interstitial ad
    public void loadBannerAd() {
        LinearLayout adLayout = (LinearLayout) getView().findViewById(R.id.ad_layout);
        if (adLayout.getChildCount() == 0) {
            MobileAds.initialize(getContext(), APIKeys.getAdmobAppId());
            AdView adView = new AdView(getActivity());
            adView.setVisibility(View.VISIBLE);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(APIKeys.getBookmarkBannerAdId());
            adLayout.addView(adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isNetworkAvailable()) {
            loadBannerAd();
        }
    }
}