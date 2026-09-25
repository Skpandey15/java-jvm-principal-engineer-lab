package lab.order.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lab.order.api.ApiModels.PageResponse;
import lab.order.api.ApiModels.ProductResponse;
import lab.order.catalog.CatalogService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
class CatalogController {

    private final CatalogService catalog;

    CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    /** Example: GET /products?category=Books&q=Smart&page=0&size=20 */
    @GetMapping
    PageResponse<ProductResponse> search(@RequestParam @NotBlank String category,
                                         @RequestParam(required = false) String q,
                                         @RequestParam(defaultValue = "0") @Min(0) int page,
                                         @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.from(catalog.search(category, q, PageRequest.of(page, size, Sort.by("name"))),
                ProductResponse::from);
    }

    @GetMapping("/{id}")
    ProductResponse get(@PathVariable Long id) {
        return ProductResponse.from(catalog.get(id));
    }
}
