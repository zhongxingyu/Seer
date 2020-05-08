 package au.id.wolfe.tribs.data;
 
import com.google.common.collect.Lists;

 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Basic history of an issue.
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 public class HistoryEntry {
 
     private String field;
    private List<StatusChange> froms = Lists.newLinkedList();
    private List<StatusChange> tos = Lists.newLinkedList();
 
     private Date createdDate;
 
     public String getField() {
         return field;
     }
 
     public void setField(String field) {
         this.field = field;
     }
 
     public List<StatusChange> getFroms() {
         return froms;
     }
 
     public void setFroms(List<StatusChange> froms) {
         this.froms = froms;
     }
 
     public List<StatusChange> getTos() {
         return tos;
     }
 
     public void setTos(List<StatusChange> tos) {
         this.tos = tos;
     }
 
     public Date getCreatedDate() {
         return createdDate;
     }
 
     public void setCreatedDate(Date createdDate) {
         this.createdDate = createdDate;
     }
 }
