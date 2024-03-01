// We could use a Regular Expression:
// String regex = "\\s*([-+]?[1-9]\\d*\\s+)*[-+]?[1-9]\\d*\\s*0\\s*";
package cnf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Clause {

    private int numVars = 0;
    private int numLiterals = 0;
    private int[] literals = null;
    private boolean is_empty = false;

    public Clause(int numVars, String description) throws IllegalArgumentException {

        description = description.trim();
        
        if (description.isEmpty()) {
            throw new IllegalArgumentException("The clause has no literals!");
        }

        if (description.equals("0")) {
            is_empty = true;
        }

        String[] str_literals = description.split("\\s+");

        for (int i = 0; i < str_literals.length - 1; i++) {
            if (str_literals[i].equals("0")) {
                throw new IllegalArgumentException("The 0 should only be at the end of the clause!");
            }
        }


        int[] num_literals = Arrays.stream(str_literals).mapToInt(Integer::parseInt).distinct().toArray();

        int num_literals_length = num_literals.length - 1;

        if (num_literals[num_literals_length] != 0) {
            throw new IllegalArgumentException("The last element of the clause must be 0!");
        }

        // Excepcional case: Clause(numVars:1,description:"1 -1 0");
        // Count how many diff numbers are (abs value)
        if (numVars < num_literals_length) {
            Set<Integer> numerosDistintos = new HashSet<>();
            for (int i = 0; i < num_literals_length; i++) { 
                numerosDistintos.add(Math.abs(num_literals[i]));
            }

            if (numVars < numerosDistintos.size()) {
                throw new IllegalArgumentException("More literals are being used than available variables!");
            }
        }

        this.literals = num_literals;
        this.numVars = numVars;
        this.numLiterals = num_literals_length;
    }    

    // Getters
    public int getnumVars() {
        return numVars;
    }

    public int getnumLiterals() {
        return numLiterals;
    }

    public int[] getLiterals() {
        return literals;
    }

    public boolean isEmpty() {
        return is_empty;
    }    

    public void printLiterals() {
        System.out.print("[");
        if (this.numLiterals>0) {
            int i;
            for (i=0; i<this.numLiterals-1; i++) {
                System.out.print(literals[i]+", ");
            }
            System.out.print(literals[i]);
        }
        System.out.print("]\n");
    }

    public boolean eval(int[] vals) {        
        for (int i = 0; i < numLiterals; i++) {
            int literal = literals[i];
            int index = Math.abs(literal) - 1;
            boolean value = literal > 0 ? vals[index] == 1 : vals[index] == -1;
            if (value) //If we find at least one true literal, we do not need to further evaluate
                return true;
        }
        return false; // All literals are false or empty Clause
    }

    private String StringVar(int literal) {
        if (literal > 0) {
            return "X" + literal;
        } else {
            return "\u00ACX" + Math.abs(literal); // Negation Unicode Symbol
        }
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder("");
        if (numLiterals == 0) {
            description.append("\u00D8"); // Empty Unicode Symbol
        } else {
            for (int i = 0; i < numLiterals - 1; i++) {
                description.append(StringVar(literals[i])).append(" v "); // Variable seguida de símbolo or
            }
            description.append(StringVar(literals[numLiterals - 1])); // Last literal in the Clause
        }
        return description.toString();
    }


    public static void main(String[] args) {
      try {      
          Clause c = new Clause(3,"1 3 1 -2 2 1 0 ");
          int[] vals = {-1, 1, 1}; // Values for each variable (depends on the position)
          System.out.println("Clause Object Created = " + c);
          System.out.print("Literals in the Clause = ");
          c.printLiterals();
          System.out.println("NumVars in the Problem = " + c.getnumVars());
          System.out.println("NumLiterals in the Clause = " + c.getnumLiterals());
          System.out.println("¿Empty Clause? = " + c.isEmpty());
          System.out.println("Evaluation Result: " + c.eval(vals));
      } catch (IllegalArgumentException e) {
          e.printStackTrace();
      }
  }

}
