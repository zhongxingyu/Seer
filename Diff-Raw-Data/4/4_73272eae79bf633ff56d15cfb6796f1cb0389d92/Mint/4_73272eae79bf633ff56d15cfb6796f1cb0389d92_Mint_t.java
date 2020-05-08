 package gutenberg.workers;
 
 import gutenberg.blocs.AssignmentType;
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.PageType;
 import gutenberg.blocs.QuizType;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.util.HashMap;
 
 public class Mint implements ITagLib {
     
     public Mint(Config config) throws Exception {
         sharedPath = config.getPath(Resource.shared);
         mintPath = config.getPath(Resource.mint);
         vaultPath = config.getPath(Resource.vault);
         atm = ATM.instance(config);
     }
 
     /**
      * Creates PDFs and preview jpegs for a quiz. Steps are: 0. Set up
      * directories 1. Write latex 2. Link Makefiles 3. Make (the PDFs) 4.
      * Populate and return manifest
      * 
      * @param quiz
      * @return manifest containing generated files
      * @throws Exception
      * 
      */
     public ManifestType generate(QuizType quiz) throws Exception {
 
         String quizId = quiz.getQuiz().getId();
         String quizKey = quiz.getQuiz().getValue();
         
         Path quizDir = mintPath.resolve(quizDirName).resolve(quizKey);
         Path staging = quizDir.resolve(stagingDirName);
         Path preview = quizDir.resolve(previewDirName);
         
         if (!Files.exists(quizDir)) {
             Files.createDirectory(quizDir);
             Files.createDirectory(staging);
             Files.createDirectory(preview);
         }
 
         Path blueprintTex = staging.resolve(blueprintFile);
         DocumentWriter blueprintDoc = new DocumentWriter(blueprintTex);
         PageType[] pages = quiz.getPage();
         for (PageType page : pages) {
             
             EntryType[] questions = page.getQuestion();
             if (questions == null) //2nd page of multi-part
                 continue;
             
             for (EntryType question : questions) {
                 blueprintDoc.rollDice(0);
                 copyQuestion(staging, blueprintDoc, question.getId());
             }
            blueprintDoc.newPage();            
         }
         blueprintDoc.close();
         
         Path answerkeyTex = staging.resolve(String.format(nameFormat,
             "answer", "key", quizId, "tex"));
         DocumentWriter answerkeyDoc = new DocumentWriter(answerkeyTex);        
         answerkeyDoc.writePreamble(quiz.getQuiz().getName());
         answerkeyDoc.printAuthor(quiz.getTeacher().getName());
         answerkeyDoc.beginQuiz();
         answerkeyDoc.printAnswers();        
         answerkeyDoc.writeTemplate(blueprintTex);
         answerkeyDoc.endQuiz();
         answerkeyDoc.close();
         
         Path toMakefile = staging.relativize(sharedPath).resolve(makefilesDir).
             resolve(quizMakefile);
         if (!Files.exists(staging.resolve(makefile)))
             Files.createSymbolicLink(staging.resolve(makefile), toMakefile);
 
         if (make(staging, quizDir, preview) != 0) {
             throw new Exception("Problemo! Non-zero return code from make");
         }
         Files.delete(answerkeyTex);
         
         //non-critical activity
         Path rubricTex = staging.resolve(String.format(nameFormat,
             "rubric", "key", quizId, "tex"));
         DocumentWriter rubricDoc = new DocumentWriter(rubricTex);        
         rubricDoc.writePreamble(quiz.getQuiz().getName());
         rubricDoc.printAuthor(quiz.getTeacher().getName());
         rubricDoc.beginQuiz();
         rubricDoc.printRubric();
         rubricDoc.printAnswers();        
         rubricDoc.writeTemplate(blueprintTex);
         rubricDoc.endQuiz();
         rubricDoc.close();
         try {
             make(staging, quizDir, null);
         } catch (Exception e) {}
         Files.delete(rubricTex);
         
         ManifestType manifest = new ManifestType();
         manifest.setRoot(mintPath.relativize(quizDir).toString());
         
         return prepareManifest(quizDir, quizDir, preview);
     }
 
     /**
      * Create an assignment (test paper). Steps: 0. Set up directories 1. Copy
      * blueprint from answer-key.tex 2. Write composite and per student latex
      * and QRCode key files 3. Make (the PDFs) 4. Populate and return manifest
      * 
      * @param assignment
      * @return manifest
      * @throws Exception
      */
     public ManifestType generate(AssignmentType assignment) throws Exception {
 
         String quizId = assignment.getQuiz().getId();
         String quizKey = assignment.getQuiz().getValue();
         String testpaperId = assignment.getInstance().getId();
         String assignmentKey = assignment.getInstance().getValue();
         
         Path quizDir = mintPath.resolve(quizDirName).resolve(quizKey);
         Path assignmentDir = mintPath.resolve(worksheetDirName).resolve(assignmentKey);
         Path staging = assignmentDir.resolve(stagingDirName);
         Path studentsDir = assignmentDir.resolve(studentDirName);
         
         Files.createDirectory(assignmentDir);        
         Files.createDirectory(staging);
         
         Path quizStaging = quizDir.resolve(stagingDirName);
         DirectoryStream<Path> plotfiles = 
             Files.newDirectoryStream(quizStaging, "*.gnuplot");
         for (Path plotfile : plotfiles) {
             Files.copy(plotfile, staging.resolve(plotfile.getFileName()));
         }
         plotfiles.close();
         
         Path compositeTex = staging.resolve(String.format(nameFormat,
             "assignment", quizId, testpaperId, "tex"));
         DocumentWriter compositeDoc = new DocumentWriter(compositeTex);
         compositeDoc.writePreamble(assignment.getQuiz().getName());
         compositeDoc.beginQuiz();
 
         Path studentDir, studentStaging;
         Path blueprintTex = quizStaging.resolve(blueprintFile);
         int totalPages = getTotalPages(blueprintTex);
         EntryType[] students = assignment.getStudents();        
         for (int i = 0; i < students.length; i++) {
             
             String studentId = students[i].getId();
             String studentKey = students[i].getValue();
             String studentName = students[i].getName();
             
             studentDir = studentsDir.resolve(studentKey);
             studentStaging = studentDir.resolve(stagingDirName);
             Files.createDirectories(studentStaging);
             
             plotfiles = Files.newDirectoryStream(quizStaging, "*.gnuplot");
             for (Path plotfile : plotfiles) {
                 Files.copy(plotfile,
                     studentStaging.resolve(plotfile.getFileName()));
             }
             plotfiles.close();
 
             Path individualTex = studentStaging.resolve(
                 String.format(nameFormat, studentId, quizId, testpaperId, "tex"));
             DocumentWriter individualDoc = new DocumentWriter(individualTex);
             individualDoc.writePreamble(assignment.getQuiz().getName());
             individualDoc.beginQuiz();                       
 
             compositeDoc.resetQuestionNumbering();
             compositeDoc.resetPageNumbering();
             
             compositeDoc.printAuthor(studentName);
             individualDoc.printAuthor(studentName);
             
             // QRKey = [TestPaperId(6)][studentIdx(3)][pageNum(1)]            
             String QRKey = String.format("%s%s", assignmentKey, studentKey);
             
             HashMap<String,String> params = new HashMap<String,String>();
             params.put(insertQR, QRKey);
             params.put(rollDice, "random");
             Path questionsTex = staging.resolve(questionsFile);            
             DocumentWriter questionsDoc = new DocumentWriter(questionsTex);            
             questionsDoc.writeTemplate(blueprintTex, params);
             questionsDoc.close();
             
             compositeDoc.writeTemplate(questionsTex);
             individualDoc.writeTemplate(questionsTex);
             
             if (totalPages % 2 != 0 && students.length > 1) compositeDoc.insertBlankPage();
             
             individualDoc.endQuiz();
             individualDoc.close();
         }
         compositeDoc.endQuiz();
         compositeDoc.close();
 
         // 3. Link Makefiles and make the PDFs
         Path toMakefile = staging.relativize(sharedPath).resolve(makefilesDir).
             resolve(quizMakefile);
         Files.createSymbolicLink(staging.resolve(makefile), toMakefile);
         
         if (make(staging, assignmentDir, null) != 0)
             throw new Exception("Problem! Non-zero return code from make");
 
         return prepareManifest(assignmentDir, assignmentDir, null);
     }
     
     /**
      * Publishes an assignment so that individual students can download 
      * it from their own *home* directories
      * 
      *  @param assignmentId (quiz, assignment)
      *  @return manifest
      *  @throws Exception
      */
     public ManifestType prepTest(AssignmentType assignment)
             throws Exception {
 
         String quizId = assignment.getQuiz().getId();
         String assignmentId = assignment.getInstance().getId();
         String assignmentValue = assignment.getInstance().getValue();
 
         Path studentsDir = mintPath.resolve(worksheetDirName).
             resolve(assignmentValue).resolve(studentDirName);
         ManifestType manifest = prepareManifest(studentsDir, null, null);
         
         Path studentDir, stagingDir, previewDir, toMakefile;        
         for (EntryType student: assignment.getStudents()) {
             
             String studentId = student.getId();
             String studentKey = student.getValue();
             studentDir = studentsDir.resolve(studentKey);
             stagingDir = studentDir.resolve(stagingDirName);
             previewDir = studentDir.resolve(previewDirName);
             
             Files.createDirectory(previewDir);
             
             toMakefile = stagingDir.relativize(sharedPath).
                 resolve(makefilesDir).resolve(quizMakefile);
             Files.createSymbolicLink(stagingDir.resolve(makefile), toMakefile);
             
             if (make(stagingDir, studentDir, previewDir) == 0) {
                 EntryType document = new EntryType();
                 document.setId(studentsDir.relativize(studentDir).resolve(
                     String.format(nameFormat, studentId, quizId, 
                     assignmentId, "pdf")).toString());
                 manifest.addDocument(document);
             } 
         }
         return manifest;    
     }
 
     public ManifestType generateStudentCode(EntryType student) 
             throws Exception {
         Path studentDir = mintPath.resolve(String.format("s%s", student.getId()));
         if (!Files.exists(studentDir)) {
             Files.createDirectory(studentDir);
         }
         return atm.deposit(studentDir);
     }
     
     private int make(Path workingDir, Path downloadsDir, Path previewsDir)
             throws Exception {
         ProcessBuilder pb = new ProcessBuilder("make");
         if (previewsDir == null) {
             pb.command().add("pdf");
         }
 
         pb.directory(workingDir.toFile());
         pb.redirectErrorStream(true);
 
         Process process = pb.start();
         BufferedReader messages = new BufferedReader(new InputStreamReader(
                 process.getInputStream()));
 
         String line = null;
         while ((line = messages.readLine()) != null) {
             System.out.println(line);
         }
         
         int retCode = 0;
         if ((retCode = process.waitFor()) == 0) {
             
             Path target = null;
             DirectoryStream<Path> stream = 
                     Files.newDirectoryStream(workingDir, "*.pdf");
             for (Path entry: stream) {
                 target = downloadsDir.resolve(entry.getFileName());
                 Files.move(entry, target, StandardCopyOption.REPLACE_EXISTING);
             }
             stream.close();
             
             if (previewsDir != null) {
                 stream = Files.newDirectoryStream(previewsDir, "*.jpeg");
                 for (Path entry: stream) Files.delete(entry);
                 stream.close();            
                 
                 stream = Files.newDirectoryStream(workingDir, "*.jpeg");
                 for (Path entry: stream) {
                     target = previewsDir.resolve(entry.getFileName());
                     Files.move(entry, target);
                 }
                 stream.close();
             }
         }
         
         ProcessBuilder pClean = new ProcessBuilder("make", "clean");
         pClean.directory(workingDir.toFile());
         Process clean = pClean.start();
         clean.waitFor();
 
         return retCode;
     }
         
     private void copyQuestion(Path staging, DocumentWriter blueprintDoc,
         String questionId) throws Exception {
         Path questionTex, plotfile, target;
         Path questionDir = vaultPath.resolve(questionId);
         questionTex = questionDir.resolve("question.tex");
         blueprintDoc.writeTemplate(questionTex);
         plotfile = questionDir.resolve("figure.gnuplot");
         if (Files.exists(plotfile)) {
             target = staging.resolve(
                 String.format("%s.gnuplot", questionId));
             Files.copy(plotfile, target,
                 StandardCopyOption.REPLACE_EXISTING);
         }
     }
 
     private int getTotalPages(Path blueprintTex) throws Exception {
         int pageCount = 0;
         String[] lines = Files.readAllLines(blueprintTex, 
             StandardCharsets.UTF_8).toArray(new String[0]);
         for (String line: lines) {
             if (line.startsWith(newpage)) pageCount++;
         }
         return pageCount;
     }
 
     private ManifestType prepareManifest(Path root,  
         Path downloads, Path preview) throws Exception {        
         ManifestType manifest = new ManifestType();
         manifest.setRoot(mintPath.relativize(root).toString());
         
         if (downloads != null) {
             DirectoryStream<Path> PDFs = 
                 Files.newDirectoryStream(downloads, "*.pdf");
             for (Path PDF : PDFs) {
                 EntryType document = new EntryType();
                 document.setId(root.relativize(PDF).toString());
                 manifest.addDocument(document);
             }
             PDFs.close();
             if (manifest.getDocument().length == 0)
                 throw new Exception("Problema! No documents for download");
         }
         
         if (preview != null) {
             DirectoryStream<Path> jpegs = 
                 Files.newDirectoryStream(preview, "*.jpeg");
             for (Path jpeg : jpegs) {
                 EntryType img = new EntryType();
                 img.setId(root.relativize(jpeg).toString());
                 manifest.addImage(img);
             }
             jpegs.close();
             if (manifest.getDocument().length == 0)
                 throw new Exception("Problema! No images for preview");
         }
 
         return manifest;
     }
     
     private Path  sharedPath, mintPath, vaultPath;
     private ATM   atm;
     
     private final String 
         stagingDirName = "staging",
         previewDirName = "preview",
         quizDirName = "quiz",
         worksheetDirName = "ws",
         studentDirName = "student",
         makefilesDir = "makefiles",
         makefile = "Makefile",
         quizMakefile = "quiz.mk",
         blueprintFile = "blueprintTex",
         questionsFile = "questionsTex",
         nameFormat = "%s-%s-%s.%s";
 
 }
