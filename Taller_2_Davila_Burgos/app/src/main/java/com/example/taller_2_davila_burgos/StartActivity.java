package com.example.taller_2_davila_burgos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button botonLogIn = (Button) findViewById(R.id.buttonLogIn);
        Button botonRegister = (Button) findViewById(R.id.buttonRegister);

        botonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logIn = new Intent(v.getContext(), LogInActivity.class);
                startActivity(logIn);

            }
        });

        botonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(v.getContext(), RegisterActivity.class);
                startActivity(register);
            }
        });
    }
}