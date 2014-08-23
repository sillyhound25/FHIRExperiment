package auth;

import auth.Person;

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
Create and return an auth token if the auth_code is valid...
 */

@WebServlet(urlPatterns= {"/auth/token"}, displayName="Token endpoint for FHIR Server")
public class Token extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        System.out.println("Token - using a GET!");




        String code = request.getParameter("code");

        //the map containing auth_code that was set during the authorization phase...
        ServletContext context =  getServletContext();
        Map<String,Person> oauthcodes = (Map<String, Person>) context.getAttribute("oauthcodes");

        //is this a valid access code?
        if (oauthcodes.containsKey(code)) {
            Person person = (Person) oauthcodes.get(code);
            String access_token  = java.util.UUID.randomUUID().toString();
            JsonObject json = Json.createObjectBuilder()
                    .add("access_token", access_token)
                    .add("patient", person.currentPatientId)
                    .add("token_type", "bearer")
                    .add("expires_in", 3600)
                    .add("scope", "patient/*.read")
                    .build();
            response.addHeader("Content-Type","application/json+fhir");

            //save the access token for later use - like the codes, you'd use a persistent store...
            Map<String,JsonObject> oauthtokens = (Map<String,JsonObject>) context.getAttribute("oauthtokens");
            oauthtokens.put(access_token,json);
            //and return the token to the applciation
            out.println(json.toString());

        } else {
            //the auth codes don't match.
            response.setStatus(403);    //forbidden.
            out.println("{}");
        }



    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        String code = request.getParameter("code");

        //the map containing auth_code that was set during the authorization phase...
        ServletContext context =  getServletContext();
        Map<String,Person> oauthcodes = (Map<String, Person>) context.getAttribute("oauthcodes");

        //is this a valid access code?
        if (oauthcodes.containsKey(code)) {
            Person person = (Person) oauthcodes.get(code);
            String access_token  = java.util.UUID.randomUUID().toString();
            JsonObject json = Json.createObjectBuilder()
                    .add("access_token", access_token)
                    .add("patient", person.currentPatientId)
                    .add("token_type", "bearer")
                    .add("expires_in", 3600)
                    .add("scope", "patient/*.read")
                    .build();
            response.addHeader("Content-Type","application/json+fhir");

            //save the access token for later use - like the codes, you'd use a persistent store...
            Map<String,JsonObject> oauthtokens = (Map<String,JsonObject>) context.getAttribute("oauthtokens");
            oauthtokens.put(access_token,json);

            //and return the token to the applciation
            out.println(json.toString());

        } else {
            //the auth codes don't match.
            response.setStatus(403);    //forbidden.
            out.println("{}");
        }
    }
}
