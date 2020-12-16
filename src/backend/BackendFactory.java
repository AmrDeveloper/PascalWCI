package backend;

import backend.compiler.CodeGenerator;
import backend.interpreter.Debugger;
import backend.interpreter.DebuggerType;
import backend.interpreter.Executor;
import backend.interpreter.RuntimeStack;
import backend.interpreter.debuggerimpl.CommandLineDebugger;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;

public class BackendFactory {

    public static Backend createBackend(String operation, String inputPath) throws Exception {
        if ("compile".equalsIgnoreCase(operation)) {
            return new CodeGenerator();
        } else if ("execute".equalsIgnoreCase(operation)) {
            return new Executor(inputPath);
        } else {
            throw new Exception(("Backend factory invalid operation " + operation));
        }
    }

    public static Debugger createDebugger(DebuggerType type,
                                          Backend backend,
                                          RuntimeStack runtimeStack) {
        switch (type) {
            case COMMAND_LINE: {
                return new CommandLineDebugger(backend, runtimeStack);
            }
            case GUI: {
                // TODO: Will support GUI Debugger later
                return null;
            }
            default: {
                return null;
            }
        }
    }

    public static Object defaultValue(TypeSpec type) {
        type = type.baseType();

        if (type == Predefined.integerType) {
            return new Integer(0);
        } else if (type == Predefined.realType) {
            return new Float(0.0f);
        } else if (type == Predefined.booleanType) {
            return new Boolean(false);
        } else if (type == Predefined.charType) {
            return new Character('#');
        }
        // String
        else {
            return new String("#");
        }
    }
}
