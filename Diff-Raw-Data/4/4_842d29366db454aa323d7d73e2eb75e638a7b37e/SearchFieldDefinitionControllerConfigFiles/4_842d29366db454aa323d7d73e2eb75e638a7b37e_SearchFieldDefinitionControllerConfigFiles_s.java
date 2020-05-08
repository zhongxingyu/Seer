 package at.owlsoft.owl.business;
 
 /**
  * this enum references resource files of the @SearchFieldDefinitionController
  * the controller will load the correct files using this enum
  */
 public enum SearchFieldDefinitionControllerConfigFiles
 {
    Default("SearchFieldCategories.xml"), NoGuiMapping(
            "SearchFieldCategoriesNoGuiMapping.xml");
 
     private String text;
 
     SearchFieldDefinitionControllerConfigFiles(String text)
     {
         this.text = text;
     }
 
     public String getText()
     {
         return this.text;
     }
 
     public static SearchFieldDefinitionControllerConfigFiles fromString(
             String text)
     {
         if (text != null)
         {
             for (SearchFieldDefinitionControllerConfigFiles b : SearchFieldDefinitionControllerConfigFiles
                     .values())
             {
                 if (text.equalsIgnoreCase(b.text))
                 {
                     return b;
                 }
             }
         }
         return null;
     }
 
 }
