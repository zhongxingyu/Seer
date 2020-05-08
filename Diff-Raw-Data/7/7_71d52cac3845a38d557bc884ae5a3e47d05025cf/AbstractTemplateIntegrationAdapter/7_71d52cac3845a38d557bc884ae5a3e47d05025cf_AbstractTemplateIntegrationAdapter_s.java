 package org.wings.adapter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.wings.IntegrationFrame;
 import org.wings.STemplateLayout;
 import org.wings.TemplateIntegrationFrame;
 import org.wings.adapter.parser.HtmlParser;
 import org.wings.conf.Integration;
 import org.wings.conf.UrlExtension;
 import org.wings.session.Session;
 import org.wings.session.SessionManager;
 import org.wings.template.TemplateSource;
 import org.wings.util.HtmlParserUtils;
 import org.mvel2.MVEL;
 
 import au.id.jericho.lib.html.Attribute;
 import au.id.jericho.lib.html.Attributes;
 import au.id.jericho.lib.html.Element;
 import au.id.jericho.lib.html.Source;
 import au.id.jericho.lib.html.StartTagType;
 import au.id.jericho.lib.html.Tag;
 
 public abstract class AbstractTemplateIntegrationAdapter extends
         AbstractIntegrationAdapter implements TemplateIntegrationAdapter, HtmlParser {
 
     private STemplateLayout layout;
     
     public AbstractTemplateIntegrationAdapter(IntegrationFrame frame, Integration integration) {
         super(frame, integration);
         
 		SessionManager.getSession().setProperty("AbstractTemplateIntegrationAdapter", this);
     }
 
     public void initialize() {
         layout = ((TemplateIntegrationFrame) frame).getTemplateIntegrationLayout();
     }
     
     public void setTemplate(TemplateSource templateSource) throws IOException {
         layout.setTemplate(templateSource);
     }
     
     public String process(String responseBody) {
         Source source = new Source(responseBody);
 
         // Resolves all includes contained in source (recursive).
         source = resolveIncludes(source);
 
         // Removes all wingS integration comments like <!-- IGNORE BEGIN --> and
         // <!-- IGNORE END -->
         source = removeIgnoreComments(source);
 
         // Parses the head of the template if head tag is present.
         if (HtmlParserUtils.isTagPresent(source, Tag.HEAD)) {
             Source headSource = HtmlParserUtils.getSourcesForTag(source, Tag.HEAD)[0];
             parseHead(headSource);
         }
 
         // Parses the head of the template if body tag is present.
         if (HtmlParserUtils.isTagPresent(source, Tag.BODY)) {
             Source bodySource = HtmlParserUtils.getSourcesForTag(source, Tag.BODY)[0];
             bodySource = parseBody(bodySource);
             return bodySource.toString();
         }
 
         return source.toString();
     }
 
     public Source resolveIncludes(Source source) {
         return resolveIncludes0(source);
     }
     
     private Source resolveIncludes0(Source source) {
         Element include;
         while ((include = source.findNextElement(0, "include")) != null) {
 
             // Get the URL (default) extension.
             Attribute type = include.getAttributes().get("type");
             UrlExtension urlExtension = integration.getResource()
                     .getUrlExtension((type != null ? type.getValue() : null));
 
             List<String> variables = urlExtension.getVariables();
             List<String> values = new ArrayList<String>();
 
             Attributes attributes = include.getAttributes();
             for (String variable : variables) {
                 Attribute attribute = attributes.get(variable);
 
                 if (attribute != null) {
                     values.add(attribute.getValue());
                 }
                 else if (variable.startsWith("$session")) {
                 	Session session = SessionManager.getSession();
                 	String expression = variable.substring(9, variable.length());
                 	
                 	Object value = MVEL.getProperty(expression, session);
                 	values.add(value.toString());
                 }
                 else {
                     System.out.println("No attribute " + variable + " has been found within element \"" + include + "\".");
                 }
             }
 
             int begin = include.getBegin();
             int end = include.getEnd();
             int end2 = source.length();
             
             StringBuffer sb = new StringBuffer();
             sb.append(source.subSequence(0, begin));
 
            // Resolves nested of includes.
             String url = prepareUrl(urlExtension.getReplacedValue(values.toArray(new String[values.size()])));
            sb.append(resolveIncludes0(new Source(requestInclude(url))).toString());
             sb.append(source.subSequence(end, end2));
             source = new Source(sb);
         }
 
         return source;
     }
     
     @SuppressWarnings("unchecked")
     private Source removeIgnoreComments(Source source) {
         List<Element> comments = source.findAllElements(StartTagType.COMMENT);
         if (comments.size() < 1) {
             return source;
         }
 
         List<Element> ignoreBeginComments = new ArrayList<Element>();
         List<Element> ignoreEndComments = new ArrayList<Element>();
         for (Element comment : comments) {
             String commentContent = comment.toString().trim();
 
             if ("<!-- IGNORE BEGIN -->".equalsIgnoreCase(commentContent)) {
                 ignoreBeginComments.add(comment);
             }
             else if ("<!-- IGNORE END -->".equalsIgnoreCase(commentContent)) {
                 ignoreEndComments.add(comment);
             }
         }
 
         // Throw an exception if size of ignore begin and size of ignore end
         // doesn't match.
         if (ignoreBeginComments.size() != ignoreEndComments.size()) {
             throw new IllegalStateException("Amount of <!-- IGNORE BEGIN --> (" + ignoreBeginComments.size()
                     + ") doesn't match amount of <!-- IGNORE END --> (" + ignoreEndComments.size() + ") comments.");
         }
         
         // Return immediately if no ignore comment has been found.
         if (ignoreBeginComments.size() < 1) {
             return source;
         }
 
         // Removes ignore comments.
         // TODO: Tweak approach to achieve performance upgrade.
         StringBuilder withoutIgnoreComments = new StringBuilder();
         int length = ignoreBeginComments.size();
         for (int i = 0; i < length; i++) {
             Element ignoreBegin = ignoreBeginComments.get(i);
             Element ignoreEnd = ignoreEndComments.get(i);
             
             withoutIgnoreComments.append(source.subSequence(0, ignoreBegin.getBegin()));
             
             if ((i + 1) == length) {
                 withoutIgnoreComments.append(source.subSequence(ignoreEnd.getEnd(), source.length()));
             }
             else {
                 Element nextIgnoreBegin = ignoreBeginComments.get(i + 1);
                 withoutIgnoreComments.append(source.subSequence(ignoreEnd.getEnd(), nextIgnoreBegin.getBegin()));
             }       
         }
         
         return new Source(withoutIgnoreComments);
     }
     
     protected abstract String requestInclude(String url);
     
     protected abstract String prepareUrl(String extensionUrl);
 
 }
