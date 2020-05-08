 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim;
 
 import heartsim.cam.CellularAutomataModel;
 import heartsim.cam.profile.Default;
 import heartsim.cam.profile.Profile;
 import heartsim.util.StringUtils;
 import org.w3c.dom.Element;
 
 /**
  * A class for different heart tissue
  * @author Lee Boynton
  */
 public class HeartTissue
 {
     protected CellularAutomataModel currentModel;
     protected String name;
     protected String description;
     protected Element element;
     protected Profile profile;
 
     public HeartTissue(String name)
     {
         this(name, "");
     }
 
     public HeartTissue(String name, String description)
     {
         this.description = description;
         setName(name);
 
         if (Application.getInstance().getCAModels().size() > 0)
         {
             currentModel = Application.getInstance().getCAModels().get(0);
         }
 
         detectProfile();
     }
 
     private void detectProfile()
     {
         for (Profile p : Application.getInstance().getTissueProfiles())
         {
             for (String alias : p.getAliases())
             {
                 if(alias.equalsIgnoreCase(name))
                 {
                     Application.getInstance().output("Selecting " + p.getName() + " profile for element " + name);
                     profile = p;
                     p.loadParameters(currentModel);
                     return;
                 }
             }
         }
 
         profile = new Default();
 
         Application.getInstance().output("Could not detect profile for " + name + ", using default profile instead");
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public void setDescription(String description)
     {
         this.description = description;
     }
 
     public CellularAutomataModel getModel()
     {
         return currentModel;
     }
 
     public void setModel(CellularAutomataModel model)
     {
         this.currentModel = model;
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = StringUtils.prettify(name);
     }
 
     public Element getElement()
     {
         return element;
     }
 
     public void setElement(Element element)
     {
         this.element = element;
     }
 
     @Override
     public String toString()
     {
         return name;
     }
 }
