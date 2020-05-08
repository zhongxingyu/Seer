 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.yace.web.utils;
 
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import net.yace.entity.Ycollection;
 import net.yace.entity.Yitem;
 import net.yace.entity.Yitemtype;
 import net.yace.entity.Yuser;
 import net.yace.facade.YitemFacade;
 
 /**
  *
  * @author MaBoy <bruno.boi@student.helha.be>
  */
 public class YaceUtils {
     
     private final static String ERROR_PAGE = "WEB-INF/view/user/errorpage.jsp";
 
     /**
      * Précondition: boxes ne contient que des clés dans [info|tip]
      * Postcondition: renvoie le contenu html de la zone contextuelle d'aide
      * 
      * @param boxes
      * @return 
      */
     public static String getAsideHelp(Map<String, List<String>> boxes) {
         String tipsHTML = "";
         String infosHTML = "";
         Iterator<String> itKeys = boxes.keySet().iterator();
 
         while (itKeys.hasNext()) {
             String key = itKeys.next();
             Iterator<String> values = boxes.get(key).iterator();
             while (values.hasNext()) {
                 String value = values.next();
                 if (key.equals("info")) {
                     infosHTML +=
                             "<div class='infobox'>"
                             + "<img class='infoicon32' title='Que dois-je faire?' src='./theme/default/img/img_trans.gif' />"
                             + "<p>" + value + "</p>"
                             + "</div>";
                 } else if (key.equals("tip")) {
                     tipsHTML +=
                             "<div class='tipbox'>"
                             + "<img class='tipicon' title='Astuce!' src='./theme/default/img/img_trans.gif' />"
                             + "<p>" + value + "</p>"
                             + "</div>";
                 }
             }
         }
 
         return infosHTML + tipsHTML;
     }
 
     /**
      * 
      * @param collection
      * @return 
      */
     public static String getAsideCollectionView(Ycollection collection) {
         return "";
     }
 
     /**
      * 
      * @param s
      * @return 
      */
     public static String digestMD5(String s) {
 
         String ret = null;
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
 
             md.reset();
             md.update(s.getBytes());
             byte[] digest = md.digest();
 
             StringBuilder sb = new StringBuilder();
             for (int i = 0; i < digest.length; i++) {
                 sb.append(Integer.toHexString(0xFF & digest[i]));
             }
 
             ret = sb.toString();
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(YaceUtils.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return ret;
     }
     
     public static boolean isValidEmail(String email)
     {
         String expr = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
         CharSequence input = email;
         Pattern pattern = Pattern.compile(expr, Pattern.CASE_INSENSITIVE);
         Matcher matcher = pattern.matcher(input);
         return matcher.matches();
     }
 
     public static enum SessionState {
         noauth, auth, admin
     };
 
     public static SessionState getSessionState(HttpServletRequest request) {
         HttpSession session = request.getSession(false);
         if (session == null) {
             return SessionState.noauth;
         } else {
             Yuser yuser = (Yuser) session.getAttribute("user");
             if (yuser == null) {
                 return SessionState.noauth;
             } else {
                 // Session ok
                 // On teste si privilèges admin
                 if (yuser.getRank().isAdmin()) {
                     return SessionState.admin;
                 } else {
                     return SessionState.auth;
                 }
             }
         }
     }
 
     public static void displayAdminError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // On défini l'erreur qui s'est produite
         request.setAttribute("errorMsg",
                 "Nous sommes désolé, mais vous ne pouvez pas accéder à l'administration.<br/>"
                 + "Référez-vous à l'aide contextuelle pour plus d'information.<br/>"
                 + "Vous n'êtes pas satisfait ? <a href='about'>Contactez-nous</a> !");
 
         // Aide contextuelle
         Map<String, List<String>> asideHelp = new HashMap<String, List<String>>();
 
         List<String> infoBoxes = new ArrayList<String>();
         List<String> tipBoxes = new ArrayList<String>();
 
         infoBoxes.add("Vous tentez d'accéder à l'administration sans en avoir les privilèges.");
         tipBoxes.add("Essayez de ne pas accéder à l'administration !");
         tipBoxes.add("N'hésitez pas à <a href='about'>nous contacter</a> si vous pensez qu'il s'agit d'une erreur de notre part. N'oubliez pas de détailler les actions qui vous ont mené à cette page, merci.");
 
         asideHelp.put("tip", tipBoxes);
         asideHelp.put("info", infoBoxes);
 
         request.setAttribute("asideHelp", YaceUtils.getAsideHelp(asideHelp));
 
         // On nomme et affiche la page
         request.setAttribute("pageTitle", "Accès non autorisé");
         request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
     }
     
     public static void displayUnknownUserError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // On défini l'erreur qui s'est produite
         request.setAttribute("errorMsg",
                 "Nous sommes désolé, mais l'utilisateur que vous recherchez n'existe pas dans notre base de données.<br/>"
                 + "Référez-vous à l'aide contextuelle pour plus d'information.<br/>"
                 + "Vous n'êtes pas satisfait ? <a href='about'>Contactez-nous</a> !");
 
         // Aide contextuelle
         Map<String, List<String>> asideHelp = new HashMap<String, List<String>>();
 
         List<String> infoBoxes = new ArrayList<String>();
         List<String> tipBoxes = new ArrayList<String>();
 
         infoBoxes.add("Vous tentez d'accéder aux informations d'un utilisateur qui est inconnu de nos services.");
         tipBoxes.add("Vérifiez le lien avec lequel vous tentez d'accéder à cette page !");
         tipBoxes.add("N'hésitez pas à <a href='about'>nous contacter</a> si vous pensez qu'il s'agit d'une erreur de notre part. N'oubliez pas de détailler les actions qui vous ont mené à cette page, merci.");
 
         asideHelp.put("tip", tipBoxes);
         asideHelp.put("info", infoBoxes);
 
         request.setAttribute("asideHelp", YaceUtils.getAsideHelp(asideHelp));
 
         // On nomme et affiche la page
         request.setAttribute("pageTitle", "Utilisateur introuvable");
         request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
     }
     
     public static void displayItemError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // On défini l'erreur qui s'est produite
         request.setAttribute("errorMsg",
                 "Nous sommes désolé, mais l'objet que vous recherchez n'existe pas dans notre base de données, ou vous n'avez pas la permission de voir les informations qui y sont liées.<br/>"
                 + "Référez-vous à l'aide contextuelle pour plus d'information.<br/>"
                 + "Vous n'êtes pas satisfait ? <a href='about'>Contactez-nous</a> !");
 
         // Aide contextuelle
         Map<String, List<String>> asideHelp = new HashMap<String, List<String>>();
 
         List<String> infoBoxes = new ArrayList<String>();
         List<String> tipBoxes = new ArrayList<String>();
 
         infoBoxes.add("Vous tentez d'accéder aux informations d'un objet qui est inconnu de nos services ou qui nécessite des droits que vous n'avez pas.");
         tipBoxes.add("Vérifiez le lien avec lequel vous tentez d'accéder à cette page !");
         tipBoxes.add("N'hésitez pas à <a href='about'>nous contacter</a> si vous pensez qu'il s'agit d'une erreur de notre part. N'oubliez pas de détailler les actions qui vous ont mené à cette page, merci.");
 
         asideHelp.put("tip", tipBoxes);
         asideHelp.put("info", infoBoxes);
 
         request.setAttribute("asideHelp", YaceUtils.getAsideHelp(asideHelp));
 
         // On nomme et affiche la page
         request.setAttribute("pageTitle", "Objet inaccessible");
         request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
     }
     
     public static void displayCollectionUnreachableError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // On défini l'erreur qui s'est produite
         request.setAttribute("errorMsg",
                 "Nous sommes désolé, mais vous ne pouvez pas accéder à la collection demandée.<br/>"
                 + "Référez-vous à l'aide contextuelle pour plus d'information.<br/>"
                 + "Vous n'êtes pas satisfait ? <a href='about'>Contactez-nous</a> !");
 
         // Aide contextuelle
         Map<String, List<String>> asideHelp = new HashMap<String, List<String>>();
 
         List<String> infoBoxes = new ArrayList<String>();
         List<String> tipBoxes = new ArrayList<String>();
 
         infoBoxes.add("La collection qui est demandée est introuvable ou n'est pas en votre possession.");
         tipBoxes.add("Essayez d'accéder à une autre collection !");
         tipBoxes.add("N'hésitez pas à <a href='about'>nous contacter</a> si vous pensez qu'il s'agit d'une erreur de notre part. N'oubliez pas de détailler les actions qui vous ont mené à cette page, merci.");
 
         asideHelp.put("tip", tipBoxes);
         asideHelp.put("info", infoBoxes);
 
         request.setAttribute("asideHelp", YaceUtils.getAsideHelp(asideHelp));
 
         // On nomme et affiche la page
         request.setAttribute("pageTitle", "Collection introuvable");
         request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
     }
     
         public static void displayMaxItemReachError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // On défini l'erreur qui s'est produite
         request.setAttribute("errorMsg",
                 "Nous sommes désolé, mais vous ne pouvez pas ajouter de nouvel objet.<br/>"
                 + "Référez-vous à l'aide contextuelle pour plus d'information.<br/>"
                 + "Vous n'êtes pas satisfait ? <a href='about'>Contactez-nous</a> !");
 
         // Aide contextuelle
         Map<String, List<String>> asideHelp = new HashMap<String, List<String>>();
 
         List<String> infoBoxes = new ArrayList<String>();
         List<String> tipBoxes = new ArrayList<String>();
 
         infoBoxes.add("Vous avez atteint le nombre maximum d'objet que vous pouvez posséder dans vos collections.");
         tipBoxes.add("Vous devriez demander une augmentation de la limite à un administrateur.");
         tipBoxes.add("N'hésitez pas à <a href='about'>nous contacter</a> si vous pensez qu'il s'agit d'une erreur de notre part. N'oubliez pas de détailler les actions qui vous ont mené à cette page, merci.");
 
         asideHelp.put("tip", tipBoxes);
         asideHelp.put("info", infoBoxes);
 
         request.setAttribute("asideHelp", YaceUtils.getAsideHelp(asideHelp));
 
         // On nomme et affiche la page
         request.setAttribute("pageTitle", "Limite d'objet maximum atteinte");
         request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
     }
     
         
     public static void displaySearchError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // erreur survenur lors de la recherche des items
         request.setAttribute("errorMsg",
                 "Nous sommes désolés, mais votre recherche n'a pas abouti.<br/>"
                 + "Vous devez introduire au moins un mot de deux caractères pour effectuer une recherche.<br/>"
                 + "Vous n'êtes pas satisfait ? <a href='about'>Contactez-nous</a> !");
 
         // Aide contextuelle
         Map<String, List<String>> asideHelp = new HashMap<String, List<String>>();
 
         List<String> infoBoxes = new ArrayList<String>();
         List<String> tipBoxes = new ArrayList<String>();
 
         infoBoxes.add("Pour effectuer une recherche, vous devez au moins introduire un mot de deux caractères.");
         tipBoxes.add("Essayez de faire une recherche sur un terme plus long");
 
         asideHelp.put("tip", tipBoxes);
         asideHelp.put("info", infoBoxes);
 
         request.setAttribute("asideHelp", YaceUtils.getAsideHelp(asideHelp));
 
         // On nomme et affiche la page
         request.setAttribute("pageTitle", "Recherche non valide");
         request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
     }
         
     //vérifie si item peut être consulté par user
     public static boolean CanDisplayItem(Yitem item, Yuser usr)
     {
         Boolean result = false;//faux par defaut
         
         if(item != null)
         {
             if(item.getCollection().isPublic())
             {
                 //item publique
                 result=true;
             }
             else
             {
                 //item privé
                 //voir si l'user est le proprietaire de collection
                 if(usr != null)
                 {
                     if(usr.getRank().isAdmin() )
                     {
                         //l'admin a l'accès total
                         result=true;
                     }
                     else if(item.getCollection().getOwner().equals(usr))
                     {
                         //item appartient à l'user
                         result=true;
                     }     
                 }
             }
         }
         
         return result;
     }
     
     //id de l'Yitem precedent de la collection
     //-1 si n'existe pas
     public static int getPrevItemId(Yitem item)
     {
         int x = -1;
         YitemFacade itemFac = ServicesLocator.getItemFacade();
         ArrayList<Yitem> itemList = new ArrayList<Yitem>(itemFac.findAll(item.getCollection()));
         x = itemList.indexOf(item);//index de l'item courant
         if(x>0)
             x = itemList.get(x-1).getIdYITEM();
         else
             x=-1;
         
         return x;
     }
     
     //id de l'Yitem suivant de la collection
     //-1 si n'existe pas
     public static int getNextItemId(Yitem item)
     {
         int x = -1;
         YitemFacade itemFac = ServicesLocator.getItemFacade();
         ArrayList<Yitem> itemList = new ArrayList<Yitem>(itemFac.findAll(item.getCollection()));
         x = itemList.indexOf(item);//index de l'item courant
         if(x<itemList.size()-1)
             x = itemList.get(x+1).getIdYITEM();
         else
             x=-1;
         
         return x;
     }
     
     //Optimisation  : un appel db au lieu de 2
     //retourne un tableau de 2 éléments, le premier est l'index prev
     //le second esr next
     //-1 si n'existe pas
     public static int[] getPrevNextItemId(Yitem item)
     {
         int x = -1;
         int y = -1;
         YitemFacade itemFac = ServicesLocator.getItemFacade();
         ArrayList<Yitem> itemList = new ArrayList<Yitem>(itemFac.findAll(item.getCollection()));
         x = y = itemList.indexOf(item);//index de l'item courant
         //precedent
         if(x>0)
             x = itemList.get(x-1).getIdYITEM();
         else
             x=-1;
         //suivant
         if(y<itemList.size()-1)
             y = itemList.get(y+1).getIdYITEM();
         else
             y=-1;
         int[] tab = {x,y};
         
         return tab;
     }
     
     /*
      * Permet d'enlever les accents, de remplacer les espaces par des underscore
      * et de mettre en miniscule
      */
     public static String deAccent(String str) {
         String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
         Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
         nfdNormalizedString = pattern.matcher(nfdNormalizedString).replaceAll("");
         return nfdNormalizedString.replaceAll(" ", "_").toLowerCase();
     }
     
     //entoure les elements sub contenus dans input avec le prefix et le suffix
     public static String envelopSubStrings(String input, String lowInput, String lowSub, String prefix, String suffix)
     {
         /*String lowInput = input.toUpperCase();
         Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
         lowInput = pattern.matcher(Normalizer.normalize(lowInput, Normalizer.Form.NFD)).replaceAll("");
         String lowSub = sub.toUpperCase();*/
         
         String result = "";
         int i = 0;
         int j = 0;
         while(lowInput.contains(lowSub))
         {
             
             i=lowInput.indexOf(lowSub);
             result = result.concat(input.substring(0, i));
             result = result.concat(prefix+input.substring(i, i+lowSub.length())+suffix);
             lowInput = lowInput.substring(i+lowSub.length());
             input = input.substring(i+lowSub.length());
             if(!lowInput.contains(lowSub) && input.length()>0)
                 result = result.concat(input);
         }
         
         return result;
     }
     
     //vérifie si item peut être consulté par user
     public static Boolean canEditItem(Yitem item, Yuser yuser) {
         boolean result = false;
         if(yuser != null && item != null)
         {
             //l'admin peut tout editer
             if(yuser.getRank().isAdmin())
                 result = true;
             else if(yuser.getIdYUSER() == item.getCollection().getOwner().getIdYUSER())
                 result = true;
         }
         
         return result;
     }
     
     //vérifie si itemtype peut être consulté par user
     public static Boolean canConsultItemType(Yitemtype itemtype, Yuser yuser) {
         boolean result = false;
         if(itemtype != null)
         {
             if (yuser != null) 
             {
                 //l'admin peut tout editer
                 if (yuser.getRank().isAdmin() || itemtype.isPublic()) {
                     result = true;
                 } else if (yuser.getIdYUSER() == itemtype.getCollection().getOwner().getIdYUSER()) {
                     result = true;
                 }
             }
             else
             {
                 //un visiteur peut consulter le type public
                 if(itemtype.isPublic())
                     result = true;
             }
         }
         return result;
     }
     
     //vérifie si itemtype peut être édité par user
     public static Boolean canEditItemType(Yitemtype itemtype, Yuser yuser)
     {
         Boolean canedit = false;
         if(yuser != null)
         {
             if(yuser.getRank().isAdmin())
                 canedit = true;
             else if(yuser.getIdYUSER()==itemtype.getCollection().getOwner().getIdYUSER())
                 canedit = true;
         }
         return canedit;
     }
     
     //vérifie si item peut être supprimé par user
     public static Boolean canDeleteItem(Yitem item, Yuser yuser)
     {
         Boolean candelete = false;
         if(item != null && yuser != null)
         {
             if(yuser.getRank().isAdmin())
                 candelete = true;
             else if(yuser.getIdYUSER()==item.getCollection().getOwner().getIdYUSER())
                 candelete = true;
         }
         return candelete;
     }
    
    public static boolean isValidURL(String url) {
        if (url == null) {
            return false;
        }
        //url format regex
        String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(url);
        // return url.matches(urlPattern);
        return matcher.matches();
    }
 }
