package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.BadRequestException;
import bg.svetozar.tastypizza.exception.ErrorCode;
import bg.svetozar.tastypizza.exception.ErrorContext;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:products}")
    private String folder;

    public String uploadBase64Image(String base64Image) {
        if (!StringUtils.hasText(base64Image)) {
            return null;
        }

        try {
            String pureBase64 = base64Image;
            if (base64Image.contains(",")) {
                pureBase64 = base64Image.split(",", 2)[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(pureBase64);

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap("folder", folder)
            );

            return String.valueOf(uploadResult.get("secure_url"));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid base64 image data",
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("field", "image")
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    public void deleteByUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }

        try {
            String[] parts = url.split("/");
            String lastPart = parts[parts.length - 1]; // abc123.png
            int dotIndex = lastPart.lastIndexOf('.');
            if (dotIndex < 0) {
                log.warn("Cloudinary delete skipped: URL has no extension. url={}", url);
                return;
            }

            String publicIdWithoutExt = lastPart.substring(0, dotIndex);

            String folderPath = folder.endsWith("/") ? folder : folder + "/";
            String publicId = folderPath + publicIdWithoutExt;

            Map<String, Object> res = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String result = String.valueOf(res.get("result")); // "ok", "not found", ...

            if (!"ok".equalsIgnoreCase(result)) {
                log.warn("Cloudinary delete returned result='{}'. publicId={} url={}", result, publicId, url);
            }
        } catch (Exception e) {
            log.warn("Failed to delete image from Cloudinary. url={}", url, e);
        }
    }
}
