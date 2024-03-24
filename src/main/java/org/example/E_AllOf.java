package org.example;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class E_AllOf {
    public static void main(String[] args) {

        Random random = new Random();

        // Build a list of Supplier tasks that produce Quotation objects
        List<Supplier<Quotation>> quotationTasks = buildQuotationTasks(random);

        // Create a list to store CompletableFuture instances for each quotation task
        List<CompletableFuture<Quotation>> quotationCFS = new ArrayList<>();
        for (Supplier<Quotation> task : quotationTasks) {

            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(task);
            quotationCFS.add(future);
        }

        // Combine all CompletableFuture instances into a single CompletableFuture that completes when all are done
        CompletableFuture<Void> allOf = CompletableFuture.allOf(quotationCFS.toArray(CompletableFuture[]::new));

        // Create an Optional that represents the result of the best quotation
        Optional<Quotation> bestQuotation = allOf.thenApply( // execute that when all futures complete
                v -> quotationCFS.stream()
                        .map(CompletableFuture::join)
                        .min(Comparator.comparing(Quotation::amount))
        ).join();

        System.out.println("bestQuotation = " + bestQuotation);
    }

    record Quotation(String server, int amount) {
    }

    private static List<Supplier<Quotation>> buildQuotationTasks(Random random) {

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

        return new ArrayList<>(List.of(fetchQuotationA, fetchQuotationB, fetchQuotationC));
    }
}
