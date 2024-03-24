package org.example;

import java.util.concurrent.ExecutionException;

public class RunAll {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
    A_RunSynchronousTasks.run();
    B_RunAsynchronousTask.run();
    C_AsynchronousCompletionStage.run();
    }

}
