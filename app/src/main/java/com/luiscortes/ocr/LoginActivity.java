package com.luiscortes.ocr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.luiscortes.ocr.R;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.luiscortes.ocr.Fragments.CameraFragment;

import java.util.Collections;

public class LoginActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Registrar el launcher en onCreate()
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser != null) {
                                Toast.makeText(LoginActivity.this, "Bienvenido " + firebaseUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Acceso denegado", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        findViewById(R.id.btnLoginGoogle).setOnClickListener(view -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = AuthUI.getInstance().createSignInIntentBuilder().
                setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build()))
                        .setIsSmartLockEnabled(false)
                                .build();
        resultLauncher.launch(signInIntent);
    }
}

