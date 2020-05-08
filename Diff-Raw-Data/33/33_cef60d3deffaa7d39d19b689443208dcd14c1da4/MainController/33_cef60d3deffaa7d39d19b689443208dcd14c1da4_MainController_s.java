 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pkgOnlineShop;
 
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 /**
  *
  * @author Andreas
  */
 public class MainController {
     
     private Database db;
     
     private String loginName;    
     private String pw;    
     private String message;
     private Person user;
     private String selectedKategorie;
     private Produkt selectedProduct;
     private boolean signedin = false;
     private String search;
     private Bestellung selectedBestellung;
     
     private List kategorien;
     
     public MainController()
     {
        try{
         db = new Database();
         kategorien = db.getKategorien();
        }catch(Exception ex){
            message=ex.toString();
        }
     }
     
     public String login()
     {
         try{
             user = db.tryLogin(loginName, pw);
 
             if(user != null)
             {
                 signedin = true;
                 if(user.getAnbieter() == 1)
                 {
                     return "adminManageUsers";
                 }
                 else
                 {
                     return "index";
                 }
             }
             else
             {
                 signedin = false;
                 message = "Login Failed";
                 return "index";
             }
         }
         catch(Exception ignore){
             message=ignore.toString();
             return "index";
         }
     }
     
     public void reload()
     {
         try{
         kategorien = db.getKategorien();
        }catch(Exception ex){
            message=ex.toString();
        }
     }
     
     public String changeKategorie(Kategorie k) {
         this.selectedKategorie = k.getBezeichnung();
         return "products";
     }
     
     public String settings(){
         return "settings";
     }
     
     public String register()
     {
         return "register";
     }
     
     public String warenkorb(){
         return "cart";
     }
     
     public String orders() {
     	return "orders";
     }
     
     public String logout(){
         user = null;
         signedin = false;
         FacesContext context = FacesContext.getCurrentInstance();
         Object remove = context.getExternalContext().getSessionMap().remove("#{OrderController}");
        message = remove+" halllo";
         return "index";
     }
     
     public boolean isadmin(){
         if(user != null){
             if(user.getAnbieter()==1)
                 return true;
         }
         return false;
     }
     
     public boolean isnotadmin(){
         if(user != null){
             if(user.getAnbieter()==1)
                 return false;
         }
         return true;
     }
    
     public boolean getsignedin(){
         return signedin;
     }
 
     public void setSignedin(boolean signedin) {
         this.signedin = signedin;
     }
     
     public boolean getnotsignedin(){
         return !signedin;
     }
 
     public List getKategorien() {
         return kategorien;
     }
 
     public void setKategorien(List kategorien) {
         this.kategorien = kategorien;
     } 
 
     public String getLoginName() {
         return loginName;
     }
 
     public void setLoginName(String login) {
         this.loginName = login;
     }
 
     public String getPw() {
         return pw;
     }
 
     public void setPw(String pw) {
         this.pw = pw;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public Person getUser() {
         return user;
     }
 
     public void setUser(Person user) {
         this.user = user;
     }
 
     public String getSelectedKategorie() {
         return selectedKategorie;
     }
 
     public void setSelectedKategorie(String selectedKategorie) {
         this.selectedKategorie = selectedKategorie;
     }
 
 	public Produkt getSelectedProduct() {
 		return selectedProduct;
 	}
 
 	public void setSelectedProduct(Produkt selectedProduct) {
 		this.selectedProduct = selectedProduct;
 	}
 
 	public String getSearch() {
 		return search;
 	}
 
 	public void setSearch(String search) {
 		this.search = search;
 	}
 
 	public Bestellung getSelectedBestellung() {
 		return selectedBestellung;
 	}
 
 	public void setSelectedBestellung(Bestellung selectedBestellung) {
 		this.selectedBestellung = selectedBestellung;
 	}
     
     
    
 }
