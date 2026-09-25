package lab.order.report;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Read-side reporting in plain SQL (JdbcClient): an aggregate over many rows is a set operation for the
 * database, not a job for loading entities into the persistence context.
 */
@Service
public class ReportService {

    static final int MAX_DAYS = 366;

    private final JdbcClient jdbc;

    public ReportService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public record DailySales(LocalDate day, long orders, BigDecimal revenue, BigDecimal averageOrderValue) {
    }

    /** Paid orders per UTC day in [from, to). Uses idx_order_created for the date range. */
    public List<DailySales> dailySales(LocalDate from, LocalDate to) {
        if (!from.isBefore(to) || ChronoUnit.DAYS.between(from, to) > MAX_DAYS) {
            throw new IllegalArgumentException("need from < to and at most " + MAX_DAYS + " days");
        }
        return jdbc.sql("""
                        SELECT (created_at AT TIME ZONE 'UTC')::date AS day,
                               count(*)                          AS orders,
                               sum(total_amount)                 AS revenue
                        FROM purchase_order
                        WHERE status = 'PAID'
                          AND created_at >= :from AND created_at < :to
                        GROUP BY 1
                        ORDER BY 1
                        """)
                .param("from", from.atStartOfDay().atOffset(ZoneOffset.UTC))
                .param("to", to.atStartOfDay().atOffset(ZoneOffset.UTC))
                .query((rs, row) -> {
                    long orders = rs.getLong("orders");
                    BigDecimal revenue = rs.getBigDecimal("revenue");
                    return new DailySales(rs.getObject("day", LocalDate.class), orders, revenue,
                            revenue.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP));
                })
                .list();
    }
}
