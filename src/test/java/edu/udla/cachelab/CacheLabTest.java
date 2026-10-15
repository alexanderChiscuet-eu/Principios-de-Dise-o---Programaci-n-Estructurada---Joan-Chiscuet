package edu.udla.cachelab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CacheLabTest {
    @Autowired BenchmarkService benchmark;
    @Autowired ProductService products;
    @Autowired ProductRepository repository;
    @Autowired MockMvc mvc;
    @Test void seededAndCacheAvoidsSqlWithoutChangingData() {
        assertThat(repository.count()).isEqualTo(100000);
        benchmark.clear();
        long before=repository.reads();
        var cold=products.cached(1000);
        var hit=products.cached(1000);
        assertThat(hit).isEqualTo(cold);
        assertThat(hit).hasSize(1000);
        assertThat(repository.reads()-before).isEqualTo(1);
        products.cached(500);
        assertThat(repository.reads()-before).isEqualTo(2);
        benchmark.clear(); products.cached(1000);
        assertThat(repository.reads()-before).isEqualTo(3);
    }
    @Test void benchmarkReportsColdMissAndWarmHits() {
        var result=benchmark.compare(1000,5);
        assertThat(result.cold().sqlQueries()).isEqualTo(1);
        assertThat(result.withoutCache().sqlQueries()).isEqualTo(5);
        assertThat(result.withCache().sqlQueries()).isZero();
        assertThat(result.withCache().samplesMs()).hasSize(5);
    }
    @Test void httpValidationAndCors() throws Exception {
        mvc.perform(get("/api/products/cached?limit=100001")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/benchmark?runs=2")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/products/uncached?limit=12")).andExpect(status().isOk()).andExpect(jsonPath("$.records").value(12)).andExpect(jsonPath("$.sqlQueries").value(1));
        mvc.perform(options("/api/benchmark").header("Origin","http://localhost:5173").header("Access-Control-Request-Method","POST"))
           .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
        mvc.perform(options("/api/cache").header("Origin","http://127.0.0.1:5173").header("Access-Control-Request-Method","DELETE"))
           .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://127.0.0.1:5173"));
        mvc.perform(options("/api/cache").header("Origin","https://untrusted.example").header("Access-Control-Request-Method","DELETE"))
           .andExpect(status().isForbidden());
        mvc.perform(get("/")).andExpect(status().isOk());
    }
}
