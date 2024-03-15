package cnf;

public class GlobalVariables {

    private int best_false_count;
    private int[] best_configuration;
    private int tryCount;
    private int ultimate_flipCount; // Flips in the try who found the solution
    private boolean shouldStop; // Boolean flag to stop all the threads

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

    public void setTryCount(int value) {
        tryCount = value;
    }

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
    
    // Update global vars
    public void updateGlobalVars(int[] new_configuration, int false_count, int flipCount) {
        if (false_count == 0 && !shouldStop) {
            setBest_false_count(false_count);
            setBest_configuration(new_configuration.clone());
            setUltimate_flipCount(flipCount);
            setShouldStop(true);
        }
    }
}
