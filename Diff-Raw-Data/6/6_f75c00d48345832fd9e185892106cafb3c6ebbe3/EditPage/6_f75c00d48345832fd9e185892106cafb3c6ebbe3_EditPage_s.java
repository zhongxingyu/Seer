 package ua.dp.primat.curriculum.view;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.wicket.Application;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.markup.html.form.upload.*;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.util.file.Files;
 import org.apache.wicket.util.file.Folder;
 import org.apache.wicket.util.lang.*;
 import org.apache.wicket.validation.validator.RangeValidator;
 import ua.dp.primat.domain.StudentGroup;
 import ua.dp.primat.repositories.StudentGroupRepository;
 import ua.dp.primat.curriculum.planparser.CurriculumParser;
 import ua.dp.primat.curriculum.planparser.CurriculumXLSRow;
 import ua.dp.primat.domain.workload.Workload;
 import ua.dp.primat.services.WorkloadService;
 import ua.dp.primat.utils.view.GroupsLoadableDetachableModel;
 
 /**
  * Wicket page for portlet's EDIT mode. It is used for managing curriculums.
  * @author fdevelop
  */
 public class EditPage extends WebPage {
 
     /**
      * Constructor, that adds needed wicket components.
      */
     public EditPage() {
         super();
 
         final Form form = new FileUploadForm("formUploadXLS");
         add(form);
 
         final Form remForm = new RemoveForm("formRemove");
         add(remForm);
 
         //add feed back panel for system information output
         final FeedbackPanel feedback = new FeedbackPanel("feedback");
         add(feedback);
     }
 
     /**
      * Returns the place for storing curriculums.
      * @return wicket's Folder
      */
     private Folder getUploadFolder() {
         return ((WicketApplication) Application.get()).getUploadFolder();
     }
     private StudentGroup parseGroup;
     @SpringBean
     private StudentGroupRepository studentGroupRepository;
     @SpringBean
     private WorkloadService workloadService;
     private static final long serialVersionUID = 2L;
     private static final int MIN_YEAR = 1910;
     private static final int MAX_YEAR = 2110;
     private static final int MAX_GROUP_NUMBER = 20;
     private static final int MAX_FILESIZE = 10; //in megabytes
 
     /**
      * Form for removing.
      */
     private class RemoveForm extends Form<Void> {
 
         private List<StudentGroup> groups;
         private StudentGroup chosenGroup;
 
         /**
          * Constructor of form.
          * @param name
          */
         public RemoveForm(String name) {
             super(name);
 
             groups = studentGroupRepository.getGroups();
             if (!groups.isEmpty()) {
                 chosenGroup = groups.get(0);
             }
             final DropDownChoice<StudentGroup> groupChoise = new DropDownChoice<StudentGroup>("group",
                     new PropertyModel<StudentGroup>(this, "chosenGroup"),
                     new GroupsLoadableDetachableModel(groups));
             add(groupChoise);
         }
 
         /**
          * Overriden method onSubmit for the form.
          * It takes input fields arguments and removes group, if it exists.
          */
         @Override
         protected void onSubmit() {
             if (chosenGroup == null) {
                 this.error(String.format("No group selected"));
             } else {
                 studentGroupRepository.remove(chosenGroup);
                 this.info(String.format("Curriculum has been removed"));
             }
         }
         private static final long serialVersionUID = 2L;
     }
 
     /**
      * Form for uploads.
      */
     private class FileUploadForm extends Form<Void> {
 
         /**
          * Constructor of the form.
          * @param name
          */
         public FileUploadForm(String name) {
             super(name);
             // set this form to multipart mode (allways needed for uploads!)
             setMultiPart(true);
             // Add file input field and other options fields
             fileUploadField = new FileUploadField("fileInput");
             add(fileUploadField);
 
             textParseSheet = new TextField<Integer>("parseSheet", new Model<Integer>(), Integer.class);
             textParseSheet.setRequired(true);
             textParseSheet.add(new RangeValidator<Integer>(0, Integer.MAX_VALUE));
             add(textParseSheet);
 
             textParseStart = new TextField<Integer>("parseStart", new Model<Integer>(), Integer.class);
             textParseStart.setRequired(true);
             textParseStart.add(new RangeValidator<Integer>(0, Integer.MAX_VALUE));
             add(textParseStart);
 
             textParseEnd = new TextField<Integer>("parseEnd", new Model<Integer>(), Integer.class);
             textParseEnd.setRequired(true);
             textParseEnd.add(new RangeValidator<Integer>(0, Integer.MAX_VALUE));
             add(textParseEnd);
 
             textParseSemester = new TextField<Integer>("parseSemester", new Model<Integer>(), Integer.class);
             textParseSemester.setRequired(true);
             textParseSemester.add(new RangeValidator<Integer>(0, Integer.MAX_VALUE));
             add(textParseSemester);
 
             textGroupSpec = new TextField<String>("groupSpec", new Model<String>(), String.class);
             textGroupSpec.setRequired(true);
             add(textGroupSpec);
 
             textGroupYear = new TextField<Integer>("groupYear", new Model<Integer>(), Integer.class);
             textGroupYear.setRequired(true);
             textGroupYear.add(new RangeValidator<Integer>(MIN_YEAR, MAX_YEAR));
             add(textGroupYear);
 
             textGroupNumber = new TextField<Integer>("groupNumber", new Model<Integer>(), Integer.class);
             textGroupNumber.setRequired(true);
             textGroupNumber.add(new RangeValidator<Integer>(1, MAX_GROUP_NUMBER));
             add(textGroupNumber);
 
             // Set maximum size to 10M
             setMaxSize(Bytes.megabytes(MAX_FILESIZE));
         }
 
         /**
          * Overriden method onSubmit for the FileUpload form.
          * It takes input fields arguments, uploads file, than parses it, stores
          * to the database and output the result.
          */
         @Override
         protected void onSubmit() {
             final Integer parseSheet = textParseSheet.getConvertedInput();
             final Integer parseStart = textParseStart.getConvertedInput();
             final Integer parseEnd = textParseEnd.getConvertedInput();
             final Integer parseSemesters = textParseSemester.getConvertedInput();
             final String groupSpec = textGroupSpec.getConvertedInput();
             final Integer groupYear = textGroupYear.getConvertedInput();
             final Integer groupNumber = textGroupNumber.getConvertedInput();
             final FileUpload upload = fileUploadField.getFileUpload();
 
            parseGroup = new StudentGroup(groupSpec, Long.valueOf(groupNumber), Long.valueOf(groupYear));
 
             //parser launch
             try {
                 //upload and create file on server
                 final String uploadedFileName = makeUploadedFile(upload);
 
                 //create and run parser
                 final CurriculumParser cParser = new CurriculumParser(parseGroup,
                         parseSheet, parseStart, parseEnd, parseSemesters,
                         uploadedFileName);
                 final List<CurriculumXLSRow> listParsed = cParser.parse();
 
                 //commit parsed objects
                 final List<Workload> workloads = new ArrayList<Workload>();
                 for (int i = 0; i < listParsed.size(); i++) {
                     workloads.addAll(listParsed.get(i).getWorkloadList());
                 }
                 workloadService.storeWorkloads(workloads);
 
                 this.info(String.format("Curriculum in '%s' was successfully"
                         + " parsed\n into database (%d items).",
                         uploadedFileName, listParsed.size()));
 
             } catch (IOException ioe) {
                 this.error(ioe);
             }// catch (Exception e) {
             //    this.info("Curriculum has been parsed, but Throwable was catched...");
             //}
         }
 
         /**
          * Upload the file on server into the getUploadFolder() returned path.
          * @param upload
          * @return The absolute path to the uploaded file on server.
          */
         private String makeUploadedFile(FileUpload upload) throws IOException {
             if (upload == null) {
                 return null;
             }
 
             // Create a new file
             final File newFile = new File(getUploadFolder(), upload.getClientFileName());
 
             // Check new file, delete if it allready existed
             checkFileExists(newFile);
 
             // Save to new file
             newFile.createNewFile();
             upload.writeTo(newFile);
             //EditPage.this.info("saved file: " + upload.getClientFileName() + "\nOriginal saved in: " + newFile.getAbsolutePath());
             return newFile.getAbsolutePath();
         }
 
         /**
          * Check whether the file already exists, and if so, try to delete it.
          * @param newFile  the file to check
          */
         private void checkFileExists(File newFile) {
             if ((newFile.exists()) && (!Files.remove(newFile))) {
                 throw new IllegalStateException("Unable to overwrite " + newFile.getAbsolutePath());
             }
         }
         private FileUploadField fileUploadField;
         private TextField<Integer> textParseSheet;
         private TextField<Integer> textParseStart;
         private TextField<Integer> textParseEnd;
         private TextField<Integer> textParseSemester;
         private TextField<String> textGroupSpec;
         private TextField<Integer> textGroupYear;
         private TextField<Integer> textGroupNumber;
         private static final long serialVersionUID = 2L;
     }
 }
