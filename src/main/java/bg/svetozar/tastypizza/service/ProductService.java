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

    public Product createProduct(Product product) {
        return productRepository.save(product);
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
