package zoo.animals;

import zoo.domains.Herbo;

public class Monkey extends Herbo {
    private static final int food = 1;

    public Monkey(String monkeyName, int monkeyNumber, int monkeyKindness) {
        super(monkeyName, monkeyNumber, monkeyKindness);
    }

    @Override
    public String getKind() {
        return "Monkey";
    }

    @Override
    public int getFood() {
        return food;
    }
}
