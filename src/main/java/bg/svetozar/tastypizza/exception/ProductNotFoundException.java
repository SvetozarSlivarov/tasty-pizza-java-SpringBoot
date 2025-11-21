package bg.svetozar.tastypizza.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(Long id) {
        super("Product with id: " + id + " not found",
                ErrorCode.PRODUCT_NOT_FOUND,
                HttpStatus.NOT_FOUND);
    }
}
