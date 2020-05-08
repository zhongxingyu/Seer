 package ru.abishev.java;
 
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.PsiClassType;
 import ru.abishev.spec.ICompletionProvider;
 
 import java.lang.reflect.Method;
 import java.util.List;
 
 import static ru.abishev.utils.CollectionUtils.newArrayList;
 
 /**
  * @author Timur Abishev (timur@abishev.ru)
  */
 public class JavaCompletionProvider implements ICompletionProvider {
     private final IJavaClassCreator JAVA_CLASS_CREATOR = new StupidJavaClassCreator();
 
     public List<String> getCompletions(Project project, PsiClassType clazz, PsiClassType... generics) {
         List<String> completions = newArrayList();
 
         Object completionObject = JAVA_CLASS_CREATOR.getObjectWithClassForPsiClassType(project, clazz.resolve());
         Object[] genericObjects = new Object[generics.length];
         Class[] genericObjectClasses = new Class[generics.length];
 
         for (int i = 0; i < genericObjects.length; i++) {
             genericObjects[i] = JAVA_CLASS_CREATOR.getObjectWithClassForPsiClassType(project, generics[i].resolve());
             genericObjectClasses[i] = genericObjects[i].getClass();
         }
 
         if (completionObject != null) {
             try {
                Method method = completionObject.getClass().getMethod("getCompletionList", genericObjectClasses);
                completions = (List<String>) method.invoke(completionObject, genericObjects);
             } catch (Exception e) {
                 // todo: ?
                 System.out.println(e);
             }
         }
 
         JAVA_CLASS_CREATOR.reload(); // todo: only when make!
 
         return completions;
     }
 }
