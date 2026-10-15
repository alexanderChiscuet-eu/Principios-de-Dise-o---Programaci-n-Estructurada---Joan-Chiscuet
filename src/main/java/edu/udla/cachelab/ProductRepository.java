package edu.udla.cachelab;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbc;
    private final AtomicLong reads = new AtomicLong();
    public ProductRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<Product> find(int limit) {
        reads.incrementAndGet();
        return List.copyOf(jdbc.query("SELECT id,name,category,price FROM products ORDER BY id LIMIT ?",
            (r,n) -> new Product(r.getLong(1),r.getString(2),r.getString(3),r.getBigDecimal(4)),limit));
    }
    public long reads() { return reads.get(); }
    public long count() { return jdbc.queryForObject("SELECT COUNT(*) FROM products",Long.class); }
}
