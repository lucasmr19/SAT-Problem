package cnf;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SATMultiThread implements Callable<LocalSearchSolution> {
    // Global variables for all the threads:
    private CNF F; 
    private int maxTries;
    private int maxFlips;
    private static int numVars_F;
    private static int num_empty_clauses;
    private static int best_false_count = Integer.MAX_VALUE;
    private static int[] best_configuration;
    private static int tryCount = 0;
    private static int ultimate_flipCount;
    private Lock lock = new ReentrantLock();
    private static boolean shouldStop = false; // Flag to stop the execution

    public SATMultiThread(CNF F, int maxTries, int maxFlips, int numVars_F) {
        this.F = F;
        this.maxTries = maxTries;
        this.maxFlips = maxFlips;
        SATMultiThread.numVars_F = numVars_F;
    }

    @Override
    public LocalSearchSolution call() {
        // Local variables:
        int[] random_configuration = RandomConfigGenerator.getRandomConfig(numVars_F);
        int[] new_configuration = random_configuration.clone();

        int false_count = F.eval(random_configuration);
        int flipCount = 0;
        lock.lock();
        try {
            updateBestSolution(new_configuration, false_count, flipCount);
        } finally {
            lock.unlock();
        }
        while (tryCount < maxTries && best_false_count != num_empty_clauses && !shouldStop) {
            flipCount = 0;
            new_configuration = random_configuration.clone();
            while (flipCount < maxFlips && best_false_count != num_empty_clauses && !shouldStop) {
                int i = 0;
                while (i < numVars_F && best_false_count != num_empty_clauses && !shouldStop) {
                    new_configuration[i] *= -1;
                    false_count = F.eval(new_configuration);
                    lock.lock();
                    try {
                        updateBestSolution(new_configuration, false_count, flipCount);
                    } finally {
                        lock.unlock();
                    }
                    new_configuration[i] *= -1;
                    i++;
                }
                new_configuration = best_configuration.clone();
                false_count = best_false_count;
                flipCount++;
            }
            random_configuration = RandomConfigGenerator.getRandomConfig(numVars_F);
            lock.lock();
            try {
                tryCount++;
            } finally {
                lock.unlock();
            }
        }
        if (best_false_count != 0) {
            return new LocalSearchSolution(null, maxTries, maxFlips);
        } else {
            return new LocalSearchSolution(best_configuration, tryCount, ultimate_flipCount);
        }
    }
    // Encapsulating the update logic of global variables: best_false_count, best_configuration & shoulStop
    private void updateBestSolution(int[] configuration, int false_count, int flipCount) {
        if (false_count < best_false_count && !shouldStop) {
            best_false_count = false_count;
            best_configuration = configuration.clone();
            if (best_false_count == 0) {
                shouldStop = true;
                ultimate_flipCount = flipCount;
            }
        }
    }

    public static LocalSearchSolution solveSATMultiThread(CNF F, int maxTries, int maxFlips, int numThreads) throws Exception {
        numVars_F = F.getNumVars();
        num_empty_clauses = F.getNumEmptyClauses();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CompletionService<LocalSearchSolution> completionService = new ExecutorCompletionService<>(executorService);

        for (int i = 0; i < numThreads; i++) {
            Callable<LocalSearchSolution> task = new SATMultiThread(F, maxTries, maxFlips, numVars_F);
            completionService.submit(task); // Start each Thread
            
        }
      
        for (int i = 0; i < numThreads; i++) {
        completionService.take(); // Wait till stop each Thread
        }

        executorService.shutdown();

        if (best_false_count != 0) {
            return new LocalSearchSolution(null, maxTries, maxFlips);
        } else {
            return new LocalSearchSolution(best_configuration, tryCount, ultimate_flipCount);
        }
    }

    public static void main(String[] args) {
        try {
            String dir = "data/";
            CNF F = CNF.readCNFFromFile(dir, "ejemplo_9.cnf");
            LocalSearchSolution result = solveSATMultiThread(F, 50, 20, 4);

            // Mostrar los resultados
            System.out.println(F);
            System.out.println("Local Result: " + Arrays.toString(result.solution));
            System.out.println("numTries: " + result.numTries);
            System.out.println("numFlips: " + result.numFlips);
            System.out.println("Evaluating CNF...: " + F.eval(result.solution));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
