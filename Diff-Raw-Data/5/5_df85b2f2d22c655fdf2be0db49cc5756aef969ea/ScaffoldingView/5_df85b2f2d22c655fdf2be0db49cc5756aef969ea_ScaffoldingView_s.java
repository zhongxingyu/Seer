 package com.attask.scaffolding;
 
 import com.attask.templating.*;
 import com.google.common.collect.ImmutableList;
 import hudson.Extension;
 import hudson.XmlFile;
 import hudson.model.*;
 import org.apache.commons.io.FileUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: joeljohnson
  * Date: 3/27/12
  * Time: 9:54 AM
  */
 public class ScaffoldingView extends View {
 	public static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\$([A-Za-z0-9_]+)");
 	public List<String> templates;
 
 	@DataBoundConstructor
 	public ScaffoldingView(String name) {
 		super(name);
 	}
 
 	@Override
 	public Collection<TopLevelItem> getItems() {
 		ImmutableList.Builder<TopLevelItem> resultBuilder = ImmutableList.builder();
 		Hudson hudson = Hudson.getInstance();
 		for (String template : templates) {
 			resultBuilder.add(hudson.getItem(template));
 		}
 		return resultBuilder.build();
 	}
 
 	@Override
 	public boolean contains(TopLevelItem item) {
 		return item != null && templates.contains(item.getName());
 	}
 
 	@Override
 	public void onJobRenamed(Item item, String oldName, String newName) {
 		Collections.replaceAll(templates, oldName, newName);
 	}
 
 	@Override
 	protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
 		String[] templateNames = req.hasParameter("templateNames") ? req.getParameterValues("templateNames") : new String[]{};
 		this.templates = Arrays.asList(templateNames);
 	}
 
 	@Override
 	public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
 		Item item = Hudson.getInstance().doCreateItem(req, rsp);
 		if (item != null) {
 			templates.add(item.getName());
 			owner.save();
 		}
 		return item;
 	}
 
 	public void doStandUpScaffolding(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
 		requirePOST();
 
 		Map<String, List<String>> templateVariableNames = new HashMap<String, List<String>>();
 		Enumeration parameterNames = request.getParameterNames();
 		while(parameterNames.hasMoreElements()) {
 			String parameterName = (String) parameterNames.nextElement();
 			if(parameterName.contains("@@")) {
 				String[] split = parameterName.split("@@", 3);
 				String templateName = split[1];
 				String variableName = split[2];
 				if(!templateVariableNames.containsKey(templateName)) {
 					templateVariableNames.put(templateName, new LinkedList<String>());
 				}
 				templateVariableNames.get(templateName).add(variableName);
 			}
 		}
 		
 		String prefix = request.getParameter("name-prefix");
 		if(prefix == null) {
 			prefix = "";
 		}
 		
 		String suffix = request.getParameter("name-suffix");
 		if(suffix == null || suffix.isEmpty()) {
 			suffix = "Impl";
 		}
 
 		Hudson hudson = Hudson.getInstance();
 		for (String template : templates) {
 			String newName = prefix + template + suffix;
 			TopLevelItem topLevelItem = hudson.createProject(TemplateImplementationProject.DESCRIPTOR, newName);
 			Project project = (Project)topLevelItem;
 			String parameters = generatePropertiesFileForParameters(template, templateVariableNames.get(template), request);
 			//noinspection unchecked
 			project.getBuildWrappersList().add(new ImplementTemplateBuildWrapper(template, parameters));
 			project.save();
 		}
 
 		response.forwardToPreviousPage(request);
 	}
 	
 	public Collection<String> getVariableNamesForTemplate(String templateName) throws IOException {
 		TopLevelItem item = Hudson.getInstance().getItem(templateName);
 		if(!(item instanceof TemplateProject)) {
 			return Collections.emptyList();
 		}
 
		ImmutableList.Builder<String> result = ImmutableList.builder();

 		TemplateProject templateProject = (TemplateProject)item;
 		String config = readConfigFile(templateProject.getConfigFile());
 
 		Matcher matcher = VARIABLE_PATTERN.matcher(config);
 		while(matcher.find()) {
 			String variableName = matcher.group(1);
 			result.add(variableName);
 		}
 
 		return result.build();
 	}
 
 	private String readConfigFile(XmlFile configFile) throws IOException {
 		File file = configFile.getFile();
 		return FileUtils.readFileToString(file);
 	}
 	
 	private String generatePropertiesFileForParameters(String templateName, List<String> variableNames, StaplerRequest request) {
 		StringBuilder sb = new StringBuilder();
 		for (String variableName : variableNames) {
 			String key = "variables@@" + templateName + "@@" + variableName;
 			String variableValue = request.getParameter(key);
 			if(variableValue != null) {
 				sb.append(variableName+"="+variableValue).append("\n");
 			}
 		}
 		return sb.toString();
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends ViewDescriptor {
 		public String getDisplayName() {
 			return "Scaffolding";
 		}
 	}
 }
