package zoo.things;

import zoo.domains.Thing;

public class Table extends Thing {
    public Table(int tableNumber) {
        super(tableNumber);
    }

    @Override
    public String getType() {
        return "Table";
    }
}
