package auth;

import auth.Person;

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
 A servlet to launch the SMART app. It is received by the web page. We know who the user is as it
 is protected by auth so can re-direct to
 */
@WebServlet(urlPatterns= {"/auth/launch"}, displayName="Launch SMART application")
public class Launch extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("launch endpoint accessed");
        String userToken = request.getParameter("usertoken");
        String patientId = request.getParameter("patientid");

        //make sure this is a logged in person (the have a valid token)
        ServletContext context =  getServletContext();
        Map<String,Person> usertokens = (Map<String,Person>) context.getAttribute("usertokens");

        if (usertokens.containsKey(userToken)) {
            //yep, this is a valid user...

            //retrieve the user object and update with the patient they have in context. We'll need this for the access token...
            Person person = (Person) usertokens.get(userToken);
            person.currentPatientId = patientId;

            //the re-direct URL. In reality the url and 'iss' would come from config...
            String url = "https://fhir.smartplatforms.org/apps/growth-chart/launch.html?";
            //url += "iss=http://localhost:8081/fhir";

            String serverName = request.getServerName();
            int serverPort = request.getServerPort();

            System.out.println(serverName + ":" + serverPort);

            url += "iss=http://" + serverName + ":" + serverPort + "/" + "fhir";
            //we'll use the user token as the launch token as we can use that to validate the Auth call..
            url += "&launch=" + userToken;// launchToken;

            System.out.println(url);

            response.sendRedirect(url);
        } else {
            response.setStatus(403);    //forbidden.
            PrintWriter out = response.getWriter();
            out.println("<html><head></head><body><h1>User not logged in</h1></body></html>");
        }
    }
}

