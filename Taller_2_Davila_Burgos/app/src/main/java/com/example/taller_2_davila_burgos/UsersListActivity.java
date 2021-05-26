package com.example.taller_2_davila_burgos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class UsersListActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private StorageReference mStorageRef;

    public static final String PATH_USERS = "users/";

    private ArrayList<Usuario> Usuarios = new ArrayList<Usuario>();
    private ArrayList<String> uidUsuarios;

    private double userLat;
    private double userLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        uidUsuarios = new ArrayList<String>();
        getUsersFromDb();
    }

    class UsuariosCustomAdapter extends BaseAdapter {

        @Override
        public int getCount(){
            return Usuarios.size();
        }

        @Override
        public Object getItem (int position){
            return Usuarios.get(position);
        }

        @Override
        public long getItemId(int position){
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            convertView = getLayoutInflater().inflate(R.layout.listview_usuarios, null);

            ImageView profile = (ImageView) convertView.findViewById(R.id.imageViewProfile);
            TextView nombre = (TextView) convertView.findViewById(R.id.textViewList);
            Button ubicacion = (Button) convertView.findViewById(R.id.buttonList);

            nombre.setText(Usuarios.get(position).name + " " + Usuarios.get(position).lastName);
            StorageReference profileRef = mStorageRef.child(PATH_USERS+Usuarios.get(position).uid+"/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(profile);
                }
            });
            ubicacion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapa = new Intent(v.getContext(), UserMapsActivity.class);
                    mapa.putExtra("userLat", String.valueOf(userLat));
                    mapa.putExtra("userLong", String.valueOf(userLong));
                    mapa.putExtra("availableUserLat", String.valueOf(Usuarios.get(position).getLatitude()));
                    mapa.putExtra("availableUserLong", String.valueOf(Usuarios.get(position).getLongitude()));
                    mapa.putExtra("otherUserID", Usuarios.get(position).getUid());
                    mapa.putExtra("nombre", Usuarios.get(position).getName() +" "+ Usuarios.get(position).getLastName());
                    startActivity(mapa);
                }
            });
            return convertView;

        }
    }
    public void getUsersFromDb(){

        mRef = mDatabase.getReference(PATH_USERS);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int size = Usuarios.size();

                    for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                        Usuario usuario = singleSnapshot.getValue(Usuario.class);
                        if (!mAuth.getCurrentUser().getUid().equals(usuario.getUid()) && usuario.getDisponible() && !uidUsuarios.contains(usuario.getUid())) {
                            Usuarios.add(usuario);
                            uidUsuarios.add(usuario.getUid());
                        } else if (mAuth.getCurrentUser().getUid().equals(usuario.getUid())){
                            userLat = usuario.getLatitude();
                            userLong = usuario.getLongitude();
                        }
                    }
                    Log.d("USPRUEBA", String.valueOf(size)+" / "+Usuarios.size());
                    if(size != Usuarios.size()) {
                        UsuariosCustomAdapter usuariosAdapter = new UsuariosCustomAdapter();
                        ListView usuariosListView = (ListView) findViewById(R.id.listViewUsuarios);
                        usuariosListView.setAdapter(usuariosAdapter);
                    }
                //}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Suscripcion Usuarios", "Error en la consulta", error.toException());
            }
        });
    }

}