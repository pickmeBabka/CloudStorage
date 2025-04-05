package com.example.cloudstorage;

import android.widget.TextView;

import java.util.Locale;


public class helper_class {
    public static class Constatns
    {
        public static String CashNick = "Nick";
        public static String Cash = "Cash";
        public static String CashIsLogged = "isLogged";
        public static String CashSessionId = "SessionId";
    }
    public static  Boolean IsStringContainsChars(String str, char... chars)
    {
        for (char ch:
             chars) {
            if (str.contains("" + ch)) return true;
        }
        return false;
    }

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
    public static boolean validEmail(String email) {
        return email.toUpperCase(Locale.ROOT).matches("[A-Z0-9._%+-][A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{3}");
    }
}
