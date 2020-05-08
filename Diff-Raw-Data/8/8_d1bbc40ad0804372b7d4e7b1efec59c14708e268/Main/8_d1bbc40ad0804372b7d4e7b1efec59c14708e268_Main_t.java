 package de.unifr.acp.trafo;
 
 import java.io.IOException;
 import java.io.InvalidClassException;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.NotFoundException;
 
 public class Main {
 
     /**
      * @param args
      * @throws NotFoundException 
      * @throws CannotCompileException 
      * @throws IOException 
      * @throws ClassNotFoundException 
      */
     public static void main(String[] args) throws NotFoundException, ClassNotFoundException, IOException, CannotCompileException {
         try {
             String className = args[0];
             String outputDir = (args.length >= 2) ? args[1] : "bin";
             ClassPool defaultPool = ClassPool.getDefault();
             CtClass target = defaultPool.get(className);
             // TransClass.doTransform(target,
             // !target.getSuperclass().equals(objectClass));
             TransClass.transformAndFlushHierarchy(className, outputDir);
 
             // condition needed to avoid bug in writing unmodified class files
             // causing invalid class files
             // if (target.isModified()) {
             // target.writeFile(outputDir);
             // }
 
         } catch (ArrayIndexOutOfBoundsException e) {
             StringBuilder usage = new StringBuilder();
             usage.append("Usage: x2traverse class [output directory]\n");
             usage.append(" (to transform the class hierarchy rootet at a class)\n");
             usage.append("where options include:\n");
             usage.append(" \n");
             System.out.println(usage);
         } catch (InvalidClassException e) {
             e.printStackTrace();
            System.out.println(e.toString());
            throw e;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            System.out.println(e.toString());
            System.out.println(e.getReason());
         }
 
     }
 
 }
