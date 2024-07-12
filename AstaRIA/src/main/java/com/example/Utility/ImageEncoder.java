package com.example.Utility;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageEncoder {
    private static String default_filePath="defaultImage.png";
    public static String encodeImage(ServletContext servletContext, String imagePath) {
        String subPath= servletContext.getInitParameter("images_folder");
        subPath="/images_folder/";
        String defaultPath = null;
        String path = null;
        try {
            defaultPath = Paths.get(ImageEncoder.class.getClassLoader().getResource(default_filePath).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        path = defaultPath;
        if(imagePath!=null)
        {
            path=defaultPath.replace(default_filePath,"");
            path=path + "..\\..\\..\\..\\..\\images_folder\\" + imagePath;
        }

        File file = new File(path);
        path = Paths.get(path).toAbsolutePath().toString();
        byte[] imageBytes=null;
        try {
            imageBytes = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            try {
                imageBytes = Files.readAllBytes(Paths.get(defaultPath));
            } catch (IOException ex) {
                return null;
            }
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
