package org.example.ex1;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Main {
    private static int value = 0;
    private static int numberOfThreads = 0;
    private static int timeInSeconds = 0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Active threads number: ");
        numberOfThreads = sc.nextInt();
        System.out.print("Time needed in seconds: ");
        timeInSeconds = sc.nextInt();

        if (numberOfThreads < 0 || timeInSeconds < 0) {
            System.out.println("Do not pass negative numbers");
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            MyThread myThread = new MyThread();
            executor.execute(myThread);
        }
        executor.shutdown();

        try {
            if(!executor.awaitTermination(timeInSeconds, TimeUnit.SECONDS)){
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.out.println("Exception occurred here");
        }

        System.out.println("our global value value is: " +value);
    }


    static class MyThread implements Runnable {
        private Random randomOperationGenerator = new Random();
        private Semaphore mutex = new Semaphore(1);

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                int randomOperation = randomOperationGenerator.nextInt(2);
                acquireMutex();
                if (randomOperation == 1) {
                    value -= 1;
                } else {
                    value += 1;
                }
                mutex.release();
            }
        }

        private void acquireMutex() {
            try {
                mutex.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
