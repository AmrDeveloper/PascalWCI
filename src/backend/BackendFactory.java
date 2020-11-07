package backend;

import backend.compiler.CodeGenerator;
import backend.interpreter.Executor;

public class BackendFactory {

    public static Backend createBackend(String operation) throws Exception {
        if("compile".equalsIgnoreCase(operation)) {
            return new CodeGenerator();
        }

        else if("execute".equalsIgnoreCase(operation)) {
            return new Executor();
        }

        else {
            throw new Exception(("Backend factory invalid operation " + operation));
        }
    }
}
