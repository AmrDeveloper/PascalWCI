package backend.interpreter.executors;

import backend.interpreter.Cell;
import backend.interpreter.Executor;
import backend.interpreter.MemoryFactory;
import intermediate.ICodeNode;
import intermediate.TypeSpec;
import intermediate.icodeimpl.ICodeNodeTypeImpl;
import intermediate.symtabimpl.Predefined;
import message.Message;
import message.MessageType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static backend.interpreter.RuntimeErrorCode.*;
import static intermediate.icodeimpl.ICodeKeyImpl.LINE;
import static intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static intermediate.typeimpl.TypeKeyImpl.SUBRANGE_MAX_VALUE;
import static intermediate.typeimpl.TypeKeyImpl.SUBRANGE_MIN_VALUE;

public class StatementExecutor extends Executor {

    public StatementExecutor(Executor executor) {
        super(executor);
    }

    public Object execute(ICodeNode node) {
        ICodeNodeTypeImpl nodeType = (ICodeNodeTypeImpl) node.getType();

        //ignore this message for now but later use it on debugger and tracking
        sendSourceLineMessage(node);

        switch (nodeType) {
            case COMPOUND: {
                CompoundExecutor compoundExecutor = new CompoundExecutor(this);
                return compoundExecutor.execute(node);
            }
            case ASSIGN: {
                AssignmentExecutor assignmentExecutor = new AssignmentExecutor(this);
                return assignmentExecutor.execute(node);
            }
            case LOOP: {
                LoopExecutor loopExecutor = new LoopExecutor(this);
                return loopExecutor.execute(node);
            }
            case IF: {
                IfExecutor ifExecutor = new IfExecutor(this);
                return ifExecutor.execute(node);
            }
            case SELECT: {
                SelectExecutor selectExecutor = new SelectExecutor(this);
                return selectExecutor.execute(node);
            }
            case CALL: {
                CallExecutor callExecutor = new CallExecutor(this);
                return callExecutor.execute(node);
            }
            case NO_OP: {
                return null;
            }
            default: {
                errorHandler.flag(node, UNIMPLEMENTED_FEATURE, this);
                return null;
            }
        }
    }

    private void sendSourceLineMessage(ICodeNode node) {
        Object lineNumber = node.getAttribute(LINE);

        if (lineNumber != null) {
            sendMessage(new Message(MessageType.SOURCE_LINE, lineNumber));
        }
    }

    /*
     * Convert a Java string to a Pascal string or character
     */
    protected Object toPascal(TypeSpec targetType, Object javaValue) {
        if (javaValue instanceof String) {
            String string = (String) javaValue;

            if (targetType == Predefined.charType) {
                // Pascal Character
                return string.charAt(0);
            } else if (targetType.isPascalString()) {
                Cell[] charCells = new Cell[string.length()];

                // Build an array of characters
                for (int i = 0; i < string.length(); ++i) {
                    charCells[i] = MemoryFactory.createCell(string.charAt(0));
                }

                // Pascal string (array of characters)
                return charCells;
            } else {
                return javaValue;
            }
        } else {
            return javaValue;
        }
    }

    protected Object toJava(TypeSpec targetType, Object pascalValue) {
        if ((pascalValue instanceof Cell[]) &&
                ((Cell[]) pascalValue)[0].getValue() instanceof Character) {
            Cell[] charCells = (Cell[]) pascalValue;
            StringBuilder string = new StringBuilder(charCells.length);

            // Build a Java String
            for (Cell ref : charCells) {
                string.append((Character) ref.getValue());
            }

            // Java String
            return string.toString();
        } else {
            return pascalValue;
        }
    }

    protected Object copyOf(Object value, ICodeNode node) {
        Object copy;

        if (value instanceof Integer) {
            copy = new Integer((Integer) value);
        } else if (value instanceof Float) {
            copy = new Float((Float) value);
        } else if (value instanceof Character) {
            copy = new Character((Character) value);
        } else if (value instanceof Boolean) {
            copy = new Boolean((Boolean) value);
        } else if (value instanceof String) {
            copy = new String((String) value);
        } else if (value instanceof HashMap) {
            copy = copyRecord((HashMap<String, Object>) value, node);
        } else {
            copy = copyArray((Cell[]) value, node);
        }
        return copy;
    }

    private Object copyRecord(HashMap<String, Object> value, ICodeNode node) {
        HashMap<String, Object> copy = new HashMap<>();

        if (value != null) {
            Set<Map.Entry<String, Object>> entries = value.entrySet();

            for (Map.Entry<String, Object> entry : entries) {
                String newKey = entry.getKey();
                Cell valueCell = (Cell) entry.getValue();
                Object newValue = copyOf(valueCell.getValue(), node);
                copy.put(newKey, MemoryFactory.createCell(newValue));
            }
        } else {
            errorHandler.flag(node, UNINITIALIZED_VALUE, this);
        }

        return copy;
    }

    private Cell[] copyArray(Cell[] valueCells, ICodeNode node) {
        int length;
        Cell[] copy;

        if (valueCells != null) {
            length = valueCells.length;
            copy = new Cell[length];

            for (int i = 0; i < length; ++i) {
                Cell valueCell = (Cell) valueCells[i];
                Object newValue = copyOf(valueCell.getValue(), node);
                copy[i] = MemoryFactory.createCell(newValue);
            }
        } else {
            errorHandler.flag(node, UNINITIALIZED_VALUE, this);
            copy = new Cell[1];
        }

        return copy;
    }

    /*
     * Runtime range check.
     */
    protected Object checkRange(ICodeNode node, TypeSpec type, Object value) {
        if (type.getForm() == SUBRANGE) {
            int minValue = (Integer) type.getAttribute(SUBRANGE_MIN_VALUE);
            int maxValue = (Integer) type.getAttribute(SUBRANGE_MAX_VALUE);

            if (((Integer) value) < minValue) {
                errorHandler.flag(node, VALUE_RANGE, this);
                return minValue;
            } else if (((Integer) value) > maxValue) {
                errorHandler.flag(node, VALUE_RANGE, this);
                return maxValue;
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    protected void sendAssignMessage(ICodeNode node, String variableName, Object value) {
        Object lineNumber = getLineNumber(node);

        // Send an ASSIGN message
        if (lineNumber != null) {
            sendMessage(new Message(MessageType.ASSIGN, new Object[]{
                    lineNumber,
                    variableName,
                    value
            }));
        }
    }

    protected void sendFetchMessage(ICodeNode node, String variableName, Object value) {
        Object lineNumber = getLineNumber(node);

        // Send an ASSIGN message
        if (lineNumber != null) {
            sendMessage(new Message(MessageType.FETCH, new Object[]{
                    lineNumber,
                    variableName,
                    value
            }));
        }
    }

    protected void sendCallMessage(ICodeNode node, String routineName) {
        Object lineNumber = getLineNumber(node);

        if(lineNumber != null) {
            sendMessage(new Message(MessageType.CALL, new Object[]{
                    lineNumber,
                    routineName
            }));
        }
    }

    protected void sendReturnMessage(ICodeNode node, String routineName) {
        Object lineNumber = getLineNumber(node);

        if(lineNumber != null) {
            sendMessage(new Message(MessageType.RETURN, new Object[]{
                    lineNumber,
                    routineName
            }));
        }
    }

    private Object getLineNumber(ICodeNode node) {
        Object lineNumber = null;

        // Go up the parent links to look for a line number
        while ((node != null) &&
                ((lineNumber = node.getAttribute(LINE)) == null)) {
            node = node.getParent();
        }

        return lineNumber;
    }
}
