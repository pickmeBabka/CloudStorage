package com.example.cloudstorage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    Button btnRegister;
    EditText password, nick;
    Boolean isRegisterAvailable = false;
    TextView txtRegNumCond, txtRegSpecCharCond, txtRegLenCond;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnRegister = findViewById(R.id.btnRegister);
        password = findViewById(R.id.password);
        nick = findViewById(R.id.RegNick);
        txtRegNumCond = findViewById(R.id.txtRegNumCond);
        txtRegSpecCharCond = findViewById(R.id.txtRegSpecCharsCond);
        txtRegLenCond = findViewById(R.id.txtRegLenSymb);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isRegisterAvailable = true;
                if (helper_class.IsStringContainsChars(password.getText().toString(), '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'))
                {
                    txtRegNumCond.setTextColor(getResources().getColor(R.color.green));
                }else {
                    txtRegNumCond.setTextColor(getResources().getColor(R.color.red));
                    isRegisterAvailable = false;
                }
                if(password.getText().length() >= 8)
                {
                    txtRegLenCond.setTextColor(getResources().getColor(R.color.green));
                }else{
                    txtRegLenCond.setTextColor(getResources().getColor(R.color.red));
                    isRegisterAvailable = false;
                }
                if (helper_class.IsStringContainsChars(password.getText().toString(), '@', '#', '$', '%', '^', '&', '+', '='))
                {
                    txtRegSpecCharCond.setTextColor(getResources().getColor(R.color.green));
                }else{
                    txtRegSpecCharCond.setTextColor(getResources().getColor(R.color.red));
                    isRegisterAvailable = false;
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRegisterAvailable) new Thread(() -> {
                    CloudServicesApi.AddNewPassword(password.getText().toString(), GetSessionId());
                    CloudServicesApi.SetNick(GetSessionId(), nick.getText().toString());
                    SharedPreferences sharedPreferences = getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE);
                    sharedPreferences.edit().putString(helper_class.Constatns.CashNick, nick.getText().toString()).putBoolean(helper_class.Constatns.CashIsLogged, true).apply();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();
                }).start();
            }
        });
    }

    public String GetSessionId() {
        SharedPreferences sharedPreferences = getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE);
        return sharedPreferences.getString(helper_class.Constatns.CashSessionId, null);
    }
}

