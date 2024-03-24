package org.example;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class G_AnyOf2 {
    public static void main(String[] args) {

        Supplier<Weather> fetchWeatherA = () -> {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
            return new Weather("Server A", "Sunny");
        };
        
        Supplier<Weather> fetchWeatherB = () -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
            return new Weather("Server B", "Almost Sunny");
        };

        CompletableFuture<Weather> taskA = CompletableFuture.supplyAsync(fetchWeatherA);
        CompletableFuture<Weather> taskB = CompletableFuture.supplyAsync(fetchWeatherB);

        // in all case B is faster than A, but when change the sleep of taskA to 11ms than may be B is faster than A
        CompletableFuture.anyOf(taskA,taskB)
                .thenAccept(System.out::println)
                .join();

        // anyOf doesn't give you the guarantee that you will get the result as soon
        System.out.println("taskA = " + taskA); // Not complete => if change sleep to 11ms this will be : Complete normally
        System.out.println("taskB = " + taskB); // Complete normally
    }

    record Weather(String server, String weather) {
    }

}
