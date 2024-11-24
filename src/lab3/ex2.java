import java.io.File;
import java.util.Scanner;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;


public class ex2 {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the directory path: ");
        String directoryPath = scanner.nextLine();

        System.out.print("Enter the keyword to search: ");
        String keyword = scanner.nextLine();

        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory path!");
            return;
        }

        long start, end;

        start = System.nanoTime();
        int count1 = FileSearchWorkStealing.countFiles(directory, keyword);
        end = System.nanoTime();
        System.out.println("Work Stealing Count: " + count1 + " (Time: " + (end - start) / 1e6 + " ms)");

        start = System.nanoTime();
        int count2 = FileSearchWorkDealing.countFiles(directory, keyword);
        end = System.nanoTime();
        System.out.println("Work Dealing Count: " + count2 + " (Time: " + (end - start) / 1e6 + " ms)");
    }








    public class FileSearchWorkDealing {
        public static int countFiles(File directory, String keyword) throws Exception {
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<Integer>> results = new ArrayList<>();
            int count = 0;

            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files == null) return 0;

                for (File file : files) {
                    if (file.isDirectory()) {
                        results.add(executor.submit(() -> countFiles(file, keyword)));
                    } else if (file.getName().contains(keyword)) {
                        count++;
                    }
                }

                for (Future<Integer> result : results) {
                    count += result.get();
                }
            }

            executor.shutdown();
            return count;
        }
    }

    public class FileSearchWorkStealing {
        static class SearchTask extends RecursiveTask<Integer> {
            private final File directory;
            private final String keyword;

            public SearchTask(File directory, String keyword) {
                this.directory = directory;
                this.keyword = keyword;
            }

            @Override
            protected Integer compute() {
                int count = 0;
                File[] files = directory.listFiles();

                if (files == null) return 0;

                for (File file : files) {
                    if (file.isDirectory()) {
                        SearchTask subTask = new SearchTask(file, keyword);
                        subTask.fork();
                        count += subTask.join();
                    } else if (file.getName().contains(keyword)) {
                        count++;
                    }
                }
                return count;
            }
        }

        public static int countFiles(File directory, String keyword) {
            ForkJoinPool pool = new ForkJoinPool();
            return pool.invoke(new SearchTask(directory, keyword));
        }
    }
}
