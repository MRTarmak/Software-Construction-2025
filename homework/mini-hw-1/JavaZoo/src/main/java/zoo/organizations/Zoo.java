package zoo.organizations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zoo.animals.Monkey;
import zoo.animals.Rabbit;
import zoo.animals.Tiger;
import zoo.animals.Wolf;
import zoo.domains.Animal;
import zoo.domains.Herbo;
import zoo.domains.Thing;
import zoo.things.Computer;
import zoo.things.Table;

import java.util.*;

@Component
public class Zoo {
    private Integer inventoryCount = 0;

    private final Set<String> kinds = new HashSet<>();

    private final List<Animal> animals = new ArrayList<>();

    private final List<Thing> things = new ArrayList<>();

    @Autowired
    private VetClinic vetClinic;

    public boolean addAnimal(String kind, String name, int kindness) throws Exception {
        if (kindness < 1 || kindness > 10)
            throw new Exception("Kindness must greater than 0 and not greater than 10");

        Animal animal = switch (kind) {
            case "monkey" -> new Monkey(name, inventoryCount + 1, kindness);
            case "rabbit" -> new Rabbit(name, inventoryCount + 1, kindness);
            case "tiger" -> new Tiger(name, inventoryCount + 1);
            case "wolf" -> new Wolf(name, inventoryCount + 1);
            default -> throw new Exception("Unknown kind of an animal");
        };

        if (vetClinic.checkAnimal(animal)) {
            inventoryCount++;

            animals.add(animal);
            kinds.add(animal.getKind());

            return true;
        }
        return false;
    }

    public void addInventory(String thingType) throws Exception {
        Thing thing = switch (thingType) {
            case "computer" -> new Computer(inventoryCount + 1);
            case "table" -> new Table(inventoryCount + 1);
            default -> throw new Exception("Unknown type of a thing");
        };

        inventoryCount++;
        things.add(thing);
    }

    public String getReport() {
        StringBuilder report = new StringBuilder();

        report.append("Animals count by kinds: \n");

        kinds.forEach(kind -> {
            long count = animals.stream().filter(animal -> animal.getKind().equals(kind)).count();
            report.append(kind).append(" - ").append(count).append("\n");
        });

        report.append("Total: ").append(animals.size()).append("\n");

        return report.toString();
    }

    public String getFoodReport() {
        StringBuilder report = new StringBuilder();

        report.append("Food count by kinds: \n");

        int totalCount = 0;

        for (String kind : kinds) {
            int kindCount = 0;
            for (Animal animal : animals) {
                if (animal.getKind().equals(kind))
                    kindCount += animal.getFood();
            }
            report.append(kind).append(" - ").append(kindCount).append("kg\n");
            totalCount += kindCount;
        }

        report.append("Total: ").append(totalCount).append("kg\n");

        return report.toString();
    }

    public String getContactReport() {
        StringBuilder report = new StringBuilder();

        report.append("The list of kind animals: \n");

        animals.stream()
                .filter(animal -> animal instanceof Herbo)
                .map(animal -> (Herbo)animal)
                .filter(Herbo::isKind)
                .forEach(herbo -> report.append(herbo.getNumber()).append(" ")
                        .append(herbo.getKind()).append(" ")
                        .append(herbo.getName()).append("\n"));

        return report.toString();
    }

    public String getInventoryReport() {
        StringBuilder report = new StringBuilder();

        report.append("The list of all animals: \n");
        animals.forEach(animal -> report.append(animal.getNumber()).append(" ")
                .append(animal.getKind()).append(" ")
                .append(animal.getName()).append("\n"));

        report.append("The list of all things: \n");
        things.forEach(thing -> report.append(thing.getNumber()).append(" ")
                .append(thing.getType()).append("\n"));

        return report.toString();
    }
}
