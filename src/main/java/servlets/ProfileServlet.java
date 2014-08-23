package servlets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.dstu.resource.Organization;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.dstu.resource.Profile;
import ca.uhn.fhir.rest.client.IGenericClient;
import util.MyMongo;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by davidhay on 18/08/14.
 */

@WebServlet(urlPatterns= {"/fhir/service/profile/list"}, displayName="Process Glucose bundle")
public class ProfileServlet extends HttpServlet {

    private FhirContext _fhirContext;
    private IGenericClient _client;
    private Bundle _baseProfiles;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //get the 'global' resources from the servlet context
        ServletContext ctx = config.getServletContext();
        _fhirContext = (FhirContext) ctx.getAttribute("fhircontext");

        String serverBase = "http://fhir.healthintersections.com.au/open";
        _client = _fhirContext.newRestfulGenericClient(serverBase);

        _baseProfiles = _client.search()
                .forResource(Profile.class)
                .where(Profile.PUBLISHER.matches().value("FHIR Project"))
                .execute();
        System.out.println("ets");

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    }
}
