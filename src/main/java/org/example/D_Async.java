package org.example;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class D_Async {

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
        

//        List<Quotation> quotations = new ArrayList<>();
//        for (CompletableFuture<Quotation> future : futures) { // this coe is blocking Thread.sleep(500);
//            // launched tasks in another thread these tasks are going to take some time to complete in the order of 100ms, and the main thread is done once all the tasks have been launched.
//            future.thenAccept(System.out::println); // doesn't print anything until add Thread.sleep(500);
//        }

//        Thread.sleep(500); // but this is not the right pattern

//        List<Quotation> quotations = new ArrayList<>(); // ArrayList is clearly not a concurrentList => SO THIS code not work
        Collection<Quotation> quotations = new ConcurrentLinkedDeque<>(); // noe i have thread-safe collection, and i can add element safely to it from threads
        List<CompletableFuture<Void>> voids = new ArrayList<>();
        for (CompletableFuture<Quotation> future : futures) { // this code non-blocking
            future.thenAccept(System.out::println);
            CompletableFuture<Void> accept = future.thenAccept(quotations::add);
            voids.add(accept); // each CompletableFuture will carry the information when the task will complete
        }

        for (CompletableFuture<Void> v : voids) {
            v.join(); // to make sure that the main thread does not die too early => add all element to teh Collection
        }

        System.out.println("quotations = " + quotations);

        Quotation bestQuotation = quotations.stream()
                .min(Comparator.comparing(Quotation::amount))
                .orElseThrow();

        Instant end = Instant.now();
        Duration duration = Duration.between(begin, end);
        System.out.println("Best quotation [ASYNC ] = " + bestQuotation +
                " (" + duration.toMillis() + "ms)"); // the result is the same as the ExecutorService pattern


    }

}
