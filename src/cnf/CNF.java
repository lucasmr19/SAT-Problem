package cnf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class CNF {
    private int numVars = 0;
    private int numClauses = 0;
    private ArrayList<Clause> clauses = null;
    private int num_empty_clauses = 0;

    public CNF() {
        this.clauses = new ArrayList<Clause>();
    }
    
    public void addClause(Clause c) {
        if (c.isEmpty()) { // Do not add the empty clause to the CNF list to save memory
            num_empty_clauses++;
        } else {
            clauses.add(c);
        }
    }

    // Getters
    public int getNumVars() {
        return numVars;
    }

    public ArrayList<Clause> getClauses() {
        return clauses;
    }

    public int getNumEmptyClauses() {
        return num_empty_clauses;
    }

    public int getNumClauses(){
        return this.numClauses;
    }

    public int getNumNonEmptyClauses(){
        return clauses.size();
    }

    // Private setters (Read from .cnf file)
    private void setNumVars(int numVars) {
        this.numVars = numVars;
    }

    private void setNumClauses(int numClauses) {
        this.numClauses = numClauses;
    }

    // Count the number of clauses that have a false value
    public int eval(int[] vals) {
        //this.check_vals(vals);
        int false_count = 0;
        for (Clause clause : clauses) {
            if (!clause.eval(vals)) {
                false_count++;
            }
        }
        return false_count + this.num_empty_clauses;
    }
  
    // Stathic method to read .cnf type files. Directly returns the created CNF object.
    // We use overloading concept to make the directory argument where the file is located optional.
    public static CNF readCNFFromFile(String dir, String filename) throws FileNotFoundException, IllegalArgumentException, Exception {
        CNF cnf = new CNF(); // Instancia de CNF a retornar        
        File file = new File(dir+filename);
        Scanner scanner = new Scanner(file);

        boolean foundCNFSection = false; // Indica si se ha encontrado la sección "p cnf"

        // Leer el archivo hasta encontrar la línea que contiene "p cnf"
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("p cnf")) {
                String[] parts = line.split("\\s+");
                cnf.setNumVars(Integer.parseInt(parts[2]));
                cnf.setNumClauses(Integer.parseInt(parts[3]));
                foundCNFSection = true;
                break; // Salir del bucle una vez encontrado "p cnf"
            }

        }

        // Check if the CNF section was found and if not throw Exception
        if (!foundCNFSection) {
            scanner.close();
            throw new IllegalArgumentException("The file does not contain the section 'p cnf numVars numClauses'");
        }

        int cnf_numVars = cnf.getNumVars();

        // Ahora procesar las cláusulas
        int clause_lines_in_CNF_file = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            try {
                Clause clause = new Clause(cnf_numVars, line);
                cnf.addClause(clause);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            clause_lines_in_CNF_file++;
        }

        scanner.close();

        if (cnf.getNumClauses() != clause_lines_in_CNF_file)
            throw new Exception("The number of Clauses defined in the file " + filename + " does not match with those established on the line 'p cnf numVars numClauses'");

        return cnf;
    }

    public static CNF readCNFFromFile(String filename) throws FileNotFoundException, IllegalArgumentException,Exception {
        return readCNFFromFile("", filename);
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder("");
        for (int i = 0; i < clauses.size(); i++) {
            if (i > 0) {
                description.append(" ^ ");
            }
            description.append("(" + clauses.get(i) + ")");
        }

        // Add the Empty Clauses at the end of the String
        for (int i = 0; i < num_empty_clauses; i++) {
            description.append(" ^ (\u00D8)");
        }

        return description.toString();
    } 

    public static void main(String[] args) {
        try {
            String dir = "data/"; // Directory where the file is located
            CNF cnf = readCNFFromFile(dir,"example_2.cnf");
            int[] vals = new RandomConfigGenerator.getRandomConfig(4) // Random Array of size 4 to evaluate
            System.out.println("CNF object" + cnf);
            System.out.println("Number of Clauses in the CNF: " +cnf.getNumClauses());
            System.out.println("Number of Empty Clauses in the CNF: " + cnf.getNumEmptyClauses());
            System.out.println("Evaluating CNF..." + cnf.eval(vals)); // Number of Clauses that are False
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
