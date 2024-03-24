package org.example;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class F_AnyOff {

    public static void main(String[] args) {

        Random random = new Random();

        // Build a list of Supplier tasks that produce Weather objects
        List<Supplier<Weather>> weatherTasks = buildWeatherTasks(random);

        List<CompletableFuture<Weather>> weatherCFS = new ArrayList<>();
        for (Supplier<Weather> task : weatherTasks) {

            CompletableFuture<Weather> future = CompletableFuture.supplyAsync(task);
            weatherCFS.add(future);
        }

        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(weatherCFS.toArray(CompletableFuture[]::new));

        anyOf.thenAccept(System.out::println).join();

    }

    record Weather(String server, String weather) {
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
}
