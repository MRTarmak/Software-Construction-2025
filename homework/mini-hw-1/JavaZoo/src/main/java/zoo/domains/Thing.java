package zoo.domains;

import zoo.interfaces.IInventory;

public class Thing implements IInventory {
    private final int number;

    public Thing(int thingNumber) {
        number = thingNumber;
    }

    public String getType() {
        return "Unknown thing";
    }

    @Override
    public int getNumber() {
        return number;
    }
}
