package com.example.taller_2_davila_burgos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class LogInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    
    private EditText mUser;
    private EditText mPassword;
    private  TextView verify;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();

        Button botonIniciarS = (Button) findViewById(R.id.buttonIniciar);

        mUser = (EditText)findViewById(R.id.editTextCorreoLog);
        mPassword = (EditText)findViewById(R.id.editTextPasswordLog);
        verify = (TextView)findViewById(R.id.textViewVerify);
        verify.setText("");
        botonIniciarS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInUser(mUser.getText().toString(), mPassword.getText().toString());
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
        }else{
            mUser.setText("");
            mPassword.setText("");

        }
    }

    private boolean validateForm(){
        boolean valid = true;
        String email = mUser.getText().toString();
        String password = mPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            mUser.setError("Requerido");
            valid = false;
        }else{
            mUser.setError(null);
        }
        if(TextUtils.isEmpty(password)){
            mPassword.setError("Requerido");
            valid = false;
        }else{
            mPassword.setError(null);
        }
        return valid;
    }

    private void logInUser(String email, String password){
        if(validateForm()){
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            }
                            if(!task.isSuccessful()){
                                Log.w("AUTH", "logInWithEmail:failed", task.getException());
                                Toast.makeText(LogInActivity.this, "Inicio de sesión fallido.",
                                        Toast.LENGTH_SHORT);
                                updateUI(null);
                                Toast.makeText(LogInActivity.this, "Correo y/o Contraseña incorrecta",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}