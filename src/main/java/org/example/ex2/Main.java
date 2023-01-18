package org.example.ex2;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Main {
    private static int timeInSeconds = 0;
    static Map<Integer, Integer> clientsAccountDetails = new ConcurrentHashMap<>();
    private static int numberOfClients = 0;
    private static Random random = new Random();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Time needed in seconds: ");
        timeInSeconds = sc.nextInt();

        System.out.print("Number of Clients: ");
        numberOfClients = sc.nextInt();

        if (numberOfClients < 0 || timeInSeconds < 0) {
            System.out.println("Do not pass negative numbers");
            return;
        }

        for (int i = 0; i < numberOfClients; i++) {
            int balance = random.nextInt(5000) + 1;
            clientsAccountDetails.put(i, balance);
        }

        ExecutorService executor = Executors.newFixedThreadPool(numberOfClients);
        for (int id = 0; id < numberOfClients; id++) {
            Client client = new Client(id);
            executor.execute(client);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(timeInSeconds, TimeUnit.SECONDS);
            Thread.sleep(timeInSeconds * 1000L);
        } catch (InterruptedException e) {
            System.out.println("Exception here" + e.getMessage());
        }


        System.out.println("Balance for each account after operations...");
        for (Map.Entry<Integer, Integer> client : clientsAccountDetails.entrySet()) {
            System.out.println("Client of id: " + client.getKey() + " has this much of money: " + client.getValue());
        }

    }

    static class Client implements Runnable {
        private int clientId;
        private final Map<Integer, Consumer<Integer>> paymentOperations = new ConcurrentHashMap<>();

        public Client(int clientId) {
            this.clientId = clientId;
            paymentOperations.put(0, this::deposit);
            paymentOperations.put(1, this::withdraw);
            paymentOperations.put(2, this::transfer);
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                int currentOperation = random.nextInt(3);
                paymentOperations.get(currentOperation).accept(clientId);
            }
        }

        private void deposit(int clientId) {
            int depositedMoney = random.nextInt(500);
            int balance = clientsAccountDetails.get(clientId);
            clientsAccountDetails.put(clientId, balance + depositedMoney);
        }

        private void withdraw(int clientId) {
            int withdrawMoney = random.nextInt(1000);
            int balance = clientsAccountDetails.get(clientId);
            if (withdrawMoney > balance) {
                System.out.println("Not enough money to withdraw for client of id: " + clientId);
                Thread.currentThread().interrupt();
                return;
            }
            clientsAccountDetails.put(clientId, balance - withdrawMoney);
        }

        private void transfer(int clientId) {
            int transferMoney = random.nextInt(1000);
            int moneyRecipientId = random.nextInt(numberOfClients);
            if (!clientsAccountDetails.containsKey(moneyRecipientId)) {
                System.out.println("recipient of id: " + moneyRecipientId + " does not exist");
                Thread.currentThread().interrupt();
            }

            int balanceOfCurrentClient = clientsAccountDetails.get(clientId);
            int balanceOfRecipient = clientsAccountDetails.get(moneyRecipientId);
            if (transferMoney > balanceOfCurrentClient) {
                System.out.println("Not enough money to transfer for client of id: " + clientId);
                Thread.currentThread().interrupt();
                return;
            }
            clientsAccountDetails.put(clientId, balanceOfCurrentClient - transferMoney);
            clientsAccountDetails.put(moneyRecipientId, balanceOfRecipient + transferMoney);
        }

    }
}
