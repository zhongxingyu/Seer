 import java.io.*;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardOpenOption;
 import java.util.*;
 
 /**
  */
 public class TaskMaker {
     public TaskMaker(String studentsFile, String tasksFile, String taskDir) throws IOException {
         taskNames = getTaskNames(tasksFile);
         studentTable = createTable(studentsFile, tasksFile);
         taskMap = readTasks(taskDir);
     }
 
     private final Map<String, byte[]> taskMap;
     private final List<List<String>> taskNames;
     private final List<Student> studentTable;
    private static final Random rand = new Random();
 
     private List<List<String>> getTaskNames(String tasksFile) throws IOException {
         List<List<String>> tasks = new ArrayList<>();
         BufferedReader tf = new BufferedReader(new FileReader(new File(tasksFile)));
         String s;
         int taskNum = 0;
         while (! ((s = tf.readLine()) == null)) {
             tasks.add(new ArrayList<String>());
             for (int i = 0; i < Integer.valueOf(s); ++i) {
                 tasks.get(taskNum).add(String.valueOf(taskNum + 1) + (char)('a' + i));
             }
             taskNum++;
         }
         return tasks;
     }
 
     public List<Student> createTable(String studentsFile, String tasksFile) throws IOException {
         List<Student> table = new ArrayList<>();
         getTaskNames(tasksFile);
         BufferedReader st = new BufferedReader(new FileReader(new File(studentsFile)));
         String group = st.readLine();
         String s;
         while (!((s = st.readLine()) == null)) {
             List<String> task = new ArrayList<>();
             for (List<String> taskName : taskNames) {
                 task.add(taskName.get(rand.nextInt(taskName.size())));
             }
             table.add(new Student(s, group, task));
         }
         return table;
     }
 
     private Map<String, byte[]> readTasks(String tasksDir) throws IOException {
         Map<String, byte[]> tasks = new HashMap<>();
         File dir = new File(tasksDir);
         for (File task : dir.listFiles()) {
             tasks.put(task.getName().split("\\.")[0], Files.readAllBytes(task.toPath()));
         }
         return tasks;
     }
 
     private void createVariant(Student st, Path outfile) throws IOException {
         //BufferedWriter writer = Files.newBufferedWriter(outfile, Charset.defaultCharset(), StandardOpenOption.APPEND);
         Files.write(outfile, ("\\center{\\textbf{" + st + "}}" + '\n').getBytes(), StandardOpenOption.APPEND);
         Files.write(outfile, "\\begin{enumerate}\n".getBytes(), StandardOpenOption.APPEND);
         for (String t : st.getTask()) {
             Files.write(outfile, new String(taskMap.get(t)).getBytes(), StandardOpenOption.APPEND);
         }
         Files.write(outfile, "\\end{enumerate}\n".getBytes(), StandardOpenOption.APPEND);
         Files.write(outfile, "\\newpage\n".getBytes(), StandardOpenOption.APPEND);
     }
 
     public void createAllVariants(String outfile) throws IOException {
         Path test = new File(outfile).toPath();
         Files.write(test, taskMap.get("begin"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
         Files.write(test, "\n".getBytes(), StandardOpenOption.APPEND);
         for (Student st : studentTable) {
             createVariant(st, test);
         }
         Files.write(test, "\\end{document}\n".getBytes(), StandardOpenOption.APPEND);
     }
     public void print(String outFile) throws FileNotFoundException {
         PrintWriter pw = new PrintWriter(new File(outFile));
         for (Student aTable : studentTable) {
             pw.println(aTable.toString());
             pw.println();
             pw.println();
             pw.println();
             pw.println();
         }
         pw.flush();
     }
 }
