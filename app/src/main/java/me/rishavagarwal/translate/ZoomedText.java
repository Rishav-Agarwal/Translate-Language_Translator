package me.rishavagarwal.translate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ZoomedText extends AppCompatActivity {

    TextView tvZoomedText;
    Button butClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoomed_text);

        tvZoomedText = (TextView) findViewById(R.id.tv_zoomed_text);
        butClose = (Button) findViewById(R.id.but_close);

        String text = getIntent().getStringExtra("TEXT");

        tvZoomedText.setText(text);

        butClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeFullscreen();
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
}
