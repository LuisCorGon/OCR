package com.luiscortes.ocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.luiscortes.ocr.Fragments.CameraFragment;
import com.luiscortes.ocr.Fragments.DocumentsFragment;
import com.luiscortes.ocr.Fragments.ProfileFragment;
import com.luiscortes.ocr.Fragments.SettingsFragment;
import com.luiscortes.ocr.Fragments.UploadFragment;
import com.luiscortes.ocr.Model.YOLODetector;
import com.luiscortes.ocr.R;
import com.firebase.ui.auth.AuthUI;
import com.luiscortes.ocr.Utils.ImageUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    private FirebaseUser user;

    private Target profileTarget;

    private YOLODetector yoloDetector;


    @Override
    protected void onStart() {
        super.onStart();
         user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null){
            Log.d("USUARIO: ", "USUARIO NO ENCONTRADO, MANDADO A REGISTRO");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Log.d("USUARIO: ", Objects.requireNonNull(user.getDisplayName()));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startYOLOModel();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setBackgroundColor(Color.TRANSPARENT);
        bottomNav.getBackground().setAlpha(0);
        bottomNav.setElevation(0);
        bottomNav.setItemIconTintList(null);

        // Configurar el DrawerLayout y la barra superior (Toolbar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Agregar botón de hamburguesa para abrir el DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Manejar navegación del DrawerLayout (menú lateral)
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
            } else if (item.getItemId() == R.id.nav_settings){
                replaceFragment(new SettingsFragment());
            } else if (item.getItemId() == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawers(); // Cerrar el menú después de seleccionar
            return true;
        });

        // Manejar navegación del BottomNavigationView (barra inferior)
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_upload) {
                    selectedFragment = new UploadFragment();
                } else if (item.getItemId() == R.id.nav_camera) {
                    selectedFragment = new CameraFragment();
                } else if (item.getItemId() == R.id.nav_results) {
                    selectedFragment = new DocumentsFragment();
                }
            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }

            return true;
        });

        // Cargar el fragmento de la cámara por defecto
        if (savedInstanceState == null) {
            replaceFragment(new CameraFragment());
        }
    }

    // Método para reemplazar fragments dinámicamente
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();

    }

    private void startYOLOModel() {
        try {
            yoloDetector = new YOLODetector(this);
            Log.d("TFLite", "✅ Modelo cargado con éxito.");

            // 🔹 Cargar imagen de prueba
            Bitmap bitmap = ImageUtils.loadImageFromResources(getResources(), R.drawable.text_test);

            // 🔹 Preparar imagen para la inferencia
            ByteBuffer inputBuffer = yoloDetector.prepareImage(bitmap);
            Log.d("YOLO", "✅ Imagen preparada con éxito.");

            // 🔹 Crear outputBuffer con el mismo tamaño que la salida del modelo [1,30,21]
            float[][][] outputBuffer = new float[1][30][21];

            // 🔹 Ejecutar la inferencia
            yoloDetector.getInterpreter().run(inputBuffer, outputBuffer);
            Log.d("TFLite", "📌 Inferencia realizada con éxito.");

            // 🔹 Extraer la salida [30,21] eliminando la primera dimensión
            float[][] processedOutput = outputBuffer[0]; // <-- Extraemos la parte útil

            // 🔍 Depuración: Ver la salida cruda del modelo
            for (int i = 0; i < 30; i++) {
                Log.d("OCR_RAW", "Fila " + i + ": " + Arrays.toString(processedOutput[i]));
            }

            // 🔹 Decodificar las predicciones a texto
            String resultadoOCR = yoloDetector.decodePredictions(processedOutput);
            Log.d("OCR", "📜 Texto detectado: " + resultadoOCR);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TFLite", "❌ Error al cargar el modelo.");
        }
    }
}