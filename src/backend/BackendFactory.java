package backend;

import backend.compiler.CodeGenerator;
import backend.interpreter.Executor;
import intermediate.TypeSpec;
import intermediate.symtabimpl.Predefined;

public class BackendFactory {

    public static Backend createBackend(String operation) throws Exception {
        if ("compile".equalsIgnoreCase(operation)) {
            return new CodeGenerator();
        } else if ("execute".equalsIgnoreCase(operation)) {
            return new Executor();
        } else {
            throw new Exception(("Backend factory invalid operation " + operation));
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
