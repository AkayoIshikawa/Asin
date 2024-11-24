import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.Random;

public class ex1 {

    public static void main(String[] args) throws InterruptedException {
        int rows = 10;
        int cols = 10;
        int range = 1000; // діапазон

        int[][] A = generateMatrix(rows, cols, range);
        int[][] B = generateMatrix(cols, rows, range);

        System.out.println("Matrix A:");
        printMatrix(A);
        System.out.println("Matrix B:");
        printMatrix(B);

        long start, end;

        start = System.nanoTime();
        int[][] C1 = MatrixMultiplicationWorkStealing.multiply(A, B);
        end = System.nanoTime();
        System.out.println("Work Stealing Time: " + (end - start) / 1e5 + " ms");

        start = System.nanoTime();
        int[][] C2 = MatrixMultiplicationWorkDealing.multiply(A, B);
        end = System.nanoTime();
        System.out.println("Work Dealing Time: " + (end - start) / 1e5 + " ms");

        //System.out.println("Result C1:");
        //printMatrix(C1);
        //System.out.println("Result C2:");
        //printMatrix(C2);
    }

    public static int[][] generateMatrix(int rows, int cols, int range) {
        int[][] matrix = new int[rows][cols];
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextInt(range);
            }
        }
        return matrix;
    }

    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.printf("%4d", val);
            }
            System.out.println();
        }
    }









    public class MatrixMultiplicationWorkStealing {
        static class MultiplyTask extends RecursiveTask<int[][]> {
            private final int[][] A, B, C;
            private final int rowStart, rowEnd;

            public MultiplyTask(int[][] A, int[][] B, int[][] C, int rowStart, int rowEnd) {
                this.A = A;
                this.B = B;
                this.C = C;
                this.rowStart = rowStart;
                this.rowEnd = rowEnd;
            }

            @Override
            protected int[][] compute() {
                if (rowEnd - rowStart <= 10) {
                    for (int i = rowStart; i < rowEnd; i++) {
                        for (int j = 0; j < C[0].length; j++) {
                            for (int k = 0; k < B.length; k++) {
                                C[i][j] += A[i][k] * B[k][j];
                            }
                        }
                    }
                    return C;
                } else { // поділ задачі
                    int mid = (rowStart + rowEnd) / 2;
                    MultiplyTask task1 = new MultiplyTask(A, B, C, rowStart, mid);
                    MultiplyTask task2 = new MultiplyTask(A, B, C, mid, rowEnd);
                    invokeAll(task1, task2);
                    return C;
                }
            }
        }

        public static int[][] multiply(int[][] A, int[][] B) {
            int rows = A.length, cols = B[0].length;
            int[][] C = new int[rows][cols];
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(new MultiplyTask(A, B, C, 0, rows));
            return C;
        }
    }

    public class MatrixMultiplicationWorkDealing {
        public static int[][] multiply(int[][] A, int[][] B) throws InterruptedException {
            int rows = A.length, cols = B[0].length;
            int[][] C = new int[rows][cols];
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (int i = 0; i < rows; i++) {
                int row = i;
                executor.submit(() -> {
                    for (int j = 0; j < cols; j++) {
                        for (int k = 0; k < B.length; k++) {
                            C[row][j] += A[row][k] * B[k][j];
                        }
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
            return C;
        }
    }
}
