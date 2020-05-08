 package com.fing.pis.bizativiti.plugin.xpdl.tasks;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.wfmc._2009.xpdl2.Task;
import org.wfmc._2009.xpdl2.TaskUser;
 
 import com.fing.pis.bizativiti.common.metamodel.MetamodelElement;
 import com.fing.pis.bizativiti.plugin.xpdl.ATranslator;
 import com.fing.pis.bizativiti.plugin.xpdl.Converter.ParserConverter;
 
 public class TranslatorTask extends ATranslator {
 
     @Override
     public List<MetamodelElement> translate(ParserConverter f, Object node, List<Object> pathFromRoot) {
 
         Task tarea = (Task) node;
         List<MetamodelElement> result = new ArrayList<MetamodelElement>();
 
         if (tarea.getTaskUser() != null) {
 
             result.addAll(f.eval(tarea.getTaskUser(), pathFromRoot));
 
         } else if (tarea.getTaskBusinessRule() != null) {
 
             result.addAll(f.eval(tarea.getTaskBusinessRule(), pathFromRoot));
 
         } else if (tarea.getTaskManual() != null) {
 
             result.addAll(f.eval(tarea.getTaskManual(), pathFromRoot));
 
         } else if (tarea.getTaskReceive() != null) {
 
             result.addAll(f.eval(tarea.getTaskReceive(), pathFromRoot));
 
         } else if (tarea.getTaskReference() != null) {
 
             result.addAll(f.eval(tarea.getTaskReference(), pathFromRoot));
 
         } else if (tarea.getTaskScript() != null) {
 
             result.addAll(f.eval(tarea.getTaskScript(), pathFromRoot));
 
         } else if (tarea.getTaskSend() != null) {
 
             result.addAll(f.eval(tarea.getTaskSend(), pathFromRoot));
 
         } else if (tarea.getTaskService() != null) {
 
             result.addAll(f.eval(tarea.getTaskService(), pathFromRoot));
 
         } else {
             //Es tarea por defecto
            tarea.setTaskUser(new TaskUser());
            result.addAll(f.eval(tarea.getTaskUser(), pathFromRoot));
         }
 
         return result;
     }
 }
