 package greenhouse.ui.wicket.page;
 
 import greenhouse.execute.Execution;
 import greenhouse.execute.ExecutionKey;
 import greenhouse.execute.ExecutionState;
 import greenhouse.execute.ScenarioExecutor;
 import greenhouse.ui.wicket.WicketUtils;
 
 import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.util.time.Duration;
 import org.springframework.util.StringUtils;
 
 import com.visural.wicket.component.dialog.Dialog;
 
 /**
  * Wraps a VisuralWicket Dialog that automatically refreshes itself to show the
  * process of a scenario exeuction.
  */
 public class OutputDialog extends Panel {
 
     @SpringBean
     private ScenarioExecutor executor;
 
     private final Dialog dialog;
     private final Label output;
     private final Label progress;
 
     private final FilteringModel filteringModel = new FilteringModel();
 
     public OutputDialog(String id) {
         super(id);
         dialog = new Dialog("dialog");
         output = new Label("output", filteringModel);
         add(dialog);
         dialog.add((progress = new Label("progress", "")).setOutputMarkupId(true));
         dialog.add(output.setOutputMarkupId(true));
         dialog.add(new AjaxFallbackLink<Void>("close1") {
             @Override
             public void onClick(AjaxRequestTarget target) {
                 dialog.close(target);
             }
         });
         dialog.add(new AjaxFallbackLink<Void>("close2") {
             @Override
             public void onClick(AjaxRequestTarget target) {
                 dialog.close(target);
             }
         });
     }
 
     public void begin(final ExecutionKey executionKey, AjaxRequestTarget target) {
         progress.setDefaultModelObject("Preparing...");
         final long startTime = System.currentTimeMillis();
         output.add(new AbstractAjaxTimerBehavior(Duration.seconds(1)) {
             @Override
             protected void onTimer(AjaxRequestTarget target) {
                 String outputText = "";
                 long runtime = (System.currentTimeMillis() - startTime) / 1000;
                 String prefix = "Running...";
                 Execution task = executor.getExecution(executionKey);
                 outputText = task.getOutput();
                 if (task.getState() == ExecutionState.COMPLETE) {
                     prefix = "Complete!";
                     stop();
                 }
                 progress.setDefaultModelObject(prefix + " (" + runtime + "s)");
                 output.setDefaultModelObject(outputText);
                 WicketUtils.addComponents(target, output, progress);
             }
         });
         filteringModel.reset();
         WicketUtils.addComponents(target, dialog, output, progress);
         dialog.open(target);
     }
 
     private static class FilteringModel extends Model<String> {
         private String text = "";
 
         public FilteringModel() {
             reset();
         }
 
         @Override
         public void setObject(final String object) {
             String filtered = object;
            String remove = "--- cuke4duke-maven-plugin";
             int index = filtered.lastIndexOf(remove);
             if (index == -1) {
                 text += ".";
             } else {
                filtered = filtered.substring(filtered.indexOf('\n', index));
                 filtered = filtered.replaceAll("\\[INFO\\] ", "");
                 if ("".equals(StringUtils.trimWhitespace(filtered))) {
                     text += ".";
                 } else {
                     text = filtered;
                 }
             }
         }
 
         @Override
         public String getObject() {
             return text;
         }
 
         public void reset() {
             text = "Loading Maven and Cucumber.";
         }
     }
}
