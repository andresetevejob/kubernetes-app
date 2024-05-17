package com.nextu.kubernetesapp.utils;

import com.nextu.kubernetesapp.exceptions.FileContentException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class FileUtils {
    private FileUtils(){}
    public static Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }
    public static String generateStringFromDate(String ext){
        return new SimpleDateFormat("yyyyMMddhhmmss'."+ext+"'").format(new Date());
    }
    public static String getExtension(String filename) throws FileContentException {
       return FileUtils.getExtensionByStringHandling(filename)
               .orElseThrow(()->new FileContentException( "Post not created, bad file extension"));
    }
}
