package com.example.cloudstorage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class JsonHelper {

    public static String convertObjectToJson(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace(); // Обработайте исключение соответствующим образом
            return null;  // Или выбросьте исключение дальше
        }
    }
}
