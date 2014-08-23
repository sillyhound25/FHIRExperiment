package auth;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * A user login page (receives just a login name at present)...
 */
@WebServlet(urlPatterns= {"/auth/login"}, displayName="Login")
public class Login extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("login");
        PrintWriter out = response.getWriter();
        String userName = request.getParameter("username");
        //Here is where we check and validate username & password. We're going to cheat right now...
        //create a user object. This would ultimately be a FHIR obect I suspect...
        Person person = new Person();
        person.userName = request.getParameter("username");
        person.userToken = java.util.UUID.randomUUID().toString(); //generate a user token

        //save the user details in the context - we previously created this map...
        ServletContext context =  getServletContext();
        Map<String,Person> usertokens = (Map<String,Person>) context.getAttribute("usertokens");
        usertokens.put(person.userToken,person);


        //save the access token for later use - like the codes, you'd use a persistent store...
        //create an access token for this person ...
        Map<String,JsonObject> oauthtokens = (Map<String,JsonObject>) context.getAttribute("oauthtokens");
        JsonObject json = Json.createObjectBuilder()
                .add("access_token", person.userToken)
                .add("token_type", "bearer")
                .add("expires_in", 3600)
                .add("scope", "patient/*.read")
                .build();
        oauthtokens.put(person.userToken,json);

        response.addHeader("Content-Type","application/json+fhir");
        out.println(person.getJson().toString());
    }
}
