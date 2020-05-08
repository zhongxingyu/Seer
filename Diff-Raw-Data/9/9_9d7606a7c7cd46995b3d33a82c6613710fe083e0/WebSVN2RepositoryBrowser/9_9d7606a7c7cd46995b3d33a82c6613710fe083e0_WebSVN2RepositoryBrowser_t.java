 /*
  * The MIT License
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package hudson.plugins.websvn2;
 
 import hudson.Extension;
 import hudson.model.Descriptor;
 import hudson.scm.RepositoryBrowser;
 import hudson.scm.SubversionChangeLogSet.LogEntry;
 import hudson.scm.SubversionChangeLogSet.Path;
 import hudson.scm.SubversionRepositoryBrowser;
 import hudson.util.FormValidation;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.kohsuke.stapler.QueryParameter;
 
 /**
  * {@link SubversionRepositoryBrowser} that produces links to http://www.websvn.info/ for SVN
  *
  * @author Andreas Mandel, based in ViewVC plugin by Mike Salnikov, based on Polarion plug-in by Jonny Wray
  */
 public class WebSVN2RepositoryBrowser extends SubversionRepositoryBrowser {
 
     private static final String CHANGE_SET_FORMAT
         = "revision.php?%1srev=%2d";
     private static final String DIFF_FORMAT
        = "diff.php?%1spath=%2s&rev=%3d";
     private static final String FILE_FORMAT
        = "filedetails.php?%1spath=%2s&rev=%d3";
     private static final Pattern URL_PATTERN
         = Pattern.compile(
             "(.*/)(revision|diff|comp|filedetails|listing|blame|dl|log)"
             + "\\.php([^?]*)\\?(repname=([^&]*))?(.*)");
 
     private static final int URL_PATTERN_BASE_URL_GROUP = 1;
     private static final int URL_PATTERN_REPNAME_GROUP = 4;
 
     public final URL url;
     private final URL baseUrl;
     private final String repname;
 
     @DataBoundConstructor
     public WebSVN2RepositoryBrowser(URL url) throws MalformedURLException {
         final Matcher webSVNurl = URL_PATTERN.matcher(url.toString());
         this.url = url;
         if (!webSVNurl.matches()) {
             this.repname = "";
             this.baseUrl = url;
         } else {
             this.baseUrl = new URL(webSVNurl.group(URL_PATTERN_BASE_URL_GROUP));
             this.repname = webSVNurl.group(URL_PATTERN_REPNAME_GROUP) + "&";
         }
     }
 
     public String getRepname() {
         return this.repname;
     }
 
     @Override
     public URL getDiffLink(Path path) throws IOException {
         return new URL(
             this.baseUrl,
             String.format(DIFF_FORMAT,
                 getRepname(),
                 URLEncoder.encode(path.getValue(), "UTF-8"),
                 path.getLogEntry().getRevision()));
     }
 
     @Override
     public URL getFileLink(Path path) throws IOException {
         // TODO: If this is a dir we should rather use listing
     	return new URL(
             this.baseUrl,
             String.format(FILE_FORMAT,
                 getRepname(),
                URLEncoder.encode(path.getValue(), "UTF-8"),
                path.getLogEntry().getRevision()));
     }
 
     @Override
     public URL getChangeSetLink(LogEntry changeSet) throws IOException {
         return new URL(
             this.baseUrl,
             String.format(CHANGE_SET_FORMAT,
                 getRepname(), changeSet.getRevision()));
     }
 
     @Extension
     public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
         public DescriptorImpl() {
             super(WebSVN2RepositoryBrowser.class);
         }
 
         public String getDisplayName() {
             return "WebSVN2";
         }
 
         public FormValidation doCheckReposUrl(@QueryParameter String value) {
             FormValidation result;
             final Matcher matcher = URL_PATTERN.matcher(value);
             if(matcher.matches())
             {
                 try
                 {
                     final URL repUrl = new URL(matcher.group(URL_PATTERN_BASE_URL_GROUP));
                     final String repName = matcher.group(URL_PATTERN_REPNAME_GROUP);
                     if (repName == null || "".equals(repName))
                     {   // Go online??
                         result = FormValidation.okWithMarkup(
                             "Please set a url including the repname property if needed.");
                     }
                     else
                     {
                         result = FormValidation.ok();
                     }
                 }
                 catch (MalformedURLException ex)
                 {
                     result = FormValidation.error(
                         "The entered url is not accepted: " + ex.getLocalizedMessage());
                 }
             }
             else if ("".equals(value))
             {
                 result = FormValidation.okWithMarkup(
                     "Please set a WebSVN url in the form "
                     + "https://<i>server</i>/websvn/listing.php?repname=<i>rep</i>&path=/trunk/..");
             }
             else
             {
                 result = FormValidation.error(
                     "Please set a url including the WebSVN php script.");
             }
             return result;
         }
 
         @Override
         public WebSVN2RepositoryBrowser newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return req.bindParameters(WebSVN2RepositoryBrowser.class, "webSVN2.");
         }
     }
 }
