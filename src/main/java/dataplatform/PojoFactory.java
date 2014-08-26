package dataplatform;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidhay on 26/08/14.
 */
public class PojoFactory {
    //return a POJO based on the code


    public IPojo getPojo(String code) {
        if (code.equalsIgnoreCase("43151-0")) {
            return new Glucose();
        }
        return null;
    }
}
