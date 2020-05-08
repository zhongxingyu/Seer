 package de.objectcode.time4u.client.ui.provider;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.ICellModifier;
 import org.eclipse.swt.widgets.Item;
 
 import de.objectcode.time4u.client.store.api.RepositoryFactory;
 import de.objectcode.time4u.client.ui.UIPlugin;
 import de.objectcode.time4u.client.ui.dnd.ProjectTaskHolder;
 import de.objectcode.time4u.server.api.data.ProjectSummary;
import de.objectcode.time4u.server.api.data.Task;
 import de.objectcode.time4u.server.api.data.TaskSummary;
 import de.objectcode.time4u.server.api.data.WorkItem;
 
 public class WorkItemTableCellModifier implements ICellModifier
 {
   public boolean canModify(final Object element, final String property)
   {
     if (element instanceof WorkItem) {
       WorkItem activeWorkItem = null;
       try {
         activeWorkItem = RepositoryFactory.getRepository().getWorkItemRepository().getActiveWorkItem();
       } catch (final Exception e) {
         UIPlugin.getDefault().log(e);
       }
       if ("end".equals(property) && (activeWorkItem == null || activeWorkItem.getId() != ((WorkItem) element).getId())) {
         return true;
       }
 
       if ("begin".equals(property) || "task".equals(property) || "comment".equals(property)) {
         return true;
       }
     }
 
     return false;
   }
 
   public Object getValue(final Object element, final String property)
   {
     if (element instanceof WorkItem) {
       final WorkItem workItem = (WorkItem) element;
 
       if ("begin".equals(property)) {
         return workItem.getBegin();
       } else if ("end".equals(property)) {
         return workItem.getEnd();
       } else if ("task".equals(property)) {
         try {
           final ProjectSummary project = RepositoryFactory.getRepository().getProjectRepository().getProjectSummary(
               workItem.getProjectId());
           final TaskSummary task = RepositoryFactory.getRepository().getTaskRepository().getTaskSummary(
               workItem.getTaskId());
 
           return new ProjectTaskHolder(project, task);
         } catch (final Exception e) {
           UIPlugin.getDefault().log(e);
         }
       } else if ("comment".equals(property)) {
         return workItem.getComment();
       }
     }
     return null;
   }
 
   public void modify(final Object swtElement, final String property, final Object value)
   {
     if (swtElement instanceof Item) {
       final Object element = ((Item) swtElement).getData();
 
       if (element instanceof WorkItem) {
         final WorkItem workItem = (WorkItem) element;
 
         if ("begin".equals(property)) {
           Assert.isTrue(value != null && value instanceof Integer);
 
           final int begin = ((Integer) value).intValue();
           if (begin >= 0 && begin <= 24 * 3600 && workItem.getBegin() != begin) {
             workItem.setBegin(begin);
             try {
               RepositoryFactory.getRepository().getWorkItemRepository().storeWorkItem(workItem);
             } catch (final Exception e) {
               UIPlugin.getDefault().log(e);
             }
 
           }
         } else if ("end".equals(property)) {
           Assert.isTrue(value != null && value instanceof Integer);
 
           final int end = ((Integer) value).intValue();
 
           if (end >= 0 && end <= 24 * 3600 && workItem.getEnd() != end) {
             workItem.setEnd(end);
 
             try {
               RepositoryFactory.getRepository().getWorkItemRepository().storeWorkItem(workItem);
             } catch (final Exception e) {
               UIPlugin.getDefault().log(e);
             }
           }
         } else if ("task".equals(property)) {
           TaskSummary task = null;
 
           if (value instanceof TaskSummary) {
             task = (TaskSummary) value;
           } else if (value instanceof ProjectTaskHolder) {
             task = ((ProjectTaskHolder) value).getTask();
           }
 
           if (task != null) {
             if (task.getId() != workItem.getTaskId()) {

              workItem.setTaskId(((Task) value).getId());
 
               try {
                 RepositoryFactory.getRepository().getWorkItemRepository().storeWorkItem(workItem);
               } catch (final Exception e) {
                 UIPlugin.getDefault().log(e);
               }
             }
           }
         } else if ("comment".equals(property)) {
           if (!value.toString().equals(workItem.getComment())) {
             workItem.setComment(value.toString());
 
             try {
               RepositoryFactory.getRepository().getWorkItemRepository().storeWorkItem(workItem);
             } catch (final Exception e) {
               UIPlugin.getDefault().log(e);
             }
           }
         }
       }
     }
   }
 }
