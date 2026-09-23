package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ProductRepository {

    private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);

    // Giả lập database table
    private final Map<String, Product> database = new ConcurrentHashMap<>();


    @PostConstruct
    public void initData() {
        database.put("P001", new Product("P001", "Tai nghe Bluetooth", 100000));
        database.put("P002", new Product("P002", "Sạc dự phòng 10000mAh", 150000));
        database.put("P003", new Product("P003", "Ốp lưng iPhone 15", 50000));

        logger.info(" Đã khởi tạo {} sản phẩm trong database giả lập", database.size());
    }


    public Integer findPriceById(String productId) {
        logger.info(" [DB QUERY] Truy vấn giá sản phẩm '{}' từ Database", productId);

        // Giả lập độ trễ database (50ms)
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Product product = database.get(productId);
        return product != null ? product.getPrice() : null;
    }

    public void updatePrice(String productId, Integer newPrice) {
        logger.info("📝 [DB UPDATE] Cập nhật giá sản phẩm '{}' → {}đ", productId, newPrice);

        Product product = database.get(productId);
        if (product != null) {
            product.setPrice(newPrice);
        } else {
            throw new RuntimeException("Sản phẩm '" + productId + "' không tồn tại trong database");
        }
    }
}
