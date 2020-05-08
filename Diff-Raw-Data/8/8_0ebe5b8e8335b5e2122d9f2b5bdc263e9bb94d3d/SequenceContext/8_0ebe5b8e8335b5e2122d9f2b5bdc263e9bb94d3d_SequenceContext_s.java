 package com.github.kongchen.ginger.sequence;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.log4j.Logger;
 
 import com.github.kongchen.ginger.GingerConstants;
 import com.github.kongchen.ginger.InputConfiguration;
 import com.github.kongchen.ginger.exception.GingerException;
 import com.github.kongchen.ginger.samplecontroller.SampleRequest;
 import com.github.kongchen.ginger.samplecontroller.SampleResponse;
 import com.github.kongchen.swagger.docgen.mustache.MustacheApi;
 import com.github.kongchen.swagger.docgen.mustache.MustacheDocument;
 import com.github.kongchen.swagger.docgen.mustache.MustacheOperation;
 import com.github.kongchen.swagger.docgen.mustache.MustacheParameter;
 import com.github.kongchen.swagger.docgen.mustache.MustacheParameterSet;
 import com.github.kongchen.swagger.docgen.mustache.MustacheSample;
 
 /**
  * Created by chekong on 13-6-14.
  */
 public class SequenceContext {
     private static final Logger LOG = Logger.getLogger(SequenceContext.class);
 
     private final boolean withFormatSuffix;
 
     private String swaggerBaseURL;
 
     private HashMap<String, SampleResponse> responseMap = new HashMap<String, SampleResponse>();
 
     private File baseDir;
 
     private Set<String> exampleSet = new HashSet<String>();
 
     private URI baseUrl;
 
     private File sampleFile;
 
     public SequenceContext(InputConfiguration input) throws GingerException {
         swaggerBaseURL = getSwaggerBaseURL(input);
         URIBuilder builder = null;
         builder = new URIBuilder(input.getSwaggerBaseURL());
 
         URI sampleURI = null;
         try {
             sampleURI = builder.removeQuery().setPath("/").build();
         } catch (URISyntaxException e) {
             //ignore
         }
         this.sampleFile = new File(input.getSamplePackage(), GingerConstants.SEQUENCEFILE);
         this.baseDir = input.getSamplePackage();
         this.baseUrl = sampleURI;
         this.withFormatSuffix = input.isWithFormatSuffix();
     }
 
     private String getSwaggerBaseURL(InputConfiguration input) throws GingerException {
         String swaggerURL = input.getSwaggerBaseURL().toASCIIString();
         int idx = swaggerURL.lastIndexOf(GingerConstants.API_DOCS_JSON);
         if (idx < 0) {
             throw new GingerException("Bad swaggerBaseURL:" + swaggerURL);
         }
         String baseURL = swaggerURL.substring(0, idx);
         idx = baseURL.lastIndexOf(input.getApiBasePath());
         if (idx < 0) {
             throw new GingerException("Bad apiBasePath:" + input.getApiBasePath());
         }
         baseURL = baseURL.substring(0, idx);
         return baseURL;
     }
 
     public File getBaseDir() {
         return baseDir;
     }
 
     public Map<String, SampleResponse> getResponseMap() {
         return responseMap;
     }
 
     public void addSample(String varName) {
         exampleSet.add(varName);
     }
 
     public SampleResponse putResponse(String varName, SampleResponse response) {
         return responseMap.put(varName, response);
     }
 
     public SampleResponse getResponse(String varName) {
         return responseMap.get(varName);
     }
 
     public URI getBaseUrl() {
         return baseUrl;
     }
 
     public Set<String> getSamples() {
         return exampleSet;
     }
 
     private boolean queryMatch(List<MustacheParameter> paras, String queryString) {
         if (queryString != null) {
             boolean match = false;
             for (MustacheParameter para : paras) {
                 if (para.isRequired()) {
                     for (String query : queryString.split("&")) {
                         if (query.startsWith(para.getName())) {
                             match = true;
                         }
                     }
                     if (!match) {
                         return false;
                     }
                 }
             }
         }
 
         return true;
     }
 
     private boolean isSamePath(String urlInSwaggerDoc, String pathInRequestFile) {
 
         String urlhostRegex = "^((http|https)://)?(\\w+\\.)*\\w+(:\\d+)?";
         String pathInSwaggerDoc = urlInSwaggerDoc.replaceAll(swaggerBaseURL, "");
         if (!pathInSwaggerDoc.startsWith("/")) {
             pathInSwaggerDoc = "/" + pathInSwaggerDoc;
         }
 
         if (withFormatSuffix) {
            String regex = pathInSwaggerDoc.replaceAll(".\\{format}", "(\\\\.json|\\\\.xml)").replaceAll("\\{\\w+}",
                     ".+");
             return pathInRequestFile.matches(regex);
         } else {
             String regex = pathInSwaggerDoc.replaceAll("\\{\\w+}", ".+");
             return pathInRequestFile.matches(regex);
         }
     }
 
     public File getSampleFile() {
         return sampleFile;
     }
 
     /**
      * @param doc
      * @return populated samples
      */
     public Set<String> populateSamplesToDoc(MustacheDocument doc) {
         Set<String> samples = new HashSet<String>();
 
         Iterator<String> it = getSamples().iterator();
         while (it.hasNext()) {
             String sampleName = it.next();
 
             SampleResponse response = responseMap.get(sampleName);
 
             MustacheOperation op = findoutOperationBySampe(doc, response);
             if (op != null) {
                 LOG.info("Sample[" + sampleName + "] will be populated in api: " + op.getHttpMethod() + " "
                         + op.getNickname());
                 MustacheSample sample = new MustacheSample();
                 sample.setSampleDescription(response.getRequest().getDescription());
                 sample.setSampleRequest(response.getRequest().toString());
                 sample.setSampleResponse(response.toString());
                 if (op.getSamples() == null) {
                     op.setSamples(new LinkedList<MustacheSample>());
                 }
                 op.getSamples().add(sample);
                 samples.add(sampleName);
             }
         }
         return samples;
     }
 
     private MustacheOperation findoutOperationBySampe(MustacheDocument doc, SampleResponse response) {
         SampleRequest request = response.getRequest();
         MustacheOperation operation = null;
         for (MustacheApi api : doc.getApis()) {
             for (MustacheOperation op : api.getOperations()) {
                 if (op.getHttpMethod().equals(request.getMethod())
                         && isSamePath(api.getUrl(), request.getRequest().getURI().getPath())) {
                     MustacheParameterSet requestQuery = op.getRequestQuery();
                     if (requestQuery == null) {
                         operation = op;
                     } else {
                         if (queryMatch(requestQuery.getParas(), request.getRequest().getURI().getQuery())) {
                             return op;
                         }
                     }
                 }
             }
         }
         return operation;
     }
 }
