package zoo.animals;

import zoo.domains.Herbo;

public class Rabbit extends Herbo {
    private static final int food = 1;

    public Rabbit(String rabbitName, int rabbitNumber, int rabbitKindness) {
        super(rabbitName, rabbitNumber, rabbitKindness);
    }

    @Override
    public String getKind() {
        return "Rabbit";
    }

    @Override
    public int getFood() {
        return food;
    }
}
