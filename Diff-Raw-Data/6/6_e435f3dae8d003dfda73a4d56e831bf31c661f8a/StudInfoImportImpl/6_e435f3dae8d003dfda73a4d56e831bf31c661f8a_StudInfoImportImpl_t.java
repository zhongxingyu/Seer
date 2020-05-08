 package no.uis.service.fsimport.impl;
 
 import java.io.File;
import java.io.FileInputStream;
 import java.io.FileReader;
import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.concurrent.FutureTask;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import no.uis.service.fsimport.StudInfoImport;
 import no.uis.service.studinfo.data.FsStudieinfo;
 import no.usit.fsws.wsdl.studinfo.StudInfoService;
 
 public class StudInfoImportImpl implements StudInfoImport {
 
   private StudInfoService fsServiceStudInfo;
   private URL transformerUrl;
 
   public void setFsServiceStudInfo(StudInfoService fsServiceStudInfo) {
     this.fsServiceStudInfo = fsServiceStudInfo;
   }
 
   public StudInfoService getFsServiceStudInfo() {
     return this.fsServiceStudInfo;
   }
 
   public void setTransformerUrl(URL transformerUrl) {
     this.transformerUrl = transformerUrl;
   }
   
   @Override
 	public FsStudieinfo fetchStudyPrograms(int institution, int year, String semester, boolean includeEP,
 			String language) throws Exception {
 
 		Integer medUPinfo = includeEP ? INTEGER_1 : INTEGER_0;
 		String studieinfoXml = fsServiceStudInfo.getStudieprogramSI(year,
 				semester, medUPinfo, null, institution, INTEGER_MINUS_1, null, null, language);
 
     return unmarshalStudieinfo(studieinfoXml);
 	}
 
   @Override
 	public FsStudieinfo fetchSubjects(int institution, int year, String semester,
 			String language) throws Exception {
 
 	  String studieinfoXml = fsServiceStudInfo.getEmneSI(Integer.valueOf(institution), null, null,
 				INTEGER_MINUS_1, null, null, year, semester, language);
 
     return unmarshalStudieinfo(studieinfoXml);
   }
 
   @Override
   public FsStudieinfo fetchCourses(int institution, int year, String semester, String language) throws Exception {
 
     String studieinfoXml = fsServiceStudInfo.getKursSI(Integer.valueOf(institution), INTEGER_MINUS_1, INTEGER_MINUS_1, INTEGER_MINUS_1, language);
 
     return unmarshalStudieinfo(studieinfoXml);
   }
 
   // see http://jarfiller.com/guide/jaxb/xslt.xhtml
   protected FsStudieinfo unmarshalStudieinfo(String studieinfoXml) throws Exception {
     
     TransformerFactory trFactory = TransformerFactory.newInstance();
     Reader unmarshalSource = null;
     Runnable cleanupTask = null; 
     
     if (transformerUrl != null) {
       Source schemaSource = new StreamSource(transformerUrl.openStream());
       Transformer stylesheet = trFactory.newTransformer(schemaSource);
   
       Source input = new StreamSource(new StringReader(studieinfoXml));
       
       final File resultFile = File.createTempFile("jaxb", ".xml");
       
       cleanupTask = new Runnable() {
         @Override
         public void run() {
           resultFile.delete();
         }
       };
       Result result = new StreamResult(resultFile);
       
       // This doens't work for CData content for some reason
       //JAXBResult result = new JAXBResult(jc);
       //return (FsStudieinfo)result.getResult();
       
       stylesheet.transform(input, result);
      unmarshalSource = new InputStreamReader(new FileInputStream(resultFile), "UTF-8");
     } else {
       unmarshalSource = new StringReader(studieinfoXml);
     }
 
     JAXBContext jc = JAXBContext.newInstance(FsStudieinfo.class);
     FsStudieinfo sinfo = (FsStudieinfo)jc.createUnmarshaller().unmarshal(unmarshalSource);
     
     unmarshalSource.close();
     
     if (cleanupTask != null) {
       cleanupTask.run();
     }
     
     return sinfo;
   }
 }
