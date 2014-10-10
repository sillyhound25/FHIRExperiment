package servlets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.*;
import ca.uhn.fhir.model.dstu.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu.composite.QuantityDt;
import ca.uhn.fhir.model.dstu.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu.resource.*;
import ca.uhn.fhir.model.dstu.valueset.AdministrativeGenderCodesEnum;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.parser.IParser;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@WebServlet(urlPatterns= {"/samplemlom"}, displayName="test servlet")
public class SampleMLOMServlet extends HttpServlet {

    private MyMongo _myMongo;
    private FhirContext _fhirContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //get the 'global' resources from the servlet context
        ServletContext ctx = config.getServletContext();
        _myMongo = (MyMongo) ctx.getAttribute("mymongo");
        _fhirContext = (FhirContext) ctx.getAttribute("fhircontext");
    }

    //echo the input back as XML...
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }


        //System.out.println(jb.toString());


        Bundle bundle = null;
        String output = null;

        if (request.getHeader("content-type").equals("application/json+fhir")) {
            bundle = _fhirContext.newJsonParser().parseBundle(jb.toString());
        } else {
            bundle = _fhirContext.newXmlParser().parseBundle(jb.toString());
        }

        if (request.getHeader("accept").equals("application/json+fhir")) {
            output = _fhirContext.newJsonParser().encodeBundleToString(bundle);
        } else {
            output = _fhirContext.newXmlParser().encodeBundleToString(bundle);
        }



       // IParser parser = _fhirContext.newXmlParser();
        response.addHeader("Content-Type",request.getHeader("accept"));


    //IParser xMLParser = _fhirContext.newXmlParser();
    //response.addHeader("Content-Type","application/xml");
        out.println(output);

    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        //allow for either XML or JSON as the output
        String hAccept = request.getHeader("accept");

        Bundle bundle = new Bundle();
        bundle.setId(new IdDt(java.util.UUID.randomUUID().toString()));

        Patient patient = new Patient();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);         //18 years old
        patient.setBirthDate(new DateTimeDt(cal.getTime()));
        patient.addName().addFamily("Power");
        patient.getName().get(0).addGiven("Cold");
        patient.setGender(AdministrativeGenderCodesEnum.M);
        patient.addIdentifier().setSystem("http://www.govt.nz/nhi").setValue("PRP1660");


        ListResource list = new ListResource();
        list.setId("list1");    //This is the id of the model entry. It's the only resource with an ID..

        //add the list to the bundle.
        list.setSubject(new ResourceReferenceDt(patient));
        bundle.addEntry().setResource(list);

        //the person who performed the review...
        Practitioner practitioner = new Practitioner();
        practitioner.getName().setText("Dr Jones");
        list.setSource(new ResourceReferenceDt(practitioner));

        //where the review was performed. It's an extension to List..
        Location location = new Location();
        location.setName("Peter's Pharmacy");
        addResourceReferenceExtension(list,"http://extension.fhir.hl7.org.nz/mlom#location",location);


        //add a sample drug. This will be a medicationStatement (with extensions)
        MedicationStatement medicationStatement = new MedicationStatement();

        //the medication resource - describes the actual drug rather than the administartion of the drug...
        Medication medication = new Medication();
        medication.setCode(new CodeableConceptDt("GENERIC-NZMT","10002203440"));
        medication.setIsBrand(true);        //indicate that this is a trade drug
        medication.setName("dolophine");
        Medication.Product product = new Medication.Product();
        //the formulation...
        product.setForm(new CodeableConceptDt("http://forms","cream"));
        medication.setProduct(product);



        medicationStatement.setMedication(new ResourceReferenceDt(medication));
        //add a generic to
        Medication generic = new Medication();
        generic.setCode(new CodeableConceptDt("GENERIC-NZMT","655519927"));
        generic.setIsBrand(false);        //idicate that theis is a genric drug
        generic.setName("methadone hydrochloride");
        addResourceReferenceExtension(medication,"http://extension.fhir.hl7.org.nz/mlom#generic",generic);

        addDTExtension(medication,"http://extension.fhir.hl7.org.nz/mlom#isRestricted",new BooleanDt(true));
        addDTExtension(medication,"http://extension.fhir.hl7.org.nz/mlom#isControlled",new BooleanDt(true));

        MedicationStatement.Dosage dosage =  medicationStatement.addDosage();
        //set the quantity - amount per dose - to 10g
        QuantityDt qty = new QuantityDt();
        qty.setValue(10);
        qty.setUnits("g");
        qty.setSystem("http://unitsofmeasure.org");
        dosage.setQuantity(qty);

        dosage.setRoute(new CodeableConceptDt("http://hl7.org/fhir/vs/route-codes","79710002"));    //topical
        dosage.setAsNeeded(new BooleanDt(false));

        addDTExtension(medicationStatement,"http://extension.fhir.hl7.org.nz/mlom#instructions",
                new StringDt("Apply when needed"));

        CodeableConceptDt indication = new CodeableConceptDt();
        indication.setText("Dry Skin");


        addDTExtension(medicationStatement, "http://extension.fhir.hl7.org.nz/mlom#indication", indication);
        addDTExtension(medicationStatement,"http://extension.fhir.hl7.org.nz/mlom#comments",
                new StringDt("Additional Comments"));


        ListResource.Entry entry = list.addEntry();// new ListResource.Entry();

        //add the medication statement to the list
        entry.setItem(new ResourceReferenceDt(medicationStatement));
        //make the 'reason added' to be new...

        entry.addFlag().setText("Newly added").addCoding().setDisplay("new")
                .setSystem("http://fhir.hl7.org.nz/vs/mlomFlag").setCode("new");


        IParser parser = null;

        if (hAccept.indexOf("json") > -1) {
            parser = _fhirContext.newJsonParser();
            response.addHeader("Content-Type","application/json");
        } else {
            parser = _fhirContext.newXmlParser();
            response.addHeader("Content-Type","application/xml");
        }

        //IParser xMLParser = _fhirContext.newXmlParser();
        //response.addHeader("Content-Type","application/xml");
        out.println(parser.encodeBundleToString(bundle));

    }

    private void addResourceReferenceExtension(BaseElement parent, String url, IResource target) {
        ExtensionDt extLocation = new ExtensionDt();
        extLocation.setModifier(false);
        extLocation.setUrl(url);
        extLocation.setValue(new ResourceReferenceDt(target));
        parent.addUndeclaredExtension(extLocation);
    }

    private void addDTExtension(BaseElement parent, String url, IIdentifiableElement dt) {
        ExtensionDt extLocation = new ExtensionDt();
        extLocation.setModifier(false);
        extLocation.setUrl(url);
        extLocation.setValue(dt);
        parent.addUndeclaredExtension(extLocation);
    }
}
