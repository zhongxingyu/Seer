 package fr.gphy.piotrgui.j2eged.controllers;
 
 import fr.gphy.piotrgui.j2eged.helpers.BrowserHelper;
 import fr.gphy.piotrgui.j2eged.model.Document;
 import fr.gphy.piotrgui.j2eged.model.Folder;
 import fr.gphy.piotrgui.j2eged.model.Metadata;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpServletRequest;
import org.primefaces.component.commandlink.CommandLink;
 import org.primefaces.model.menu.DefaultMenuItem;
 import org.primefaces.model.menu.DefaultMenuModel;
 import org.primefaces.model.menu.MenuModel;
 
 /**
  *
  * @author Piotr
  */
 @ManagedBean(name = "BrowserController")
 @SessionScoped
 public class BrowserController implements Serializable {
     
     private List<Object[]> data;
     private List<Folder> folders;
     private List<DisplayDoc> toDisplay;
     private final BrowserHelper helper;
     
     private Folder currentFolder;
     private DisplayDoc currentDocument;
     
     private int selectedView;
     
     private MenuModel breadCrumbModel;
     private final FolderHistory folderHistory;
     
     private String newFolderName;
 
     public DisplayDoc getCurrentDocument() {
         return currentDocument;
     }
 
     public void setCurrentDocument(DisplayDoc currentDocument) {
         this.currentDocument = currentDocument;
     }
     
     public String getNewFolderName() {
         return newFolderName;
     }
 
     public void setNewFolderName(String newFolderName) {
         this.newFolderName = newFolderName;
     }
     
     public BrowserController() {
         selectedView = 1;
         toDisplay = new ArrayList<DisplayDoc>();
         helper = new BrowserHelper();
         
         currentFolder = null;
         
         breadCrumbModel = new DefaultMenuModel();
         
         folderHistory = new FolderHistory();
     }
     
     public List<Object[]> getData() {
         return data;
     }
     
     public void setData(List<Object[]> data) {
         this.data = data;
     }
     
     public List<DisplayDoc> getToDisplay() {
         return toDisplay;
     }
     
     public void setToDisplay(List<DisplayDoc> toDisplay) {
         this.toDisplay = toDisplay;
     }
     
     public void clear() {
         this.toDisplay = new ArrayList<>();
     }
     
     public void onLoad() {
         folderHistory.clear();
         
         final FacesContext context = FacesContext.getCurrentInstance();
         String paramId = context.getExternalContext().getRequestParameterMap().get("idFolder");
         try {
            Integer idDestFolder = paramId.equals("null") ? null : Integer.valueOf(paramId);
            changeFolder(idDestFolder);
            context.getExternalContext().redirect("browser.xhtml");
            return;
         } catch (Exception e) {}
         
         //System.err.println(idDestFolder);
         changeFolder(currentFolder, false);
     }
     
     public MenuModel getBreadCrumbModel() {
         return breadCrumbModel;
     }
     
     public void loadToDisplay() {
         for (Folder fold : this.folders) {
             this.toDisplay.add(new DisplayDoc(fold));
         }
         
         Collections.sort(this.data, new Comparator<Object[]>() {
             public int compare(Object[] a, Object[] b) {
                 int compareTo = ((Metadata)a[1]).getName().compareTo(((Metadata)b[1]).getName());
                 return compareTo;
             }
         });
         
         for (Object[] row : this.data) {
             this.toDisplay.add(new DisplayDoc((Document) row[0], (Metadata) row[1]));
         }
     }
     
     public void changeFolder(Integer idFolder) {
         this.clear();
         
         this.helper.reloadSession();
         this.currentFolder = this.helper.getFolder(idFolder);
         changeFolder(currentFolder);
     }
     
     public void changeFolder(Folder newFolder) {
         changeFolder(newFolder, true);
     }
     
     public void changeFolder(Folder newFolder, boolean browsing) {
         Integer idFolder = null;
         if (newFolder != null) {
             idFolder = newFolder.getIdFolder();
         }
         
         this.clear();
 
         this.currentFolder = newFolder;
 
         this.helper.reloadSession();
         
         this.data = this.helper.getDocuments(idFolder);
         this.folders = this.helper.getFolders(idFolder);
         
         this.loadToDisplay();
         
         if (browsing) {
             folderHistory.add(currentFolder);
         }
         updateBreadCrumb();
     }
     
     public void updateBreadCrumb() {
         breadCrumbModel = new DefaultMenuModel();
         ArrayList<Folder> folderPath = new ArrayList<Folder>();
         
         Folder f = currentFolder;
         while (f != null) {
             folderPath.add(f);
             f = f.getFolder();
         }
         folderPath.add(null);
         
         Collections.reverse(folderPath);
         
         for(Folder f2 : folderPath) {
            DefaultMenuItem item = new DefaultMenuItem(f2 != null ? f2.getName() : "Root");
             item.setUrl("browser.xhtml?idFolder=" + (f2 != null ? f2.getIdFolder() : "null"));
             breadCrumbModel.addElement(item); 
         }
     }
     
     public void clickOnFolder(ActionEvent event) {
         String paramId = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("idFolder");
         Integer idDestFolder = !paramId.equals("null") ? Integer.valueOf(paramId) : null;
         
         changeFolder(idDestFolder);
     }
     
     public void clickOnInfo(ActionEvent event) {
         String paramId = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getParameter("idFile");
         Integer idDestFile = !paramId.equals("null") ? Integer.valueOf(paramId) : null;
         
         for(DisplayDoc d: toDisplay) {
            if(!d.isFolder() && d.getDoc().getIdDoc().equals(idDestFile)) {
                 currentDocument = d;
                 break;
             }
         }
     }
     
     public void clickOnBackward() {
         changeFolder(folderHistory.backward(), false);
     }
     
     public void clickOnForward() {
         changeFolder(folderHistory.forward(), false);
     }
 
     public Folder getCurrentFolder() {
         return currentFolder;
     }
 
     public void setCurrentFolder(Folder currenFolder) {
         this.currentFolder = currenFolder;
     }
     
     public boolean isListView() {
         return selectedView==2;
     }
     
     public boolean isIconView() {
         return selectedView==1;
     }
     
     public boolean isGalleriaView() {
         return selectedView==3;
     }
     
     public void createFolder() {
         System.err.println("createFolder = " + newFolderName);
         this.helper.createFolder(currentFolder, newFolderName);
         
         changeFolder(currentFolder, false);
         newFolderName = "";
     }
 
     public int getSelectedView() {
         return selectedView;
     }
 
     public void setSelectedView(int selectedView) {
         this.selectedView = selectedView;
     }
     
     public class FolderHistory implements Serializable {
         
         private final ArrayList<Folder> history;
         private Integer historyPosition;
         
         public FolderHistory() {
             history = new ArrayList<Folder>();
             historyPosition = null;
         }
         
         public void clear() {
             history.clear();
             historyPosition = null;
         }
         
         public void add(Folder f) {
             int pos = historyPosition != null ? historyPosition + 1 : 0;
             
             if (pos + 1 < history.size()) {
                 for (int i = pos + 1; i < history.size(); i++) {
                     history.remove(i);
                 }
             }
             history.add(pos, f);
             historyPosition = pos;
         }
         
         public Folder backward() {
             if (historyPosition != null) {
                 historyPosition--;
                 if (historyPosition < 0) {
                     historyPosition = 0;
                 }
                 
                 return history.get(historyPosition);
             }
             return null;
         }
         
         public Folder forward() {
             if (historyPosition != null) {
                 historyPosition++;
                 if (historyPosition >= history.size()) {
                     historyPosition = history.size() - 1;
                 }
                 
                 return history.get(historyPosition);
             }
             return null;
         }
     }
     
     public class DisplayDoc implements Serializable {
         
         private Document doc = null;
         private Metadata meta = null;
         private Folder folder = null;
         
         public DisplayDoc(Document doc, Metadata meta) {
             this.doc = doc;
             this.meta = meta;
         }
         
         public DisplayDoc(Folder folder) {
             this.folder = folder;
         }
         
         public Folder getFolder() {
             return folder;
         }
         
         public void setFolder(Folder folder) {
             this.folder = folder;
         }
         
         public Document getDoc() {
             return doc;
         }
         
         public Metadata getMeta() {
             return meta;
         }
         
         public Boolean isFolder() {
             return (this.folder != null);
         }
     }
 }
