 package ru.skalodrom_rf.web.pages;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import ru.skalodrom_rf.dao.UserDao;
 import ru.skalodrom_rf.model.User;
 
 /**
  */
 public class ActivateUserPage extends BasePage {
     @SpringBean
     UserDao userDao;
 
     public ActivateUserPage(PageParameters parameters) {
         super(parameters);
         try {
             String login = extractParam(parameters, "login");
             final User user = userDao.get(login);
             if (user == null) {
                 throwExeption();
             }
            if (user.getActivationCode() == null) {
                throwExeption();
            }
 
             String activationCode = extractParam(parameters, "activationCode");
            if (!activationCode.equals(user.getActivationCode().toString())) {
                 throwExeption();
             }
             add(new Label("resultMessage", "Аккаунт активирован!"));
             user.setActivationCode(null);
             userDao.saveOrUpdate(user);
 
         } catch (IllegalArgumentException npe) {
             add(new Label("resultMessage", "Аккаунт НЕ активирован!"));
         }
     }
 
     private void throwExeption() {
         throw new IllegalArgumentException("incorrect activation code");
     }
 
     private String extractParam(PageParameters parameters, String s) {
         final Object o = parameters.get(s);
         if (!(o instanceof String[])) {
             throwExeption();
         }
         String[] sa = (String[]) o;
         if (sa.length != 1) {
             throwExeption();
         }
         if (sa[0] == null) {
             throwExeption();
         }
         if (sa[0].trim().length() == 0) {
             throwExeption();
         }
         return sa[0].trim();
     }
 
 
 }
