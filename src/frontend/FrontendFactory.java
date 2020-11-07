package frontend;

import frontend.pascal.PascalParserTD;
import frontend.pascal.PascalScanner;

public class FrontendFactory {

    public static Parser createParser(String language, String type, Source source) throws Exception{
        if("Pascal".equalsIgnoreCase(language) && "top-down".equalsIgnoreCase(type)) {
            Scanner scanner = new PascalScanner(source);
            return new PascalParserTD(scanner);
        }
        else if(!"Pascal".equalsIgnoreCase(language)) {
            throw new Exception("Parser factory : Invalid Language " + language);
        }
        else{
            throw new Exception("Parser factory : Invalid type " + type);
        }
    }
}
