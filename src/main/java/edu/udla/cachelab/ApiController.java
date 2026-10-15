package edu.udla.cachelab;

import java.util.Map;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Validated
public class ApiController {
    private final BenchmarkService benchmark;
    private final ProductRepository repository;
    public ApiController(BenchmarkService benchmark,ProductRepository repository) { this.benchmark=benchmark; this.repository=repository; }
    @GetMapping("/info") public Map<String,Object> info() { return Map.of("records",repository.count(),"cache","Caffeine","ttlMinutes",10,"maxEntries",8); }
    @GetMapping("/products/uncached") public BenchmarkService.Measurement uncached(@RequestParam(defaultValue="100000") @Min(1) @Max(100000) int limit) { return benchmark.read(limit,false); }
    @GetMapping("/products/cached") public BenchmarkService.Measurement cached(@RequestParam(defaultValue="100000") @Min(1) @Max(100000) int limit) { return benchmark.read(limit,true); }
    @PostMapping("/benchmark") public BenchmarkService.Comparison compare(@RequestParam(defaultValue="100000") @Min(1) @Max(100000) int limit,@RequestParam(defaultValue="10") @Min(3) @Max(30) int runs) { return benchmark.compare(limit,runs); }
    @DeleteMapping("/cache") public Map<String,String> clear() { benchmark.clear(); return Map.of("message","Caché vaciada"); }
}
