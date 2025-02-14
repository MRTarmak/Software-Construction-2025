package zoo.animals;

import zoo.domains.Predator;

public class Tiger extends Predator {
    private static final int food = 4;

    public Tiger(String tigerName, int tigerNumber) {
        super(tigerName, tigerNumber);
    }

    @Override
    public String getKind() {
        return "Tiger";
    }

    @Override
    public int getFood() {
        return food;
    }
}
