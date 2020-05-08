 package controllers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 
 import models.App;
 import models.Hash;
 import models.PasswordPolicy;
 import models.Role;
 import models.User;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.data.validation.Required;
 import play.data.validation.Valid;
 import services.AccountingLogger;
 import services.AppNotificationService;
 import services.RMQService;
 import controllers.dto.AppUserConfiguration;
 import controllers.dto.AppUserConfiguration.UserConfiguration;
 import controllers.dto.BaseAppConfig;
 
 public class Application extends SecureController {
 
     @Inject static RMQService rmqService; 
     @Inject static AppNotificationService appNotificationService;
     @Inject static AccountingLogger accountingLogger;
     
     /** sirve página principal */
     public static void index() {
     	Boolean passwordNearExpiration = PasswordPolicy.usersWithPasswordsNearExpiration(15).contains(connectedUser());
         List<App> appsToConfigure = App.toConfigureBy(connectedUser());
         render(appsToConfigure, passwordNearExpiration);
     }
 
     public static void list() {
         List<App> apps = App.all().fetch();
         render(apps);
     }
     
     /** sirve pantalla de alta de aplicaciones */
     @Check(Role.SIMS_CREATE_APP_ROLE)
     public static void create() {
         List<User> users = User.all().fetch();
         List<Hash> hashTypes = Arrays.asList(Hash.values());
         render(users, hashTypes);
     }
     
     /** pantalla de detalle de aplicación */
     public static void detail(final String appName) {
         User user = connectedUser();
         App app = App.forName(appName);
         if (app == null) {
             notFound();
             return;
         }
         if (!app.owner.username.equals(user.username)) {
             unauthorized();
             return;
         }
         
         List<User> users = User.all().fetch();
         Integer roleCount = app.roles.size();
         List<Role> defaultRoles = Role.defaultForApp(app);
         render(app, users, roleCount, defaultRoles);
     }
     
     public static void configureApp(@Required final String appName,
                                     @Valid final BaseAppConfig baseConfig,
                                     @Valid final AppUserConfiguration userConfigurations) {
         User user = connectedUser();
         App app = App.forName(appName);
         
         if (app == null) {
             notFound();
         }
         if (!app.owner.username.equals(user.username)) {
             unauthorized();
             return;
         }
         
         if (app.configured == false) {
             processBaseConfiguration(app, baseConfig);
         } else {
             processUserConfiguration(app, userConfigurations);
         }
         
     }
     
     @Check(Role.SIMS_CREATE_APP_ROLE)
     public static void postApp(@Required final String name,
                                @Required final String ownerName,
                                @Required final Hash hash) {
         boolean hasErrors = false;
         if (validation.hasErrors()) {
             Set<String> missingFields = validation.errorsMap().keySet();
             flash.error("Falta completar los campos: %s", missingFields);
             hasErrors = true;
         }
         
         if (hash == null) {
             flash.error("Debe elegir un algoritmo de hash para almacenar las claves");
             hasErrors = true;
         }
         if (App.count("name = ?", name) > 0) {
             flash.error("Ya existe una aplicación con ese nombre");
             hasErrors = true;
         }
 
         if(!App.NAME_PATTERN.matcher(name).matches()) {
             flash.error("\"%s\" no es un nombre de aplicación inválido", name);
             hasErrors = true;
         }
         
         if (StringUtils.isEmpty(ownerName)) {
             flash.error("No se especificó usuario a cargo.");
             hasErrors = true;            
         }
         
         User owner = User.forUsername(ownerName);
         
         try {
             rmqService.setupApplication(name);
         } catch (Exception e) {
             flash.error("Error desconocido");
             hasErrors = true;
         }
         
         // si hay errores vuelvo a mostrar el formulario.
         if (hasErrors) {
             create();
             return;
         }
         
         App app = new App();
         app.owner = owner;
         app.name = name;
         app.hashType = hash;
         app.roles = new LinkedList<Role>();
         app.save();
         accountingLogger.logAppCreated(connectedUser(), app);
         
         flash.success(String.format("Aplicación %s creada exitosamente", name));
         index();
     }
    
     /** Realiza la configuración inicial de una aplicación. */
     private static void processBaseConfiguration(App app, BaseAppConfig baseConfig) {
         String appName = app.name;
         if (!baseConfig.validate(flash)) {
             detail(appName);
         } else {
             try {
                 Set<String> roles = baseConfig.getRoles();
                 Set<String> defaultRoles = baseConfig.getDefaultRoles();
                 
                 rmqService.changeUserPassword(appName, baseConfig.rmqPass);
                 for (String roleName : roles) {
                     Role role = new Role();
                     role.name = roleName;
                     role.app = app;
                     role.selectedByDefault = defaultRoles.contains(roleName);
                     app.roles.add(role);
                 }
                 app.configured = true;
                 app.save();
                 flash.success("Aplicación " + appName + " configurada correctamente");
                 index();
             } catch (Exception e) {
                 flash.error("Error grave al guardar configuración de la aplicación.");
                 detail(appName);
             }
         }
     }
     
     /** Actualiza la configuración de usuarios de una aplicación. */
     private static void processUserConfiguration(App app, AppUserConfiguration userConfig) {
         List<UserConfiguration> configurations = userConfig.userConfigurations;
         
         List<User> newUsers = new LinkedList<User>();
         List<User> modifiedUsers = new LinkedList<User>();
         List<User> removedUsers = new LinkedList<User>();
         
         // proceso para cada usuario.
         for (UserConfiguration userConfiguration : configurations) {
             User user = User.forUsername(userConfiguration.username);
             
             if (user == null) {
                 // TODO: handle invalid user.
             }
             
             Set<Role> roles = new LinkedHashSet<Role>();
             for (String roleName : userConfiguration.roles) {
                 Role role = Role.find(app, roleName);
                 if (role == null) {
                     // TODO: handle invalid role.
                 }
                 roles.add(role);
             }
             
             if (userConfiguration.enabled) {
                 if (user.apps.contains(app)) {
                     // me fijo si hay que modificar roles
                     boolean changed = false;
                     for (Role role : roles) {
                         if (!user.roles.contains(role)) {
                             changed = true;
                             user.roles.add(role);
                             accountingLogger.logRoleChanged(connectedUser(), user, role);
                         }
                     }
                     
                     // para evitar modificar al mismo momento que se itera.
                     List<Role> rolesForApp = user.getRoles(app);
                     List<Role> toRemove = new LinkedList<Role>();
                     for (Role role : rolesForApp) {
                         if (!roles.contains(role)) {
                             changed = true;
                             toRemove.add(role);
                         }
                     }
                     user.roles.removeAll(toRemove);
                    for (Role role : toRemove) {
						accountingLogger.logRoleChanged(connectedUser(), user, role);
					}
                     
                     if (changed) {
                         modifiedUsers.add(user);
                     }
                 } else {
                     // hay que agregarlo.
                     user.apps.add(app);
                     for (Role role : roles) {
                         user.roles.add(role);
                     }
                     newUsers.add(user);
                 }
             } else {
                 if (user.apps.contains(app)) {
                     // hay que sacarlo
                     user.apps.remove(app);
                     for (Role role : app.roles) {
                         user.roles.remove(role);
                     }
                     removedUsers.add(user);
                 }
             }
         }
         
         for (User user : newUsers) {
             user.save();
             appNotificationService.notifyNewUser(user, app);
             accountingLogger.logAppAccessChanged(connectedUser(), user, app);
         }
         for (User user : modifiedUsers) {
             user.save();
             appNotificationService.notifyRolesChanged(user, app);
         }
         for (User user : removedUsers) {
             user.save();
             appNotificationService.notifyUserRemove(user, app);
             accountingLogger.logAppAccessChanged(connectedUser(), user, app);
         }
         
         response.status = 204;
     }
     
 }
