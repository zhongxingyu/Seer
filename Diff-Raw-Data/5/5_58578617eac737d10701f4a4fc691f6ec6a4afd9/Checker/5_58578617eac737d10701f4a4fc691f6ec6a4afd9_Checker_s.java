 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class Checker {
 
     public static void main(String[] args) throws Exception {
         PrintStream console = System.out;
 
         Class expectedClass = new Parser(System.in).parse();
 
         ByteArrayOutputStream resultInfo = new ByteArrayOutputStream();
         System.setOut(new PrintStream(resultInfo));
         ClassParser.printInfo(new FileInputStream("inputClass.class"));
 
         Class gotClass;
         try {
             gotClass = new Parser(new ByteArrayInputStream(resultInfo.toByteArray())).parse();
         } catch (Exception e) {
             gotClass = null;
         }
 
         console.println(expectedClass.equals(gotClass));
     }
 }
 
 class Parser {
 
     private final List<String> lines;
     private int curLine;
 
     Parser(InputStream stream) throws Exception {
         lines = new ArrayList<>();
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
         while (true) {
             String line = reader.readLine();
             if (line == null) {
                 break;
             }
             if (line.trim().isEmpty()) {
                 continue;
             }
             lines.add(line);
         }
     }
 
     public Class parse() {
         return new Class(
                 nextLine("version:"),
                 nextLine("name:"),
                 nextLine("extends:"),
                 new HashSet<>(Arrays.asList(nextLine("implements:").split(" "))),
                 new HashSet<>(Arrays.asList(nextLine("access_flags:").split(" "))),
                 parseFields(),
                 parseMethods());
     }
 
     private Set<Field> parseFields() {
         assertStarts(nextLine(), "fields:");
         Set<Field> fields = new HashSet<>();
         while (true) {
             Field field = parseField();
             if (field == null) {
                 return fields;
             }
             fields.add(field);
         }
     }
 
     private Set<Method> parseMethods() {
         assertStarts(nextLine(), "methods:");
         Set<Method> methods = new HashSet<>();
         while (true) {
             Method method = parseMethod();
             if (method == null) {
                 return methods;
             }
             methods.add(method);
         }
     }
 
     private Field parseField() {
         String line = nextLine();
         if (line.startsWith("methods:")) {
             goToPrev();
             return null;
         } else {
             goToPrev();
         }
         return new Field(
                 nextLine("    name:"),
                 nextLine("    type:"),
                 new HashSet<>(Arrays.asList(nextLine("    access_flags:").split(" ")))
         );
     }
 
     private Method parseMethod() {
         if (curLine == lines.size()) {
             return null;
         }
         return new Method(
                 nextLine("    name:"),
                 nextLine("    arguments:"),
                 nextLine("    return:"),
                 new HashSet<>(Arrays.asList(nextLine("    access_flags:").split(" "))),
                 new HashSet<>(Arrays.asList(nextLine("    throws:").split(" ")))
         );
     }
 
     private void assertStarts(String line, String start) {
         if (!line.startsWith(start)) {
            throw new FormatException();
         }
     }
 
     private String nextLine() {
         return lines.get(curLine++);
     }
 
     private String nextLine(String prefix) {
         String line = nextLine();
         assertStarts(line, prefix);
         if (line.length() == prefix.length()) {
             return "";
         } else {
             return line.substring(prefix.length() + 1).trim();
         }
     }
 
     private void goToPrev() {
         curLine--;
     }
 }
 
class FormatException extends RuntimeException {}

 class Class {
     public final String name;
     public final String superClass;
     public final Set<String> interfaces;
     public final String version;
     public final Set<String> accessFlags;
     public final Set<Field> fields;
     public final Set<Method> methods;
 
     Class(String version, String name, String superClass, Set<String> interfaces,
           Set<String> accessFlags, Set<Field> fields, Set<Method> methods) {
         this.accessFlags = accessFlags;
         this.name = name;
         this.superClass = superClass;
         this.interfaces = interfaces;
         this.version = version;
         this.fields = fields;
         this.methods = methods;
         accessFlags.remove("");
         interfaces.remove("");
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Class aClass = (Class) o;
 
         if (!accessFlags.equals(aClass.accessFlags)) return false;
         if (!fields.equals(aClass.fields)) return false;
         if (!interfaces.equals(aClass.interfaces)) return false;
         if (!methods.equals(aClass.methods)) return false;
         if (!name.equals(aClass.name)) return false;
         if (!superClass.equals(aClass.superClass)) return false;
         if (!version.equals(aClass.version)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = name.hashCode();
         result = 31 * result + superClass.hashCode();
         result = 31 * result + interfaces.hashCode();
         result = 31 * result + version.hashCode();
         result = 31 * result + accessFlags.hashCode();
         result = 31 * result + fields.hashCode();
         result = 31 * result + methods.hashCode();
         return result;
     }
 }
 
 class Field {
     public final String name;
     public final String type;
     public final Set<String> accessFlags;
 
     Field(String name, String type, Set<String> accessFlags) {
         this.accessFlags = accessFlags;
         this.name = name;
         this.type = type;
         accessFlags.remove("");
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Field field = (Field) o;
 
         if (!accessFlags.equals(field.accessFlags)) return false;
         if (!name.equals(field.name)) return false;
         if (!type.equals(field.type)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = name.hashCode();
         result = 31 * result + type.hashCode();
         result = 31 * result + accessFlags.hashCode();
         return result;
     }
 }
 
 class Method {
     public final String name;
     public final String returnType;
     public final String arguments;
     public final Set<String> throwsExceptions;
     public final Set<String> accessFlags;
 
     Method(String name, String arguments, String returnType, Set<String> accessFlags, Set<String> throwsExceptions) {
         this.accessFlags = accessFlags;
         this.name = name;
         this.returnType = returnType;
         this.arguments = arguments;
         this.throwsExceptions = throwsExceptions;
         accessFlags.remove("");
         throwsExceptions.remove("");
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Method method = (Method) o;
 
         if (!accessFlags.equals(method.accessFlags)) return false;
         if (!arguments.equals(method.arguments)) return false;
         if (!name.equals(method.name)) return false;
         if (!returnType.equals(method.returnType)) return false;
         if (!throwsExceptions.equals(method.throwsExceptions)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = name.hashCode();
         result = 31 * result + returnType.hashCode();
         result = 31 * result + arguments.hashCode();
         result = 31 * result + throwsExceptions.hashCode();
         result = 31 * result + accessFlags.hashCode();
         return result;
     }
 }
