package com.example.productservice.service;

import com.example.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
public class ProductPriceService {

    private static final Logger logger = LoggerFactory.getLogger(ProductPriceService.class);

    private final ProductRepository productRepository;

    public ProductPriceService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @Cacheable(
            value = "productPrices",
            key = "#productId",
            condition = "#productId != null && !#productId.isBlank()"
    )
    public Integer getProductPrice(String productId) {
        // Fail-fast validation — ném exception ngay nếu input không hợp lệ
        validateProductId(productId);

        logger.info("🔍 Cache MISS cho sản phẩm '{}' — truy vấn Database", productId);
        Integer price = productRepository.findPriceById(productId);

        if (price == null) {
            logger.warn("⚠️ Sản phẩm '{}' không tồn tại trong Database", productId);
        }

        return price;
    }

    
    @CacheEvict(
            value = "productPrices",
            key = "#productId"
    )
    public void updateProductPrice(String productId, Integer newPrice) {
        // Fail-fast validation
        validateProductId(productId);
        validatePrice(newPrice);

        logger.info("💰 Cập nhật giá sản phẩm '{}': {}đ — Cache sẽ bị evict", productId, newPrice);
        productRepository.updatePrice(productId, newPrice);
        logger.info("✅ Đã cập nhật giá và evict cache cho sản phẩm '{}'", productId);
    }

   
    private void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException(
                    "productId không được null hoặc rỗng. Giá trị nhận được: '"
                            + productId + "'"
            );
        }
    }

  
    private void validatePrice(Integer price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException(
                    "Giá sản phẩm phải là số dương. Giá trị nhận được: " + price
            );
        }
    }
}
