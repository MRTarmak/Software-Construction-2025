package zoo.animals;

import zoo.domains.Predator;

public class Wolf extends Predator {
    private static final int food = 2;

    public Wolf(String wolfName, int wolfNumber) {
        super(wolfName, wolfNumber);
    }

    @Override
    public String getKind() {
        return "Wolf";
    }

    @Override
    public int getFood() {
        return food;
    }
}
