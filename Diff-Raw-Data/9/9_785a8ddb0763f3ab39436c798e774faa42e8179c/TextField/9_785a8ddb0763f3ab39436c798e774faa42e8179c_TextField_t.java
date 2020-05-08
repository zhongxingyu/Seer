 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package libs.form.fields;
 
 import java.util.Map;
 
 
 public class TextField extends FormField {
     private int minLength = 0;
 
     public TextField(String name) {
         super(name);
         setType("text");
 
         setErrorText("minlength", "Le texte est trop court (au moins %d caractères).");
         setErrorText("maxlength", "Le texte est trop long (pas plus de %d caractères).");
     }
 
     /**
      * Permet de définir la taille du champ
      *
      * @param $size La nouvelle taille (0 pour supprimer cet attribut)
      *
      * @return this
      */
     public FormField setSize(int size)
     {
         if(size > 0)
             setAttr("size", String.valueOf(size));
         else if(size == 0)
             setAttr("size", null);
 
         return this;
     }
 
     /**
      * Change la longueur minimale du contenu du champ
      *
      * @param $len nouvelle valeur (0 pour désactiver la limitation)
      *
      * @return obj (le champ en question)
      */
     public FormField setMinLength(int len)
     {
         minLength = len > 0 ? len : 0;
 
         return this;
     }
 
     /**
      * Change la longueur maximale du contenu du champ
      *
      * @param $len nouvelle valeur (0 pour désactiver la limitation)
      *
      * @return obj (le champ en question)
      */
     public FormField setMaxLength(int len)
     {
         if(len > 0)
             setAttr("maxlength", String.valueOf(len));
         else if(len == 0)
             setAttr("maxlength", null);
 
         return this;
     }
 
     @Override
     public boolean isValid()
     {
         if(super.isValid())
         {
             if(getValue().length() < minLength)
             {
                 error("minlength", minLength);
                 return false;
             }
 
             Map<String, String> attrs = getAttrs();
             if(attrs.get("maxlength") != null) {
                 int max = Integer.parseInt(attrs.get("maxlength"));
                 
                 if(getValue().length() > max) {
                     error("maxlength", max);
                     return false;
                 }
             }
 
             return true;
         }
 
         return false;
     }
 }
