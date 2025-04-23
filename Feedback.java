public class Feedback {

    private double lenientFactor; //in seconds

    public Feedback(double lenientFactor) {
        this.lenientFactor = lenientFactor;
    }

    /**
     * Determines if the actual timing is rushing, dragging, or on time.
     *
     * @param actual   The actual note time
     * @param expected The expected note time
     * @return 1 if rushing, -1 if dragging, 0 if within lenient margin
     */
    public int isRushing(double actual, double expected) {
        if (actual > expected + lenientFactor) {
            return 1;  // Rushing
        } else if (actual < expected - lenientFactor) {
            return -1; // Dragging
        } else {
            return 0;  // On time
        }
    }

    public double offset(double actual, double expected){
        return Math.abs(actual - expected);
    }

    public String toString(double actual, double expected){
        String ret = "";
        double offset = actual - expected;
        int timingStatus = this.isRushing(actual, expected);
    
        if (timingStatus == 1) {
            ret += "Rushing ";
        } else if (timingStatus == -1) {
            ret += "Dragging ";
        } else {
            ret += "Nice rhythm! ";
            if (offset > 0) {
                ret += "Slightly ahead ";
            } else if (offset < 0) {
                ret += "Slightly behind ";
            } else {
                ret += "You're perfectly on time!";
                return ret;
            }
        }
    
        ret += "by " + String.format("%.3f", Math.abs(offset)) + " seconds.";
        return ret;
    
    }
}

