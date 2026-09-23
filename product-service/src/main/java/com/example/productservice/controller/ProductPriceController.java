package com.example.productservice.controller;

import com.example.productservice.service.ProductPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST Controller cho API giá sản phẩm.
 */
@RestController
@RequestMapping("/api/products")
public class ProductPriceController {

    private static final Logger logger = LoggerFactory.getLogger(ProductPriceController.class);

    private final ProductPriceService productPriceService;

    public ProductPriceController(ProductPriceService productPriceService) {
        this.productPriceService = productPriceService;
    }

    /**
     * GET /api/products/{id}/price
     *
     * Lấy giá sản phẩm. Kết quả sẽ được cache trong Redis.
     * Lần gọi đầu tiên: cache miss → query DB.
     * Các lần sau: cache hit → trả kết quả từ Redis (nhanh hơn nhiều).
     */
    @GetMapping("/{id}/price")
    public ResponseEntity<Map<String, Object>> getProductPrice(@PathVariable("id") String productId) {
        logger.info(" [API] GET /api/products/{}/price", productId);

        Integer price = productPriceService.getProductPrice(productId);

        if (price == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productId", productId);
        response.put("price", price);
        response.put("currency", "VND");

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/price")
    public ResponseEntity<Map<String, Object>> updateProductPrice(
            @PathVariable("id") String productId,
            @RequestParam("newPrice") Integer newPrice) {

        logger.info(" [API] PUT /api/products/{}/price — newPrice={}", productId, newPrice);

        productPriceService.updateProductPrice(productId, newPrice);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productId", productId);
        response.put("newPrice", newPrice);
        response.put("message", "Giá đã được cập nhật và cache đã bị xóa trên Redis");

        return ResponseEntity.ok(response);
    }
}
