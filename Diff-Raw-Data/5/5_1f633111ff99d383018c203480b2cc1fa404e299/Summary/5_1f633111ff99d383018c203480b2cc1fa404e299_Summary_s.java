 package org.jenkinsci.plugins.jobparametersummary;
 
 import hudson.Extension;
 import hudson.matrix.MatrixConfiguration;
 import hudson.model.Action;
 import hudson.model.InvisibleAction;
 import hudson.model.ParameterValue;
 import hudson.model.TransientProjectActionFactory;
 import hudson.model.AbstractProject;
 import hudson.model.BooleanParameterDefinition;
 import hudson.model.BooleanParameterValue;
 import hudson.model.ParameterDefinition;
 import hudson.model.ParametersDefinitionProperty;
 import hudson.model.StringParameterDefinition;
 import hudson.model.StringParameterValue;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 public class Summary extends InvisibleAction {
 
 	final private AbstractProject<?, ?> project;
 
 	public Summary(@SuppressWarnings("rawtypes") AbstractProject project) {
 
 	    this.project = project;
 	}
 
 	public List<ParameterDefinition> getParameters() {
 
 	    return definitionProperty(project).getParameterDefinitions();
 	}
 
 	@Override
 	public String toString() {
 
 	    return "Job parameter summary for " + project.toString();
 	}
 
 	private static ParametersDefinitionProperty definitionProperty(
 	        final AbstractProject<?, ?> project
 	) {
 
 	    return project.getProperty(ParametersDefinitionProperty.class);
 	}
 
 	/**
 	 * Get default value for {@link ParameterDefinition} that has any and it can be displayed
 	 */
 	public String getDefault(final ParameterDefinition d) {
 
 	    final ParameterValue v = d.getDefaultParameterValue();
 
 	    if (d instanceof BooleanParameterDefinition) return new Boolean(((BooleanParameterValue) v).value).toString();
 	    if (d instanceof StringParameterDefinition) return "\"" + ((StringParameterValue) v).value + "\"";
 
 	    return null;
 	}
 
     @Extension
     public static class SummaryFactory extends TransientProjectActionFactory {
 
         /**
          * For matrix projects parameter actions are attached to the MatrixProject
          */
         @Override
         public Collection<? extends Action> createFor(
                 @SuppressWarnings("rawtypes") AbstractProject target
         ) {
 
             if (target instanceof MatrixConfiguration) {
 
                 target = ((MatrixConfiguration) target).getParent();
             }
 
            if (isParameterized(target)) return Collections.emptyList();
 
             return Arrays.asList(new Summary(target));
         }
 
         private boolean isParameterized(final AbstractProject<?, ?> project) {
 
            return definitionProperty(project) == null;
         }
     }
 }
