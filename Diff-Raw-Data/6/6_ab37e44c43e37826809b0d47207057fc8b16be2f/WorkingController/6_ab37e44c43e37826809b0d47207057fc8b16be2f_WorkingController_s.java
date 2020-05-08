 package ru.kpfu.quantum.spring.controller;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.reflect.TypeToken;
 import com.google.gson.stream.JsonWriter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import ru.kpfu.quantum.service.integration.IntegrationService;
 import ru.kpfu.quantum.spring.entities.Function;
 import ru.kpfu.quantum.spring.entities.Project;
 import ru.kpfu.quantum.spring.entities.ProjectGroup;
 import ru.kpfu.quantum.spring.entities.User;
 import ru.kpfu.quantum.spring.repository.FunctionRepository;
 import ru.kpfu.quantum.spring.repository.ProjectGroupRepository;
 import ru.kpfu.quantum.spring.repository.ProjectRepository;
 import ru.kpfu.quantum.spring.repository.UserRepository;
 import ru.kpfu.quantum.spring.utils.UserUtils;
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 
 @Controller
 public class WorkingController {
 
 
     @Autowired
     private ProjectGroupRepository projectGroupRepository;
 
     @Autowired
     private ProjectRepository projectRepository;
 
     @Autowired
     private UserRepository userRepository;
 
     @Autowired
     private FunctionRepository functionRepository;
 
 
     @Autowired
     private IntegrationService integrationService;
 
     Gson gson = new Gson();
 
     private Random random = new Random();
 
 
     enum Filters {All, Working, Archive}
 
     @RequestMapping("/")
     public String working(HttpServletRequest request) {
         User currentUser = UserUtils.getUserFromSession(request);
         List<ProjectGroup> groups = projectGroupRepository.findAllGroupsOwnByUser(currentUser);
         request.setAttribute("groups", groups);
         request.setAttribute("filters", Filters.values());
         return "working/working";
     }
 
     @RequestMapping(value = "/working/project-list", method = RequestMethod.GET)
     public String getProjectDataList(HttpServletRequest request,
                                      @RequestParam Long groupId,
                                      @RequestParam String filter) {
         List<Project> projects;
         if(filter.equals("All")){
             projects = projectGroupRepository.findOneFetchChildren(groupId).getProjects();
         }
         else{
             if(filter.equals("Working")){
                 projects = projectRepository.findAllByArchive(groupId, false);
             }
             else{
                 projects = projectRepository.findAllByArchive(groupId, true);
             }
         }
         request.setAttribute("records", projects);
         return "working/data-list";
     }
 
     @RequestMapping(value = "/working/group-list", method = RequestMethod.GET)
     public String getGroupDataList(HttpServletRequest request) {
         User currentUser = UserUtils.getUserFromSession(request);
         List<ProjectGroup> groups = projectGroupRepository.findAllGroupsOwnByUser(currentUser);
         request.setAttribute("records", groups);
         return "working/data-list";
     }
 
     @ResponseBody
     @RequestMapping(value = "/working/create-group", method = RequestMethod.POST)
     public String createGroup(HttpServletRequest request,
                               @RequestParam String groupName) {
         User currentUser = UserUtils.getUserFromSession(request);
         ProjectGroup group = new ProjectGroup(groupName, currentUser);
         projectGroupRepository.save(group);
         return String.valueOf(group.getId());
     }
 
     @ResponseBody
     @RequestMapping(value = "/working/create-project", method = RequestMethod.POST)
     public String createProject(@RequestParam Long groupId,
                                 @RequestParam String projectName) {
         Project project = new Project(projectName, new ArrayList<Function>());
         Project saved = projectRepository.save(project);
         ProjectGroup group = projectGroupRepository.findOneFetchChildren(groupId);
         group.getProjects().add(saved);
         projectGroupRepository.save(group);
         return String.valueOf(project.getId());
     }
 
 
     @RequestMapping("/working/get-project")
     public String getCode(HttpServletRequest request,
                           @RequestParam Long projectId){
         Project project = projectRepository.findOneFetchFunctions(projectId);
         request.setAttribute("project", project);
         final String imageName = project.getId() + ".png";
         request.setAttribute("imageName", imageName);
         return "working/project";
     }
 
     @ResponseBody
     @RequestMapping("/working/get-code")
     public String getCode(@RequestParam Long projectId){
 //        Project project = projectRepository.findOne(projectId);
 //        return project.getCode();
         final List<String> codes = new ArrayList<>();
         Project project = projectRepository.findOneFetchFunctions(projectId);
         for(Function function : project.getFunctions()) {
             codes.add(function.getCode());
         }
         return gson.toJson(codes, new TypeToken<List<String>>() {}.getType());
     }
 
 
     @ResponseBody
     @RequestMapping("/working/get-circuit")
     public String getCircuit(@RequestParam String code) throws IOException{
         final String circuit = integrationService.codeToCircuit(code);
         return circuit;
     }
 
     @ResponseBody
     @RequestMapping(value = "/working/save", method = RequestMethod.POST)
     public String save(@RequestParam Long projectId,
                           @RequestParam String code,
                           @RequestParam String typeOfEditor) throws IOException
     {
         final Project project = projectRepository.findOneFetchFunctions(projectId);
         project.setCalculated(false);
         final Project saved = projectRepository.save(project);
         functionRepository.delete(saved.getFunctions());
         project.getFunctions().clear();
         //
         final List<String> codes;
         if(!typeOfEditor.equals("text")) {
             final String codesString = integrationService.circuitToCode(code);
             codes = gson.fromJson(codesString, new TypeToken<List<String>>() {}.getType());
         } else {
             codes = gson.fromJson(code, new TypeToken<List<String>>() {}.getType());
         }
         List<Function> functions = new ArrayList<>();
         for(String codeEntry : codes) {
             Function function = new Function("", codeEntry, null, saved);
             functions.add(function);
         }
         functionRepository.save(functions);
         return "";
     }
 
     @RequestMapping(value = "/working/calculate", method = RequestMethod.POST)
     public String calculate(HttpServletRequest request,
                             @RequestParam Long projectId,
                             @RequestParam String code,
                             @RequestParam String typeOfEditor) throws IOException {
         Project project = projectRepository.findOneFetchFunctions(projectId);
         project.setCalculated(true);
         projectRepository.save(project);
         functionRepository.delete(project.getFunctions());
         project.getFunctions().clear();
         //
         final List<Function> functions = new ArrayList<>();
         final List<String> codes;
         if(!typeOfEditor.equals("text")) {
             final String codesString = integrationService.circuitToCode(code);
            codes = gson.fromJson(codesString, new TypeToken<List<String>>() {}.getType());
         } else {
             codes = gson.fromJson(code, new TypeToken<List<String>>() {}.getType());
         }
         for(String codeEntry : codes) {
             byte[] image = integrationService.codeToFile(codeEntry);
             final String imageName = saveImage(project, image);
             Function function = new Function("", codeEntry, imageName, project);
             functions.add(function);
         }
         functionRepository.save(functions);
         request.setAttribute("functions", functions);
         return "working/result";
     }
 
     private String saveImage(Project parent, byte[] image) throws IOException {
         final String imageName = parent.getId() + random.nextInt(100000) + ".png";
         final String imagePath = System.getProperty("jboss.server.data.dir") + "/quantum/media/" + imageName;
         File file = new File(imagePath);
         try(FileOutputStream fileOutputStream = new FileOutputStream(file, false)) {
             fileOutputStream.write(image);
             fileOutputStream.flush();
         }
         return imageName;
     }
 
     @ResponseBody
     @RequestMapping(value = "/working/archive", method = RequestMethod.POST)
     public String archive(@RequestParam Long projectId)
     {
         Project project = projectRepository.findOne(projectId);
         project.setArchive(true);
         projectRepository.save(project);
         return "";
     }
 }
