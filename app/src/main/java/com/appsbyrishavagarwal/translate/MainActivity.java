package com.appsbyrishavagarwal.translate;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.appsbyrishavagarwal.translate.Translate.mInterstitialAd;
import static com.appsbyrishavagarwal.translate.Translate.showToast;

public class MainActivity extends AppCompatActivity {

    SyncedAdapter syncedAdapter;

    private static final int RC_SIGN_IN = 1000;

    public static FirebaseAuth firebaseAuth;
    public static FirebaseAuth.AuthStateListener authStateListener;
    public static ChildEventListener childEventListener;

    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(pager);
        pager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setSelectedTabIndicatorHeight(8);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#ffffff"));

        LinearLayout linearLayout1 = new LinearLayout(this);
        ImageView imageView1 = new ImageView(this);
        imageView1.setPadding(35, 35, 35, 35);
        imageView1.setImageResource(R.drawable.tab_translate);
        linearLayout1.addView(imageView1);
        tabLayout.getTabAt(0).setCustomView(linearLayout1);

        LinearLayout linearLayout2 = new LinearLayout(this);
        ImageView imageView2 = new ImageView(this);
        imageView2.setPadding(35, 35, 35, 35);
        imageView2.setImageResource(R.drawable.tab_history);
        linearLayout2.addView(imageView2);
        tabLayout.getTabAt(1).setCustomView(linearLayout2);

        LinearLayout linearLayout3 = new LinearLayout(this);
        ImageView imageView3 = new ImageView(this);
        imageView3.setPadding(40, 40, 40, 40);
        imageView3.setImageResource(R.drawable.tab_bokmarks);
        linearLayout3.addView(imageView3);
        tabLayout.getTabAt(2).setCustomView(linearLayout3);

        List<Translation> list = new LinkedList<>();
        syncedAdapter = new SyncedAdapter(this, android.R.layout.activity_list_item, list);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            databaseReference = firebaseDatabase.getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Translations");

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    databaseReference = firebaseDatabase.getReference().child(user.getUid()).child("Translations");
                    if (childEventListener == null) {
                        childEventListener = new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                Translation translation = dataSnapshot.getValue(Translation.class);
                                syncedAdapter.add(translation);
                                Log.d("Added", translation.getFrom_lang() + "->" + translation.getTo_lang());
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        };
                        databaseReference.addChildEventListener(childEventListener);
                    }
                } else {
                    syncedAdapter.clear();
                    if (childEventListener != null) {
                        databaseReference.removeEventListener(childEventListener);
                        childEventListener = null;
                    }
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_CANCELED) {
                showToast(getString(R.string.sign_in_cancelled), this);
            } else if (resultCode == RESULT_OK) {
                showToast(getString(R.string.signed_in), this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (childEventListener != null) {
            databaseReference.removeEventListener(childEventListener);
            childEventListener = null;
        }
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private void setupViewPager(ViewPager pager) {
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new Translate(), getString(R.string.app_name));
        adapter.addFrag(new HistoryFragment(), getString(R.string.history));
        adapter.addFrag(new BookmarkFragment(), getString(R.string.bookmarks));
        pager.setAdapter(adapter);
    }

    //When back button is pressed, perform following tasks
    @Override
    public void onBackPressed() {
        //If interstitial ad is loaded, show it
        if (mInterstitialAd != null && mInterstitialAd.isLoaded())
            mInterstitialAd.show();

        //Building confirmation box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_exit));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            //User clicked "Yes" button, finish the activity
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "No" button, just dismiss the box
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Create and inflate(show) menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_translate, menu);
        return true;
    }

    //Handle menu options click events
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String appPackageName;
        switch (item.getItemId()) {
            case R.id.menu_share:       //Share apps link
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_TEXT, "Hey, check out this awesome Language Translator made by Rishav Agarwal-\n\nhttps://play.google.com/store/apps/details?id=" + getPackageName());
                share.setType("text/plain");
                startActivity(share);
                return true;
            case R.id.menu_rate:        //Open app page in google play store (For rating)
                appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    //If Google Play Store is installed, open in Play Store
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    //Else, open in browser
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                return true;
            case R.id.menu_more_apps:   //Open my developer page
                try {
                    //If Google Play Store is installed, open in Play Store
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=5607922630024317590")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=5607922630024317590")));
                }
                return true;
            case R.id.menu_pro:         //Open pro app page in play store
                appPackageName = getPackageName();
                try {
                    //If Google Play Store is installed, open in Play Store
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName + "_pro")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    //Else, open in browser
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName + "_pro")));
                }
                return true;
            case R.id.menu_about:       //Pops up a dialog box which shows info about app and me
                CustomDialogAbout customDialogAbout = new CustomDialogAbout(this);
                customDialogAbout.show();
                return true;
            case R.id.menu_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
            case R.id.menu_sign:
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    startActivityForResult(AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(true)
                                    .setLogo(R.drawable.translate_logo)
                                    .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                } else {//Building confirmation box
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Are you sure you want to sign out?");
                    builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        //User clicked "Yes" button, sign out
                        public void onClick(DialogInterface dialog, int id) {
                            AuthUI.getInstance().signOut(MainActivity.this);
                            showToast("Signed Out", MainActivity.this);
                        }
                    });
                    builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked the "No" button, just dismiss the box
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    });

                    // Create and show the AlertDialog
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                return true;
            case R.id.menu_view_synced_translations:
                CustomDialogSyncedTranslations customDialogSyncedTranslations = new CustomDialogSyncedTranslations(MainActivity.this);
                customDialogSyncedTranslations.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                customDialogSyncedTranslations.setTitle(getString(R.string.synced_translations));
                customDialogSyncedTranslations.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menu_sign);
        if (firebaseAuth.getCurrentUser() == null)
            menuItem.setTitle(getString(R.string.sign_in));
        else
            menuItem.setTitle(getString(R.string.sign_out));

        if (firebaseAuth.getCurrentUser() != null) {
            menuItem = menu.findItem(R.id.menu_view_synced_translations);
            menuItem.setVisible(true);
        } else {
            menuItem = menu.findItem(R.id.menu_view_synced_translations);
            menuItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            return mFragmentList.get(pos);
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    //Class to show the "about" dialog box
    private class CustomDialogAbout extends Dialog {

        Button bAboutOk;
        TextView tvYandex;

        CustomDialogAbout(Activity activity) {
            super(activity);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.about);
            bAboutOk = findViewById(R.id.but_about_ok);
            tvYandex = findViewById(R.id.tv_yandex_powered);

            //Dismiss the dialog box when "OK" is clicked
            bAboutOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            //Open the Yandex link when "Powered by..." is clicked
            tvYandex.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent yandex = new Intent(Intent.ACTION_VIEW);
                    yandex.setData(Uri.parse("http://translate.yandex.com/"));
                    startActivity(yandex);
                }
            });
        }
    }

    private class CustomDialogSyncedTranslations extends Dialog {

        ListView lvSynced;

        CustomDialogSyncedTranslations(Activity activity) {
            super(activity);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.synced_translation);

            lvSynced = (ListView) findViewById(R.id.lv_synced_history);
            lvSynced.setAdapter(syncedAdapter);
            lvSynced.setEmptyView(findViewById(R.id.empty_sync));
        }
    }
}
