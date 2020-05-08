 package org.metam.ui;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.zip.GZIPOutputStream;
 
 import org.jdom2.Element;
 import org.jdom2.output.XMLOutputter;
 import org.metam.Utterance;
 
public class SaveFile extends XMLOutputter {
 
     private Element save;
 
     public SaveFile(Display parent) {
         this.save = new Element("SaveFile");
         Element mmos = new Element("MMO");
         Element utterances = new Element("Utterances");
         for (Utterance u : parent.mmos) {
             utterances.addContent(u.toXML());
         }
         mmos.addContent(utterances);
         save.addContent(mmos);
 
         Element text = new Element("Text");
         text.setText(parent.textPane.getText());
         save.addContent(text);
 
         save.addContent(parent.vocabulary.toXML());
     }
 
     public void output(OutputStream out) throws IOException {
         GZIPOutputStream gos = new GZIPOutputStream(out);
        super.output(this.save, gos);
         gos.close();
     }
 }
