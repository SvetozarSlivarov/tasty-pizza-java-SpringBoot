package bg.svetozar.tastypizza.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:products}")
    private String folder;

    public String uploadBase64Image(String base64Image) {
        if (base64Image == null || base64Image.isBlank()) {
            return null;
        }

        try {
            String pureBase64 = base64Image;
            if (base64Image.contains(",")) {
                pureBase64 = base64Image.split(",", 2)[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(pureBase64);

            Map uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap(
                    "folder", folder
            ));

            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    public void deleteByUrl(String url) {
        if (url == null || url.isBlank()) {
            return;
        }

        try {
            String[] parts = url.split("/");
            String lastPart = parts[parts.length - 1]; // abc123.png
            int dotIndex = lastPart.lastIndexOf('.');
            if (dotIndex < 0) {
                return;
            }

            String publicIdWithoutExt = lastPart.substring(0, dotIndex);

            String folderPath = folder.endsWith("/") ? folder : folder + "/";
            String publicId = folderPath + publicIdWithoutExt;

            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
        }
    }
}
