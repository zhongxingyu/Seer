 package org.synyx.hades.roo.addon;
 
 import java.util.logging.Logger;
 
 import org.springframework.roo.model.JavaType;
 import org.springframework.roo.shell.CliAvailabilityIndicator;
 import org.springframework.roo.shell.CliCommand;
 import org.springframework.roo.shell.CliOption;
 import org.springframework.roo.shell.CommandMarker;
 import org.springframework.roo.shell.converters.StaticFieldConverter;
 import org.springframework.roo.support.lifecycle.ScopeDevelopmentShell;
 import org.springframework.roo.support.util.Assert;
 
 
 /**
 * Sample of a command class. The command class is registered by the Roo shell
 * following an automatic classpath scan. You can provide simple user
 * presentation-related logic in this class. You can return any objects from
 * each method, or use the logger directly if you'd like to emit messages of
 * different severity (and therefore different colours on non-Windows systems).
  */
 @ScopeDevelopmentShell
 public class HadesCommands implements CommandMarker {
 
     private static Logger logger =
             Logger.getLogger(HadesCommands.class.getName());
 
     private HadesOperations operations;
     private HadesInstallationOperations installationOperations;
 
 
     public HadesCommands(StaticFieldConverter staticFieldConverter,
             HadesOperations operations,
             HadesInstallationOperations installationOperations) {
 
         Assert.notNull(staticFieldConverter, "Static field converter required");
         Assert.notNull(operations, "Operations object required");
 
         staticFieldConverter.add(PropertyName.class);
 
         this.operations = operations;
         this.installationOperations = installationOperations;
 
         logger.warning("Loaded " + HadesCommands.class.getName()
                 + "; try the 'welcome' commands");
     }
 
 
     @CliCommand(value = "hades dao", help = "Creates a Hades DAO interface")
     public void createDaoInterface(
             @CliOption(key = "entity", mandatory = true, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The entity you want to create the DAO for.") JavaType typeName) {
 
         operations.createDaoClass(typeName);
     }
 
 
     @CliAvailabilityIndicator("hades dao")
     public boolean isCreateDaoInterfaceAvailable() {
 
         return installationOperations.isInstalled();
     }
 
 
     @CliCommand(value = "hades install", help = "Installs Hades for the project")
     public void installHades() {
 
         installationOperations.installHades();
     }
 
 
     @CliAvailabilityIndicator("hades install")
     public boolean canInstallHades() {
 
         return installationOperations.canBeInstalled();
     }
 }
