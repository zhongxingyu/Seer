 /**
 * $Id: JUnitEEServlet.java,v 1.23 2003-01-30 08:48:43 o_rossmueller Exp $
  * $Source: C:\Users\Orionll\Desktop\junitee-cvs/JUnitEE/src/testrunner/org/junitee/servlet/JUnitEEServlet.java,v $
  */
 
 package org.junitee.servlet;
 
 
 import java.io.*;
 import java.util.*;
 import java.util.jar.*;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.junitee.output.HTMLOutput;
 import org.junitee.output.XMLOutput;
 import org.junitee.output.AbstractOutput;
 import org.junitee.output.OutputProducer;
 import org.junitee.runner.TestRunnerListener;
 import org.junitee.runner.TestRunner;
 import org.junitee.runner.TestRunnerResults;
 
 
 /**
  * This servlet implements the JUnitEE test runner. By default the classloader of this servlet is used also for
  * loading the test classes. This will work in almost any case, but if necessary you can change this behaviour by
  * subclassing this class and overwrite the method {@link #getDynamicClassLoader} to answer the classloader of your
  * choice.
  *
  * @author <a href="mailto:oliver@oross.net">Oliver Rossmueller</a>
  * @since   1.5
  */
 public class JUnitEEServlet extends HttpServlet {
 
   // todo: run tests in another thread and update result page using HTTP refresh
 
   /**
    * The form parameter which defines the name of the suite
    * class to run.  This parameter can appear more than once
    * to run multiple test suites.
    */
   protected static final String PARAM_SUITE = "suite";
 
   /**
    * The form parameter which defines the test out of the defined suite to be run.
    */
   protected static final String PARAM_TEST = "test";
 
   /**
    * The form parameter which defines if
    * resources should be checked to run all included test cases
    */
   protected static final String PARAM_RUN_ALL = "all";
   protected static final String PARAM_SEARCH = "search";
   protected static final String PARAM_OUTPUT = "output";
   protected static final String PARAM_XSL = "xsl";
   protected static final String PARAM_FILTER_TRACE = "filterTrace";
   protected static final String PARAM_STOP = "stop";
   protected static final String PARAM_THREAD = "thread";
 
   protected static final String INIT_PARAM_RESOURCES = "searchResources";
   protected static final String INIT_PARAM_XSL = "xslStylesheet";
 
   protected static final String OUTPUT_HTML = "html";
   protected static final String OUTPUT_XML = "xml";
 
   private static final String RESOURCE_PREFIX = "resource";
 
   private static final String TESTRUNNER_KEY = "testrunner";
   private static final String TESTRESULT_KEY = "testresult";
 
   // for cactus support
   public static final String CACTUS_CONTEXT_URL_PROPERTY = "cactus.contextURL";
 
 
   private String searchResources;
   private String xslStylesheet;
 
 
   /**
    * Answer the classloader used to load the test classes. The default implementation
    * answers the classloader of this class, which usally will be the classloader of
    * the web application the servlet is a part of.
    *
    * If this default behaviour does not work for you, overwrite this method and answer
    * the classloader that fits your needs.
    */
   protected ClassLoader getDynamicClassLoader() {
     return getClass().getClassLoader();
   }
 
 
   public void init(ServletConfig config) throws ServletException {
     super.init(config);
 
     searchResources = config.getInitParameter(INIT_PARAM_RESOURCES);
     xslStylesheet = config.getInitParameter(INIT_PARAM_XSL);
   }
 
 
   /**
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     String resource = request.getPathInfo();
 
     if (resource != null) {
       streamResource(resource, response);
       return;
     }
 
     String test = request.getParameter(PARAM_TEST);
     String runAll = request.getParameter(PARAM_RUN_ALL);
     String xsl = request.getParameter(PARAM_XSL);
     String stop = request.getParameter(PARAM_STOP);
     String output = request.getParameter(PARAM_OUTPUT);
     String[] testClassNames = null;
     String message;
     boolean filterTrace = true;
     boolean threaded = "true".equals(request.getParameter(PARAM_THREAD)) | getDefaultThreadMode();
 
     if ("false".equals(request.getParameter(PARAM_FILTER_TRACE))) {
       filterTrace = false;
     }
 
     // xsl parameter overwrites init param, so use the init param only if the request parameter is null
     if (xsl == null) {
       xsl = xslStylesheet;
     }
 
     if (output == null) {
       output = getDefaultOutput();
     }
 
     HttpSession session = request.getSession(false);
 
     if (session != null && stop != null) {
       TestRunner runner = (TestRunner)session.getAttribute(TESTRUNNER_KEY);
       runner.stop();
     }
 
     TestRunnerResults results = null;
 
     if (threaded && session != null) {
       results = (TestRunnerResults)session.getAttribute(TESTRESULT_KEY);
     }
     if (results != null) {
       renderResults(results, request, response, xsl, filterTrace);
       if (results.isFinished()) {
         session.removeAttribute(TESTRESULT_KEY);
         session.removeAttribute(TESTRUNNER_KEY);
       }
       return;
     }
 
     if (runAll != null) {
       testClassNames = searchForTests(request.getParameterValues(PARAM_SEARCH));
     } else {
       testClassNames = request.getParameterValues(PARAM_SUITE);
     }
 
     if (testClassNames == null) {
       if (runAll == null) {
         message = "";
       } else {
         message = "You requested all test cases to be run by setting the \"all\" parameter, but no test case was found.";
       }
       errorResponse(searchForTests(request.getParameterValues(PARAM_SEARCH)), request.getContextPath() + request.getServletPath(), message, response.getWriter(), output, request, response, xsl, filterTrace);
       return;
     }
     if ((test != null) && (testClassNames.length != 1)) {
       message = "You requested to run a single test case but provided more than one test suite.";
       errorResponse(searchForTests(request.getParameterValues(PARAM_SEARCH)), request.getContextPath() + request.getServletPath(), message, response.getWriter(), output, request, response, xsl, filterTrace);
       return;
     }
 
     // Support for Jakarta Cactus test cases:
     // Set up default Cactus System properties so that there is no need
     // to have a cactus.properties file in WEB-INF/classes
     System.setProperty(CACTUS_CONTEXT_URL_PROPERTY, "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath());
 
     if (threaded) {
       session = request.getSession(true);
       session.setAttribute("test", "xy");
     }
 
     results = runTests(test, testClassNames, request, threaded);
 
     renderResults(results, request, response, xsl, filterTrace);
 
    if ((!threaded) && (session != null)) {
       session.removeAttribute(TESTRESULT_KEY);
     }
   }
 
 
   protected void renderResults(TestRunnerResults results, HttpServletRequest request, HttpServletResponse response, String xsl, boolean filterTrace) throws IOException {
     // Set up the response
     response.setHeader("Cache-Control", "no-cache");
 
     OutputProducer output = getOutputProducer(results, request.getParameter(PARAM_OUTPUT), response, request.getContextPath() + request.getServletPath(), request.getQueryString(), xsl, filterTrace);
 
     if (output != null) {
       output.render();
     }
   }
 
 
   protected TestRunnerResults runTests(String test, String[] testClassNames, HttpServletRequest request, boolean forkThread) {
     TestRunnerResults results = new TestRunnerResults();
     TestRunner tester = new TestRunner(this.getDynamicClassLoader(), results, forkThread);
 
     if (test == null) {
       if (forkThread) {
         HttpSession session = request.getSession(true);
         session.setAttribute(TESTRUNNER_KEY, tester);
         session.setAttribute(TESTRESULT_KEY, results);
       }
       tester.run(testClassNames);
     } else {
       tester.run(testClassNames[0], test);
     }
     return results;
   }
 
 
   private void streamResource(String resource, HttpServletResponse response) throws IOException {
     InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PREFIX + resource);
     OutputStream out = response.getOutputStream();
     byte[] buffer = new byte[1024];
     int r = 0;
 
     if (resource.endsWith(".gif")) {
       response.setContentType("image/gif");
     } else if (resource.endsWith(".png")) {
       response.setContentType("image/png");
     }
     while ((r = in.read(buffer)) != -1) {
       out.write(buffer, 0, r);
     }
     in.close();
   }
 
 
   /**
    */
   public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     // Chain to get so that either will work
     this.doGet(request, response);
   }
 
 
   protected String[] searchForTests(String[] param) throws IOException {
     StringBuffer buffer = new StringBuffer();
 
     if (param == null) {
       return searchForTests((String)null);
     }
     for (int i = 0; i < param.length; i++) {
       buffer.append(param[i]).append(",");
     }
     return searchForTests(buffer.toString());
   }
 
 
   /**
    * Search all resources set via the searchResources init parameter for classes ending with "Tests"
    */
   protected String[] searchForTests(String param) throws IOException {
     if (searchResources == null && param == null) {
       return searchForTestCaseList();
     }
 
 
     StringTokenizer tokenizer;
 
     if (param != null) {
       tokenizer = new StringTokenizer(param, ",");
     } else {
       tokenizer = new StringTokenizer(searchResources, ",");
     }
 
     List tests = new ArrayList();
 
     while (tokenizer.hasMoreTokens()) {
       String token = tokenizer.nextToken().trim();
 
       try {
         InputStream in = getServletContext().getResourceAsStream("WEB-INF/lib/" + token);
 
         if (in != null) {
           JarInputStream jar = new JarInputStream(in);
           try {
             JarEntry entry = null;
 
             while ((entry = jar.getNextJarEntry()) != null) {
               String name = entry.getName();
 
               if (name.endsWith("Tests.class") || name.endsWith("Test.class")) {
                 tests.add(name.substring(0, name.length() - 6).replace('/', '.'));
               }
             }
           } catch (EOFException e) {
           } finally {
             if (jar != null) {
               try {
                 jar.close();
               } catch (IOException e) {
               }
             }
           }
 
         }
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
     String[] answer = new String[tests.size()];
     tests.toArray(answer);
     return answer;
   }
 
 
   protected String[] searchForTestCaseList() throws IOException {
     InputStream in = getServletContext().getResourceAsStream("WEB-INF/testCase.txt");
 
     if (in == null) {
       return null;
     }
     try {
       return parseTestCaseList(in);
     } finally {
       in.close();
     }
   }
 
 
   protected String[] parseTestCaseList(InputStream stream) throws IOException {
     BufferedReader in = new BufferedReader(new InputStreamReader(stream));
     String line;
     ArrayList list = new ArrayList();
 
     while ((line = in.readLine()) != null) {
       if (line.charAt(0) != '#') {
         list.add(line);
       }
     }
 
     String[] answer = new String[list.size()];
     list.toArray(answer);
     return answer;
   }
 
 
   /**
    * Answer the default output format of the test report. This implementation returns html as the default output. It
    * is possible to set the output format by using the <code>output</code> request parameter. Overwrite this method
    * in your subclass to change the output format without the need for the request parameter.
    *
    * @return
    */
   protected String getDefaultOutput() {
     return OUTPUT_HTML;
   }
 
 
   /**
    * Answer the default for the thread mode.
    * @return true if a thread should be forked
    */
   protected boolean getDefaultThreadMode() {
     return false;
   }
 
 
   /**
    * Answer the output producer for the given output format.
    *
    * @param outputParam required output format
    * @param response  response object of the current servlet request
    * @param servletPath path of this servlet
    * @return  output producer
    * @throws IOException
    */
   protected OutputProducer getOutputProducer(TestRunnerResults results, String outputParam, HttpServletResponse response, String servletPath, String queryString, String xsl, boolean filterTrace) throws IOException {
     String output = outputParam;
 
     if (output == null) {
       output = getDefaultOutput();
     }
 
     if (output.equals(OUTPUT_HTML)) {
       return new HTMLOutput(results, response, servletPath, queryString, filterTrace);
     }
     if (output.equals(OUTPUT_XML)) {
       return new XMLOutput(results, response, xsl, filterTrace);
     }
     return null;
   }
 
 
   protected void errorResponse(String[] testCases, String servletPath, String message, PrintWriter pw, String output, HttpServletRequest request, HttpServletResponse response, String xsl, boolean filterTrace) throws IOException {
     if (OUTPUT_XML.equals(output)) {
       TestRunnerResults results = new TestRunnerResults();
       results.runFailed(message);
       results.finish();
       renderResults(results, request, response, xsl, filterTrace);
     } else {
       printIndexHtml(testCases, servletPath, message, pw);
     }
   }
 
 
   protected void printIndexHtml(String[] testCases, String servletPath, String message, PrintWriter pw) throws IOException {
     InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PREFIX + "/runner.html");
     BufferedReader reader = new BufferedReader(new InputStreamReader(in));
     String line;
     StringBuffer bufferList = null;
 
     if (testCases != null) {
       bufferList = new StringBuffer();
 
       for (int i = 0; i < testCases.length; i++) {
         bufferList.append("        <tr><td class=\"cell\"><input type=\"checkbox\" name=\"suite\" value=\"");
         bufferList.append(testCases[i]).append("\">&nbsp;&nbsp;").append(testCases[i]).append("</td></tr>\n");
       }
     }
 
     while ((line = reader.readLine()) != null) {
       if (testCases != null) {
         if (line.startsWith("###")) {
           pw.print(bufferList.toString());
         } else if (line.startsWith("<!-- message -->")) {
           pw.println(message);
         } else if (line.indexOf("<form>") != -1) {
           pw.print("  <form action=\"");
           pw.print(servletPath);
           pw.println("\" method=\"get\">\n");
         } else if (!((line.startsWith("<!-- beginList")) || (line.startsWith("endList -->")))) {
           pw.println(line);
         }
       } else {
         pw.println(line);
       }
     }
     reader.close();
   }
 }
