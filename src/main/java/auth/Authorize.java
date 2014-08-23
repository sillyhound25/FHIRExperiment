package auth;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import javax.xml.ws.spi.http.HttpContext;
import java.io.IOException;
import java.util.Map;

/**
 This is the authorize call from the smart app. We know who the user is from the session...
 */
@WebServlet(urlPatterns= {"/auth/authorize"}, displayName="Authorize endpoint for FHIR Server")
public class Authorize extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("auth check...");

        String response_type = request.getParameter("response_type");
        String client_id = request.getParameter("client_id");   //the id of the client
        String redirect_uri = request.getParameter("redirect_uri");
        String scope = request.getParameter("scope");   //what the app wants to do
        String state = request.getParameter("state");

        //the scope parameter includes the launch token - eg patient/*.read launch:7bceb3c6-66e9-46c9-8efd-9f87e76a5f9a
        //so we would pull out both scope and token, check that the token matches the one we set (actually the patient token)
        //and that the scope is acceptable to us. Should move this to a function somewhere...
        String[] arScopes =  scope.split(" ");
        String launchToken = "";
        for (int i = 0; i < arScopes.length; i++){
            System.out.println(arScopes[i]);
            if (arScopes[i].substring(0,7).equals("launch:")) {
                launchToken = arScopes[i].substring(7);
            }
        }

        ServletContext context =  getServletContext();// request.    .setAttribute("oauthtokens", oauthtokens);
        Map<String,Person> usertokens = (Map<String,Person>) context.getAttribute("usertokens");

        if (usertokens.containsKey(launchToken)) {
            //we'll assume that the user is OK with this scope, but this is where we can check...
            //so, now we create an auth_code and re-direct to the redirect_url...
            String auth_code = java.util.UUID.randomUUID().toString();
            //we'll save the auth code in a previously defined context variable. In real life you'd use a
            //persistent store of some type, and likely save more details...
            Map<String,Person> oauthcodes = (Map<String,Person>) context.getAttribute("oauthcodes");

            Person person = (Person) usertokens.get(launchToken);

            oauthcodes.put(auth_code,person);
            //and re-direct to the 'authenticated' endpoint of the application
            response.sendRedirect(redirect_uri + "?code="+auth_code+ "&state="+state);
        } else {
            response.setStatus(403);    //forbidden.
        }

    }
}
