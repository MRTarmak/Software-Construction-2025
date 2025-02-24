package zoo.organizations;

import org.springframework.stereotype.Component;
import zoo.domains.Animal;

import java.util.Random;

@Component
public class VetClinic {
    private final Random random = new Random();

    public boolean checkAnimal(Animal animal) throws Exception {
        int randomInt = random.nextInt(100);

        return switch (animal.getKind()) {
            case "Monkey" -> randomInt > 30;
            case "Rabbit" -> randomInt > 70;
            case "Tiger" -> randomInt > 20;
            case "Wolf" -> randomInt > 60;
            default -> throw new Exception("Unknown kind of an animal");
        };
    }
}
