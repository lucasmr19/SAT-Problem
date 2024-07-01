package cnf;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ParallelVariableSelection implements Runnable {

    private int[] subset; // Variable subconjunto que tomará cada hilo (array con las vars de ese hilo)
    private CNF F;
    private GlobalVariables globalVars; // Utilizamos la clase GlobalVariables
    private Lock lock;
    private CyclicBarrier barrier;

    public ParallelVariableSelection(CNF F, int maxTries, int maxFlips, int[] subset, GlobalVariables globalVars, Lock lock, CyclicBarrier barrier) {
        this.F = F;
        this.subset = subset;
        this.globalVars = globalVars; // Inicializamos la clase GlobalVariables
        this.lock = lock;
        this.barrier = barrier;
    }

    private static class GlobalVariables {

        private int best_false_count;
        private int[] best_configuration;
        private boolean shouldStop;
        private int[] new_configuration;
    
        public GlobalVariables(){
            this.best_false_count = Integer.MAX_VALUE;
            this.best_configuration = null;
            this.shouldStop = false;
            this.new_configuration = null;
        }
    
        // Getters:
        public int getBest_false_count() {
            return best_false_count;
        }
    
        public int[] getBest_configuration() {
            return best_configuration;
        }

        public int[] getNew_configuration() {
            return new_configuration;
        }
    
        public boolean getShouldStop() {
            return shouldStop;
        }
    
        // Setters:
        public void setBest_false_count(int value) {
            best_false_count = value;
        }
    
        public void setBest_configuration(int[] value) {
            best_configuration = value;
        }

        public void setNew_configuration(int[] value) {
            new_configuration = value;
        }

        public void change_value_in_New_configuration(int index_value, Lock lock) {
            lock.lock();
            if (!shouldStop && index_value != -1) {
                new_configuration[index_value] *= -1;
            }
            lock.unlock();
        }


        public void setShouldStop(boolean value) {
            shouldStop = value;
        }
    
        
        // Update vars for ParallelVariableSelection Algorithm
        public void updateGlobalVars(int[] configuration, int false_count, Lock lock) {
            lock.lock();
            if (false_count == 0 && !shouldStop) {
                setBest_false_count(false_count);
                setBest_configuration(configuration.clone());
                setShouldStop(true);
            }
            lock.unlock();
        }
        
    }

    private void updateGlobalVars(int[] new_configuration, int false_count, Lock lock) {
        globalVars.updateGlobalVars(new_configuration, false_count, lock);
    }

    @Override
    public void run() {
        // Utilizamos una barrera para asegurarnos de que todos los hilos hagan un get() de la misma variable global NewConfiguration
        // antes de modificar su valor para evitar "Race Conditions".
        int[] new_configuration_local = globalVars.getNew_configuration().clone(); // Debe ser igual para todos los hilos
        try {
            this.barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        int false_count;
        int best_false_count_local = Integer.MAX_VALUE;
        int best_var_to_change = -1; // Variable a cambiar del subconjunto. Dar un valor absurdo para inicializar
        int i = 0;
        int index; // Cada indice del subconjunto de cada hilo
        while (i < subset.length && !globalVars.getShouldStop()) {
            index = subset[i];
            new_configuration_local[index] *= -1;
            false_count = F.eval(new_configuration_local);
            if (false_count < best_false_count_local && !globalVars.getShouldStop()) {
                best_false_count_local = false_count;
                best_var_to_change = index;
                // Si se ha mejorado la mejor solución local al hilo intentar actualizar las vars globales y parar todo
                updateGlobalVars(new_configuration_local, best_false_count_local, this.lock);
            }
            new_configuration_local[index] *= -1;
            i++;
        }
        globalVars.change_value_in_New_configuration(best_var_to_change, this.lock);
    }

    
    public static LocalSearchSolution solveParallelVariableSelection(CNF F, int maxTries, int maxFlips, int numThreads) {
        if (F.getNumEmptyClauses() != 0) {
            return new LocalSearchSolution(null, 0, 0);
        }
        int numVars_F = F.getNumVars();
        List<Thread> threads;
        GlobalVariables globalVars = new GlobalVariables();
        Lock lock = new ReentrantLock();
        CyclicBarrier barrier = new CyclicBarrier(numThreads); // Inicializamos la barrera con el número de hilos a esperar
        int[][] subset_array = new int[numThreads][]; // Array con todos los subcconjuntos
        
        // Crear una lista con todos los números del 0 al numVars_F - 1
        ArrayList<Integer> allNumbers = new ArrayList<Integer>();
        for (int i = 0; i < numVars_F; i++) {
            allNumbers.add(i);
        }

        // Barajar aleatoriamente la lista
        Collections.shuffle(allNumbers);

        // Calcular el tamaño de cada subconjunto
        int subsetSize = numVars_F / numThreads;
        int remainder = numVars_F % numThreads;

        // Crear y mostrar los subset_array
        int startIndex = 0;
        for (int i = 0; i < numThreads; i++) {
            int endIndex = startIndex + subsetSize + (i < remainder ? 1 : 0);
            int[] subset = allNumbers.subList(startIndex, endIndex).stream().mapToInt(Integer::intValue).toArray();
            subset_array[i] = subset;
            startIndex = endIndex;
        }
        
        // Inicializamos las variables necesarias:
        int[] random_configuration = RandomConfigGenerator.getRandomConfig(numVars_F);
        globalVars.setNew_configuration(random_configuration.clone());
        int flipCount = 0;
        int tryCount  = 0;

        while (tryCount < maxTries && !globalVars.getShouldStop()) {
            flipCount = 0;
            globalVars.setNew_configuration(random_configuration.clone()); // El array sobre el que permutar es variable global compartida
            while (flipCount < maxFlips && !globalVars.getShouldStop()) {
                // PROCESO A PARALELIZAR:
                threads = new ArrayList<Thread>();
                for (int i = 0; i < numThreads; i++) {
                    Thread thread = new Thread(new ParallelVariableSelection(F, maxTries, maxFlips, subset_array[i], globalVars, lock, barrier), "Thread " + (i + 1));
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
                flipCount++;   
            }
            random_configuration = RandomConfigGenerator.getRandomConfig(numVars_F); // Crear un nuevo array random para el próximo try
            tryCount++;
        }

        if (globalVars.getBest_false_count() != 0) {
            return new LocalSearchSolution(null, maxTries, maxFlips);
        } else {
            return new LocalSearchSolution(globalVars.getBest_configuration(), tryCount, flipCount);
        }
    }

    public static void main(String[] args) {
        try {
            String dir = "data/";
            CNF F = CNF.readCNFFromFile(dir, "ejemplo_3.cnf");
            LocalSearchSolution result = solveParallelVariableSelection(F, 5, 20, 4);
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
