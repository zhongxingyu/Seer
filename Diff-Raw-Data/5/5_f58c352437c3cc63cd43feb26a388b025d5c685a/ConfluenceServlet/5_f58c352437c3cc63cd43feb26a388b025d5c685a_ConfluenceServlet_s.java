 package org.iplantc.de.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 
 import org.apache.log4j.Logger;
 import org.iplantc.de.shared.ConfluenceException;
 import org.iplantc.de.shared.services.ConfluenceService;
 import org.swift.common.cli.CliClient.ClientException;
 import org.swift.common.soap.confluence.RemoteComment;
 import org.swift.common.soap.confluence.RemotePage;
 
 import com.google.gwt.user.client.rpc.SerializationException;
 
 /**
  * A service for interfacing with Confluence via SOAP.
  * 
  * @author hariolf
  * 
  */
 public class ConfluenceServlet extends SessionManagementServlet implements ConfluenceService {
     private static final long serialVersionUID = -8576144366505536966L;
 
     private static final Logger LOG = Logger.getLogger(ConfluenceServlet.class);
     
     /** A filled star */
     private static final String BLACK_STAR = "\u2605"; //$NON-NLS-1$
     
     /** An outlined star */
     private static final String WHITE_STAR = "\u2606"; //$NON-NLS-1$
     
     /** Name of the session attribute that stores the authentication token */
     private static final String AUTH_TOKEN_ATTR = "confluenceToken"; //$NON-NLS-1$
 
     /** a wiki template file containing Confluence markup */
     private static final String TEMPLATE_FILE = "wiki_template"; //$NON-NLS-1$
 
     /** a string in the template that is replaced with the tool name */
     private static final String TOOL_NAME_PLACEHOLDER = "@TOOLNAME"; //$NON-NLS-1$
 
     /** a string in the template that is replaced with the tool description */
     private static final String DESCRIPTION_PLACEHOLDER = "@DESCRIPTION"; //$NON-NLS-1$
 
     /** a string in the template that is replaced with rating stars */
     private static final String AVG_RATING_PLACEHOLDER = "@AVGRATING"; //$NON-NLS-1$
 
     /** name of the init parameter that contains the name of the .properties file */
     private static final String PROPERTIES_FILE_KEY = "org.iplantc.properties.confluence"; //$NON-NLS-1$
 
     private ConfluenceProperties properties;
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         ServletContext context = config.getServletContext();
         String propFilename = context.getInitParameter(PROPERTIES_FILE_KEY);
         properties = new ConfluenceProperties(propFilename);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
    public String addPage(String toolName, String description) {
         String content;
         try {
             content = getTemplate();
             content = replaceTemplate(content, toolName, description);
         } catch (IOException e) {
             LOG.error("Can't read wiki template file.", e); //$NON-NLS-1$
             // if the template cannot be read, use the raw description text instead
             content = description;
         }
 
         try {
             getConfluenceClient().addPage(toolName, content);
         } catch (Exception e) {
            throw new RuntimeException("Can't create Confluence page!", e); //$NON-NLS-1$
         }
 
         return properties.getConfluenceSpaceUrl() + toolName;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void updatePage(String toolName, int avgRating) throws ConfluenceException {
         String space = properties.getConfluenceSpaceName();
         try {
             IPlantConfluenceClient client = getConfluenceClient();
             long pageId = client.getContentId(toolName, space);
             RemotePage page = client.getPage(pageId);
             String content = page.getContent();
 
             int start = content.indexOf(BLACK_STAR);
             if (start < 0) {
                 start = content.indexOf(WHITE_STAR);
             }
             if (start < 0) {
                 return;
             }
 
             // remove all stars
             content = content.replaceAll(BLACK_STAR, ""); //$NON-NLS-1$
             content = content.replaceAll(WHITE_STAR, ""); //$NON-NLS-1$
 
             // add the new stars
             StringBuilder stars = new StringBuilder();
             for (int i = 0; i < avgRating; i++) {
                 stars.append(BLACK_STAR);
             }
             for (int i = 0; i < 5 - avgRating; i++) {
                 stars.append(WHITE_STAR);
             }
             content = content.substring(0, start) + stars + content.substring(start);
 
             // update page
             client.storePage(page, toolName, space, null, content, true, false);
         } catch (Exception e) {
             throw new ConfluenceException(e);
         }
     }
 
     /**
      * Substitutes placeholders in a template with a tool name and a tool description.
      * 
      * @param template the contents of the template file in one string
      * @param toolName name of the DE tool
      * @param description description of the DE Tool
      * @return a string with the tool name and description filled in
      */
     private String replaceTemplate(String template, String toolName, String description) {
         String defaultStars = WHITE_STAR + WHITE_STAR + WHITE_STAR + WHITE_STAR + WHITE_STAR;
         return template.replaceAll(TOOL_NAME_PLACEHOLDER, toolName)
                 .replaceAll(DESCRIPTION_PLACEHOLDER, description)
                 .replace(AVG_RATING_PLACEHOLDER, defaultStars);
     }
 
     /**
      * Reads the template file from a Resource in the same directory as this class. Although
      * ConfluenceClient can read a file via the --file option, this option is not used here because the
      * file is not guaranteed to be in the file system (it could be in a .jar).
      * 
      * @return the contents of TEMPLATE_FILE
      * @throws IOException
      */
     private String getTemplate() throws IOException {
         InputStream stream = ConfluenceServlet.class.getResourceAsStream(TEMPLATE_FILE);
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
             StringBuilder builder = new StringBuilder();
             char[] buffer = new char[1024];
             while (true) {
                 int charsRead = reader.read(buffer);
                 if (charsRead < 0) {
                     break;
                 }
                 builder.append(Arrays.copyOf(buffer, charsRead));
             }
             return builder.toString();
         } finally {
             if (stream != null) {
                 stream.close();
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String addComment(String toolName, String comment) throws ConfluenceException {
         String space = properties.getConfluenceSpaceName();
         try {
             RemoteComment confluenceComment = getConfluenceClient().addComment(space, toolName, comment);
             return String.valueOf(confluenceComment.getId());
         } catch (Exception e) {
             throw new ConfluenceException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void removeComment(String toolName, Long commentId) throws ConfluenceException {
         try {
             getConfluenceClient().removeComment(commentId);
         } catch (Exception e) {
             throw new ConfluenceException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void editComment(String toolName, Long commentId, String newComment)
             throws ConfluenceException {
         try {
             getConfluenceClient().editComment(commentId, newComment);
         } catch (Exception e) {
             throw new ConfluenceException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getComment(long commentId) throws ConfluenceException {
         try {
             return getConfluenceClient().getComment(commentId);
         } catch (Exception e) {
             throw new ConfluenceException(e);
         }
     }
 
     private IPlantConfluenceClient getConfluenceClient() throws SerializationException, ClientException {
         String authToken = getAttribute(AUTH_TOKEN_ATTR);
         return new IPlantConfluenceClient(properties, authToken);
     }
 }
