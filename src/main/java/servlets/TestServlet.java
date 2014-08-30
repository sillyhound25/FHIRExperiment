package servlets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.*;
import ca.uhn.fhir.model.dstu.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu.valueset.IssueSeverityEnum;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.orchestral.data.healthkit.web.data.BaseMeasurement;
import com.orchestral.data.healthkit.web.fhir.ProcessBundle;
import util.MyMongo;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * for generic testing...
 */

@WebServlet(urlPatterns= {"/test"}, displayName="test servlet")
public class TestServlet extends HttpServlet {

    private MyMongo _myMongo;
    private FhirContext _fhirContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //get the 'global' resources from the servlet context
        ServletContext ctx = config.getServletContext();
        _myMongo = (MyMongo) ctx.getAttribute("mymongo");
        _fhirContext = (FhirContext) ctx.getAttribute("fhircontext");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }


        System.out.println(jb.toString());

        Bundle bundle = _fhirContext.newXmlParser().parseBundle(jb.toString());

        //------- this code segment is what observationService would do...
        try {
            //process the bundle, returning a list of Pojo's that implement IDapPojo
            List<BaseMeasurement> lst = ProcessBundle.process(bundle);
            for (BaseMeasurement pojo : lst) {
                String modelName = pojo.getEventTypeName();

                System.out.println(modelName);
                //=========>>>>>>>> write out to the log
            }
            OperationOutcome operationOutcome = new OperationOutcome();
            OperationOutcome.Issue issue = operationOutcome.addIssue();
            issue.setSeverity(IssueSeverityEnum.INFORMATION);
            issue.setDetails(lst.size() + " Resources added");
            response.setStatus(422);
            out.println(_fhirContext.newXmlParser().encodeResourceToString(operationOutcome));

        } catch (Exception exception) {
            //set the response to 422 (Unprocessable error and return the OperationOutcome
           // response.setStatus(exception.getStatusCode());
            //return exception.getOperationOutcome();

            OperationOutcome operationOutcome = new OperationOutcome();
            OperationOutcome.Issue issue = operationOutcome.addIssue();
            issue.setSeverity(IssueSeverityEnum.FATAL);
            issue.setDetails(exception.getMessage());
            response.setStatus(422);
            out.println(_fhirContext.newXmlParser().encodeResourceToString(operationOutcome));

        }

        //--------------- code segment ends here ---------------



        /*


        //The profile will be in a bundle tag (ie a 'category' feed element)
        String profileName = null;
        TagList tagList = bundle.getCategories();
        for (Tag tag : tagList) {
            if (tag.getScheme().contains("profile")) {  //probably a better way to do this...
                profileName = tag.getTerm();
            }
        }

        //here is where we can the bundle if it doesn't have the right profile name
        //can supply a validator class later...

        //the factory will return the correct pojo based on the Observation code...
        PojoFactory pojoFactory = new PojoFactory();

        Patient patient = null;     //this will be the patient resource. There should only be one....
        Device device = null;       //this will be the device resource. There should only be one....
        List<IPojo> lstPojo = new ArrayList<IPojo>();   //the list of POJO's for insertion into the datastore...

        //generate a list of resources. This will be the list of resources that we return to the sender...
        List<IResource> theResources = new ArrayList<IResource>();    //list of resources in bundle
        //first, locate the patient and device resources - we can;t assume they are first in the bundle...
        for (BundleEntry entry : bundle.getEntries()) {
            IResource resource = entry.getResource();
            if (resource instanceof Patient) {
                //don't really need to capture the patient actually...
                patient = (Patient) resource;

            } else if (resource instanceof Device) {
                device = (Device) resource;
            } else {
                //this is an unknown resource. What do we do?
            }
        }

        //now that we have the device resource, we can properly process the bundle.
        for (BundleEntry entry : bundle.getEntries()) {
            IResource resource = entry.getResource();

            if (resource instanceof Observation) {
                //generate a POJO based on the Observation code.
                //I'm pretty sure that HAPI creates child elements as we go so shouldn't throw an exception - but should check...
                String code = ((Observation) resource).getName().getCodingFirstRep().getCode().getValueAsString();
                IPojo iPojo = pojoFactory.getPojo(code);
                if (iPojo != null) {
                    //so we have a pojo that can be populated from this resource...
                    iPojo.setDevice(device);
                    iPojo.populate(resource);
                    lstPojo.add(iPojo);
                }
            }
        }

        //at this point, lstPojo will contain the pojo's from the bundle that can be persisted in the Platform...
        //.getModelName() will return the modelName for each one...

        out.println(jb.toString());
*/

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();




        out.println("h");


    }
}
