package backend.interpreter.debuggerimpl;

import backend.interpreter.Cell;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NameValuePair {

    // variable's name
    private String variableName;

    // variable's value string
    private String valueString;

    protected NameValuePair(String variableName, Object value) {
        this.variableName = variableName;
        this.valueString = valueString(value);
    }

    private static final int MAX_DISPLAYED_ELEMENT = 10;

    protected static String valueString(Object value) {
        StringBuilder buffer = new StringBuilder();

        // Undefined value
        if (value == null) {
            buffer.append("?");
        }

        // Dereference a VAR parameter
        else if (value instanceof Cell) {
            buffer.append(valueString(((Cell) value).getValue()));
        }

        // Array value
        else if (value instanceof Cell[]) {
            arrayValueString((Cell[]) value, buffer);
        }

        // Record value
        else if (value instanceof HashMap) {
            recordValueString((HashMap) value, buffer);
        }

        // Character value
        else if (value instanceof Character) {
            buffer.append("'").append((Character) value).append("'");
        }

        // Numeric or boolean value
        else {
            buffer.append(value.toString());
        }

        return buffer.toString();
    }

    private static void arrayValueString(Cell[] array, StringBuilder buffer) {
        int elementCount = 0;
        boolean first = true;
        buffer.append("[");

        // Loop over each array element up to MAX_DISPLAYED_ELEMENTS times.
        for (Cell cell : array) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }

            if (++elementCount <= MAX_DISPLAYED_ELEMENT) {
                buffer.append(valueString(cell.getValue()));
            } else {
                buffer.append("...");
                break;
            }
        }

        buffer.append("]");
    }

    private static void recordValueString(HashMap<String, Cell> record, StringBuilder buffer) {
        boolean first = true;
        buffer.append("{");
        Set<Map.Entry<String, Cell>> entries = record.entrySet();
        Iterator<Map.Entry<String, Cell>> it = entries.iterator();

        // Loop over each record field
        while (it.hasNext()) {
            Map.Entry<String, Cell> entry = it.next();

            if (first) {
                first = false;
            }
            else {
                buffer.append(", ");
            }

            buffer.append(entry.getKey()).append(": ").append(valueString(entry.getValue().getValue()));
        }

        buffer.append("}");
    }

    public String getValueString() {
        return valueString;
    }

    public String getVariableName() {
        return variableName;
    }
}
