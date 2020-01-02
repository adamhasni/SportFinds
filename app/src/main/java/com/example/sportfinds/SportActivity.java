package com.example.sportfinds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class SportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport);


        ImageButton btnFutsal = (ImageButton) findViewById(R.id.futsal);
        ImageButton btnBadminton = (ImageButton) findViewById(R.id.badminton);
        ImageButton btnTakraw = (ImageButton) findViewById(R.id.takraw);


        btnFutsal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SportActivity.this, FutsalActivity.class));
            }
        });

        btnBadminton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SportActivity.this, BadmintonActivity.class));
            }
        });

        btnTakraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SportActivity.this, TakrawActivity.class));
            }
        });
    }
}
