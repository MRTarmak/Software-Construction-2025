package zoo.domains;

import lombok.Getter;
import zoo.interfaces.IAlive;
import zoo.interfaces.IInventory;

public class Animal implements IAlive, IInventory {
    @Getter
    private final String name;

    private final int number;

    public Animal(String animalName, int animalNumber) {
        name = animalName;
        number = animalNumber;
    }

    public String getKind() {
        return "Unknown animal";
    }

    @Override
    public int getFood() {
        return 0;
    }

    @Override
    public int getNumber() {
        return number;
    }
}