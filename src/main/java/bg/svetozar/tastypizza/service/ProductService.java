package bg.svetozar.tastypizza.service;


import bg.svetozar.tastypizza.exception.*;
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


    @Transactional(readOnly = true)
    public Product getByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found",
                        ErrorCode.PRODUCT_NOT_FOUND,
                        ErrorContext.of("productId", id)
                ));
    }

    @Transactional(readOnly = true)
    public BigDecimal getBasePriceOrThrow(Long id) {
        return productRepository.findBasePriceById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Base price not found for product",
                        ErrorCode.PRODUCT_BASE_PRICE_NOT_FOUND,
                        ErrorContext.of("productId", id)
                ));
    }

    @Transactional(readOnly = true)
    public ProductType getTypeOrThrow(Long id) {
        return productRepository.findTypeById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Product type not found for product",
                        ErrorCode.PRODUCT_TYPE_NOT_FOUND,
                        ErrorContext.of("productId", id)
                ));
    }

    public Product createProduct(
            String name,
            String description,
            BigDecimal basePrice,
            ProductType type,
            String imageBase64
    ) {
        validateBasePrice(basePrice);

        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new BadRequestException(
                    "Image is required",
                    ErrorCode.IMAGE_REQUIRED,
                    ErrorContext.of("field", "imageBase64")
            );
        }

        String imageUrl = cloudinaryService.uploadBase64Image(imageBase64);

        Product product = Product.builder()
                .name(name)
                .description(description)
                .basePrice(basePrice)
                .type(type)
                .imageUrl(imageUrl)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .deletedAt(null)
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
        validateBasePrice(basePrice);

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

        if (product.isDeleted()) {
            throw new ConflictException(
                    "Product is already deleted",
                    ErrorCode.PRODUCT_ALREADY_DELETED,
                    ErrorContext.of("productId", id)
            );
        }

        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
    }

    public void restoreDeletedProduct(Long id) {
        Product product = getByIdOrThrow(id);

        if (!product.isDeleted()) {
            throw new ConflictException(
                    "Product is not deleted",
                    ErrorCode.PRODUCT_NOT_DELETED,
                    ErrorContext.of("productId", id)
            );
        }

        product.setDeleted(false);
        product.setDeletedAt(null);
    }


    private void validateBasePrice(BigDecimal basePrice) {
        if (basePrice == null) {
            throw new BadRequestException(
                    "Base price is required",
                    ErrorCode.INVALID_PRICE,
                    ErrorContext.of("field", "basePrice")
            );
        }
        if (basePrice.signum() < 0) {
            throw new BadRequestException(
                    "Base price must be >= 0",
                    ErrorCode.INVALID_PRICE,
                    ErrorContext.of("field", "basePrice", "value", basePrice)
            );
        }
    }
}
