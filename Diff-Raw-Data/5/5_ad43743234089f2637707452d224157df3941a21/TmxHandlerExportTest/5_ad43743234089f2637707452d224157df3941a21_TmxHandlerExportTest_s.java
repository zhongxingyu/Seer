 package net.sf.okapi.tm.pensieve.tmx;
 
 import java.io.StringWriter;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.filters.tmx.TmxFilter;
 import net.sf.okapi.tm.pensieve.seeker.PensieveSeeker;
 import net.sf.okapi.tm.pensieve.seeker.ITmSeeker;
 import net.sf.okapi.tm.pensieve.writer.PensieveWriter;
 import net.sf.okapi.tm.pensieve.writer.ITmWriter;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import net.sf.okapi.common.filterwriter.TMXWriter;
 import net.sf.okapi.common.LocaleId;
 
 public class TmxHandlerExportTest {
 
 	LocaleId locEN = LocaleId.fromString("EN");
 	LocaleId locIT = LocaleId.fromString("IT");
 	
     @Test
     public void exportTmx_sample_metadata() throws Exception {
         Directory ramDir = new RAMDirectory();
         ITmWriter tmWriter = new PensieveWriter(ramDir, true);
         OkapiTmxImporter tmxImporter = new OkapiTmxImporter(locEN, new TmxFilter());
         OkapiTmxExporter tmxExporter = new OkapiTmxExporter();
         tmxImporter.importTmx(this.getClass().getResource("/sample_tmx.xml").toURI(), locIT, tmWriter);
         tmWriter.close();
         ITmSeeker seeker = new PensieveSeeker(ramDir);
         StringWriter sWriter = new StringWriter();
         tmxExporter.exportTmx(locEN, locIT, seeker, new TMXWriter(new XMLWriter(sWriter)));
 
         String expectedTmx = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                 "<tmx version=\"1.4\">" +
                 "<header creationtool=\"pensieve\" creationtoolversion=\"0.0.1\" " +
                 "segtype=\"sentence\" o-tmf=\"pensieve\" adminlang=\"en\" " +
                 "srclang=\"en\" datatype=\"unknown\"></header>" +
                 "<body>" +
                "<tu tuid=\"hello123\">" +
                "<prop type=\"Txt::GroupName\">ImAGroupie</prop>" +
                 "<prop type=\"Txt::FileName\">GeorgeInTheJungle.hdf</prop>" +
                 "<tuv xml:lang=\"en\"><seg>hello</seg></tuv>" +
                 "<tuv xml:lang=\"it\"><seg>ciao</seg></tuv></tu>" + 
                 "<tu tuid=\"world\">" +
                 "<tuv xml:lang=\"en\"><seg>world</seg></tuv>" +
                 "<tuv xml:lang=\"it\"><seg>mondo</seg></tuv>" + 
                 "</tu></body></tmx>";
         assertEquals("tmx content", expectedTmx.replaceAll("[\\n\\r]+", ""), sWriter.toString().replaceAll("[\\n\\r]+", ""));
     }
 }
