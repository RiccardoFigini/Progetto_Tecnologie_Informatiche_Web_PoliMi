package utils;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageEncoder {
    private static String default_filePath="defaultImage.png";
    public static String encodeImage(ServletContext servletContext, String imagePath) {
        String path= servletContext.getInitParameter("images_folder");
        if(imagePath==null)
        {
            path=path+default_filePath;
        }
        else
        {
            path=path+imagePath;
        }
        File file = new File(path);

        byte[] imageBytes=null;
        try {
            imageBytes = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            return null;
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
