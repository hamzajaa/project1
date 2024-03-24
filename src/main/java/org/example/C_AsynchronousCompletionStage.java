package org.example;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class C_AsynchronousCompletionStage {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        run();
    }

    record Quotation(String server, int amount) {
    }

    public static void run() throws ExecutionException, InterruptedException {

        Random random = new Random();

        Supplier<Quotation> fetchQuotationA =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    return new Quotation("Server A", random.nextInt(40, 60));
                };

        Supplier<Quotation> fetchQuotationB =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    return new Quotation("Server B", random.nextInt(30, 70));
                };

        Supplier<Quotation> fetchQuotationC =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    return new Quotation("Server C", random.nextInt(40, 80));
                };

        List<Supplier<Quotation>> quotationTasks = List.of(fetchQuotationA, fetchQuotationB, fetchQuotationC);

        Instant begin = Instant.now();

        List<CompletableFuture<Quotation>> futures = new ArrayList<>();
        for (Supplier<Quotation> task : quotationTasks) { // CompletableFuture is an impl of both Future interface and CompletionStage API
            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(task);
            futures.add(future);
        }

        List<Quotation> quotations = new ArrayList<>();
        for (CompletableFuture<Quotation> future : futures) {
            Quotation quotation = future.join(); // same as get(), but join() doesn't throw exception
            quotations.add(quotation);
        }

        Quotation bestQuotation = quotations.stream()
                .min(Comparator.comparing(Quotation::amount))
                .orElseThrow();

        Instant end = Instant.now();
        Duration duration = Duration.between(begin, end);
        System.out.println("Best quotation [ASYNC ] = " + bestQuotation +
                " (" + duration.toMillis() + "ms)"); // the result is the same as the ExecutorService pattern

    }

}
