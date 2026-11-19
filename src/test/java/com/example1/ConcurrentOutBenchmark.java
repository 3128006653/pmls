package com.example1;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 50-thread concurrent stock-out benchmark. Standalone main, JDK-only, no third-party jar. */
public class ConcurrentOutBenchmark {

    private static final int THREADS = 50;
    private static final String BASE = System.getProperty("api.base", "http://localhost:8080/api");
    private static final String USER = System.getProperty("api.user", "admin");
    private static final String PWD  = System.getProperty("api.pwd", "123456");
    private static final String REMARK = "bench_50_out";

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"data\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CODE_200 = Pattern.compile("\"code\"\\s*:\\s*200");

    public static void main(String[] args) throws Exception {
        String base = args.length > 0 ? args[0] : BASE;
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String token = login(client, base);
        System.out.println("[OK] login success, token prefix: "
                + token.substring(0, Math.min(20, token.length())) + "...");
        System.out.println("      target : " + base + "/stocks/out");
        System.out.println("      threads: " + THREADS + ", each deducts 1");
        System.out.println("      remark : " + REMARK + "\n");

        String payload = "{\"branchId\":1,\"materialId\":1,\"quantity\":1,\"remark\":\"" + REMARK + "\"}";
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        long t0 = System.nanoTime();
        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(base + "/stocks/out"))
                            .timeout(Duration.ofSeconds(30))
                            .header("Content-Type", "application/json; charset=utf-8")
                            .header("Authorization", "Bearer " + token)
                            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                            .build();
                    HttpResponse<String> resp = client.send(req,
                            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                    if (CODE_200.matcher(resp.body()).find()) {
                        success.incrementAndGet();
                    } else {
                        rejected.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                    System.err.println("[ERR] " + e);
                } finally {
                    done.countDown();
                }
            });
        }

        startGate.countDown();
        boolean finished = done.await(60, TimeUnit.SECONDS);
        long costMs = (System.nanoTime() - t0) / 1_000_000;
        pool.shutdownNow();

        System.out.println("========== BENCHMARK RESULT ==========");
        System.out.println("elapsed        : " + costMs + " ms");
        System.out.println("success        : " + success.get());
        System.out.println("rejected       : " + rejected.get());
        System.out.println("errors         : " + errors.get());
        System.out.println("total          : " + (success.get() + rejected.get() + errors.get()));
        System.out.println("all finished   : " + finished);
        System.out.println("======================================");
        System.out.println("EXPECTED: success=30, rejected=20, errors=0, all finished=true");
    }

    private static String login(HttpClient client, String base) throws Exception {
        String payload = "{\"username\":\"" + USER + "\",\"password\":\"" + PWD + "\"}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base + "/auth/login"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Matcher m = TOKEN_PATTERN.matcher(resp.body());
        if (!CODE_200.matcher(resp.body()).find() || !m.find()) {
            throw new IllegalStateException("login failed, HTTP " + resp.statusCode()
                    + ", body: " + resp.body());
        }
        return m.group(1);
    }
}