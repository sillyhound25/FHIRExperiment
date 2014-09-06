package cc;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidhay on 6/09/14.
 */
@WebServlet(urlPatterns= {"/cc/profilelist"}, displayName="Get list of profiles")
public class GetProfilesListServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String> lstProfile = new ArrayList<String>();

        lstProfile.add("ccAllergyIntolerance");
        lstProfile.add("ccObservation");
        lstProfile.add("ccObservation-Simple");
        lstProfile.add("ccCondition");
        lstProfile.add("ccEncounter");
        lstProfile.add("ccFamilyHistory");
        lstProfile.add("ccMedication");
        lstProfile.add("ccMedicationPrescription");
        lstProfile.add("ccMedicationDispense");
        lstProfile.add("ccMedicationStatement");
        lstProfile.add("ccList");
        lstProfile.add("ccPractitioner");
        lstProfile.add("ccDiagnosticReport");
        lstProfile.add("ccCarePlan");
        lstProfile.add("ccImagingStudy");

        JsonObjectBuilder json = Json.createObjectBuilder();// .build();

        JsonArrayBuilder array = Json.createArrayBuilder();//.build();
        for (String profile : lstProfile) {
            array.add(profile);
        }

        json.add("profile",array);

        StringWriter stWriter = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(stWriter);
        jsonWriter.writeObject(json.build());
        jsonWriter.close();

        PrintWriter out = response.getWriter();
        response.addHeader("Content-Type","application/json+fhir");
        out.println(stWriter.toString());



    }
}
