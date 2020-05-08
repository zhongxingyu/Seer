 package org.sonatype.maven.polyglot.clojure;
 
 import clojure.lang.Var;
 import clojure.lang.RT;
 
 import com.google.common.base.Join;
 import org.apache.maven.model.*;
 import org.apache.maven.model.io.ModelWriter;
 import org.codehaus.plexus.component.annotations.Component;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 import org.sonatype.maven.polyglot.io.ModelWriterSupport;
 
 import java.io.*;
 import java.text.MessageFormat;
 import java.util.Map;
 
 @Component(role = ModelWriter.class, hint = "clojure")
 public class ClojureModelWriter extends ModelWriterSupport {
 
     private boolean isExtendedDependency(Dependency dependency) {
         return dependency.getScope() != null || dependency.getClassifier() != null ||
                 !dependency.getExclusions().isEmpty();
     }
 
     public void buildDependencyString(ClojurePrintWriter out, Dependency dependency) {
 
         if (isExtendedDependency(dependency)) {
 
             String dep = MessageFormat.format("\"{0}:{1}",
                     dependency.getGroupId(),
                     dependency.getArtifactId());
 
             if (dependency.getVersion() != null) {
                 dep += ":" + dependency.getVersion();
             }
             dep += "\"";
 
             out.printAtNewIndent("[" + dep + " {");
             out.printField("classifier", dependency.getClassifier());
             out.printField("scope", dependency.getScope());
 
             if (!dependency.getExclusions().isEmpty()) {
 
                 out.printAtNewIndent(":exclusions [");
                 for (Exclusion exclusion : dependency.getExclusions()) {
 
                     out.printLnAtCurrent(
                             "\"" + exclusion.getGroupId() + ":" +
                                     exclusion.getArtifactId() + "\"");
                 }
                 out.append("]");
                 out.popIndent();
             }
 
             out.append("}]");
             out.popIndent();
 
         } else {
 
             String dep = MessageFormat.format("[\"{0}:{1}",
                     dependency.getGroupId(),
                     dependency.getArtifactId());
 
             if (dependency.getVersion() != null) {
                 dep += ":" + dependency.getVersion();
             }
 
             dep += "\"]";
 
             out.printLnAtCurrent(dep);
 
         }
 
     }
 
     private void writeDom(ClojurePrintWriter out, Xpp3Dom dom) {
 
         if (dom.getChildCount() == 0) {
             out.printLnAtCurrent("\"" + dom.getName() + "\" \"" + dom.getValue() + "\"");
         } else {
             out.printAtNewIndent("\"" + dom.getChild(0).getName() + "\" [");
 
             boolean pad = false;
             for (Xpp3Dom xpp3Dom : dom.getChildren()) {
                 if (pad) {
                     out.printAtCurrent(" ");
                 } else {
                     pad = true;
                 }
                 out.printAtCurrent("\"" + xpp3Dom.getValue() + "\"");
             }
 
             out.printLnAtCurrent("]");
             out.popIndent();
         }
     }
 
     public void buildPluginString(ClojurePrintWriter out, Plugin plugin) {
 
         String ref = MessageFormat.format("\"{0}:{1}",
                 plugin.getGroupId(),
                 plugin.getArtifactId());
 
         if (plugin.getVersion() != null) {
             ref += ":" + plugin.getVersion();
         }
 
         ref += "\"";
 
        out.printAtNewIndent("[" + ref);
 
         if (!plugin.getExecutions().isEmpty() || plugin.getConfiguration() != null) {
 
             out.printAtNewIndent(" {");
 
             if (plugin.getConfiguration() != null) {
                 appendConfiguration(out, plugin.getConfiguration());
             }
 
             if (!plugin.getExecutions().isEmpty()) {
 
                 out.printAtNewIndent(":executions [");
 
                 for (PluginExecution execution : plugin.getExecutions()) {
                     out.printAtNewIndent("{");
                     out.printField("id", execution.getId());
                     out.printField("phase", execution.getPhase());
 
                     if (execution.getConfiguration() != null) {
                         appendConfiguration(out, execution.getConfiguration());
                     }
 
                     if (execution.getGoals() != null && !execution.getGoals().isEmpty()) {
                         out.printLnAtCurrent(":goals [\"" + Join.join("\" \"", execution.getGoals()) + "\"]");
                     }
 
                     out.append("}");
                     out.popIndent();
                 }
                 out.append("]");
                 out.popIndent();
             }
             out.append("}");
             out.popIndent();
         }
         out.append("]");
         out.popIndent();
 
     }
 
     private void appendConfiguration(ClojurePrintWriter out, Object con) {
         Xpp3Dom configuration = (Xpp3Dom) con;
 
         if (configuration.getChildCount() != 0) {
             out.printAtNewIndent(":configuration {");
 
             for (Xpp3Dom xpp3Dom : configuration.getChildren()) {
 
                 writeDom(out, xpp3Dom);
             }
 
             out.append("}");
             out.popIndent();
         }
 
     }
 
     public void write(Writer writer, Map<String, Object> stringObjectMap, Model model) throws IOException {
 
         ClojurePrintWriter out = new ClojurePrintWriter(writer);
 
         out.printLnAtCurrent("(defproject main \"" + model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion() + "\"");
         out.pushIndent(4);
         out.printField("model-version", model.getModelVersion());
 
         final Parent parent = model.getParent();
         if (parent != null) {
             out.printField("parent", parent.getGroupId() + ":" + parent.getArtifactId() + ":" + parent.getVersion());
         }
 
         out
                 .printField("name", model.getName())
                 .printField("description", model.getDescription())
                 .printField("packaging", model.getPackaging())
                 .printField("url", model.getUrl())
                 .printField("inceptionYear", model.getInceptionYear());
 
         if (model.getProperties() != null && !model.getProperties().isEmpty()) {
 
             out.printAtNewIndent(":properties {");
             for (Map.Entry<Object, Object> entry : model.getProperties().entrySet()) {
                 out.printLnAtCurrent("\"" + entry.getKey() + "\" \"" + entry.getValue() + "\"");
             }
 
             out.print("}");
             out.popIndent();
 
         }
 
         if (!model.getDependencies().isEmpty()) {
 
             out.printAtNewIndent(":dependencies [");
 
             for (Dependency dependency : model.getDependencies()) {
                 buildDependencyString(out, dependency);
             }
 
             out.print("]");
             out.popIndent();
         }
 
         if (model.getBuild() != null && !model.getBuild().getPlugins().isEmpty()) {
 
             out.printAtNewIndent(":plugins [");
 
             for (Plugin plugin : model.getBuild().getPlugins()) {
                 buildPluginString(out, plugin);
             }
 
             out.print("]");
             out.popIndent();
         }
 
         out.print(")\n");
         out.flush();
 
     }
 }
