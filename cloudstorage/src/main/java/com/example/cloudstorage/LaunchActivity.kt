package com.example.cloudstorage

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.cloudstorage.ui.theme.Lern_BroadCastFilesOnServerTheme

class LaunchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var prefs = getSharedPreferences("Cash", MODE_PRIVATE);
        if(prefs.getBoolean("isLogged", false))
        {
            var i = Intent(applicationContext, MainActivity().javaClass);
            startActivity(i);
            finish();
        }else
        {
            var i = Intent(applicationContext, CheckEmailActivity().javaClass);
            startActivity(i);
            finish();
        }
    }
}