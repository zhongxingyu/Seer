 /**
 * $Id: JUnitEEServlet.java,v 1.28 2003-07-27 22:27:15 o_rossmueller Exp $
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
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.junitee.output.HTMLOutput;
 import org.junitee.output.OutputProducer;
 import org.junitee.output.XMLOutput;
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
 
   /**
    * The form parameter which defines the name of the suite
    * class to run.  This parameter can appear more than once
    * to run multiple test suites.
    */
   private static final String PARAM_SUITE = "suite";
 
   /**
    * The form parameter which defines the test out of the defined suite to be run.
    */
   private static final String PARAM_TEST = "test";
 
   /**
    * The form parameter which defines if
    * resources should be checked to run all included test cases
    */
   private static final String PARAM_RUN_ALL = "all";
   private static final String PARAM_SEARCH = "search";
   private static final String PARAM_OUTPUT = "output";
   private static final String PARAM_XSL = "xsl";
   private static final String PARAM_FILTER_TRACE = "filterTrace";
   private static final String PARAM_STOP = "stop";
   private static final String PARAM_THREAD = "thread";
   private static final String PARAM_SHOWMETHODS = "showMethods";
 
   private static final String INIT_PARAM_RESOURCES = "searchResources";
   private static final String INIT_PARAM_XSL = "xslStylesheet";
 
   public static final String OUTPUT_HTML = "html";
   public static final String OUTPUT_XML = "xml";
 
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
     boolean showMethods = "true".equals(request.getParameter(PARAM_SHOWMETHODS));
     String[] testClassNames = null;
     String message;
     boolean filterTrace = true;
     boolean threaded = "true".equals(request.getParameter(PARAM_THREAD)) | getDefaultThreadMode();
     boolean forkThread = threaded;
 
 
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
       errorResponse(searchForTests(request.getParameterValues(PARAM_SEARCH)), request.getContextPath() + request.getServletPath(), message, output, request, response, xsl, filterTrace, showMethods);
       return;
     }
     if ((test != null) && (testClassNames.length != 1)) {
       message = "You requested to run a single test case but provided more than one test suite.";
       errorResponse(searchForTests(request.getParameterValues(PARAM_SEARCH)), request.getContextPath() + request.getServletPath(), message, output, request, response, xsl, filterTrace, showMethods);
       return;
     }
 
     // Support for Jakarta Cactus test cases:
     // Set up default Cactus System properties so that there is no need
     // to have a cactus.properties file in WEB-INF/classes
     System.setProperty(CACTUS_CONTEXT_URL_PROPERTY, "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath());
 
     forkThread = forkThread & (testClassNames.length > 1);
 
     if (forkThread) {
       session = request.getSession(true);
       session.setAttribute("test", "xy");
     }
 
     results = runTests(test, testClassNames, request, forkThread);
 
     renderResults(results, request, response, xsl, filterTrace);
 
     if ((!forkThread) && (session != null)) {
       session.removeAttribute(TESTRESULT_KEY);
     }
   }
 
 
   protected void renderResults(TestRunnerResults results, HttpServletRequest request, HttpServletResponse response, String xsl, boolean filterTrace) throws IOException {
     OutputProducer output = getOutputProducer(results, request.getParameter(PARAM_OUTPUT), request, response, xsl, filterTrace);
 
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
       return filterTests(searchForTests((String)null));
     }
     for (int i = 0; i < param.length; i++) {
       buffer.append(param[i]).append(",");
     }
     return filterTests(searchForTests(buffer.toString()));
   }
 
 
   /**
    * Filter class which are not a subclass of TestCase and suites with no tests
    *
    * @param tests
    * @return
    */
   private String[] filterTests(String[] tests) {
     ClassLoader loader = getDynamicClassLoader();
     TestRunnerResults testResults = new TestRunnerResults();
     TestRunner tester = new TestRunner(loader, testResults, false);
     List names = new ArrayList(Arrays.asList(tests));
     Iterator iterator = names.iterator();
 
     while (iterator.hasNext()) {
       String name = (String)iterator.next();
       try {
         Class clazz = loader.loadClass(name);
 
         if (!Test.class.isAssignableFrom(clazz)) {
           iterator.remove();
         } else {
           Test test = tester.getTest(name);
 
           if (test instanceof TestSuite) {
             TestSuite suite = (TestSuite)test;
 
             if (suite.testCount() == 0) {
               iterator.remove();
             } else if (suite.testCount() == 1) {
               Test singleTest = suite.testAt(0);
 
               if (singleTest.getClass().getName().equals("junit.framework.TestSuite$1")) {
                 // no test method found
                 iterator.remove();
               }
             }
           }
         }
       } catch (ClassNotFoundException e) {
         iterator.remove();
       }
     }
     return (String[])names.toArray(new String[names.size()]);
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
 
         if (in == null) {
           // there are issues with some containers, so try again with a trailing slash
           in = getServletContext().getResourceAsStream("/WEB-INF/lib/" + token);
         }
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
       // there are issues with some containers with/without trailing slash, so try it again
       in = getServletContext().getResourceAsStream("/WEB-INF/testCase.txt");
     }
 
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
       line = line.trim();
 
       if (line.length() == 0) {
         continue;
       }
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
    * @param results
    * @param outputParam
    * @param request
    * @param response
    * @param xsl
    * @param filterTrace
    * @return
    * @throws IOException
    */
   protected OutputProducer getOutputProducer(TestRunnerResults results, String outputParam, HttpServletRequest request, HttpServletResponse response, String xsl, boolean filterTrace) throws IOException {
     String output = outputParam;
 
     if (output == null) {
       output = getDefaultOutput();
     }
 
     if (output.equals(OUTPUT_HTML)) {
       return new HTMLOutput(results, request, response, filterTrace);
     }
     if (output.equals(OUTPUT_XML)) {
       return new XMLOutput(results, response, xsl, filterTrace);
     }
     return null;
   }
 
 
   protected void errorResponse(String[] testCases, String servletPath, String message, String output, HttpServletRequest request, HttpServletResponse response, String xsl, boolean filterTrace, boolean showMethods) throws IOException {
     if (OUTPUT_XML.equals(output)) {
       TestRunnerResults results = new TestRunnerResults();
       results.runFailed(message);
       results.finish();
       renderResults(results, request, response, xsl, filterTrace);
     } else {
       response.setContentType("text/html");
       printIndexHtml(testCases, servletPath, message, response.getWriter(), showMethods);
     }
   }
 
 
   protected void printIndexHtml(String[] testCases, String servletPath, String message, PrintWriter pw, boolean showMethods) throws IOException {
     InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PREFIX + "/runner.html");
     BufferedReader reader = new BufferedReader(new InputStreamReader(in));
     String line;
     StringBuffer bufferList = null;
 
     if (testCases != null) {
       bufferList = new StringBuffer();
 
       for (int i = 0; i < testCases.length; i++) {
         bufferList.append("        <tr><td class=\"cell\"><input type=\"checkbox\" name=\"suite\" value=\"");
         bufferList.append(testCases[i]).append("\">&nbsp;&nbsp;").append(testCases[i]).append("</td></tr>\n");
 
         if (showMethods) {
           printIndexHtmlTestMethods(bufferList, testCases[i], servletPath);
         }
       }
     }
 
     while ((line = reader.readLine()) != null) {
       if (testCases != null) {
         if (line.startsWith("###")) {
           pw.print(bufferList.toString());
         } else if (line.startsWith("##show/hide")) {
           pw.print("(<a href=\"");
           pw.print(servletPath);
           pw.print("?showMethods=");
           pw.print(!showMethods);
           pw.print("\">");
           if (showMethods) {
             pw.print("hide");
           } else {
             pw.print("show");
           }
           pw.print(" tests</a>)");
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
 
 
   /**
    * Generates links to run individual test methods
    *
    * @param bufferList
    * @param testCase
    * @param servletPath
    */
   protected void printIndexHtmlTestMethods(StringBuffer bufferList, String testCase, String servletPath) {
     String[] methods = getTestClassMethods(testCase);
 
     if (methods.length > 0) {
 
       for (int j = 0; j < methods.length; j++) {
         bufferList.append("        <tr><td class=\"methodcell\">");
         bufferList.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"" + servletPath + "?" + PARAM_SUITE + "=" + testCase + "&" + PARAM_TEST + "="
                           + methods[j] + "\">");
         bufferList.append(methods[j]);
         bufferList.append("</a></td></tr>");
       }
     }
   }
 
 
   /**
    * Looks up all the test methods for a particular test class.
    * Not particulary efficient, it just leverages existing test lookup
    * functionality from org.junitee.runner.TestRunner
    *
    * @param testClass
    *
    * @return
    */
   protected String[] getTestClassMethods(String testClass) {
     TestRunnerResults results = new TestRunnerResults();
     TestRunner tester = new TestRunner(this.getDynamicClassLoader(), results, false);
     Test test = tester.getTest(testClass);
     ArrayList testMethodList = new ArrayList();
 
     if (test instanceof TestSuite) {
       TestSuite suite = (TestSuite)test;
 
       for (int i = 0; i < suite.testCount(); i++) {
         Test testMethod = suite.testAt(i);
 
         if (testMethod instanceof TestCase) {
           testMethodList.add(((TestCase)testMethod).getName());
         }
       }
     }
 
     String[] testMethodArray = new String[testMethodList.size()];
 
     return (String[])testMethodList.toArray(testMethodArray);
   }
 }
