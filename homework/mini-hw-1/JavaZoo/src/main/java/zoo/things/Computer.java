package zoo.things;

import zoo.domains.Thing;

public class Computer extends Thing {
    public Computer(int computerNumber) {
        super(computerNumber);
    }

    @Override
    public String getType() {
        return "Computer";
    }
}
