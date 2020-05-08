 package controllers;
 
 import java.util.*;
 
 import models.NewsItem;
 
 import play.data.*;
 
 public class Static
 {
  private static final String [] MENU_STRINGS = { "News", "Info", /*"Media",*/ "Forum" };
  private static final String [] MENU_ADMIN_STRINGS = { "Users", "News Items", "Forums" };
   private static final String [] UNDER_CON_STRINGS = { "Info", "Media" };
   private static final List<String> MenuItems = new ArrayList<String>(MENU_STRINGS.length);
   private static final List<String> MenuAdminItems = new ArrayList<String>(MENU_ADMIN_STRINGS.length);
   private static final List<String> UnderConItems = new ArrayList<String>(UNDER_CON_STRINGS.length);
   private static final List<String> UnderConItemsLower = new ArrayList<String>(UNDER_CON_STRINGS.length);
   static
   {
     for (String i : MENU_STRINGS)
     {
       MenuItems.add(i);
     }
     for (String i : MENU_ADMIN_STRINGS)
     {
       MenuAdminItems.add(i);
     }
     for (String i : UNDER_CON_STRINGS)
     {
       UnderConItems.add(i);
       UnderConItemsLower.add(i.toLowerCase());
     }
   }
 
   public static List<String> menuList()
   {
     return MenuItems;
   }
 
   public static List<String> menuAdminList()
   {
     return MenuAdminItems;
   }
 
   public static List<String> constructionList()
   {
     return UnderConItems;
   }
 
   public static List<String> constructionListLower()
   {
     return UnderConItemsLower;
   }
 
   public static String toTitle(String item)
   {
     return item.substring(0, 1).toUpperCase() + item.substring(1);
   }
   
   public static String toLink(String item)
   {
     return item.replace(' ', '_').toLowerCase();
   }
 
   public static Integer pageCount(int countPerPage, int countTotal)
   {
     return (countTotal + countPerPage - 1) / countPerPage;
   }
 
   public static Integer indexForPageStart(int page, int countPerPage, int countTotal)
   {
     page = Math.max(1, page) - 1;
     int startIndex = Math.min(page * countPerPage, countTotal);
     return startIndex;
   }
 
   public static Integer indexForPageEnd(int page, int countPerPage, int countTotal)
   {
     page = Math.max(1, page) - 1;
     int startIndex = Math.min(page * countPerPage, countTotal);
     int endIndex = Math.min(startIndex + countPerPage, countTotal);
     return endIndex;
   }
 }
