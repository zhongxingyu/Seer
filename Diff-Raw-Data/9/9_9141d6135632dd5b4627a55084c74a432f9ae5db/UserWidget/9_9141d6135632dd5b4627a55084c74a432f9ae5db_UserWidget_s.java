 package models.tm;
 
 import java.util.Map;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 /**
  * Represents the widgets a user has, including preferences such as position etc.
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "id", columnNames = {"project_id", "naturalId"})}, name = "tm_UserWidget")
 
 public class UserWidget extends ProjectModel implements Widget {
 
     public UserWidget(Project project) {
         super(project);
     }
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = false)
     public TMUser user;
 
     @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE}, optional = false)
     public ProjectWidget widget;
 
     @Column(name = "widgetColumn")
     public String column;
 
     public boolean open;
 
     public String getTitle() {
         return widget.title;
     }
 
     public String getCategory() {
         return widget.category;
     }
 
     public String getDescription() {
         return widget.description;
     }
 
     public String getCreator() {
         return widget.creator;
     }
 
     public boolean isTemplate() {
         return widget.templateWidget;
     }
 
     public WidgetType getType() {
         return widget.widgetType;
     }
 
     public Map<String, Object> getParameters() {
         return widget.parameters;
     }
 
     public String getColumn() {
         return column;
     }
 
     public boolean isOpen() {
         return open;
     }
 
 
 }
