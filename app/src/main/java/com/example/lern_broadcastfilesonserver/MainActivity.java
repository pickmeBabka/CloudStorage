package com.example.lern_broadcastfilesonserver;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    TextView lbl, lblJson;

    Button BSelectImage, btnLogin, btnAuth;

    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        try {
            SecureCashServices.Init();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
        }
        lbl = findViewById(R.id.lbl);
        lblJson = findViewById(R.id.lblJson);
        BSelectImage = findViewById(R.id.BSelectImage);
        btnLogin = findViewById(R.id.btnLogin);
        btnAuth = findViewById(R.id.btnAuth);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("HardwareIds")
            public void onClick(View v) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ChatEngine_bot?start=penis"));
                startActivity(browserIntent);
            }
        });
        // handle the Choose Image button to trigger
        // the image chooser function
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    @SuppressLint("HardwareIds")
                    public void run() {

                        String sId = CreateNewSession();
                        String authKey = GetAuthKey(sId);

                        runOnUiThread(new helper_class.EditTextViewOnUIThread(sId + "\n" + authKey, lbl));
                        try {
                            CashSessionId(sId);
                        } catch (IOException e) {

                        }
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ChatEngine_bot?start=" + authKey));
                        startActivity(browserIntent);
                    }

                    private String CreateNewSession() { return "";
                    }
                }).start();
            }
        });
    }

    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        String[] mimetypes = {"image/*", "video/*"};
        i.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri selectedImageUri = clipData.getItemAt(i).getUri();

                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
                    String exst;


                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);
                        exst = filePath.substring(filePath.lastIndexOf(".") + 1);
                    } else {
                        exst = "";
                    }
                    if (null != selectedImageUri) {
                        runOnUiThread(new helper_class.EditTextViewOnUIThread(selectedImageUri.getEncodedPath(), lbl));
                        // update the preview image in the layout
                    }
                    new Thread(() -> {
                        try {
                            InputStream iStream = getContentResolver().openInputStream(selectedImageUri);
                            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                            int bufferSize = 32768;
                            byte[] buffer = new byte[bufferSize];

                            int len = 0;
                            int totalLen = 0;
                            long Size = 0;
                            int fullSize = iStream.available();
                            boolean isBigUpload = false;
                            String fileID = null;
                            if (fullSize >= 5242880) {
                                isBigUpload = true;
                                fileID = GetFileId(exst);
                                byteBuffer.write((fileID + " ").getBytes(StandardCharsets.UTF_8));
                            }
                            while ((len = iStream.read(buffer)) != -1) {
                                byteBuffer.write(buffer, 0, len);
                                totalLen += len;
                                Size += len;
                                runOnUiThread(new helper_class.EditTextViewOnUIThread("Totallen: " + ((float) totalLen / 1048576) + "MB\r\nlen: " + len + "\r\nfile id: " + fileID + "\r\nsize: " + ((float) Size / 1048576) + "MB\r\navailable " + ((float) fullSize / 1048576) + "MB\r\nprogress: " + ((float) Size / (float) fullSize * 100f) + "%\r\n", lbl));
                                if (totalLen == 5242880) {
                                    UploadData("https://web-hobby.ru:8713/BigUpload", byteBuffer.toByteArray());
                                    byteBuffer.reset();
                                    byteBuffer.write((fileID + " ").getBytes(StandardCharsets.UTF_8));
                                    totalLen = 0;
                                }
                            }
                            runOnUiThread(new helper_class.AppendTextViewOnUIThread("\r\n" + totalLen + " " + byteBuffer.size(), lblJson));
                            if (isBigUpload) {
                                UploadData("https://web-hobby.ru:8713/BigUpload", byteBuffer.toByteArray());
                            } else {
                                UploadData("https://web-hobby.ru:8713/TESTPOST/LightImg?ext=" + exst, byteBuffer.toByteArray());
                            }
                        } catch (Exception e) {
                            runOnUiThread(new helper_class.EditTextViewOnUIThread(e.getClass().getName() + "\r\n" + e.getMessage(), lbl));
                        }

                    }).start();
                }
            }
        }
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
    public String GetAuthKey(String sId)
    {
        try {
            URL url = new URL("https://web-hobby.ru:8713/Authorize?sId="+sId);
            HttpURLConnection connectionLog = (HttpURLConnection) url.openConnection();
            connectionLog.setRequestMethod("POST");
            OutputStream out = new BufferedOutputStream(connectionLog.getOutputStream());

            out.write((sId).getBytes(StandardCharsets.UTF_8));

            if (connectionLog.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = (InputStream) connectionLog.getContent();
                return IOUtils.toString(stream, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            runOnUiThread(new helper_class.AppendTextViewOnUIThread("\n" + e.getMessage() + "\n" + e.getClass(), lblJson));
        }
        return "";
    }

    public static String GetFileId(String exst)
    {
        try {
            URL urlLog = new URL("https://web-hobby.ru:8713/GetBigPostFileId?exstention=" + exst);
            HttpURLConnection connectionLog = (HttpURLConnection) urlLog.openConnection();
            connectionLog.setRequestMethod("GET");

            if (connectionLog.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = (InputStream) connectionLog.getContent();
                return IOUtils.toString(stream, StandardCharsets.UTF_8);
            } else {
            }
        } catch (Exception e) {

        }
        return "";
    }

    public String UploadData(String Url, byte[] data)
    {
        try {
            URL urlLog = new URL(Url);
            HttpURLConnection connectionLog = (HttpURLConnection) urlLog.openConnection();
            connectionLog.setRequestMethod("POST");
            OutputStream out = new BufferedOutputStream(connectionLog.getOutputStream());

            out.write(data);

            if (connectionLog.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = (InputStream) connectionLog.getContent();

                return IOUtils.toString(stream, StandardCharsets.UTF_8);
            } else {
                runOnUiThread(new helper_class.AppendTextViewOnUIThread("Photo Not Sended", lblJson));
            }
        } catch (Exception e) {
            runOnUiThread(new helper_class.AppendTextViewOnUIThread(e.getClass().getName() + "\r\n" + e.getMessage(), lblJson));
        }
        return "";
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    class UploadPackage
    {
        byte[] bytes;
        String fileId;

        UploadPackage(byte[] bytes1, String fileId)
        {
            bytes = bytes1;
            this.fileId = fileId;
        }
    }

    public int sumBytes(byte[] bytes)
    {
        int res = 0;
        for (byte aByte : bytes) {
            res += Byte.toUnsignedInt(aByte);
        }
        return  res;
    }
    public void CashSessionId(String sessionId) throws IOException {
        String enc = "";
        try {
            enc = Arrays.toString(SecureCashServices.encrypt(sessionId.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException | InvalidAlgorithmParameterException |
                 NoSuchAlgorithmException ignored) {
        }
        final BufferedWriter out = new BufferedWriter(new FileWriter("sessionId"), enc.getBytes().length);
        out.write(enc);
        out.close();
    }

    public  String GetSessionId() throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        FileReader reader = new FileReader("sessionId");
        char[] chars = new char[128];
        reader.read(chars);
        return IOUtils.toString(SecureCashServices.decrypt(String.valueOf(chars).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8.name());
    }
}