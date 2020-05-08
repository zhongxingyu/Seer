 package tech4;
 
 import org.springframework.stereotype.Component;
 import org.springframework.web.context.request.SessionScope;
 
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 /**
  *
  */
 @Component
 public class Tech42013 {
 
     static int counter = 0;
 
     public static LinkedList<String> kommentare = new LinkedList<String>();
 
    public synchronized int getCounter() {
         Tech42013.counter = Tech42013.counter + 1;
         return Tech42013.counter;
     }
 
     public void updateList(String komm) {
         if (komm != null && !komm.equals("")) {
             kommentare.add(0,komm);
             if (kommentare.size() > 10) {
                 kommentare.removeLast();
             }
             kommentar = null;
         }
     }
 
     public synchronized void save() {
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         String txtProperty = request.getParameter("form:text");
         updateList(txtProperty);
     }
 
     public List<String> getKommentare() {
         return (List<String>) kommentare;
     }
 
     private String kommentar = "";
 
     public synchronized String getKommentar() {
         return kommentar;
     }
 
     public void setKommentar(String kommentar) {
         this.kommentar = kommentar;
     }
 
 }
