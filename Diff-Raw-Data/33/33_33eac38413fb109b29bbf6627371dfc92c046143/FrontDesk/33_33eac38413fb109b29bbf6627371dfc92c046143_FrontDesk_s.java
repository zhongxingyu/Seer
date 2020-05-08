 package gutenberg.workers;
 
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.StudentGroupType;
 import gutenberg.blocs.TeacherType;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.nio.file.StandardOpenOption;
 import java.util.List;
 
 public class FrontDesk {
 
     public FrontDesk(Config config) throws Exception {
         frontdeskPath = new File(config.getPath(Resource.frontdesk)).toPath();
         sharedPath = new File(config.getPath(Resource.shared)).toPath();
         locker = new Locker(config);
     }
 
     public ManifestType generateSuggestionForm(TeacherType teacherInfo)
             throws Exception {
 
         EntryType teacher = teacherInfo.getTeacher();
         EntryType school = teacherInfo.getSchool();
 
         Path outputDirPath = frontdeskPath.resolve(String.format(
                 "teachers/%s-%s/petty-cash", school.getId(), teacher.getId()));
         if (!Files.exists(outputDirPath))
             Files.createDirectories(outputDirPath);
                 
         Path keyFile = outputDirPath.resolve("keyFile");
         PrintWriter keyFileWriter = new PrintWriter(Files.newBufferedWriter(
                 keyFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE));
         String QRKey = String.format("%s-%s", school.getId(), teacher.getId());
         String QRKeyVal = String.format("%s:%s-%s-0-0-0-1-1", 
                 QRKey, teacher.getId(), teacher.getName());
         keyFileWriter.println(QRKeyVal);
         keyFileWriter.close();
 
         Path workingDirPath = outputDirPath.resolve("working");
         Files.createDirectory(workingDirPath);
        String templateName = "templates/suggestion.tex";
         Path texPath = workingDirPath.resolve(templateName);
         PrintWriter writer = new PrintWriter(Files.newBufferedWriter(texPath,
                 StandardCharsets.UTF_8, StandardOpenOption.CREATE));
         
         List<String> template = Files.readAllLines(
                sharedPath.resolve(templateName), StandardCharsets.UTF_8);        
         String trimmed = null;
         for (String line : template) {
             
             trimmed = line.trim();
             if (trimmed.startsWith(school_tag)) {
                 line = line.replace("school", school.getName());
             } else if (trimmed.startsWith(author_tag)) {
                 line = line.replace("name", teacher.getName().replace('-', ' '));
             } else if (trimmed.startsWith(insertQRC_tag)) {
                 line = line.replace("QRC", QRKey);
             }
             writer.println(line);
         }
         writer.close();
 
         Files.createSymbolicLink(workingDirPath.resolve("Makefile"),
                 sharedPath.resolve("makefiles/front-desk.mk"));
 
         String outputFile = String.format("%s-%s.pdf", teacher.getId(), teacher
                 .getName().split("-")[0]);
         generatePdf(workingDirPath, outputDirPath.resolve(outputFile));
         
         ManifestType manifest = new ManifestType();
         manifest.setRoot(outputDirPath.toString());        
         EntryType document = new EntryType();
         document.setId(outputFile);
         manifest.addDocument(document);
         
         // Make room in the locker for receiving scans        
         locker.makeRoom("0", teacher.getId());
         
         return manifest;
     }
 
     public ManifestType generateStudentRoster(StudentGroupType studentGroup)
             throws Exception {
 
         EntryType group = studentGroup.getGroup();
         EntryType school = studentGroup.getSchool();
         EntryType[] members = studentGroup.getMembers();
 
         if (members == null)
             members = new EntryType[0];
         if (members.length % 2 != 0) {
             throw new Exception(
                     "Doh! Come on, send an even number of elements I say!");
         }
 
         Path outputDirPath = frontdeskPath.resolve(String.format(
                 "schools/%s/rosters", school.getId()));
         if (!Files.exists(outputDirPath))
             Files.createDirectories(outputDirPath);
 
         Path workingDirPath = outputDirPath.resolve(group.getId());
         if (!Files.exists(workingDirPath))
             Files.createDirectory(workingDirPath);
 
        String templateName = "templates/roster.tex";
         Path texPath = workingDirPath.resolve(templateName);
         PrintWriter writer = new PrintWriter(Files.newBufferedWriter(texPath,
                 StandardCharsets.UTF_8, StandardOpenOption.CREATE));
         List<String> template = Files.readAllLines(
                sharedPath.resolve(templateName), StandardCharsets.UTF_8);
 
         String trim = null;
         for (String line : template) {
             trim = line.trim();
             if (trim.contains("{school}")) {
                 line = line.replace("school", school.getName());
             } else if (trim.contains("{sektion}")) {
                 line = line.replace("sektion", group.getName());
             } else if (trim.startsWith(table_end)) {
                 // number of lines = members.length / 2
                 String row = "%s & %s & & %s & %s \\\\";
                 for (int i = 0; i < members.length; i += 2) {
                     writer.println(String.format(row, members[i].getName(),
                             members[i].getId(), members[i + 1].getName(),
                             members[i + 1].getId()));
                 }
             }
             writer.println(line);
         }
         writer.close();
 
         Files.createSymbolicLink(workingDirPath.resolve("Makefile"),
                sharedPath.resolve("front-desk.mk"));
 
         String outputFile = String.format("%s-%s.pdf", group.getId(), group
                 .getName().replace(' ', '_'));
         generatePdf(workingDirPath, outputDirPath.resolve(outputFile));
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(outputDirPath.toString());
         EntryType document = new EntryType();
         document.setId(outputFile);
         manifest.addDocument(document);
         return manifest;
     }
 
     public ManifestType generateQuizReport(StudentGroupType studentGroup)
             throws Exception {
 
         EntryType testpaper = studentGroup.getGroup();
         EntryType school = studentGroup.getSchool();
         EntryType[] members = studentGroup.getMembers();
 
         if (members == null)
             members = new EntryType[0];
         if (members.length % 2 != 0) {
             throw new Exception(
                     "Doh! Come on, send an even number of elements I say!");
         }
 
         Path outputDirPath = frontdeskPath.resolve(String.format(
                 "schools/%s/reports", school.getId()));
         if (!Files.exists(outputDirPath))
             Files.createDirectories(outputDirPath);
 
         Path workingDirPath = outputDirPath.resolve(testpaper.getId());
         if (!Files.exists(workingDirPath))
             Files.createDirectory(workingDirPath);
 
         String outputCSV = String.format("%s-%s.csv", testpaper.getId(),
                 testpaper.getName().replace(' ', '_'));
         Path csvPath = outputDirPath.resolve(outputCSV);
         PrintWriter csvWriter = new PrintWriter(Files.newBufferedWriter(csvPath,
                 StandardCharsets.UTF_8, StandardOpenOption.CREATE));
         csvWriter.println("name,marks");
         
        String templateName = "templates/groupreport.tex";
         Path texPath = workingDirPath.resolve(templateName);
         PrintWriter writer = new PrintWriter(Files.newBufferedWriter(texPath,
                 StandardCharsets.UTF_8, StandardOpenOption.CREATE));
         
         List<String> template = Files.readAllLines(
                sharedPath.resolve(templateName), StandardCharsets.UTF_8);
         String trim = null;
         for (String line : template) {
             trim = line.trim();
             if (trim.contains("{school}")) {
                 line = line.replace("school", school.getName());
             } else if (trim.contains("{sektion-testpaper}")) {
                 line = line.replace("sektion-testpaper", testpaper.getName());
             } else if (trim.startsWith(table_end)) {
                 // number of lines = members.length / 2
                 String row = "%s & %s & & %s & %s \\\\";
                 for (int i = 0; i < members.length; i += 2) {
                     writer.println(String.format(row, members[i].getName(),
                             members[i].getValue(), members[i + 1].getName(),
                             members[i + 1].getValue()));
                     csvWriter.println(members[i].getName() + ","
                             + members[i].getValue());
                     csvWriter.println(members[i + 1].getName() + ","
                             + members[i + 1].getValue());
                 }
             }
             writer.println(line);
         }
         writer.close();
         csvWriter.close();
 
         Files.createSymbolicLink(workingDirPath.resolve("Makefile"),
                sharedPath.resolve("front-desk.mk"));
 
         String outputFile = String.format("%s-%s.pdf", testpaper.getId(),
                 testpaper.getName().replace(' ', '_'));
         generatePdf(workingDirPath, outputDirPath.resolve(outputFile));
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(outputDirPath.toString());
         EntryType document = new EntryType();
         document.setId(outputFile);
         manifest.addDocument(document);
         document = new EntryType();
         document.setId(outputCSV);
         manifest.addDocument(document);
         return manifest;
     }
 
     private Locker locker;
     private Path frontdeskPath, sharedPath;
     private final String school_tag = "\\School", author_tag = "\\DocAuthor",
             insertQRC_tag = "\\insertQR", table_end = "\\end{tabular}";
 
     private int generatePdf(Path workingDirPath, Path outputFilePath)
             throws Exception {
         ProcessBuilder pb = new ProcessBuilder("make");
 
         pb.directory(workingDirPath.toFile());
         pb.redirectErrorStream(true);
 
         Process build = pb.start();
         BufferedReader messages = new BufferedReader(new InputStreamReader(
                 build.getInputStream()));
 
         String line = null;
         while ((line = messages.readLine()) != null) {
             System.out.println(line);
         }
 
         if (build.waitFor() == 0) {
             Path src = workingDirPath.toFile()
                     .listFiles(new NameFilter(".pdf"))[0].toPath();
             Files.move(src, outputFilePath, StandardCopyOption.REPLACE_EXISTING);
 
             ProcessBuilder pClean = new ProcessBuilder("make", "clean");
             pClean.directory(workingDirPath.toFile());
             Process clean = pClean.start();
             if (clean.waitFor() == 0) {
                 Files.delete(workingDirPath.resolve("Makefile"));
                 Files.delete(workingDirPath);
             }
         }
 
         return 0;
     }
 }
