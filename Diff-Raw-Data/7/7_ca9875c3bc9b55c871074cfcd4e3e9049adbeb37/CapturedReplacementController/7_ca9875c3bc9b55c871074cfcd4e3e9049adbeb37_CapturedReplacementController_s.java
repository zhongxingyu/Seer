 package org.jetbrains.plugins.xml.searchandreplace.ui.controller.replace;
 
 import com.intellij.lang.Language;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.RangeMarker;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.xml.XmlElement;
 import org.jetbrains.plugins.xml.searchandreplace.replace.ReplacementProvider;
 import org.jetbrains.plugins.xml.searchandreplace.replace.Utils;
 import org.jetbrains.plugins.xml.searchandreplace.search.Node;
 
 import javax.swing.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class CapturedReplacementController extends ReplacementController {
 
   private List<CaptureEntry> entries = new ArrayList<CaptureEntry>();
 
   public void setEditor(Editor editor) {
     this.editor = editor;
   }
 
   public Editor getEditor() {
     return editor;
   }
 
   private Editor editor;
 
   private Language language;
   private Project project;
 
   public CapturedReplacementController(Language language, Project project) {
     this.language = language;
     this.project = project;
   }
 
   public void addCaptureEntry(Capture c, RangeMarker r) {
     entries.add(new CaptureEntry(r, c));
   }
 
   @Override
   public JPanel getView() {
     return null;
   }
 
   @Override
   public ReplacementProvider getReplacementProvider() {
     return new ReplacementProvider() {
       @Override
       public XmlElement getReplacementFor(XmlElement element, Map<Node, XmlElement> match) {
         StringBuilder result = new StringBuilder(editor.getDocument().getText());
         for (CaptureEntry entry : entries) {
           Capture capture = entry.capture;
           Node n = capture.getNode();
           String captureResolution = capture.value(match.get(n));
          result.replace(entry.range.getStartOffset(), entry.range.getEndOffset(), captureResolution);
         }
         return Utils.createXmlElement(language, project, result.toString());
       }
     };
   }
 }
