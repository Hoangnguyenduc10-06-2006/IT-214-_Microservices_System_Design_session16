package com.example.productservice.service;

import com.example.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service xử lý giá sản phẩm — ĐÃ SỬA từ Local Cache sang Redis Distributed Cache.
 *
 * === TRƯỚC KHI SỬA (code gốc có bug) ===
 *
 * private final Map<String, Integer> localPriceCache = new HashMap<>();
 *
 * public Integer getProductPrice(String productId) {
 *     if (localPriceCache.containsKey(productId)) {
 *         return localPriceCache.get(productId);   // ← Mỗi instance cache riêng!
 *     }
 *     Integer price = productRepository.findPriceById(productId);
 *     localPriceCache.put(productId, price);
 *     return price;
 * }
 *
 * public void updateProductPrice(String productId, Integer newPrice) {
 *     productRepository.updatePrice(productId, newPrice);
 *     localPriceCache.put(productId, newPrice);    // ← Chỉ update cache của instance này!
 * }
 *
 * === SAU KHI SỬA ===
 * - Sử dụng @Cacheable / @CacheEvict để Spring Cache quản lý Redis tự động.
 * - Tất cả instance chia sẻ cùng một Redis → giá luôn nhất quán.
 * - Có validation đầu vào và xử lý ngoại lệ.
 */
@Service
public class ProductPriceService {

    private static final Logger logger = LoggerFactory.getLogger(ProductPriceService.class);

    private final ProductRepository productRepository;

    public ProductPriceService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Lấy giá sản phẩm — tự động cache vào Redis.
     *
     * @Cacheable hoạt động:
     * 1. Spring kiểm tra Redis có key "productPrices::P001" không.
     * 2. Nếu CÓ (cache hit) → trả kết quả từ Redis, KHÔNG gọi method.
     * 3. Nếu KHÔNG (cache miss) → gọi method, lưu kết quả vào Redis.
     *
     * condition: Chỉ cache khi productId hợp lệ → tránh tạo key rác trên Redis.
     *
     * @param productId ID sản phẩm
     * @return Giá sản phẩm (VNĐ)
     * @throws IllegalArgumentException khi productId null hoặc rỗng
     */
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

    /**
     * Cập nhật giá sản phẩm — tự động xóa cache cũ trên Redis.
     *
     * @CacheEvict hoạt động:
     * 1. Gọi method để cập nhật giá trong Database.
     * 2. Sau khi thành công, XÓA key "productPrices::P001" khỏi Redis.
     * 3. Lần getProductPrice() tiếp theo sẽ cache miss → query DB lấy giá mới.
     *
     * Vì cache bị evict trên Redis (shared), TẤT CẢ instance sẽ thấy giá mới
     * ở lần truy vấn tiếp theo → đảm bảo nhất quán!
     *
     * @param productId ID sản phẩm
     * @param newPrice  Giá mới (VNĐ)
     * @throws IllegalArgumentException khi productId null/rỗng hoặc giá không hợp lệ
     */
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

    /**
     * Kiểm tra productId hợp lệ.
     * Fail-fast: ném exception ngay lập tức, không cho phép xử lý tiếp.
     */
    private void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException(
                    "productId không được null hoặc rỗng. Giá trị nhận được: '"
                            + productId + "'"
            );
        }
    }

    /**
     * Kiểm tra giá hợp lệ.
     */
    private void validatePrice(Integer price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException(
                    "Giá sản phẩm phải là số dương. Giá trị nhận được: " + price
            );
        }
    }
}
