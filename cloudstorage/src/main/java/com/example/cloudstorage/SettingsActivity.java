package com.example.cloudstorage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class SettingsActivity extends AppCompatActivity {
    Button btnDecreaseNetUseProfile, btnIncreaseNetUseProfile, btnExit;
    TextView txtNetProfile;
    TableLayout tab;
    TableRow row;
    TableLayout.LayoutParams tabLay;
    TableRow.LayoutParams rowLay;
    ImageView menu;
    LinearLayout home, cloudGallery, Settings;
    DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        drawerLayout = findViewById(R.id.main);
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.galary);
        cloudGallery = findViewById(R.id.CloudGallary);
        Settings = findViewById(R.id.Settings);
        btnDecreaseNetUseProfile = findViewById(R.id.btnDecreaseNetUseProfile);
        btnIncreaseNetUseProfile = findViewById(R.id.btnIncreaseNetUseProfile);
        btnExit = findViewById(R.id.btnExit);
        txtNetProfile = findViewById(R.id.txtNetProfile);
        tab = findViewById(R.id.tabSessions);
        tabLay = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 0);
        rowLay = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1);

        new Thread(this::UpdateSessions).start();

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloudServicesApi.ExitSession(getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, ""));
                getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).edit().putString(helper_class.Constatns.CashSessionId, null).putBoolean(helper_class.Constatns.CashIsLogged, false).commit();
                redirectActivity(SettingsActivity.this, CheckEmailActivity.class);
                finish();
            }
        });

        btnIncreaseNetUseProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int last = getSharedPreferences("Settings", MODE_PRIVATE).getInt("NetworkProfile", 1);
                btnDecreaseNetUseProfile.setVisibility(View.VISIBLE);
                if(SettingsEnums.NetworkProfileEnum.values()[last] != SettingsEnums.NetworkProfileEnum.High) {
                    getSharedPreferences("Settings", MODE_PRIVATE).edit().putInt("NetworkProfile", last + 1).apply();
                    txtNetProfile.setText(SettingsEnums.getNetworkProfileName(getResources(), SettingsEnums.NetworkProfileEnum.values()[last + 1]));
                    if(SettingsEnums.NetworkProfileEnum.values()[last + 1] == SettingsEnums.NetworkProfileEnum.High)
                    {
                        v.setVisibility(View.GONE);
                    }
                }
            }
        });

        btnDecreaseNetUseProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int last = getSharedPreferences("Settings", MODE_PRIVATE).getInt("NetworkProfile", 1);
                btnIncreaseNetUseProfile.setVisibility(View.VISIBLE);
                if(SettingsEnums.NetworkProfileEnum.values()[last] != SettingsEnums.NetworkProfileEnum.Low) {
                    getSharedPreferences("Settings", MODE_PRIVATE).edit().putInt("NetworkProfile", last - 1).apply();
                    txtNetProfile.setText(SettingsEnums.getNetworkProfileName(getResources(), SettingsEnums.NetworkProfileEnum.values()[last - 1]));
                    if(SettingsEnums.NetworkProfileEnum.values()[last - 1] == SettingsEnums.NetworkProfileEnum.Low)
                    {
                        v.setVisibility(View.GONE);
                    }
                }
            }
        });

        txtNetProfile.setText(SettingsEnums.getNetworkProfileName(getResources(), SettingsEnums.NetworkProfileEnum.values()[getSharedPreferences("Settings", MODE_PRIVATE).getInt("NetworkProfile", 1)]));

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });
        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer(drawerLayout);
            }
        });
        cloudGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(SettingsActivity.this, CloudGalleryActivity.class);
                finish();
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(SettingsActivity.this, MainActivity.class);
                finish();
            }
        });
    }

    void UpdateSessions()
    {
        LinearLayout.LayoutParams layLay = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        AtomicReference<CloudServicesApi.SessionInfo[]> sessions = new AtomicReference<>();
        AtomicBoolean isDone = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                sessions.set(CloudServicesApi.GetSessions(getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, "")));
                isDone.set(true);
            } catch (IOException e) {
                Log.e("GetSessionsIO", "onCreate: ", e);
                sessions.set(new CloudServicesApi.SessionInfo[0]);
                isDone.set(true);
            } catch (JSONException e) {
                Log.e("GetSessionsJSON", "onCreate: ", e);
                sessions.set(new CloudServicesApi.SessionInfo[0]);
                isDone.set(true);
            }
        }).start();
        while (!isDone.get()) ;
        if(Looper.getMainLooper() == Looper.myLooper())
        {
            tab.removeAllViews();
        }else
        {
            runOnUiThread(() -> tab.removeAllViews());
        }
        for (int i = 0; i < sessions.get().length; i++) {
            CloudServicesApi.SessionInfo session = sessions.get()[i];
            if(Looper.getMainLooper() == Looper.myLooper())
            {
                row = new TableRow(this);

                row.setLayoutParams(tabLay);

                LinearLayout lay = new LinearLayout(this), textLay = new LinearLayout(this);
                lay.setOrientation(LinearLayout.HORIZONTAL);
                textLay.setOrientation(LinearLayout.VERTICAL);
                lay.setLayoutParams(rowLay);
                textLay.setLayoutParams(rowLay);

                TextView txtBrand = new TextView(this), txtModel = new TextView(this);
                txtBrand.setText(session.Brand);
                txtModel.setText(session.Model);
                if(session.isCurrent)
                {
                    txtModel.setText(txtModel.getText() + " (" + getResources().getString(R.string.set_currentSession) + ")" );
                }
                textLay.addView(txtBrand);
                textLay.addView(txtModel);

                lay.addView(textLay);

                Button btnDeleteSession = new Button(this);
                btnDeleteSession.setText(getResources().getString(R.string.set_TerminateSession));
                btnDeleteSession.setTag(session);
                btnDeleteSession.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CloudServicesApi.SessionInfo s = (CloudServicesApi.SessionInfo) v.getTag();
                        if(s.isCurrent){
                            CloudServicesApi.ExitSession(getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, ""));
                            getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).edit().putString(helper_class.Constatns.CashSessionId, null).putBoolean(helper_class.Constatns.CashIsLogged, false).commit();
                            redirectActivity(SettingsActivity.this, CheckEmailActivity.class);
                            finish();
                        }else {
                            CloudServicesApi.TerminateSession(getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, ""), s.idHash);
                            UpdateSessions();
                        }
                    }
                });
                lay.addView(btnDeleteSession);

                row.addView(lay);

                tab.addView(row);
            }else {
                runOnUiThread(()->{
                    row = new TableRow(this);

                    row.setLayoutParams(tabLay);

                    LinearLayout lay = new LinearLayout(this), textLay = new LinearLayout(this);
                    lay.setOrientation(LinearLayout.HORIZONTAL);
                    textLay.setOrientation(LinearLayout.VERTICAL);
                    lay.setLayoutParams(rowLay);
                    textLay.setLayoutParams(rowLay);

                    TextView txtBrand = new TextView(this), txtModel = new TextView(this);
                    txtBrand.setText(session.Brand);
                    txtModel.setText(session.Model);
                    if(session.isCurrent)
                    {
                        txtModel.setText(txtModel.getText() + " (" + getResources().getString(R.string.set_currentSession) + ")" );
                    }
                    textLay.addView(txtBrand);
                    textLay.addView(txtModel);

                    lay.addView(textLay);

                    Button btnDeleteSession = new Button(this);
                    btnDeleteSession.setText(getResources().getString(R.string.set_TerminateSession));
                    btnDeleteSession.setTag(session);
                    btnDeleteSession.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CloudServicesApi.SessionInfo s = (CloudServicesApi.SessionInfo) v.getTag();
                            if(s.isCurrent){
                                CloudServicesApi.ExitSession(getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, ""));
                                getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).edit().putString(helper_class.Constatns.CashSessionId, null).putBoolean(helper_class.Constatns.CashIsLogged, false).commit();
                                redirectActivity(SettingsActivity.this, CheckEmailActivity.class);
                                finish();
                            }else {
                                CloudServicesApi.TerminateSession(getSharedPreferences(helper_class.Constatns.Cash, MODE_PRIVATE).getString(helper_class.Constatns.CashSessionId, ""), s.idHash);
                                UpdateSessions();
                            }
                        }
                    });
                    lay.addView(btnDeleteSession);

                    row.addView(lay);

                    tab.addView(row);
                });
            }
        }
    }

    public static void openDrawer(DrawerLayout drawerLayout)
    {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout)
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void redirectActivity(Context activity, Class secondActivity)
    {
        Intent i = new Intent(activity, secondActivity);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(i);
    }
}