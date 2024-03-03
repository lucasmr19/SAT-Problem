package cnf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Tester class for lab 1. 
 *
 * @author XXXXXXXX
 */
public class SATTester {

    /**
     * Tests exercise 1.
     *
     * @param cnfFile            File containing the CNF description.
     * @param inputFile          File containing the input (truth value of each 
     * 							 variable in the CNF).
     *
     * @return    				 The number of clauses in the CNF that evaluate to 
     * 							 false with the given input.
     */
	public static int testEx1(String cnfFile, String inputFile) throws IllegalArgumentException, FileNotFoundException, Exception {

        CNF cnf = CNF.readCNFFromFile(cnfFile);
        ArrayList<Integer> inputVals = new ArrayList<>();
        File input = new File(inputFile);
        try (Scanner scanner = new Scanner(input)) {
            while (scanner.hasNextInt()) {
                int val = Integer.parseInt(scanner.next().trim());
                if (val != 1 && val != -1) {
                    throw new IllegalArgumentException("El archivo de entrada" + inputFile + "debe contener solo los valores 1 (Verdadero) o -1 (Falso).");
                }
                inputVals.add(val);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: El archivo " + e.getMessage() + " no pudo ser encontrado.");
        } catch (Exception e) {
            throw new Exception("Error al leer el archivo de entrada: " + e.getMessage());
        }

        if (inputVals.size() != cnf.getNumVars())
        throw new IllegalArgumentException("La longitud de los valores de verdad del archivo " + inputFile + " ("  + inputVals.size() + ")  debe ser igual a la cantidad de variables disponibles en el archivo " + cnfFile + " ("  + cnf.getNumVars() + ")");
        
        // Pasamos los valores a un array de enteros y evaluamos
        int[] vals = inputVals.stream().mapToInt(Integer::intValue).toArray();
        return cnf.eval(vals);
    }    

	
    /**
     * Tests exercise 2. 
     *
     * @param cnfFile        File containing the CNF description.
     * @param maxTries			 Maximum number or tries.
     * @param maxFlips			 Maximum number of flips per try.
     * @param initialConf		 Initial configuration. If a non null value is passed, this configuration
     * 							 should be used for all tries, so it should be combined with 
     * 							 <code>numTries = 1</code>. If a null value is passed, a random initial
     * 							 configuration should be used for each try.
     *
     * @return    				 A <code>LocalSearchSolution</code> object containing the result of the
     * 							 local search algorithm, including the number of tries needed, the number
     * 							 of flips in the try that found the solution, and the solution itself. 
     * 							 If no solution is found the <code>LocalSearchSolution</code> object should 
     * 							 contain <code>numTries = maxTries</code>, <code>numFlips = maxFlips</code>
     * 							 and <code>solution = null</code>.
     */
	public static LocalSearchSolution testEx2(String cnfFile, int maxTries, int maxFlips, int[] initialConf) throws IllegalArgumentException, FileNotFoundException, Exception {
        try {
            // Crear un objeto CNF leyendo el archivo de entrada
            CNF cnf = CNF.readCNFFromFile(cnfFile);

            // Resolver el problema usando el método solveSAT
            return SAT.solveSAT(cnf, maxTries, maxFlips, initialConf);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (FileNotFoundException e) {
            System.err.println("Error: El archivo " + cnfFile + " no pudo ser encontrado.");
            throw e;
        } catch (Exception e) {
            // Código para manejar cualquier otra excepción
            System.err.println("Error inesperado: " + e.getMessage());
            throw e;
        }
    }
    
	
    /**
     * Tests exercise 3. 
     *
     * @param cnfFile        File containing the CNF description.
     * @param maxTries			 Maximum number or tries.
     * @param maxFlips			 Maximum number of flips per try.
     * @param numThreads		 Number of threads to be run in parallel.
     *
     * @return    				 A <code>LocalSearchSolution</code> object containing the result of the
     * 							 local search algorithm, including the number of tries needed, the number
     * 							 of flips in the try that found the solution, and the solution itself. 
     * 							 If no solution is found the <code>LocalSearchSolution</code> object should 
     * 							 contain <code>numTries = maxTries</code>, <code>numFlips = maxFlips</code>
     * 							 and <code>solution = null</code>.
     */
	public static LocalSearchSolution testEx3(String cnfFile, int maxTries, int maxFlips, int numThreads) throws IllegalArgumentException, FileNotFoundException, Exception {
	    try {
            // Crear un objeto CNF leyendo el archivo de entrada
            CNF cnf = CNF.readCNFFromFile(cnfFile);

            // Resolver el problema usando el método solveSAT
            return SATMultiThread.solveSATMultiThread(cnf, maxTries, maxFlips, numThreads);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (FileNotFoundException e) {
            System.err.println("Error: El archivo " + cnfFile + " no pudo ser encontrado.");
            throw e;
        } catch (Exception e) {
            // Código para manejar cualquier otra excepción
            System.err.println("Error inesperado: " + e.getMessage());
            throw e;
        }
    }
}
