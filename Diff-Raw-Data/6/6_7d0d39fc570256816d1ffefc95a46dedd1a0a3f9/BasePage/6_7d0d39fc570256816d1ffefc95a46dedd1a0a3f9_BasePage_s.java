 package hu.woomler.gastrobees.web;
 
 import hu.woomler.gastrobees.web.menu.MenuPanel;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 /**
  * Minden page-nek a kozos ose. 
  * Ebbol mindig szarmazni kell!
  *
  * @author joe
  */
 public abstract class BasePage extends WebPage{
   
     /** A menu panel wicket:id-je. Egyezik a html kodban megadott ID-vel. */
     protected static final String MENU_PANEL_ID = "MenuPanel";
     
     @SuppressWarnings("OverridableMethodCallInConstructor")
     public BasePage(PageParameters pageParams){
         super(pageParams);
                 
        Label dummyFooter = new Label("baseFooter", getString("BasePage.footer.label"));                
        add(dummyFooter);
        
         // ez adja hozza a menu-t
         addMenuPanel(MENU_PANEL_ID);
         
         // itt jon letre a valodi tartalom
         createContent();
     }
     
     /**
      * Legyartja a main content tartalmat.
      * Ez a valodi tartalom, a leszarmazottak felelossege ezt implementalni.
      * 
      * @param id
      * @return 
      */
     protected abstract void createContent();
     
     /**
      * Az aktualis session-t adja vissza.
      * 
      * @return 
      */
     public GastroSession getGastroSession(){
         return (GastroSession)getSession();
     }
     
     /**
      * Hozzaadja a komponens fahoz a menu panelt.
      * 
      * @param id 
      */
     protected void addMenuPanel(String id){
         add(new MenuPanel(id));
     }
     
 }
