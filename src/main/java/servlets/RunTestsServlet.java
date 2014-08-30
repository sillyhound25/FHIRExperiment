package servlets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.dstu.resource.OperationOutcome;
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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by davidhay on 30/08/14.
 */
@WebServlet(urlPatterns= {"/runtests"}, displayName="test servlet")
public class RunTestsServlet extends HttpServlet {

    private MyMongo _myMongo;
    private FhirContext _fhirContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //get the 'global' resources from the servlet context
        ServletContext ctx = config.getServletContext();
        _myMongo = (MyMongo) ctx.getAttribute("mymongo");
        _fhirContext = (FhirContext) ctx.getAttribute("fhircontext");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        int succeed = 0;
        int fail = 0;

        StringBuilder sb = new StringBuilder();

        ServletContext context = request.getServletContext();
        String path = context.getRealPath("/testFiles");///Fail - missing ID.xml");
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        sb.append("<html><head><title>Test Report on FHIR parsing for healthkit POC</title>");

        sb.append("<link href='//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css' rel='stylesheet'>");

        sb.append("</head><body style='padding:8px;font-family:calibri'>");
        sb.append("<h3>Testing Bundle parsing for Healthkit POC</h3>");

        sb.append("==summary==");
        sb.append("<br />");
        sb.append("<table class='table table-condensed table-bordered'>");

        for (File file : listOfFiles) {
            String fileName = file.getName();
            System.out.println(fileName);
            try {
                byte[] encoded = Files.readAllBytes(file.toPath());
                String s = new String(encoded, StandardCharsets.UTF_8);

                Bundle bundle = _fhirContext.newXmlParser().parseBundle(s);


                //process the bundle, returning a list of Pojo's that implement IDapPojo
                List<BaseMeasurement> lst = ProcessBundle.process(bundle);

                String klass = "success";
                if (fileName.substring(0,4).equalsIgnoreCase("fail")) {
                    //this was supposed to fail...
                    klass = "danger";
                    fail++;
                } else {
                    succeed++;
                }

                sb.append("<tr class='"+klass+"'>");
                sb.append("<td>" + fileName + "</td>");
                sb.append("<td>Number of POJOs: " + lst.size() + "</td>");
                sb.append("</tr>");

            } catch (UnprocessableEntityException exception) {
                //set the response to 422 (Unprocessable error and return the OperationOutcome

                OperationOutcome operationOutcome = exception.getOperationOutcome();

                String message = operationOutcome.getIssueFirstRep().getDetails().toString();
                String klass = "success";
                if (fileName.substring(0,7).equalsIgnoreCase("succeed")) {
                    //this was supposed to fail...
                    klass = "danger";
                    fail++;
                } else {
                    succeed++;
                }

                sb.append("<tr class='"+klass+"'>");
                sb.append("<td>" + file.getName() + "</td>");
                sb.append("<td>" + message + "</td>");
                sb.append("</tr>");

               // response.setStatus(exception.getStatusCode());
                //out.println(_fhirContext.newXmlParser().encodeResourceToString(exception.getOperationOutcome()));

                //return ;
            }
            //other exceptions that w
             catch (Exception e) {
                 fail++;
                 String message = "This was an unexpected error: "+ e.getMessage();
                 sb.append("<tr class='danger'>");
                 sb.append("<td>" + file.getName() + "</td>");
                 sb.append("<td>" + message + "</td>");
                 sb.append("</tr>");

                System.out.println(e.getStackTrace());
            }
        }
        sb.append("</table></body></html>");
        response.setStatus(200);
        response.setHeader("content-type","text/html");

        String output = sb.toString();

        StringBuilder sbSummary = new StringBuilder();
        sbSummary.append("<h3>Overall result ");
        if (fail > 0) {
            sbSummary.append("<button type='button' class='btn btn-danger'>Fail</button>");
        } else {
            sbSummary.append("<button type='button' class='btn btn-success'>Pass</button>");
        }
        sbSummary.append("</h3>");

        sbSummary.append("<h3>Summary of Results</h3>");
        sbSummary.append("<table width='30%'>");
        int total = succeed+fail;
        sbSummary.append("<tr><td>Total number of tests</td><td>"+total+"</td></tr>");
        sbSummary.append("<tr><td>Tests that passed</td><td>"+succeed+"</td></tr>");
        sbSummary.append("<tr><td>Tests that failed</td><td>"+fail+"</td></tr>");
        sbSummary.append("</table>");
        sbSummary.append("<h3>Details</h3>");
        String reply = output.replace("==summary==",sbSummary);

        out.println(reply);

    }
}