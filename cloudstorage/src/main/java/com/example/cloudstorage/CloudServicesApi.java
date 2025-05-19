package com.example.cloudstorage;

import android.net.Network;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class CloudServicesApi {

    public static byte[] callApiBigResponse(String methodName, byte[] body)
    {
        try {
            URL url = new URL("https://web-hobby.ru:8713/CloudServices/Api/" + methodName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            if (body != null) connection.getOutputStream().write(body);

//            Log.i("BodyLength", "callApi: " + (body != null ? body.length : "array is null") + " " + methodName);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = (InputStream)connection.getContent();
                if (connection.getContentLength() > 0) {
                    byte[] buffer = new byte[connection.getContentLength()];
//                    Log.i("Response length", "callApi: " + buffer.length);
                    ByteBuffer buf = ByteBuffer.wrap(buffer);
                    byte[] smallBuffer = new byte[1024];
                    int readLen = stream.read(smallBuffer);
                    buf.put(smallBuffer);
                    while(readLen != -1)
                    {
                        stream = (InputStream)connection.getContent();
                        try {
                            readLen = stream.read(smallBuffer, 0, smallBuffer.length);
                            if(readLen > 0) buf.put(smallBuffer, 0, readLen);

                        }catch (Exception e){
                            Log.e("ReadStream", "callApi: ", e);
                        }
                    }
//                    Log.i("responseLen", "callApi: buffer: " + buffer.length);
                    return buffer;
                }
            }
        } catch (Exception e) {
            Log.e(methodName, "callApi: " + e.getMessage(), e);
        }
        return new byte[0];
    }

    public static byte[] callApi(String methodName, byte[] body)
    {
        try {
            URL url = new URL("https://web-hobby.ru:8713/CloudServices/Api/" + methodName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            if (body != null) connection.getOutputStream().write(body);

            Log.i("BodyLength", "callApi: " + (body != null ? body.length : "array is null") + " " + methodName);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = (InputStream)connection.getContent();
                if (connection.getContentLength() > 0) {
                    byte[] buffer = new byte[connection.getContentLength()];
                    Log.i("Response length", "callApi: " + buffer.length + "readed: " +
                            stream.read(buffer));
                    return buffer;
                }
            }
        } catch (Exception e) {
            Log.e(methodName, "callApi: " + e.getMessage(), e);
        }
        return new byte[0];
    }


    public static Boolean callApiBool(String methodName, byte[] body)
    {
        try {
            URL url = new URL("https://web-hobby.ru:8713/CloudServices/Api/" + methodName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            if (body != null) connection.getOutputStream().write(body);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }else return  false;
        } catch (Exception e) {
            Log.e(methodName, "callApi: " + e.getClass().getName(), e);
        }
        return false;
    }

    public static void POSTLightImg(byte[] body, String exst, String SessionID)
    {
        byte[] exstB = (exst + "\n").getBytes(StandardCharsets.UTF_8), SessionIDB = (SessionID + "\n").getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(new byte[exstB.length + SessionIDB.length + body.length]);
        buffer.put(SessionIDB);
        buffer.put(exstB);
        buffer.put(body);
        callApi("POSTLightImg", buffer.array());
    }

    public static String GetBigPostFileId(String exst, String SessionID, long length) throws IOException {
        byte[] exstB = (exst + "\n").getBytes(StandardCharsets.UTF_8), SessionIDB = (SessionID + "\n").getBytes(StandardCharsets.UTF_8), lengthB = ("" + length).getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(new byte[exstB.length + SessionIDB.length + lengthB.length]);
        buffer.put(SessionIDB);
        buffer.put(exstB);
        buffer.put(lengthB);
        return IOUtils.toString(callApi("GetBigPostFileId", buffer.array()), StandardCharsets.UTF_8.name());
    }

    public static String GetThumbFileId(String exst, String SessionID, long length, String originFileId) throws IOException {
        return IOUtils.toString(callApi("GetThumbId", (SessionID + "\n" + exst + "\n" + length + "\n" + originFileId).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8.name());
    }

    public static void BigUpload(String fileId, int part, @NonNull byte[] file)
    {
        Log.i("fileLength", "BigUpload: " + file.length);
        byte[] fileIdBytes = (fileId + " " + part + " ").getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[fileIdBytes.length + file.length];
        ByteBuffer buffer = ByteBuffer.wrap(body);
        buffer.put(fileIdBytes);
        buffer.put(file);
        callApi("BigUpload", body);
    }

    static long CountByteSum(byte... bytes)
    {
        long sum = 0;
        for (byte b:
             bytes) {
            sum += b & 0xff;
        }
        return  sum;
    }

    public static String CreateNewSession() throws IOException
    {
        return IOUtils.toString(callApi("CreateNewSession", (Build.MODEL + "\n" + Build.BRAND + "\n" + Build.ID + "\n" + Build.DEVICE).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8.name());
    }

    public static void AddNewPassword(String password, String SessionId) {
        callApi("AddNewPassword", (SessionId + "\n" + password).getBytes(StandardCharsets.UTF_8));
    }


    public static Boolean CheckEmail(String email)
    {
        return callApiBool("CheckEmail", email.getBytes(StandardCharsets.UTF_8));
    }


    public static Boolean Register(String email, String sessionId)
    {
        return callApiBool("Register", (email + "\n" + sessionId).getBytes(StandardCharsets.UTF_8));
    }


    public static Boolean Authorize(String email, String sID, String password)
    {
        return callApiBool("Authorize", (email + "\n" + password + "\n" + sID).getBytes(StandardCharsets.UTF_8));
    }

    public static Boolean verifyEmail(String token, String sessionId)
    {
        return callApiBool("verifyEmail", (token + "\n" + sessionId).getBytes(StandardCharsets.UTF_8));
    }

    public static  void SetNick(String sID, String Nick)
    {
        callApi("SetNick", (sID + "\n" + Nick).getBytes(StandardCharsets.UTF_8));
    }

    public static String[] GetMyFiles(String sID) throws IOException {
        String filesStr = IOUtils.toString(callApi("GetMyFiles", sID.getBytes(StandardCharsets.UTF_8)), "UTF_8");
        Log.i("MyFiles", "GetMyFiles: " + filesStr);
        return filesStr.split("\n");
    }

    public static UserFileInfo GetFileInfo(String fileID) throws IOException, JSONException {
        String fileStr = IOUtils.toString(callApi("GetFileInfo", fileID.getBytes(StandardCharsets.UTF_8)), "UTF_8");
        Log.i("json", "GetFileInfo: " + fileStr);
        JSONObject json = new JSONObject(fileStr);
        UserFileInfo fileInfo = new UserFileInfo();
        fileInfo.FileId = fileID;
        if(json.has("extension")) fileInfo.extension = json.getString("extension");
        if(json.has("parts")) fileInfo.parts = json.getInt("parts");
        if(json.has("length")) fileInfo.length = json.getInt("length");
        if(json.has("thumbId")) fileInfo.thumbId = json.getString("thumbId");
        return fileInfo;
    }

    public static byte[] DownloadPart(String fileID, int part)
    {
        return callApiBigResponse("DownloadPart", (fileID + "\n" + part).getBytes(StandardCharsets.UTF_8));
    }

    public static SessionInfo[] GetSessions(String sId) throws IOException, JSONException {
        String sessions = IOUtils.toString(callApi("GetMySessions", sId.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8.name());
        Log.i("sessions", "GetSessions: " + sessions);
        JSONArray json = new JSONArray(sessions);
        List<SessionInfo> array = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject obj = json.getJSONObject(i);
            SessionInfo session = new SessionInfo();
            session.Brand = obj.getString("Brand");
            session.Model = obj.getString("Model");
            session.idHash = obj.getString("hashId");
            session.isCurrent = obj.getBoolean("isCurrent");
            array.add(session);
        }
        return array.toArray(new SessionInfo[0]);
    }

    public static boolean CheckAuth(String sId)
    {
        AtomicReference<Boolean> isAuth = new AtomicReference<>(false);
        AtomicReference<Boolean> isDone = new AtomicReference<>(false);

        new Thread(()->{
            isAuth.set(callApiBool("CheckAuth", sId.getBytes(StandardCharsets.UTF_8)));
            isDone.set(true);
        }).start();
        while(!isAuth.get()) ;

        return isAuth.get();
    }

    public static void TerminateSession(String sId, String tHashSessionId) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(() -> {
                callApiBool("TerminateSession", (sId + "\n" + tHashSessionId).getBytes(StandardCharsets.UTF_8));
            }).start();
        } else {
            callApiBool("TerminateSession", (sId + "\n" + tHashSessionId).getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void ExitSession(String sId)
    {
        if(Looper.myLooper() == Looper.getMainLooper())
        {
            new Thread(()-> {
                callApiBool("ExitSession", (sId).getBytes(StandardCharsets.UTF_8));
            }).start();
        }else
        {
            callApiBool("ExitSession", (sId).getBytes(StandardCharsets.UTF_8));
        }
    }

    public static class UserFileInfo
    {
        public String FileId;
        public String extension;
        public String thumbId;
        public int parts;
        public long length;
    }
    public static class SessionInfo
    {
        String Brand;
        String Model;
        String idHash;
        Boolean isCurrent;

    }
}
