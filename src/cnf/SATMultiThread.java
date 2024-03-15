package cnf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SATMultiThread implements Runnable {
    private int numVars_F;
    private CNF F;
    private int maxTries;
    private int maxFlips;
    private GlobalVariables globalVars;
    private Lock lock;

    

    public SATMultiThread(CNF F, int maxTries, int maxFlips, int numVars_F, GlobalVariables globalVars, Lock lock) {
        this.F = F;
        this.maxTries = maxTries;
        this.maxFlips = maxFlips;
        this.numVars_F = numVars_F;
        this.globalVars = globalVars;
        this.lock = lock;
    }


    @Override
    public void run() {
        // Local vars
        int[] random_configuration = RandomConfigGenerator.getRandomConfig(numVars_F);
        int[] new_configuration = random_configuration.clone();
        int[] best_configuration_local = random_configuration.clone();


        int false_count;
        int best_false_count_local = numVars_F; // Big number for initialize
        int flipCount;
        int i;

        this.lock.lock();
        while (globalVars.getTryCount() < maxTries && !globalVars.getShouldStop()) {
            
            globalVars.incTryCount();
            this.lock.unlock();

            flipCount = 0;
            new_configuration = random_configuration.clone();
            while (flipCount < maxFlips && !globalVars.getShouldStop()) {
                i = 0;
                while (i < numVars_F && !globalVars.getShouldStop()) {
                    new_configuration[i] *= -1;
                    false_count = F.eval(new_configuration);
                    if (false_count < best_false_count_local && !globalVars.getShouldStop()) {
                        best_false_count_local = false_count;
                        best_configuration_local = new_configuration.clone();
                        updateGlobalVars(new_configuration, false_count, flipCount);
                    }
                    new_configuration[i] *= -1;
                    i++;
                }
                new_configuration = best_configuration_local.clone();
                false_count = best_false_count_local;
                flipCount++;
            }
            random_configuration = RandomConfigGenerator.getRandomConfig(numVars_F);
            this.lock.lock();
        }
        this.lock.unlock();
    }

    private void updateGlobalVars(int[] new_configuration, int false_count, int flipCount) {
        globalVars.updateGlobalVars(new_configuration, false_count, flipCount);
    }
    
    public static LocalSearchSolution solveSATMultiThread(CNF F, int maxTries, int maxFlips, int numThreads) throws IllegalArgumentException, InterruptedException {
        if (F.getNumEmptyClauses() != 0) {
            return new LocalSearchSolution(null, 0, 0);
        }

        int numVars_F = F.getNumVars();
        GlobalVariables globalVars = new GlobalVariables();
        Lock lock = new ReentrantLock();

        List<Thread> threads = new ArrayList<Thread>();

        for (int i = 1; i < numThreads +1 ; i++) {
            Thread thread = new Thread(new SATMultiThread(F, maxTries, maxFlips, numVars_F, globalVars, lock));
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (globalVars.getBest_false_count() != 0) {
            return new LocalSearchSolution(null, maxTries, maxFlips);
        } else {
            return new LocalSearchSolution(globalVars.getBest_configuration(), globalVars.getTryCount(), globalVars.getUltimate_flipCount());
        }
    }

    public static void main(String[] args) {
        try {
            String dir = "data/";
            CNF F = CNF.readCNFFromFile(dir, "example_7.cnf");

            LocalSearchSolution result = solveSATMultiThread(F, 5, 20, 4);

            System.out.println("numTries: " + result.numTries);
            System.out.println("numFlips: " + result.numFlips);
            if (result.solution == null){
                System.out.println("No solution found :(");
            } else {
            System.out.println("Evaluating CNF...: " + F.eval(result.solution));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
