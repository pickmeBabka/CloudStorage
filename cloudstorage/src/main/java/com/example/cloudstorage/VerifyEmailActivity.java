package com.example.cloudstorage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class VerifyEmailActivity extends AppCompatActivity {
    Button btnVerfCode;
    EditText verfCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_email);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnVerfCode = findViewById(R.id.btnVerfCode);
        verfCode = findViewById(R.id.verfCode);

        btnVerfCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    Boolean isVerified = CloudServicesApi.verifyEmail(verfCode.getText().toString(), GetSessionId());
                    if(isVerified)
                    {
                        Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                        startActivity(i);
                        finish();
                    }else{
                        runOnUiThread(() -> {
                            Toast.makeText(VerifyEmailActivity.this, "Ошибка верификации попробуйте ещё раз", Toast.LENGTH_SHORT).show();
                            verfCode.setText("");
                        });
                    }
                }).start();
            }
        });
    }

    public String GetSessionId() {
        SharedPreferences sharedPreferences = getSharedPreferences("Cash", MODE_PRIVATE);
        return sharedPreferences.getString("SessionId", null);
    }
}