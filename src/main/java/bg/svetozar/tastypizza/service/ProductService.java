package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.entity.Product;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;


    public Product getByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public BigDecimal getBasePriceOrThrow(Long id) {
        return productRepository.findBasePriceById(id)
                .orElseThrow(() -> new IllegalArgumentException("Base price not found for product: " + id));
    }

    public ProductType getTypeOrThrow(Long id) {
        return productRepository.findTypeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product type not found for product: " + id));
    }

    public Product createProduct(
            String name,
            String description,
            BigDecimal basePrice,
            ProductType type,
            String imageBase64
    ) {
        String imageUrl = cloudinaryService.uploadBase64Image(imageBase64);

        Product product = Product.builder()
                .name(name)
                .description(description)
                .basePrice(basePrice)
                .type(type)
                .imageUrl(imageUrl)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        return productRepository.save(product);
    }
    public Product updateProduct(
            Long id,
            String name,
            String description,
            BigDecimal basePrice,
            ProductType type,
            String newImageBase64
    ) {
        Product product = getByIdOrThrow(id);

        product.setName(name);
        product.setDescription(description);
        product.setBasePrice(basePrice);
        product.setType(type);

        if (newImageBase64 != null && !newImageBase64.isBlank()) {
            String oldUrl = product.getImageUrl();
            String newUrl = cloudinaryService.uploadBase64Image(newImageBase64);
            product.setImageUrl(newUrl);

            if (oldUrl != null && !oldUrl.isBlank()) {
                cloudinaryService.deleteByUrl(oldUrl);
            }
        }

        return product;
    }



    public void softDelete(Long id) {
        Product product = getByIdOrThrow(id);
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
    }
    public void restoreDeletedProduct(Long id) {
        Product product = getByIdOrThrow(id);
        product.setDeleted(false);
        product.setDeletedAt(null);
    }
}
