package lab.order.catalog;

import lab.order.domain.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final ProductRepository products;
    private final InventoryRepository inventory;

    public CatalogService(ProductRepository products, InventoryRepository inventory) {
        this.products = products;
        this.inventory = inventory;
    }

    public record ProductWithStock(Product product, int available) {
    }

    /** One query for the page of products and one for their stock (not one stock query per product). */
    @Transactional(readOnly = true)
    public Page<ProductWithStock> search(String category, String namePrefix, Pageable pageable) {
        Page<Product> page = (namePrefix == null || namePrefix.isBlank())
                ? products.findByCategory(category, pageable)
                : products.findByCategoryAndNameStartingWithIgnoreCase(category, namePrefix, pageable);
        Map<Long, Integer> stock = inventory.findAllById(page.map(Product::getId).getContent()).stream()
                .collect(Collectors.toMap(Inventory::getProductId, Inventory::getAvailable));
        return page.map(p -> new ProductWithStock(p, stock.getOrDefault(p.getId(), 0)));
    }

    @Transactional(readOnly = true)
    public ProductWithStock get(Long id) {
        Product product = products.findById(id).orElseThrow(() -> new NotFoundException("product", id));
        int available = inventory.findById(id).map(Inventory::getAvailable).orElse(0);
        return new ProductWithStock(product, available);
    }
}
