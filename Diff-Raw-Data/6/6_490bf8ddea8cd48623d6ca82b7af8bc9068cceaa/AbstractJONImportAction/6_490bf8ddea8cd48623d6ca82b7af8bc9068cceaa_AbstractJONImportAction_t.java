 package org.jboss.rhq.sync.tool.actions.impl;
 
 import java.util.Map;
 
 import org.jboss.rhq.sync.tool.BaseRemote;
 import org.jboss.rhq.sync.tool.LoginConfiguration;
 import org.jboss.rhq.sync.tool.actions.JONAction;
 import org.jboss.rhq.sync.tool.actions.JonActionResult.JonActionResultType;
 
 
 /**
  *  @author Romain PELISSE - <belaran@redhat.com>
  *
  */
 public abstract class AbstractJONImportAction<T> extends AbstractJONAction implements JONAction {
 
     public static final String IMPORT_FILENAME = "export.filename";
 	public static final String IMPORT_DIRECTORY_PROPERTY = "export.directory";
 
     public AbstractJONImportAction(LoginConfiguration loginConfiguration,BaseRemote baseRemote) {
     	super(loginConfiguration,baseRemote);
     }
 
     public AbstractJONImportAction() {
     	super();
     }
 
     protected abstract T loadFromFile(String filename);
 
     protected abstract JonActionResultType doImport(T valueToImport);
 
 	@Override
	protected JonActionResultType perform(Map<String, String> values) {	    
		return doImport(loadFromFile(values.get(AbstractJONExportAction.WORKING_DIRECTORY_PROPERTY).concat(values.get(IMPORT_FILENAME))));
 	}
 
 }
