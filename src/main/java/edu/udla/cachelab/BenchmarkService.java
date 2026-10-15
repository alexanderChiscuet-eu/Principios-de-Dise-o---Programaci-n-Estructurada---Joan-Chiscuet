package edu.udla.cachelab;

import java.util.*;
import java.util.function.Supplier;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class BenchmarkService {
    private final ProductService products;
    private final ProductRepository repository;
    private final CacheManager caches;
    public BenchmarkService(ProductService products, ProductRepository repository, CacheManager caches) {
        this.products=products; this.repository=repository; this.caches=caches;
    }
    public record Measurement(double elapsedMs,long sqlQueries,int records,List<Product> sample) {}
    public record Series(List<Double> samplesMs,double meanMs,double medianMs,long sqlQueries) {}
    public record Comparison(int limit,int runs,Measurement cold,Series withoutCache,Series withCache,double speedup,double savedPercent) {}
    private Measurement measure(Supplier<List<Product>> operation) {
        long queries=repository.reads(), start=System.nanoTime();
        List<Product> result=operation.get();
        double ms=(System.nanoTime()-start)/1_000_000.0;
        return new Measurement(ms,repository.reads()-queries,result.size(),result.stream().limit(10).toList());
    }
    public synchronized Measurement read(int limit,boolean cached) {
        return measure(() -> cached ? products.cached(limit) : products.uncached(limit));
    }
    public synchronized void clear() { Objects.requireNonNull(caches.getCache("products")).clear(); }
    public synchronized Comparison compare(int limit,int runs) {
        // Warm up JDBC, mapping and JVM; these reads are excluded from reported series.
        for(int i=0;i<3;i++) products.uncached(limit);
        clear();
        Measurement cold=measure(() -> products.cached(limit));
        List<Double> plain=new ArrayList<>(), hits=new ArrayList<>();
        long plainQueries=0, hitQueries=0;
        // Alternate order to reduce systematic ordering bias.
        for(int i=0;i<runs;i++) {
            Measurement a,b;
            if(i%2==0) { a=read(limit,false); b=read(limit,true); }
            else { b=read(limit,true); a=read(limit,false); }
            plain.add(a.elapsedMs()); hits.add(b.elapsedMs());
            plainQueries+=a.sqlQueries(); hitQueries+=b.sqlQueries();
        }
        Series a=series(plain,plainQueries), b=series(hits,hitQueries);
        return new Comparison(limit,runs,cold,a,b,a.meanMs()/Math.max(b.meanMs(),0.000001),100*(1-b.meanMs()/Math.max(a.meanMs(),0.000001)));
    }
    private Series series(List<Double> values,long queries) {
        List<Double> sorted=values.stream().sorted().toList(); int n=sorted.size();
        double median=n%2==0 ? (sorted.get(n/2-1)+sorted.get(n/2))/2 : sorted.get(n/2);
        return new Series(List.copyOf(values),values.stream().mapToDouble(Double::doubleValue).average().orElseThrow(),median,queries);
    }
}
