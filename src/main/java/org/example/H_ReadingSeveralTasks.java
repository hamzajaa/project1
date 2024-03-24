package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class H_ReadingSeveralTasks {

    record TravelPage(Quotation quotation, Weather weather) {
    }

    record Quotation(String server, int amount) {
    }

    record Weather(String server, String weather) {
    }

    public static void main(String[] args) {
        run();
    }

    private static void run() {
        Random random = new Random();

        List<Supplier<Weather>> weatherTasks = buildWeatherTasks(random);
        List<Supplier<Quotation>> quotationTasks = buildQuotationTasks(random);

        List<CompletableFuture<Weather>> weatherCFs = new ArrayList<>();
        for (Supplier<Weather> task : weatherTasks) {
            CompletableFuture<Weather> future = CompletableFuture.supplyAsync(task);
            weatherCFs.add(future);
        }

        CompletableFuture<Weather> anyWeather = CompletableFuture.anyOf(weatherCFs.toArray(CompletableFuture[]::new))
                .thenApply(o -> (Weather) o); // do typeCasting because the return type is CompletableFuture of Weather

        List<CompletableFuture<Quotation>> quotationCFs = new ArrayList<>();
        for (Supplier<Quotation> task : quotationTasks) {
            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(task);
            quotationCFs.add(future);
        }

        CompletableFuture<Void> allOfQuotations = CompletableFuture.allOf(quotationCFs.toArray(CompletableFuture[]::new));
        CompletableFuture<Quotation> bestQuotationCF = allOfQuotations.thenApply(
                v -> quotationCFs.stream()
                        .map(CompletableFuture::join)
                        .min(Comparator.comparing(Quotation::amount))
                        .orElseThrow()
        );

//        TravelPage page = new TravelPage(bestQuotationCF.join(),anyWeather.join()); // is a blocking code
//        System.out.println("page = " + page);

        CompletableFuture<TravelPage> pageCompletableFuture =
                bestQuotationCF.thenCombine(
                        anyWeather, (q, w) -> new TravelPage(q, w));
        pageCompletableFuture.thenAccept(System.out::println).join(); // join() to show it in the console

        CompletableFuture<TravelPage> pageCompletableFuture2 = bestQuotationCF.thenCompose(
                quotation -> anyWeather
                        .thenApply(weather -> new TravelPage(quotation, weather))
        );
        pageCompletableFuture2.thenAccept(System.out::println).join();

        //// simplify all that
        CompletableFuture<TravelPage> pageCompletableFuture3 =
                bestQuotationCF.thenCombine(
                        CompletableFuture.anyOf(weatherCFs.toArray(CompletableFuture[]::new))
                                .thenApply(o -> (Weather) o), TravelPage::new
                );
        pageCompletableFuture3.thenAccept(System.out::println).join(); // not show weather

        CompletableFuture<TravelPage> pageCompletableFuture4 = bestQuotationCF.thenCompose(
                quotation ->
                        CompletableFuture.anyOf(weatherCFs.toArray(CompletableFuture[]::new))
                                .thenApply(o -> (Weather) o)
                                .thenApply(weather -> new TravelPage(quotation, weather))
        );
        pageCompletableFuture4.thenAccept(System.out::println).join(); // not show weather

    }

    private static List<Supplier<Weather>> buildWeatherTasks(Random random) {

        Supplier<Weather> fetchWeatherA =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    return new Weather("Server A", "Sunny");
                };

        Supplier<Weather> fetchWeatherB =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
                    return new Weather("Server B", " ");
                };

        Supplier<Weather> fetchWeatherC =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException();
                    }
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
