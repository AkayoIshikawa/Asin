import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main{
    private static final int MAX_NUMBER = 1000;
    private static final int THREAD_COUNT = 10; // Кіл потоків

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введіть число N (до " + MAX_NUMBER + "): ");
        int N = scanner.nextInt();
        // Таймер
        long startTime = System.currentTimeMillis();
        // List для збереження чисел
        CopyOnWriteArrayList<Integer> primes = new CopyOnWriteArrayList<>();
        // Діапазон
        int rangeSize = (N + THREAD_COUNT - 1) / THREAD_COUNT;
        // Для потоков ExecutorService
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            int start = i * rangeSize;
            int end = Math.min(start + rangeSize, N);
            if (start > N) break; // Пропускаю зайве
            Callable<Void> task = new PrimeCalculator(start, end, primes);
            futures.add(executor.submit(task));
        }
        // Перевіряємо завершення
        for (Future<?> future : futures) {
            try {
                if (!future.isCancelled()) {
                    future.get(); // Очікуємо кінця
                    if (future.isDone()) {
                        System.out.println("Завдання виконано успішно.");
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Помилка виконання потоку: " + e.getMessage());
            }
        }
        executor.shutdown();
        // Сорт
        List<Integer> sortedPrimes = new ArrayList<>(primes);
        Collections.sort(sortedPrimes);
        // Вивід
        System.out.println("Прості числа до " + N + ": " + sortedPrimes);
        // Кінець таймера
        long endTime = System.currentTimeMillis();
        System.out.println("Час виконання програми: " + (endTime - startTime) + " мс.");
    }
    // Для обчислень
    static class PrimeCalculator implements Callable<Void> {
        private final int start;
        private final int end;
        private final CopyOnWriteArrayList<Integer> primes;
        public PrimeCalculator(int start, int end, CopyOnWriteArrayList<Integer> primes) {
            this.start = start;
            this.end = end;
            this.primes = primes;
        }
        @Override
        public Void call() {
            for (int i = Math.max(start, 2); i < end; i++) {
                if (isPrime(i)) {
                    primes.add(i);
                }
            }
            return null;
        }
        private boolean isPrime(int number) {
            if (number < 2) return false;
            for (int i = 2; i <= Math.sqrt(number); i++) {
                if (number % i == 0) return false;
            }
            return true;
        }
    }
}
