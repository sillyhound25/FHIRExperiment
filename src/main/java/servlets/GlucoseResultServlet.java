package servlets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu.resource.OperationOutcome;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.parser.IParser;
import fhir.server.GlucoseBundleProcessor;
import util.MyMongo;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by davidhay on 18/08/14.
 */

/* no longer using a custom service...
@WebServlet(urlPatterns= {"/fhir/service/glucoseprocessor"}, displayName="Process Glucose bundle")
public class GlucoseResultServlet extends HttpServlet {

    private MyMongo _myMongo;
    private FhirContext _fhirContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //get the 'global' resources from the servlet context
        ServletContext ctx = config.getServletContext();
        _myMongo =  (MyMongo) ctx.getAttribute("mymongo");
        _fhirContext = (FhirContext) ctx.getAttribute("fhircontext");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        //first parse into a fhir bundle (need to check the mime type here. Should also check for _format paramters as well)
        //if we have to to this a lot, then a separate utility class would be good...

        Bundle bundle = null;
        IParser parser = null;
        String ct =  request.getHeader("content-type");
        if (ct.equals("application/xml+fhir")){
            parser = _fhirContext.newXmlParser();
            bundle = parser.parseBundle(request.getReader());
        } else if (ct.equals("application/json+fhir")){
            parser = _fhirContext.newJsonParser();
            bundle = parser.parseBundle(request.getReader());
        } else {
            OperationOutcome operationOutcome = new OperationOutcome();
            operationOutcome.getText().setDiv("Invalid content-type header: " + ct);
            parser = _fhirContext.newXmlParser();
            response.setStatus(415);    //unsupported media type
            out.println(parser.encodeResourceToString(operationOutcome));
            return;
        }

        //now we have a bundle we can process it...
        GlucoseBundleProcessor glucoseBundleProcessor = new GlucoseBundleProcessor(_myMongo);
        //process the bundle, and get back the list of resources with updated ID's...
        List<IResource> resources = glucoseBundleProcessor.processGlucoseUploads(bundle);
        Bundle newBundle = new Bundle();
        newBundle.getTitle().setValue("Processed Glucose results");
        newBundle.setId(new IdDt(java.util.UUID.randomUUID().toString()));
        newBundle.setPublished(new InstantDt());
        for (IResource resource : resources) {
            newBundle.addResource(resource,_fhirContext,"http://localServer/fhir");
        }

        response.setStatus(201);    //created
        out.println(parser.encodeBundleToString(newBundle));

    }



}

*/