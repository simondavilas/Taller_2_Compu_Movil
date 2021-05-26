package com.example.taller_2_davila_burgos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();

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
    @Override
    protected  void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser){
        if(currentUser != null){
            Intent iniciarS = new Intent (getBaseContext(), PuntosMapaActivity.class);
            iniciarS.putExtra("user", currentUser.getEmail());
            startActivity(iniciarS);
        }
    }
}