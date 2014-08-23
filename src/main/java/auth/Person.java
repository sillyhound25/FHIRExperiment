package auth;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * Created by davidhay on 10/08/14.
 */
public class Person {
    public String userName;
    public String userToken;
    public String currentPatientId;

    public JsonObject getJson() {

        if (this.userName == null) {
            this.userName = "Not Supplied";
        }
        JsonObject json = Json.createObjectBuilder()
                .add("userToken", this.userToken)
                .add("userName", this.userName)
                .build();
        return json;
    }


}
