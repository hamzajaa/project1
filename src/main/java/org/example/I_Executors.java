package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

public class I_Executors {

    record Quotation(String server, int amount) {
    }

    record Weather(String server, String weather) {
    }

    record TravelPage(Quotation quotation, Weather weather) {
    }

    public static void main(String[] args) {
        run();
    }

    private static int qoutationThreadIndex = 0;
    private static int weatherThreadIndex = 0;
    private static int minThreadIndex = 0;
    static ThreadFactory quotationThreadFactory = (r) -> new Thread(r, "Quotation-" + qoutationThreadIndex++);
    static ThreadFactory weatherThreadFactory = (r) -> new Thread(r, "Weather-" + weatherThreadIndex++);
    static ThreadFactory minThreadFactory = (r) -> new Thread(r, "Min-" + minThreadIndex++);

    public static void run() {

        ExecutorService quotationExecutorService = Executors.newFixedThreadPool(4, quotationThreadFactory);
        ExecutorService weatherExecutorService = Executors.newFixedThreadPool(4, weatherThreadFactory);
        ExecutorService minExecutorService = Executors.newFixedThreadPool(1, minThreadFactory);

        Random random = new Random();

        List<Supplier<Weather>> weatherTasks = buildWeatherTasks(random);
        List<Supplier<Quotation>> quotationTasks = buildQuotationTasks(random);

        List<CompletableFuture<Weather>> weatherCFs = new ArrayList<>();
        for (Supplier<Weather> task : weatherTasks) {
            CompletableFuture<Weather> future = CompletableFuture
                    .supplyAsync(task, quotationExecutorService); // run in it's out ThreadPool if remove the quotationExecutorService from this function will be able to see the different in the console
            weatherCFs.add(future );
        }
        CompletableFuture<Weather> anyOfWeathers = CompletableFuture
                .anyOf(weatherCFs.toArray(CompletableFuture[]::new))
                .thenApply(o -> (Weather) o);


        List<CompletableFuture<Quotation>> quotationCFs = new ArrayList<>();
        for (Supplier<Quotation> task : quotationTasks) {
            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(task, weatherExecutorService);
            quotationCFs.add(future);
        }
        CompletableFuture<Void> allOfQuotations = CompletableFuture
                .allOf(quotationCFs.toArray(CompletableFuture[]::new));

        CompletableFuture<Quotation> bestQuotationCF = allOfQuotations.thenApplyAsync(
                v -> {
                    System.out.println("AllOf then apply " + Thread.currentThread());
                    return quotationCFs.stream()
                            .map(CompletableFuture::join)
                            .min(Comparator.comparing(Quotation::amount))
                            .orElseThrow();
                },
                minExecutorService
        );

        CompletableFuture<Void> done = bestQuotationCF
                .thenCompose(
                        quotation ->
                                anyOfWeathers.thenApply(
                                        weather -> new TravelPage(quotation, weather))
                ).thenAccept(System.out::println);
        done.join();

        quotationExecutorService.shutdown();
        weatherExecutorService.shutdown();
        minExecutorService.shutdown();


    }


    private static List<Supplier<Weather>> buildWeatherTasks(Random random) {

        Supplier<Weather> fetchWeatherA =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    System.out.println("WA running in " + Thread.currentThread());
                    return new Weather("Server A", "Sunny");
                };

        Supplier<Weather> fetchWeatherB =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    System.out.println("WA running in " + Thread.currentThread());
                    return new Weather("Server B", " ");
                };

        Supplier<Weather> fetchWeatherC =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    System.out.println("WC running in " + Thread.currentThread());
                    return new Weather("Server C", "Almost Sunny");
                };

        return new ArrayList<>(List.of(fetchWeatherA, fetchWeatherB, fetchWeatherC));
    }

    private static List<Supplier<Quotation>> buildQuotationTasks(Random random) {

        Supplier<Quotation> fetchQuotationA =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    System.out.println("QA running in " + Thread.currentThread());
                    return new Quotation("Server A", random.nextInt(40, 60));
                };

        Supplier<Quotation> fetchQuotationB =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    System.out.println("QB running in " + Thread.currentThread());
                    return new Quotation("Server B", random.nextInt(30, 70));
                };

        Supplier<Quotation> fetchQuotationC =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    System.out.println("QC running in " + Thread.currentThread());
                    return new Quotation("Server C", random.nextInt(40, 80));
                };

        return new ArrayList<>(List.of(fetchQuotationA, fetchQuotationB, fetchQuotationC));
    }
}
