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
                        ErrorMessage.PRODUCT_NOT_FOUND,
                        ErrorCode.PRODUCT_NOT_FOUND,
                        ErrorContext.of("productId", id)
                ));
    }

    @Transactional(readOnly = true)
    public BigDecimal getBasePriceOrThrow(Long id) {
        return productRepository.findBasePriceById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.BASE_PRICE_NOT_FOUND_FOR_PRODUCT,
                        ErrorCode.PRODUCT_BASE_PRICE_NOT_FOUND,
                        ErrorContext.of("productId", id)
                ));
    }

    @Transactional(readOnly = true)
    public ProductType getTypeOrThrow(Long id) {
        return productRepository.findTypeById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.PRODUCT_TYPE_NOT_FOUND,
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
                    ErrorMessage.REQUIRED_IMAGE,
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
                    ErrorMessage.PRODUCT_ALREADY_DELETED,
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
                    ErrorMessage.PRODUCT_NOT_DELETED,
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
                    ErrorMessage.REQUIRED_PRICE,
                    ErrorCode.INVALID_PRICE,
                    ErrorContext.of("field", "basePrice")
            );
        }
        if (basePrice.signum() < 0) {
            throw new BadRequestException(
                    ErrorMessage.INVALID_PRICE_MUST_BE_POSITIVE,
                    ErrorCode.INVALID_PRICE,
                    ErrorContext.of("field", "basePrice", "value", basePrice)
            );
        }
    }
}
