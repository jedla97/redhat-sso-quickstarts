package org.keycloak.quickstart.profilejee;


import org.jboss.as.quickstarts.ejb.remote.stateless.RemoteCalculator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

@WebServlet(name = "Calculator", urlPatterns = {"/calc"})
public class CalculatorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();

        // if logout was passed as a get parameter, perform the logout by invalidating the session.
        if (request.getParameter("logout") != null)
        {
            request.getSession().invalidate();
            response.sendRedirect("test");
            return;
        }

        String username = request.getRemoteUser();
        out.println("Hello " + username);
        out.println("Remote calling EJB that requires a SAML assertion credential...");
        try {
            invokeStatelessBean(out);
        } catch (NamingException ex) {
            out.println("NamingException: " + ex.getMessage());
            out.close();
            return;
        }
//        Principal principal;
//        try
//        {
//            try
//            {
//                principal = bean.invokeAdministrativeMethod();
//                out.println(principal.getName() + " successfully called administrative method!");
//            }
//            catch (EJBAccessException eae)
//            {
//                out.println(username + " is not authorized to call administrative method!");
//            }
//
//            // invoke method that requires the RegularUser role.
//            try
//            {
//                principal = bean.invokeRegularMethod();
//                out.println(principal.getName() + " successfully called regular method!");
//            }
//            catch (EJBAccessException eae)
//            {
//                out.println(username + " is not authorized to call regular method!");
//            }
//
//            // invoke method that allows all roles.
//            try
//            {
//                principal = bean.invokeUnprotectedMethod();
//                out.println(principal.getName() + " successfully called unprotected method!");
//            }
//            catch (EJBAccessException eae)
//            {
//                // this should never happen as long as the user has successfully authenticated.
//                out.println(username + " is not authorized to call unprotected method!");
//            }
//
//            // invoke method that denies access to all roles.
//            try
//            {
//                principal = bean.invokeUnavailableMethod();
//                // this should never happen because the method should deny access to all roles.
//                out.println(principal.getName() + " successfully called unavailable method!");
//            }
//            catch (EJBAccessException eae)
//            {
//                out.println(username    + " is not authorized to call unavailable method!");
//            }
//        }
//        catch (Exception e)
//        {
//            out.println("Error processing request:  " + e.getMessage());
//            throw new RuntimeException("Error processing request:  "  + e.getMessage(), e);
//        }
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        this.doGet(req, resp);
    }

    private static void invokeStatelessBean(PrintWriter out) throws NamingException {
        // Let's lookup the remote stateless calculator
        final RemoteCalculator statelessRemoteCalculator = lookupRemoteStatelessCalculator();
        /*System.*/out.println("Obtained a remote stateless calculator for invocation");
        // invoke on the remote calculator
        int a = 204;
        int b = 340;
        /*System.*/out.println("Adding " + a + " and " + b + " via the remote stateless calculator deployed on the server");
        int sum = statelessRemoteCalculator.add(a, b);
        /*System.*/out.println("Remote calculator returned sum = " + sum);
        if (sum != a + b) {
            throw new RuntimeException("Remote stateless calculator returned an incorrect sum " + sum + " ,expected sum was "
                    + (a + b));
        }
        // try one more invocation, this time for subtraction
        int num1 = 3434;
        int num2 = 2332;
        /*System.*/out.println("Subtracting " + num2 + " from " + num1
                + " via the remote stateless calculator deployed on the server");
        int difference = statelessRemoteCalculator.subtract(num1, num2);
        /*System.*/out.println("Remote calculator returned difference = " + difference);
        if (difference != num1 - num2) {
            throw new RuntimeException("Remote stateless calculator returned an incorrect difference " + difference
                    + " ,expected difference was " + (num1 - num2));
        }
    }

    /**
     * Looks up and returns the proxy to remote stateless calculator bean
     *
     * @return
     * @throws NamingException
     */
    private static final String HTTP = "http";

    private static RemoteCalculator lookupRemoteStatelessCalculator() throws NamingException {
        final Hashtable<String, String> jndiProperties = new Hashtable<String, String>();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        if(Boolean.getBoolean(HTTP)) {
            //use HTTP based invocation. Each invocation will be a HTTP request
            jndiProperties.put(Context.PROVIDER_URL,"http://localhost:8280/wildfly-services");
        } else {
            //use HTTP upgrade, an initial upgrade requests is sent to upgrade to the remoting protocol
            jndiProperties.put(Context.PROVIDER_URL,"remote+http://localhost:8280");
        }
        final Context context = new InitialContext(jndiProperties);

        // The JNDI lookup name for a stateless session bean has the syntax of:
        // ejb:<appName>/<moduleName>/<distinctName>/<beanName>!<viewClassName>
        //
        // <appName> The application name is the name of the EAR that the EJB is deployed in
        // (without the .ear). If the EJB JAR is not deployed in an EAR then this is
        // blank. The app name can also be specified in the EAR's application.xml
        //
        // <moduleName> By the default the module name is the name of the EJB JAR file (without the
        // .jar suffix). The module name might be overridden in the ejb-jar.xml
        //
        // <distinctName> : EAP allows each deployment to have an (optional) distinct name.
        // This example does not use this so leave it blank.
        //
        // <beanName> : The name of the session been to be invoked.
        //
        // <viewClassName>: The fully qualified classname of the remote interface. Must include
        // the whole package name.

        // let's do the lookup
        return (RemoteCalculator) context.lookup("ejb:/ejb-remote-server-side/CalculatorBean!"
                + RemoteCalculator.class.getName());
    }
}
