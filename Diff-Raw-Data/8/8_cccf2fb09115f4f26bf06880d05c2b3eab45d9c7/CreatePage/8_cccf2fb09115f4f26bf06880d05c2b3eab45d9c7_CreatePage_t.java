 package greenhouse.ui.wicket.page.create;
 
 import greenhouse.index.StepMethod;
 import greenhouse.ui.wicket.WicketUtils;
 import greenhouse.ui.wicket.link.ExecuteGherkinLink;
 import greenhouse.ui.wicket.page.BaseProjectPage;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
 import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
 import org.apache.wicket.extensions.ajax.markup.html.autocomplete.DefaultCssAutoCompleteTextField;
 import org.apache.wicket.markup.head.CssHeaderItem;
 import org.apache.wicket.markup.head.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.request.resource.CssResourceReference;
 import org.apache.wicket.util.string.Strings;
 import org.apache.wicket.util.visit.IVisit;
 import org.apache.wicket.util.visit.IVisitor;
 import org.springframework.util.StringUtils;
 import org.wicketstuff.annotation.mount.MountPath;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 
 @MountPath("/projects/${project}/create")
 public class CreatePage extends BaseProjectPage {
 
     /**
      * Splits a Gherkin step into a list of the predicate part and the remainder
      * part.
      * 
      * Input: "given I do x" Result: 0 -> "Given ", 1 -> "I do x"
      * 
      * Input: "I do x" Result: 0 -> "", 1 -> "I do x"
      * 
      * @param step A gherkin step
      * @return A two-element List of (predicate, remainder)
      */
     private static ImmutableList<String> splitStep(String step) {
         for (String arg : ImmutableList.of("given ", "when ", "then ", "and ", "but ")) {
             if (step.toLowerCase(Locale.ENGLISH).startsWith(arg)) {
                 int index = step.indexOf(' ') + 1;
                 return ImmutableList.of(Strings.capitalize(step.substring(0, index).toLowerCase(Locale.ENGLISH)), step.substring(index));
             }
         }
         return ImmutableList.of("", step);
     }
 
     public static class AppendingGherkinModel extends Model<String> {
         private final List<String> steps = Lists.newArrayList();
 
         public AppendingGherkinModel(String value) {
             super.setObject(value);
         }
 
         @Override
         public String getObject() {
             return Joiner.on('\n').join(steps);
         }
 
         @Override
         public void setObject(String newSteps) {
             steps.clear();
             for (String step : Splitter.on('\n').split(StringUtils.trimWhitespace(newSteps))) {
                 append(step);
             }
         }
 
         public void append(String step) {
             ImmutableList<String> split = splitStep(step);
             String prep = split.get(0);
             String remainder = split.get(1);
             if ("".equals(prep)) {
                 steps.add((steps.isEmpty() ? "Given " : "And ") + remainder);
             } else {
                 steps.add(Strings.capitalize(prep) + remainder);
             }
         }
 
         public Iterator<String> steps() {
             return steps.iterator();
         }
 
     }
 
     @Override
     public void renderHead(IHeaderResponse response) {
         // TODO: verify
         response.render(CssHeaderItem.forReference(new CssResourceReference(DefaultCssAutoCompleteTextField.class, "DefaultCssAutoCompleteTextField.css")));
     }
 
     public CreatePage(PageParameters params) {
         super(params);
 
         final AppendingGherkinModel gherkinModel = new AppendingGherkinModel("");
         IModel<String> previewModel = new AbstractReadOnlyModel<String>() {
             @Override
             public String getObject() {
                 String formatted = Joiner.on("\n\t\t").join(gherkinModel.steps());
                 return "Feature: Custom feature\n\n\tScenario: Custom scenario\n\t\t" + formatted;
             }
         };
         final Label preview = new Label("preview", previewModel);
         final TextArea<String> scenario = new TextArea<String>("scenario", gherkinModel);
         scenario.add(new AjaxFormComponentUpdatingBehavior("onblur") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 WicketUtils.addComponents(target, scenario, preview);
             }
         });
         add(scenario.setOutputMarkupId(true));
 
         add(new ExecuteGherkinLink("execute", project().getKey(), previewModel));
 
         add(preview.setOutputMarkupId(true));
 
         Form<Void> form = new Form<Void>("form");
         add(form);
 
         final WebMarkupContainer inputs = new WebMarkupContainer("inputs");
         final WebMarkupContainer parts = new WebMarkupContainer("parts");
         form.add(inputs.add(parts.add(new WebMarkupContainer("part"))).setVisible(false));
 
         final Model<String> stepModel = Model.of("");
 
         AutoCompleteSettings settings = new AutoCompleteSettings();
         settings.setPreselect(true);
         settings.setThrottleDelay(100);
         settings.setShowListOnFocusGain(true);
         settings.setAdjustInputWidth(true);
         final AutoCompleteTextField<String> stepField = new AutoCompleteTextField<String>("step", stepModel, String.class, settings) {
 
             @Override
             protected Iterator<String> getChoices(String input) {
                 if ("".equals(input)) {
                     return ImmutableList.<String> of().iterator();
                 }
                 ImmutableList<String> split = splitStep(input);
                 String prep = split.get(0);
                 String remainder = split.get(1);
 
                 List<String> tokens = ImmutableList.copyOf(Splitter.on(' ').split(remainder));
 
                 ImmutableSet<StepMethod> steps = index().steps();
                 ArrayList<String> choices = new ArrayList<String>();
                 for (StepMethod step : steps) {
                     boolean all = true;
                     String regex = step.getRegex();
                     for (String token : tokens) {
                         if (!regex.contains(token)) {
                             all = false;
                             break;
                         }
                     }
                     if (all) {
                         // Trim ^regex$ to regex
                         String trimmed = regex;
                         if (trimmed.charAt(0) == '^') {
                             trimmed = trimmed.substring(1);
                         }
                         if (trimmed.charAt(trimmed.length() - 1) == '$') {
                             trimmed = trimmed.substring(0, trimmed.length() - 1);
                         }
 
                         // Replace escaped braces \( and \) with symbolic placeholders
                         ImmutableList<String> types = step.getTypes();
                         trimmed = trimmed.replaceAll("\\\\\\(", "\\{\\{\\{");
                         trimmed = trimmed.replaceAll("\\\\\\)", "\\}\\}\\}");
 
                         int i = 0;
                         while (trimmed.contains("(")) {
                             int indexOf = trimmed.indexOf('(');
                             String newTrimmed = trimmed.substring(0, indexOf);
                             String type = types.get(i);
                             newTrimmed += "[[" + type.substring(type.lastIndexOf('.') + 1) + "]]";
                             trimmed = newTrimmed + trimmed.substring(trimmed.indexOf(')') + 1);
                             i++;
                         }
 
                         trimmed = trimmed.replaceAll("\\{\\{\\{", "(");
                         trimmed = trimmed.replaceAll("\\}\\}\\}", ")");
 
                         choices.add(prep + trimmed);
                     }
                 }
                 Collections.sort(choices);
                 return choices.iterator();
             }
         };
         form.add(stepField.setMarkupId("step").setOutputMarkupId(true));
 
         final AjaxButton substitute = new AjaxButton("substitute", form) {
             @SuppressWarnings("rawtypes")
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 final List<String> values = Lists.newArrayList();
                 form.visitChildren(FormComponent.class, new IVisitor<FormComponent, Void>() {
                     @SuppressWarnings("unchecked")
                     @Override
                     public void component(FormComponent component, IVisit<Void> visit) {
                         if ((component instanceof TextField) || (component instanceof DropDownChoice)) {
                             values.add(((FormComponent<String>) component).getModelObject());
                         }
                     }
                 });
                 List<Token> tokens = tokenize(stepModel.getObject());
                 int i = 0;
                 String newStep = "";
                 for (Token token : tokens) {
                     newStep += token.isClass ? values.get(i++) : token.text;
                 }
 
                 gherkinModel.append(newStep);
                 stepModel.setObject("");
 
                 setVisible(false);
                 inputs.setVisible(false);
                 form.get("add").setVisible(true);
                 WicketUtils.addComponents(target, scenario, preview, form);
             }
         };
         inputs.add(substitute.setVisible(false).setOutputMarkupPlaceholderTag(true));
 
         form.add(new AjaxButton("add", form) {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 String text = stepModel.getObject();
                 List<Token> tokens = tokenize(text);
                 if (tokens.size() > 1) {
                     inputs.replace(new ListView<Token>("parts", tokens) {
                         @Override
                         protected void populateItem(ListItem<Token> item) {
                             Token token = item.getModelObject();
                             Fragment part;
                             if (token.isClass) {
                                 String type = token.text;
                                 ImmutableMap<String, ImmutableList<String>> examples = index().examples();
                                 List<String> values = null;
                                 for (String clazz : examples.keySet()) {
                                     if (type.equals(clazz.substring(clazz.lastIndexOf('.') + 1))) {
                                         values = examples.get(clazz);
                                         break;
                                     }
                                 }
                                 if (values == null) {
                                    part = new Fragment("part", "text", inputs);
                                     part.add(new Label("addOn", type));
                                     part.add(new TextField<String>("input", Model.of("")).setRequired(true));
                                 } else {
                                    part = new Fragment("part", "select", inputs);
                                     part.add(new DropDownChoice<String>("input", Model.of(""), values));
                                 }
                             } else {
                                part = new Fragment("part", "label", inputs);
                                 part.add(new Label("text", token.text));
                             }
                             item.add(part);
                         }
                     });
                     setVisible(false);
                     inputs.setVisible(true);
                     substitute.setVisible(true);
                     target.add(form);
                 } else {
                     text = text.replaceAll("\\\\\\(", "(");
                     text = text.replaceAll("\\\\\\)", ")");
                     gherkinModel.append(text);
                     WicketUtils.addComponents(target, scenario, preview, form);
                 }
             }
         });
     }
 
     private static List<Token> tokenize(String text) {
         List<Token> tokens = Lists.newArrayList();
         while (!"".equals(text)) {
             int start = text.indexOf("[[");
             if (start == -1) {
                 tokens.add(new Token(text, false));
                 text = "";
             } else {
                 String prefix = text.substring(0, start);
                 if (!"".equals(prefix)) {
                     tokens.add(new Token(prefix, false));
                 }
 
                 int endIndex = text.indexOf("]]");
                 String clazz = text.substring(start + 2, endIndex);
                 tokens.add(new Token(clazz, true));
                 text = text.substring(endIndex + 2);
             }
         }
         return tokens;
     }
 
     private static class Token implements Serializable {
         public String text;
         public boolean isClass;
 
         public Token(String text, boolean isClass) {
             this.text = text;
             this.isClass = isClass;
         }
 
     }
 
 }
