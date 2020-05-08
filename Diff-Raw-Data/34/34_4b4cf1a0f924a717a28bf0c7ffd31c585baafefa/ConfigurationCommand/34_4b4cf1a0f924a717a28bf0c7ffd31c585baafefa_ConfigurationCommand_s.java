 package com.github.enr.clap.impl;
 
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import com.github.enr.clap.api.Command;
 import com.github.enr.clap.api.CommandResult;
 import com.github.enr.clap.api.Configuration;
 import com.github.enr.clap.api.Constants;
 import com.github.enr.clap.api.Reporter;
 import com.google.common.base.Strings;
 
 public class ConfigurationCommand implements Command {
 
     private Configuration configuration;
 	private Reporter reporter;
 
 	private ConfigurationCommandArgs args = new ConfigurationCommandArgs();
 	
 	@Inject
 	public ConfigurationCommand(Configuration configuration, Reporter reporter) {
 		this.reporter = reporter;
 		this.configuration = configuration;
 	}
 
 	@Override
 	public CommandResult execute() {
 		if (args.files) {
		    executeFiles();
		}
        if (args.list) {
            executeList();
         }
        if (!Strings.isNullOrEmpty(args.get)) {
            executeGet();
        }
		return new CommandResult();
 	}
 	
    private void executeFiles() {
         reporter.out("Configuration files:");
         for (Map.Entry<String, Boolean> entry : configuration.getPaths().entrySet()) {
             reporter.out("- %s (%s)", entry.getKey(), entry.getValue());
         }
     }
     
	private void executeList() {
         for (Map.Entry<String, Object> entry : configuration.getAllProperties().entrySet()) {
             reporter.out("%s=%s", entry.getKey(), String.valueOf(entry.getValue()));
         }
     }
     
    private void executeGet() {
         Object val = configuration.get(args.get);
         if (val != null) {
             reporter.out(String.valueOf(val));
         }
     }
 
     @Override
 	public String getId() {
 		return Constants.CONFIG_COMMAND_ID;
 	}
 
 	@Override
 	public Object getParametersContainer() {
 		return args;
 	}
 
 }
