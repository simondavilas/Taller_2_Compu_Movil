package com.example.taller_2_davila_burgos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private StorageReference mStorageRef;

    public static final String PATH_USERS = "users/";

    private EditText mName;
    private EditText mLastName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mDocument;
    private ImageView mImageView;
    private Uri imagenUri;

    private boolean imageInclude = false;

    int IMAGE_PICKER_REQUEST = 1;
    int REQUEST_IMAGE_CAPTURE = 2;

    private int STORAGE_PERMISSION_CODE = 1;
    private int CAMERA_PERMISSION_CODE = 2;

    private static final int REQUEST_LOCATION = 410;
    private FusedLocationProviderClient fusedLocationProviderClient;
    String[] location_permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private double userLat;
    private double userLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Button botonRegistar = (Button) findViewById(R.id.buttonRegistro);
        Button botonFoto = (Button) findViewById(R.id.buttonFoto);

        mName = (EditText) findViewById(R.id.editTextNombre);
        mLastName = (EditText) findViewById(R.id.editTextApellido);
        mEmail = (EditText) findViewById(R.id.editTextCorreoReg);
        mPassword = (EditText) findViewById(R.id.editTextPasswordReg);
        mDocument = (EditText) findViewById(R.id.editTextDocument);
        mImageView = (ImageView) findViewById(R.id.imagenFotoPerfil);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getLocation();

        botonRegistar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    signInUser(mEmail.getText().toString(), mPassword.getText().toString());
                }
            }
        });

        botonFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(RegisterActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickImage.setType("image/*");
                    startActivityForResult(pickImage, IMAGE_PICKER_REQUEST);
                } else {
                    requestStoragePermission();
                }
            }
        });
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permiso requerido")
                    .setMessage("Este permiso es requerido para el acceso a su galería")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    RegisterActivity.this, new String[]{
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                    }, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(
                    this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permisssions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permisssions, grantResults);
        switch (requestCode) {
            case 1:
                if (requestCode == STORAGE_PERMISSION_CODE) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("STORAGEPERMISSION", "Permiso Concedido");
                    } else {
                        Toast.makeText(this, "Permiso Denegado", Toast.LENGTH_SHORT).show();
                    }
                }
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                userLat = location.getLatitude();
                                userLong = location.getLongitude();
                            } else {
                                //Active Location
                            }
                        }
                    });
                }else {
                    Toast.makeText(this, "Location services were denied by the user", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
    @Override
    protected void onActivityResult (int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case 1: {
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        mImageView.setImageBitmap(selectedImage);
                        imageInclude = true;
                        imagenUri = imageUri;
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri){
        StorageReference fileRef = mStorageRef.child("users/"+mAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(mImageView);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Fallo en cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateForm(){
        boolean valid = true;
        String name = mName.getText().toString();
        String lastName = mLastName.getText().toString();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String document = mDocument.getText().toString();

        if(!imageInclude){
            Toast.makeText(getBaseContext(), "Debe seleccionar una foto de perfil", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if(TextUtils.isEmpty(name)){
            mName.setError("Requerido");
            valid = false;
        }else{
            mName.setError(null);
        }
        if(TextUtils.isEmpty(lastName)){
            mLastName.setError("Requerido");
            valid = false;
        }else{
            mLastName.setError(null);
        }
        if(TextUtils.isEmpty(email)){
            mEmail.setError("Requerido");
            valid = false;
        }else if(!isEmailValid(email)){
            mEmail.setError("Ingrese un correo válido");
            valid = false;
        }else{
            mEmail.setError(null);
        }
        if(TextUtils.isEmpty(password)){
            mPassword.setError("Requerido");
            valid = false;
        }else{
            mPassword.setError(null);
        }
        if(TextUtils.isEmpty(document)){
            mDocument.setError("Requerido");
            valid = false;
        }else{
            mDocument.setError(null);
        }
        return valid;
    }

    private boolean isEmailValid(String email){
        if(!email.contains("@") || !email.contains(".") || email.length() < 5 )
            return false;
        return true;
    }

    private void updateUI(FirebaseUser currentUser) throws ParseException{
        if(currentUser != null){
            //currentUser = mAuth.getCurrentUser();
            if(validateForm()){
                uploadImageToFirebase(imagenUri);
                Usuario usuario = new Usuario();

                usuario.setUid(currentUser.getUid());
                usuario.setName(mName.getText().toString());
                usuario.setLastName(mLastName.getText().toString());
                usuario.setEmail(mEmail.getText().toString());
                usuario.setPassword(mPassword.getText().toString());
                usuario.setDocument(Long.parseLong(mDocument.getText().toString()));
                usuario.setLatitude(userLat);
                usuario.setLongitude(userLong);
                usuario.setDisponible(false);

                mRef = mDatabase.getReference(PATH_USERS + currentUser.getUid());

                mRef.setValue(usuario);
            }
            Intent intent = new Intent (getBaseContext(), PuntosMapaActivity.class);
            Toast.makeText(getBaseContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();

            loadUsersSuscription();
            startActivity(intent);
        }else{
            mName.setText("");
            mLastName.setText("");
            mEmail.setText("");
            mPassword.setText("");
            mDocument.setText("");
        }
    }

    private void signInUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null){
                                UserProfileChangeRequest.Builder upcrb = new UserProfileChangeRequest.Builder();
                                upcrb.setDisplayName(mEmail.getText().toString());
                                user.updateProfile(upcrb.build());
                                try{
                                    updateUI(user);
                                }catch (ParseException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        if(!task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Falló el registro" + task.getException().toString(),
                                    Toast.LENGTH_SHORT).show();
                            Log.e("", task.getException().getMessage());
                        }
                    }
                });
    }

    public void loadUsersSuscription(){
        mRef = mDatabase.getReference(PATH_USERS);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnahpshot : dataSnapshot.getChildren()) {
                    Usuario usuario = singleSnahpshot.getValue(Usuario.class);

                    Log.i("Suscripcion Usuarios", "Encontró usuario: " + usuario.getEmail());
                    String name = usuario.getEmail();
                    String contraseña = usuario.getPassword();
                    //Toast.makeText(getBaseContext(), name + " /" + contraseña, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Suscripcion Usuarios", "Error en la consulta", databaseError.toException());
            }
        });
    }

    public void getLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null){
                        userLat = location.getLatitude();
                        userLong = location.getLongitude();
                    }else{
                        Log.i("Location", "Location is null");
                        //Active location
                    }
                }
            });
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Permiso requerido")
                    .setMessage("Este permiso es requerido para el acceso a su ubicación")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(
                                    RegisterActivity.this, new String[]{
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                    }, REQUEST_LOCATION);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else{
            ActivityCompat.requestPermissions(this, location_permissions, REQUEST_LOCATION);
        }
    }
}