package servlets;

/**
 * The 'main' class for the servlet that supports fhir queries. For each resource to support,
 * create a new resource provider and add it to the  resourceProviders list...
 */


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu.resource.ListResource;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.rest.annotation.Transaction;
import ca.uhn.fhir.rest.annotation.TransactionParam;
import ca.uhn.fhir.rest.method.SearchMethodBinding;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import fhir.*;
import util.MyMongo;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns= {"/fhir/*"}, displayName="FHIR Server")
public class OrionRestfulServlet extends RestfulServer {

    private static final long serialVersionUID = 1L;
    //private MyMongo _myMongo;

    //ServletContext context =  getServletContext();
    //FhirContext ctx = (FhirContext) getServletContext().getAttribute("fhircontext");

    private MyMongo _myMongo;
    private FhirContext _fhirContext;

    /*

    @Override
    public void init(ServletConfig config) throws ServletException {
        //get the 'global' resources from the servlet context
        ServletContext ctx = config.getServletContext();
       // _myMongo = (MyMongo) ctx.getAttribute("mymongo");
        _fhirContext = (FhirContext) ctx.getAttribute("fhircontext");
    }
*/

    public OrionRestfulServlet() {

        _myMongo = new MyMongo();   //constructor is called before init (of course)...



        //ServletContext ctx =  getServletContext();
        // ServletContext ctx = config.getServletContext();
        // _myMongo = (MyMongo) ctx.getAttribute("mymongo");
       // _fhirContext = (FhirContext) ctx.getAttribute("fhircontext");



        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
        resourceProviders.add(new ConditionResourceProvider(_myMongo));
        resourceProviders.add(new ObservationResourceProvider(_myMongo));
        resourceProviders.add(new PatientResourceProvider(_myMongo));
        resourceProviders.add(new PractitionerResourceProvider(_myMongo));
        resourceProviders.add(new FamilyHistoryResourceProvider(_myMongo));
        resourceProviders.add(new ProfileResourceProvider(_myMongo));
        setResourceProviders(resourceProviders);

        List<Object> plainProviders=new ArrayList<Object>();
        plainProviders.add(new TransactionProvider(_myMongo));
        setPlainProviders(plainProviders);

        setFhirContext(new FhirContext(ListResource.class));
        //setFhirContext(_fhirContext);
        setServerConformanceProvider(new ConformanceProvider(this ));
        setServerName("FHIRBlog server");
        setServerVersion("0.1");
    }

    //called by the RestfulServer immediately before it's own init...

    protected void initialize() throws ServletException {
        //http://docs.oracle.com/javaee/7/tutorial/doc/jsonp003.htm#BABHAHIA
        JsonObject model = Json.createObjectBuilder()
                .add("startUp", (new Date()).toString())
                .build();
        _myMongo.addToLog(model);

        ServletContext ctx =  getServletContext();
        System.out.println("test");
       // ServletContext ctx = config.getServletContext();
        // _myMongo = (MyMongo) ctx.getAttribute("mymongo");
        _fhirContext = (FhirContext) ctx.getAttribute("fhircontext");

    }



    //as an initial security check. I'm sure HAPI will be enhancing this...
    //apart from conformance, you need to have a valid access token
    @Override
    public void handleRequest(SearchMethodBinding.RequestType theRequestType,
                              javax.servlet.http.HttpServletRequest theRequest,
                              javax.servlet.http.HttpServletResponse theResponse)
            throws javax.servlet.ServletException,
            IOException {

        String uri = theRequest.getRequestURI();
        //set true to disable access token checking...
        Boolean allowAll = true;

        //anyone can access metadata...
        if (uri.equals("/fhir/metadata") || allowAll) {
            super.handleRequest(theRequestType,theRequest,theResponse);
        } else {
            //but you need to be authorized to access clincial data...
            String auth = theRequest.getHeader("Authorization");
            if (auth != null){
                auth = auth.substring(7);//get rid of the 'Bearer ' at the front
                ServletContext context =  getServletContext();
                Map<String,JsonObject> oauthtokens = (Map<String,JsonObject>) context.getAttribute("oauthtokens");
                if (oauthtokens.containsKey(auth)) {
                    //we could pull out the actual access token, and apply security logic there...
                    super.handleRequest(theRequestType,theRequest,theResponse);
                } else {
                    theResponse.setStatus(403);    //forbidden.
                }
            } else {
                theResponse.setStatus(403);    //forbidden.
            }
        }
    }



    @Override
    public void destroy(){
        super.destroy();
        _myMongo.closeConnection();
    }


    //add a header to all responses - just because I can!
    @Override
    public void addHeadersToResponse (HttpServletResponse theHttpResponse) {
        super.addHeadersToResponse(theHttpResponse);
        theHttpResponse.addHeader("OH-prototype-by", "David Hay");
    }








}
