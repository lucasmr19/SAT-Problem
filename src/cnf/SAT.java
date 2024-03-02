package cnf;

public class SAT {
    // LocalSearch for SAT Algorithm. Proves all the possibles values for the variables (2^n_variables - 1) and evaluates.
    public static LocalSearchSolution solveSAT(CNF F, int Max_tries, int Max_flips, int[] initial_configuration) throws IllegalArgumentException {
        // Initialize:
        int numVars_F = F.getNumVars();
        int Maximum_tries = Max_tries;
        int[] random_configuration;

        if (initial_configuration == null) {
            random_configuration = RandomConfigGenerator.getRandomConfig(numVars_F);
        } else {
            random_configuration = initial_configuration;
            Maximum_tries = 1;
        }
        if (random_configuration.length != numVars_F)
            throw new IllegalArgumentException("The initial configuration size (" + initial_configuration.length + ") must match the number of variables in the CNF ("+numVars_F +")");
        
        int[] best_configuration = random_configuration.clone();
        int[] new_configuration = random_configuration.clone();
        int false_count = F.eval(random_configuration);
        int best_false_count = false_count;
        int flipCount = 0;
        int tryCount  = 0;
        int num_empty_clauses = F.getNumEmptyClauses();
        
        while (tryCount < Maximum_tries && best_false_count != num_empty_clauses) {
            flipCount = 0;
            new_configuration = random_configuration.clone();
            while (flipCount < Max_flips && best_false_count != num_empty_clauses) {
                int i = 0;
                while (i < numVars_F && best_false_count != num_empty_clauses) {
                    new_configuration[i] *= -1;
                    false_count = F.eval(new_configuration);
                    if (false_count < best_false_count) {
                        best_false_count = false_count;
                        best_configuration = new_configuration.clone();
                    }
                    new_configuration[i] *= -1;
                    i++;
                }
                new_configuration = best_configuration.clone();
                false_count = best_false_count;
                flipCount++;          
            }
            random_configuration = RandomConfigGenerator.getRandomConfig(numVars_F);
            tryCount++;
        }
        if (best_false_count != 0) {
            return new LocalSearchSolution(null, Maximum_tries, Max_flips);
        } else {
            return new LocalSearchSolution(best_configuration, tryCount, flipCount);
        }
    } 

    public static LocalSearchSolution solveSAT(CNF F, int Max_tries, int Max_flips) throws IllegalArgumentException{
        return solveSAT(F, Max_tries, Max_flips, null);
    }
