package listeners;

import auth.Person;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu.resource.ListResource;
import util.MyMongo;

import javax.json.JsonObject;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidhay on 24/07/14.
 */

@WebListener
public class GlobalListener implements ServletContextListener,ServletRequestListener {

    //when the context (?application) initializes...
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();

        Map<String,Person> oauthcodes = new HashMap<String, Person>();

        //map of outh codes, indexed by code
        context.setAttribute("oauthcodes",oauthcodes);

        //map of oauth tokens, indexed by token
        context.setAttribute("oauthtokens", new HashMap<String,JsonObject>());

        //logged in users
        context.setAttribute("usertokens", new HashMap<String, Person>());

        //A HAPI FHIR context object
        context.setAttribute("fhircontext",new FhirContext(ListResource.class));

        //Access to the mongo database
        context.setAttribute("mymongo",new MyMongo());

        System.out.println("Context Init");

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    //when a new httprequest is received
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        ServletRequest req = sre.getServletRequest();
        req.setAttribute("start",System.nanoTime());
    }

    //when an http request is completed
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        ServletRequest req = sre.getServletRequest();
        Long start = (Long) req.getAttribute("start");
        Long end = System.nanoTime();
        HttpServletRequest hReq = (HttpServletRequest) req;
        String uri = hReq.getRequestURI();
        Long elap = (end-start)/1000000;
        String method = ((HttpServletRequest) req).getMethod();
        System.out.println(method + " " + uri + " took " + elap.toString() + " milliseconds");

        //add to the mongo log (if it is active)
        MyMongo myMongo = (MyMongo) sre.getServletContext().getAttribute("mymongo");
        myMongo.addToHttpLog(uri,elap);

    }


}
