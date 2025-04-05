package com.example.lern_broadcastfilesonserver;

import android.os.AsyncTask;
import android.util.Xml;
import android.widget.TextView;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;


public class helper_class {
    public static class EditTextViewOnUIThread			//Нечто, реализующее интерфейс Runnable
            implements Runnable		//(содержащее метод run())
    {
        TextView _lbl;
        String s;

        public  EditTextViewOnUIThread(String s, TextView _lbl)
        {
            this._lbl = _lbl;
            this.s = s;
        }
        public void run()		//Этот метод будет выполняться в побочном потоке
        {
            _lbl.setText(s);
        }
    }

    public static class AppendTextViewOnUIThread            //Нечто, реализующее интерфейс Runnable
            implements Runnable		//(содержащее метод run())
    {
        TextView _lbl;
        String s;

        public AppendTextViewOnUIThread(String s, TextView _lbl)
        {
            this._lbl = _lbl;
            this.s = s;
        }
        public void run()		//Этот метод будет выполняться в побочном потоке
        {
            _lbl.append(s);
        }
    }
}
