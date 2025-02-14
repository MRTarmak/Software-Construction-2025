package zoo.domains;

public class Herbo extends Animal {
    private final int kindness;

    public Herbo(String animalName, int animalNumber, int animalKindness) {
        super(animalName, animalNumber);
        kindness = animalKindness;
    }

    public boolean isKind() {
        return kindness > 5;
    }
}