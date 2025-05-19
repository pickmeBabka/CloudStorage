package com.example.cloudstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

class SettingsEnums {

    enum NetworkProfileEnum {
        Low, Medium, High, Custom
    }

    static class NetworkProfile{
        int downloadThreads;
        int uploadThreads;
    }

    static String getNetworkProfileName(Resources resources, NetworkProfileEnum profileEnum)
    {
        switch (profileEnum)
        {
            case Low:
                return resources.getString(R.string.s_NetworkUsingProfile_Low);
            case Medium:
                return resources.getString(R.string.s_NetworkUsingProfile_Medium);
            case High:
                return resources.getString(R.string.s_NetworkUsingProfile_High);
            case Custom:
                return resources.getString(R.string.s_NetworkUsingProfile_Custom);
        }
        return null;
    }

    static NetworkProfile getNetworkProfile(Context context)
        {
            SharedPreferences settings = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
            NetworkProfileEnum profileEnum = NetworkProfileEnum.values()[settings.getInt("NetworkProfile", 1)];
            switch (profileEnum)
            {
                case Low :
                    return new NetworkProfile() {
                        {
                            downloadThreads = 1;
                            uploadThreads = 1;
                        }
                    };
                case Medium:
                    return new NetworkProfile() {
                        {
                            downloadThreads = 5;
                            uploadThreads = 5;
                        }
                    };
                case High:
                    return new NetworkProfile() {
                        {
                            downloadThreads = 25;
                            uploadThreads = 25;
                        }
                    };
                case Custom:
                    return new NetworkProfile() {
                        {
                            downloadThreads = settings.getInt("CustomDownloadThreads", 5);
                            uploadThreads = settings.getInt("CustomUploadThreads", 5);
                        }
                    };
            }
            return null;
    }
}
