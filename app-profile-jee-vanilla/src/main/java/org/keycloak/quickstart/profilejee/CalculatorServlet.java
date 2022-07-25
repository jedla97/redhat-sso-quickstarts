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
import java.util.Properties;

@WebServlet(name = "Calculator", urlPatterns = {"calc"})
public class CalculatorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String username = request.getRemoteUser();
        out.println("Hello " + username);
        out.println("Remote calling EJB that requires a SAML assertion credential...");

        try {
            invokeStatelessBean(out);
            out.println("bean invoked");
        } catch (NamingException ex) {
            out.println("NamingException: " + ex.getMessage());
            out.close();
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
        final RemoteCalculator statelessRemoteCalculator = lookupRemoteStatelessCalculator(out);
        /*System.*/
        out.println("Obtained a remote stateless calculator for invocation");
        // invoke on the remote calculator
        int a = 204;
        int b = 340;
        /*System.*/
        int sum = 0;
        out.println("Adding " + a + " and " + b + " via the remote stateless calculator deployed on the server");
        sum = statelessRemoteCalculator.add(a, b);
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

    private static RemoteCalculator lookupRemoteStatelessCalculator(PrintWriter out) throws NamingException {
        final Hashtable<String, String> jndiProperties = new Hashtable<>();
        //jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.PROVIDER_URL, "remote+http://localhost:8080");
        final Context context = new InitialContext(jndiProperties);

        return (RemoteCalculator) context
                .lookup("ejb:/vanilla/CalculatorBean!" + RemoteCalculator.class.getName());
    }
}
