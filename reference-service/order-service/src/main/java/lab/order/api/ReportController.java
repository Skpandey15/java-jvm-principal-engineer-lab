package lab.order.api;

import lab.order.report.ReportService;
import lab.order.report.ReportService.DailySales;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
class ReportController {

    private final ReportService reports;

    ReportController(ReportService reports) {
        this.reports = reports;
    }

    /** Example: GET /reports/daily-sales?from=2026-09-01&to=2026-09-26 (to is exclusive) */
    @GetMapping("/daily-sales")
    List<DailySales> dailySales(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reports.dailySales(from, to);
    }
}
