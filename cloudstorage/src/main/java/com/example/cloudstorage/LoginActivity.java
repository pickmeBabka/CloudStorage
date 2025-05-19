package com.example.cloudstorage;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import static com.example.cloudstorage.helper_class.validEmail;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = findViewById(R.id.logEmail);
        password = findViewById(R.id.logPassword);
        btnLogin = findViewById(R.id.btnLogin);
        email.setText(getIntent().getStringExtra("email"));
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!validEmail(email.getText().toString())) email.setError(getResources().getString(R.string.invalid_emal) + " " + email.getText().toString(), getDrawable(R.drawable.error));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validEmail(email.getText().toString()))
                {
                    new Thread(() ->
                    {
                        if(CloudServicesApi.Authorize(email.getText().toString(), getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, ""), password.getText().toString())) {
                            getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).edit().putBoolean(helper_class.Constatns.CashIsLogged, true).apply();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    }).start();
                }
            }
        });
    }
}