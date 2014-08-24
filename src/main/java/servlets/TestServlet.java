package servlets;

import ca.uhn.fhir.context.FhirContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
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
import java.io.StringReader;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();



        List<String> lst = _myMongo.getSimpleXml(null);

        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();


            for (String s : lst) {
                InputSource is = new InputSource(new StringReader(s));
                Document doc = docBuilder.parse(is);


                //NodeList l = doc.getElementsByTagName("GlucoseResults");
                //Integer ll =  l.getLength();

//doc.getDocumentElement();

                NodeList children = doc.getDocumentElement().getChildNodes();
                //NodeList children = l.item(0).getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Element element = (Element) children.item(i);

                    String tagName = element.getTagName();
                    String Value = element.getTextContent();

                    //populate Observation Object...
                    if (tagName == "PatientID") {

                    } else if (tagName == "Value") {

                    }






                    System.out.println(tagName + " " + Value);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        out.println("h");


    }
}
