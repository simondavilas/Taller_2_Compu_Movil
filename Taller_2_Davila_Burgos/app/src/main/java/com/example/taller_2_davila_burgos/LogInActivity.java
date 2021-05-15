package com.example.taller_2_davila_burgos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LogInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        Button botonIniciarS = (Button) findViewById(R.id.buttonIniciar);

        botonIniciarS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iniciarS = new Intent(v.getContext(), HomeActivity.class);
                startActivity(iniciarS);
            }
        });

    }
}