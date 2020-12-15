package backend.interpreter;

import java.util.List;

public interface MemoryMap {

    public Cell getCell(String name);

    public List<String> getAllNames();
}
