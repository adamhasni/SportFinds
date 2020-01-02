package com.example.sportfinds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class BadmintonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badminton);

        Button btnFindCourt = (Button) findViewById(R.id.find_court1);

        btnFindCourt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BadmintonActivity.this, BadmintonMapsActivity.class));
            }
        });

        Button btnFindMatch = (Button) findViewById(R.id.find_match1);

        btnFindMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BadmintonActivity.this, PostActivity.class));
            }
        });
    }
}
