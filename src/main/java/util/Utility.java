package util;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by davidhay on 27/07/14.
 */
public class Utility {



    public static Date getDateFromOHDate(String date){
        SimpleDateFormat formatIn = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat formatOut = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date newDate = formatIn.parse(date);
            return formatIn.parse(date);

        } catch (Exception ex){
            System.out.println("Error parsing date: " + ex.getMessage());
            return null;
        }


    }

    public static String getPatientIDFromResolvingIdentifier(JsonObject ri) {

        String id = Utility.safeGetJsonValue(ri,"id");
        String system =  Utility.safeGetJsonValue(ri,"namespace");
        return system + "|" + id;
    }

    //get the value of a json property - returning null if it doesn't exist...
    public static String safeGetJsonValue(JsonObject node, String inx) {
        if (node != null) {
            try {
                JsonString obj = node.getJsonString(inx);
                if (obj != null) {
                    return obj.getString();
                } else {
                    return null;
                }
            } catch (Exception e){
                System.out.println("Error getting the value for " + inx + " - it isn't a string...");
                return null;
            }

        } else {
            return null;
        }
    }
}
