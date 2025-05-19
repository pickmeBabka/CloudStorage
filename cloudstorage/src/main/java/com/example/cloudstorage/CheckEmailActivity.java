package com.example.cloudstorage;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.example.cloudstorage.helper_class.validEmail;

public class CheckEmailActivity extends AppCompatActivity {
    EditText txt;
    Button login;
    ProgressBar loading;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_email);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        DataBaseServices.setDb(getApplicationContext().openOrCreateDatabase("dataLol.db", MODE_PRIVATE, null));
        if(getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getBoolean(helper_class.Constatns.CashIsLogged, false) && CloudServicesApi.CheckAuth(getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, "")))
        {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        txt = findViewById(R.id.email);
        login = findViewById(R.id.login);
        loading = findViewById(R.id.loading);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txt.getText().toString();
                if (validEmail(email)) {
                    loading.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String session = null;
                            try {
                                session = CloudServicesApi.CreateNewSession();
                                Log.i("session", "run: " + session);
                            } catch (IOException e) {
                                Log.e("CreatingNewSession", "run: ", e);
                            }
                            try {
                                CashSessionId(session);
                            } catch (IOException e) {
                                Log.e("CashSessionId", "run: ", e);
                            }
                            if (CloudServicesApi.CheckEmail(email)) {
                                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(i.putExtra("email", email));
                                finish();
                            }else
                            {
                                CloudServicesApi.Register(email, session);
                                Intent i = new Intent(getApplicationContext(), VerifyEmailActivity.class);
                                startActivity(i);
                                finish();
                            }
                            runOnUiThread(() -> {loading.setVisibility(View.GONE);});
                        }
                    }).start();
                }
            }
        });
        txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!validEmail(txt.getText().toString())) txt.setError(getResources().getString(R.string.invalid_emal) + " " + txt.getText().toString(), getDrawable(R.drawable.error));
            }
        });
    }



    public void CashSessionId(String sessionId) throws IOException {
        SharedPreferences sharedPreferences = getSharedPreferences("Cash", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SessionId", sessionId);
        editor.apply();
    }

    public String GetSessionId() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        SharedPreferences sharedPreferences = getSharedPreferences("Cash", MODE_PRIVATE);
        return sharedPreferences.getString("SessionId", null);
    }
}