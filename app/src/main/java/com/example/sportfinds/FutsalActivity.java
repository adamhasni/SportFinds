package com.example.sportfinds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FutsalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_futsal);

        Button btnFindCourt = (Button)findViewById(R.id.find_court);

        btnFindCourt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FutsalActivity.this, FutsalMapsActivity.class));
            }
        });

        Button btnFindMatch = (Button)findViewById(R.id.find_match);

        btnFindMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FutsalActivity.this, PostActivity.class));
            }
        });

    }
}
