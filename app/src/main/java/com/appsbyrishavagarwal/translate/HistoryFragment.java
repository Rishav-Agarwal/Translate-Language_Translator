package com.appsbyrishavagarwal.translate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;

public class HistoryFragment extends Fragment {

    ListView lvTranslations;

    public static HistoryAdapter historyAdapter;
    HistoryDatabaseHandler historyDatabaseHandler;

    List<Translation> translationList;

    AdView adView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lvTranslations = (ListView) getView().findViewById(R.id.lv_history);

        historyDatabaseHandler = new HistoryDatabaseHandler(getContext());

        translationList = historyDatabaseHandler.getAllTranslations();

        historyAdapter = new HistoryAdapter(getContext(), android.R.layout.activity_list_item, translationList);
        lvTranslations.setAdapter(historyAdapter);
        lvTranslations.setEmptyView(getView().findViewById(R.id.empty_history));
    }

    //Function to load and display banner ad and load interstitial ad
    public void loadBannerAd() {
        MobileAds.initialize(getContext(), "ca-app-pub-2751644825615773~3794936645");
        adView = (AdView) getView().findViewById(R.id.ad_history_banner);
        adView.setVisibility(View.VISIBLE);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
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
