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
    private GlobalVariables globalVars; // Utilizamos la clase GlobalVariables
    private Lock lock;

    public SATMultiThread(CNF F, int maxTries, int maxFlips, int numVars_F, GlobalVariables globalVars, Lock lock) {
        this.F = F;
        this.maxTries = maxTries;
        this.maxFlips = maxFlips;
        this.numVars_F = numVars_F;
        this.globalVars = globalVars; // Inicializamos la clase GlobalVariables
        this.lock = lock;
    }

    private static class GlobalVariables {

        private int best_false_count;
        private int[] best_configuration;
        private int tryCount;
        private int ultimate_flipCount; // Numero de flips en el try que encuentra la solución
        private boolean shouldStop;
    
        public GlobalVariables(){
            this.best_false_count = Integer.MAX_VALUE;
            this.best_configuration = null;
            this.tryCount = 0;
            this.ultimate_flipCount = 0; // Numero de flips en el try que encuentra la solución
            this.shouldStop = false;
    
        }
    
        // Getters:
        public int getBest_false_count() {
            return best_false_count;
        }
    
        public int[] getBest_configuration() {
            return best_configuration;
        }
    
        public int getTryCount() {
            return tryCount;
        }
    
        public int getUltimate_flipCount() {
            return ultimate_flipCount;
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
        /*
        public void setTryCount(int value) {
            tryCount = value;
        }
        */
        public void setUltimate_flipCount(int value) {
            ultimate_flipCount = value;
        }
    
        public void setShouldStop(boolean value) {
            shouldStop = value;
        }
    
        // Increment
        public void incTryCount (){
            this.tryCount++;
        
        }
        
        // Update vars for Multithread Algorithm
        public void updateGlobalVars(int[] configuration, int false_count, int flipCount, Lock lock) {
            lock.lock();
            if (false_count == 0 && !shouldStop) {
                setBest_false_count(false_count);
                setBest_configuration(configuration.clone());
                setUltimate_flipCount(flipCount);
                setShouldStop(true);
            }
            lock.unlock();
        }
    }

    // Intenta actualizar (una sola vez) el valor de las variables globales. (Solo se actualizan cuando la CNF es SAT)
    private void updateGlobalVars(int[] configuration, int false_count, int flipCount, Lock lock) {
        globalVars.updateGlobalVars(configuration, false_count, flipCount, lock);
    }

    @Override
    public void run() {
        // Inicializamos las variables locales a cada hilo
        int[] random_configuration = RandomConfigGenerator.getRandomConfig(numVars_F);
        int[] new_configuration = random_configuration.clone();
        int[] best_configuration_local = random_configuration.clone();


        int false_count;
        int best_false_count_local = Integer.MAX_VALUE; // Elegir un número grande cualquiera para la inicialización de la variable
        int flipCount;
        int i;
        // Este candado es necesario porque siempre existe el caso de que varios hilos se metan a la vez en el while y entonces
        // se hacen intentos de más.
        this.lock.lock();
        while (globalVars.getTryCount() < maxTries && !globalVars.getShouldStop()) {
            
            globalVars.incTryCount();
            this.lock.unlock();

            flipCount = 0;
            new_configuration = random_configuration.clone();
            while (flipCount < maxFlips && !globalVars.getShouldStop()) {
                flipCount++;
                i = 0;
                while (i < numVars_F && !globalVars.getShouldStop()) {
                    new_configuration[i] *= -1;
                    false_count = F.eval(new_configuration);
                    if (false_count < best_false_count_local && !globalVars.getShouldStop()) {
                        best_false_count_local = false_count;
                        best_configuration_local = new_configuration.clone();
                        // Si se ha mejorado la mejor solución local al hilo intentar actualizar las vars globales y parar todo
                        updateGlobalVars(best_configuration_local, false_count, flipCount, this.lock);
                    }
                    new_configuration[i] *= -1;
                    i++;
                }
                new_configuration = best_configuration_local.clone();
            }
            random_configuration = RandomConfigGenerator.getRandomConfig(numVars_F);
            this.lock.lock();
        }
        this.lock.unlock();
    }  

    public static LocalSearchSolution solveSATMultiThread(CNF F, int maxTries, int maxFlips, int numThreads) throws IllegalArgumentException, InterruptedException {
        // Ni siquiera intentarlo si hay cláusulas vacías
        if (F.getNumEmptyClauses() != 0) {
            return new LocalSearchSolution(null, 0, 0);
        }

        int numVars_F = F.getNumVars();
        GlobalVariables globalVars = new GlobalVariables(); // Creamos una instancia de GlobalVariables
        Lock lock = new ReentrantLock();

        // Crear una lista para almacenar los hilos
        List<Thread> threads = new ArrayList<Thread>();

        // Crear e iniciar los hilos con .start()
        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(new SATMultiThread(F, maxTries, maxFlips, numVars_F, globalVars, lock));
            threads.add(thread); // Agregar el hilo a la lista
            thread.start(); // Iniciar cada hilo
        }

        // Realizar .join() para cada hilo
        for (Thread thread : threads) {
            try {
                thread.join(); // Esperar a que cada hilo termine su ejecución
            } catch (InterruptedException e) {
                e.printStackTrace(); // Manejar la excepción si se interrumpe la espera
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
            CNF F = CNF.readCNFFromFile(dir, "ejemplo_7.cnf");
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
