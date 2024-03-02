package cnf;

// Represent the LocalSolution found as an object:
public class LocalSearchSolution {
    public int[] solution = null;
    public int numTries = 0;
    public int numFlips = 0;
    
    public LocalSearchSolution(int[] solution, int numTries, int numFlips) {
    	this.solution = solution;
    	this.numTries = numTries;
    	this.numFlips = numFlips;
    }
}
