 package org.synyx.minos.skillz.web;
 
 import static org.springframework.web.bind.annotation.RequestMethod.*;
 
 import java.util.Locale;
 
 import org.joda.time.DateMidnight;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.DataBinder;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.support.SessionStatus;
 import org.synyx.minos.core.Core;
 import org.synyx.minos.core.domain.User;
 import org.synyx.minos.core.web.CurrentUser;
 import org.synyx.minos.core.web.DateTimeEditor;
 import org.synyx.minos.core.web.Message;
 import org.synyx.minos.core.web.UrlUtils;
 import org.synyx.minos.skillz.domain.Category;
 import org.synyx.minos.skillz.domain.Level;
 import org.synyx.minos.skillz.domain.MatrixTemplate;
 import org.synyx.minos.skillz.domain.Project;
 import org.synyx.minos.skillz.domain.Skill;
 import org.synyx.minos.skillz.service.SkillManagement;
 import org.synyx.minos.skillz.web.validation.CategoryValidator;
 import org.synyx.minos.skillz.web.validation.LevelValidator;
 import org.synyx.minos.skillz.web.validation.MatrixTemplateValidator;
 import org.synyx.minos.skillz.web.validation.ProjectValidator;
 import org.synyx.minos.umt.service.UserManagement;
 
 
 /**
  * Controller to expose {@link SkillManagement} functionality to the web.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  * @author Michael Herbold - herbold@synyx.de
  * @author Markus Knittig - knittig@synyx.de
  */
 @Controller
 public class SkillzController {
 
     private final SkillManagement skillManagement;
     private final UserManagement userManagement;
     private final ProjectValidator projectValidator;
     private final CategoryValidator categoryValidator;
     private final MatrixTemplateValidator matrixTemplateValidator;
     private final LevelValidator levelValidator;
 
 
     /**
      * Creates a new {@link SkillzController} instance.
      */
     @Autowired
     public SkillzController(SkillManagement skillManagement,
             UserManagement userManagement, ProjectValidator projectValidator,
             CategoryValidator categoryValidator,
             MatrixTemplateValidator matrixTemplateValidator,
             LevelValidator levelValidator) {
 
         this.skillManagement = skillManagement;
         this.userManagement = userManagement;
         this.projectValidator = projectValidator;
         this.categoryValidator = categoryValidator;
         this.matrixTemplateValidator = matrixTemplateValidator;
         this.levelValidator = levelValidator;
     }
 
 
     @InitBinder
     public void initBinder(DataBinder binder, Locale locale) {
 
         binder.registerCustomEditor(DateMidnight.class, new DateTimeEditor(
                 locale, "MM/yyyy").forDateMidnight());
     }
 
 
     @RequestMapping("/skillz")
     public String indexRedirect() {
 
         return UrlUtils.redirect("/skillz/");
     }
 
 
     @RequestMapping("/skillz/")
     public String index(Model model) {
 
         model.addAttribute("levels", skillManagement.getLevels());
         model.addAttribute("levelsSize", skillManagement.getLevels().size());
         model.addAttribute("projects", skillManagement.getPublicProjects());
         model.addAttribute("categories", skillManagement.getCategories());
         model.addAttribute("templates", skillManagement.getTemplates());
 
         return "skillz/skillz";
     }
 
 
     // Manage categories
 
     @RequestMapping(value = { "/skillz/categories/{id}",
             "/skillz/categories/form{id}" }, method = GET)
     public String category(@PathVariable("id") Category category, Model model) {
 
         model.addAttribute("category", null == category ? BeanUtils
                 .instantiateClass(Category.class) : category);
         model.addAttribute("categories", skillManagement.getCategories());
 
         return "skillz/category";
     }
 
 
     @RequestMapping(value = "/skillz/categories", method = POST)
     public String saveCategory(@ModelAttribute("category") Category category,
             Errors errors, Model model) {
 
         categoryValidator.validate(category, errors);
 
         if (errors.hasErrors()) {
             return "skillz/category";
         }
 
         skillManagement.save(category);
 
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.category.save.success", category.getName()));
 
         return UrlUtils.redirect("../skillz");
     }
 
 
     @RequestMapping(value = "/skillz/categories/{id}", method = DELETE)
     public String deleteCategory(@PathVariable("id") Category category,
             Model model) {
 
         if (null == category) {
             model.addAttribute(Core.MESSAGE, Message
                     .error("skillz.category.delete.error"));
             return UrlUtils.redirect("../../");
         }
 
         skillManagement.delete(category);
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.category.delete.success", category.getName()));
         return UrlUtils.redirect("../");
     }
 
 
     // Manage Skillz
 
     /**
      * Moves the skill to the given {@link Category}.
      * 
      * @param skill the {@link Skill} to be moved
      * @param category the targed {@link Category} to move the {@link Skill} to
      * @param model
      * @return
      */
     @RequestMapping(value = "/skillz/skill", method = POST)
     public String moveSkill(@RequestParam Skill skill,
             @RequestParam Category category, Model model) {
 
         Category currentCategory = skill.getCategory();
 
         skillManagement.moveSkill(skill, category);
 
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.skill.save.success", skill.getName()));
 
         return UrlUtils.redirect("categories/" + currentCategory.getId());
     }
 
 
     @RequestMapping(value = { "/skillz", "/skillz/" }, method = POST)
     public String saveSkill(@ModelAttribute Skill skill, Model model) {
 
        Skill savedSkill = skillManagement.save(skill);
 
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.skill.save.success", skill.getName()));
 
        return UrlUtils.redirect("categories/"
                + savedSkill.getCategory().getId());
     }
 
 
     @RequestMapping(value = "/skillz/skill/{id}", method = DELETE)
     public String deleteSkill(@PathVariable("id") Skill skill, Model model) {
 
         skillManagement.delete(skill);
 
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.skill.delete.success", skill.getName()));
 
         return UrlUtils
                 .redirect("../categories/" + skill.getCategory().getId());
     }
 
 
     // Manage templates
 
     /**
      * Shows the form for a {@link MatrixTemplate}.
      * 
      * @param template
      * @param id
      * @param model
      * @return
      */
     @RequestMapping(value = { "/skillz/templates/{id}",
             "/skillz/templates/form{id}" }, method = GET)
     public String template(@PathVariable("id") MatrixTemplate template,
             Model model) {
 
         model.addAttribute("template", null == template ? BeanUtils
                 .instantiateClass(MatrixTemplate.class) : template);
         model.addAttribute("categories", skillManagement.getCategories());
 
         return "skillz/template";
     }
 
 
     /**
      * Saves a {@link MatrixTemplate}.
      * 
      * @param template
      * @param model
      * @return
      */
     @RequestMapping(value = "/skillz/templates", method = POST)
     public String saveTemplate(
             @ModelAttribute("template") MatrixTemplate template, Errors errors,
             Model model) {
 
         matrixTemplateValidator.validate(template, errors);
 
         if (errors.hasErrors()) {
             model.addAttribute("categories", skillManagement.getCategories());
             return "skillz/template";
         }
 
         MatrixTemplate result = skillManagement.save(template);
 
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.template.save.success", result.getName()));
 
         return UrlUtils.redirect("../skillz#tabs-2");
     }
 
 
     @RequestMapping(value = "/skillz/templates/{id}", method = DELETE)
     public String deleteTemplate(@PathVariable("id") MatrixTemplate template,
             Model model) {
 
         skillManagement.delete(template);
 
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.template.delete.success", template.getName()));
 
         return UrlUtils.redirect("../#tabs-2");
     }
 
 
     // Manage projects
 
     @RequestMapping(value = { "/skillz/projects/{usernameOrFormString:[a-zA-Z_]\\w*}" }, method = GET)
     public String project(
             @PathVariable("usernameOrFormString") String usernameOrFormString,
             Model model) {
 
         if (usernameOrFormString.equals("form")) {
             return createOrEditProject(null, model);
         }
 
         return showUserProjects(usernameOrFormString, model);
     }
 
 
     public String showUserProjects(@PathVariable("username") String username,
             Model model) {
 
         User user = userManagement.getUser(username);
         model.addAttribute("projects", skillManagement
                 .getPrivateProjectsFor(user));
         model.addAttribute("projectsUrl", "/skillz/projects/" + username);
         model.addAttribute("username", username);
 
         return "skillz/projects";
     }
 
 
     @RequestMapping(value = { "/skillz/projects/{id:\\d+}" }, method = GET)
     public String createOrEditProject(@PathVariable("id") Project project,
             Model model) {
 
         model.addAttribute("project", null == project ? BeanUtils
                 .instantiateClass(Project.class) : project);
         model.addAttribute("projects", skillManagement.getPublicProjects());
         model.addAttribute("skills", skillManagement.getSkills());
 
         return "skillz/project";
     }
 
 
     /**
      * Shows the form for a private project.
      * 
      * @param project
      * @param model
      * @param user
      * @return
      */
     @RequestMapping(value = { "/skillz/projects/{username:[a-zA-Z_]\\w*}/form" }, method = GET)
     public String privateProjectForm(
             @RequestParam(value = "id", required = false) Project project,
             Model model, @CurrentUser User user) {
 
         if (null == project) {
 
             project = BeanUtils.instantiateClass(Project.class);
 
         } else {
 
             if (!project.belongsTo(user)) {
                 model.addAttribute(Core.MESSAGE, "skillz.project.id.invalid");
                 return UrlUtils.redirect("/skillz/projects/private");
             }
         }
 
         model.addAttribute("project", project);
         model.addAttribute("owner", user);
         model.addAttribute("skills", skillManagement.getSkills());
 
         return "skillz/project";
     }
 
 
     /**
      * Saves a new public project.
      * 
      * @param project
      * @param errors
      * @param conversation
      * @param model
      * @param user
      * @return
      */
     @RequestMapping(value = "/skillz/projects", method = POST)
     public String saveNewPublicProject(
             @ModelAttribute("project") Project project, Errors errors,
             SessionStatus conversation, Model model, @CurrentUser User user) {
 
         return savePublicProject(project, "../skillz#tabs-3", errors,
                 conversation, model, user);
     }
 
 
     /**
      * Saves a existing project (aka update).
      * 
      * @param project
      * @param errors
      * @param conversation
      * @param model
      * @param user
      * @return
      */
     @RequestMapping(value = "/skillz/projects/{id:\\d+}", method = PUT)
     public String saveExistingPublicProject(
             @ModelAttribute("project") Project project, Errors errors,
             SessionStatus conversation, Model model, @CurrentUser User user) {
 
         return savePublicProject(project, "../../skillz#tabs-3", errors,
                 conversation, model, user);
     }
 
 
     private String savePublicProject(Project project, String redirectUrl,
             Errors errors, SessionStatus conversation, Model model, User user) {
 
         // validate project
         projectValidator.validate(project, errors);
 
         if (errors.hasErrors()) {
             return "skillz/project";
         }
 
         // validation was successful
         saveProject(project, model, conversation);
 
         if (project.belongsTo(user)) {
             return UrlUtils.redirect(user.getUsername());
         }
 
         return UrlUtils.redirect(redirectUrl);
     }
 
 
     /**
      * Saves a private project.
      * 
      * @param project
      * @param errors
      * @param model
      * @param conversation
      * @param user
      * @return
      */
     @RequestMapping(value = { "/skillz/projects/{username:[a-zA-Z_]\\w*}" }, method = POST)
     public String savePrivateProject(
             @ModelAttribute("project") Project project, Errors errors,
             Model model, SessionStatus conversation, @CurrentUser User user) {
 
         // validate project
         projectValidator.validate(project, errors);
 
         if (errors.hasErrors()) {
 
             model.addAttribute("owner", user);
 
             return "skillz/project";
         }
 
         saveProject(project, model, conversation);
 
         return UrlUtils.redirect("../projects/"
                 + project.getOwner().getUsername());
     }
 
 
     /**
      * Invokes the service layer to save the given project.
      * 
      * @param project
      * @param model
      * @param conversation
      */
     private void saveProject(Project project, Model model,
             SessionStatus conversation) {
 
         project = skillManagement.save(project);
         conversation.setComplete();
 
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.project.save.success", project.getName()));
     }
 
 
     /**
      * Deletes a project.
      * 
      * @param project
      * @param model
      * @param user
      * @return
      */
     @RequestMapping(value = "/skillz/projects/{id:\\d+}", method = DELETE)
     public String deleteProject(@PathVariable("id") Project project,
             Model model, @CurrentUser User user) {
 
         skillManagement.delete(project);
 
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.project.delete.success", project.getName()));
 
         if (project.belongsTo(user)) {
             return UrlUtils.redirect("./" + user.getUsername());
         }
 
         return UrlUtils.redirect("../#tabs-3");
     }
 
 
     // Manage levels
 
     /**
      * Shows the form for a {@link Level}.
      */
     @RequestMapping(value = { "/skillz/levels/{id}", "/skillz/levels/form{id}" }, method = GET)
     public String level(@PathVariable("id") Level level, Model model) {
 
         model.addAttribute("level", null == level ? BeanUtils
                 .instantiateClass(Level.class) : level);
         model.addAttribute("levels", skillManagement.getLevels());
 
         return "skillz/level";
     }
 
 
     @RequestMapping(value = "/skillz/levels", method = POST)
     public String saveLevel(@ModelAttribute("level") Level level,
             Errors errors, Model model) {
 
         levelValidator.validate(level, errors);
 
         if (errors.hasErrors()) {
             return "skillz/level";
         }
 
         Level result = skillManagement.save(level);
 
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.level.save.success", result.getName()));
 
         return UrlUtils.redirect("../skillz#tabs-4");
     }
 
 
     @RequestMapping(value = "/skillz/levels/{id}", method = DELETE)
     public String deleteLevel(@PathVariable("id") Level level, Model model) {
 
         skillManagement.delete(level);
 
         model.addAttribute(Core.MESSAGE, Message.success(
                 "skillz.level.delete.success", level.getName()));
 
         return UrlUtils.redirect("../#tabs-4");
     }
 
 
     /**
      * Swaps a given {@link Level} with the next upper level.
      * 
      * @param level
      * @param model
      * @return
      */
     @RequestMapping(value = "/skillz/levels/{id}/up", method = POST)
     public String moveLevelUp(@PathVariable("id") Level level, Model model) {
 
         skillManagement.moveLevelUp(level);
 
         return UrlUtils.redirect("../../../skillz#tabs-4");
     }
 
 
     /**
      * Swaps a given {@link Level} it with the next lower level.
      * 
      * @param level
      * @param model
      * @return
      */
     @RequestMapping(value = "/skillz/levels/{id}/down", method = POST)
     public String moveLevelDown(@PathVariable("id") Level level, Model model) {
 
         skillManagement.moveLevelDown(level);
 
         return UrlUtils.redirect("../../../skillz#tabs-4");
     }
 
 }
