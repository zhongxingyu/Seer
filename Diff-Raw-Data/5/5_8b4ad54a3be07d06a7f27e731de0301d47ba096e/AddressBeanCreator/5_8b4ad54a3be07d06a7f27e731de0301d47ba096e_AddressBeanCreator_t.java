 package fr.cg95.cvq.util.admin;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.hibernate.transform.Transformers;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import fr.cg95.cvq.business.users.Address;
 import fr.cg95.cvq.dao.hibernate.GenericDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.impl.LocalAuthorityRegistry;
 
 /**
  * @author jsb@zenexity.fr
  *
  */
 public class AddressBeanCreator {
 
     private LocalAuthorityRegistry localAuthorityRegistry;
     private CustomDAO customDAO;
     private String forbiddenCharacters = "[\\s,;]";
     private Pattern streetNumberPattern = Pattern.compile("(\\d{1,5})(.*)");
     private Pattern otherFieldsPattern = Pattern.compile("(.*?)?((\\d{5})(.*?))?");
 
     public static void main(String[] args) {
         ClassPathXmlApplicationContext cpxa = SpringApplicationContextLoader.loadContext(null);
         AddressBeanCreator addressBeanCreator = new AddressBeanCreator();
         addressBeanCreator.localAuthorityRegistry = (LocalAuthorityRegistry)cpxa.getBean("localAuthorityRegistry");
         addressBeanCreator.customDAO = new CustomDAO();
         addressBeanCreator.localAuthorityRegistry.browseAndCallback(addressBeanCreator, "createAddressBeans", new Object[]{args[0], args[1]});
     }
 
     public void createAddressBeans(String table, String field) {
         String localAuthorityName = SecurityContext.getCurrentSite().getName();
         System.out.println(table + "'s addresses migration for : " + localAuthorityName);
         System.out.println();
         List<BeanDTO> errors = new ArrayList<BeanDTO>();
         for (BeanDTO o : customDAO.list(table, field)) {
             System.out.println("\tdealing with address " + o.address);
             if (o.address != null) {
                 Address address = parseAddress(o.address);
                 if (address != null) {
                     System.out.println("\tgenerated address bean : " + address);
                     customDAO.saveOrUpdate(address);
                     o.address = address.getId().toString();
                 } else {
                     errors.add(o);
                     o.address = null;
                 }
                 customDAO.updateAddress(table, field, o);
             }
             System.out.println();
         }
         try {
             File file = 
                 File.createTempFile(localAuthorityName + "_" + table + "_ambiguous_addresses_", ".txt");
             FileOutputStream fos = new FileOutputStream(file);
             for (BeanDTO o : errors) {
                 fos.write((o.id + " : " + o.address + "\n").getBytes());
             }
         } catch(IOException ioe){
             ioe.printStackTrace();
         }
     }
 
     private Address parseAddress(String source) {
         System.out.println("\t\tParsing string : " + source);
         source = source.replaceAll(forbiddenCharacters, " ").trim();
         System.out.println("\t\tSanitized string : " + source);
         String tempStreetNumber = null;
         Matcher streetNumberMatcher = streetNumberPattern.matcher(source);
         if (streetNumberMatcher.matches()) {
             tempStreetNumber = streetNumberMatcher.group(1);
             System.out.println("\t\tExtracting street number : " + tempStreetNumber);
             source = streetNumberMatcher.group(2).trim();
             System.out.println("\t\tRemainging source : " + source);
         }
         Matcher otherFieldsMatcher = otherFieldsPattern.matcher(source);
         if (otherFieldsMatcher.matches()) {
             System.out.println("\t\tSource matches our pattern");
             Address address = new Address();
             address.setStreetNumber(tempStreetNumber);
             if (otherFieldsMatcher.group(1) != null) {
                 address.setStreetName(otherFieldsMatcher.group(1).trim());
                if (address.getStreetName().length() > 114)
                    return null;
             }
             if (otherFieldsMatcher.group(3) != null) {
                 address.setPostalCode(otherFieldsMatcher.group(3).trim());
             }
             if (otherFieldsMatcher.group(4) != null) {
                 address.setCity(otherFieldsMatcher.group(4).trim());
                if (address.getCity().length() > 32)
                    return null;
             }
             return address;
         }
         if ((tempStreetNumber + " " + source).trim().length() < 115) {
             System.out.println("\t\tSource doesn't match our pattern but is small enough, we put it in street name");
             Address address = new Address();
             address.setStreetName((tempStreetNumber + " " + source).trim());
             return address;
         }
         System.out.println("\t\tCouldn't handle address !");
         return null;
     }
 
     private static class CustomDAO extends GenericDAO {
         @SuppressWarnings("unchecked")
         public List<BeanDTO> list(String table, String field) {
             return (List<BeanDTO>)HibernateUtil.getSession()
                 .createSQLQuery("select id, " + field + " as address from " + table)
                 .addScalar("id")
                 .addScalar("address")
                 .setResultTransformer(Transformers.aliasToBean(BeanDTO.class))
                 .list();
         }
         public void updateAddress(String table, String field, BeanDTO o) {
             HibernateUtil.getSession()
                 .createSQLQuery("update " + table + " set " + field + " = :address where id = :id")
                 .setString("address", o.address)
                 .setLong("id", o.id)
                 .executeUpdate();
         }
     }
 
     public static class BeanDTO {
         private Long id;
         public String address;
         public void setId(BigInteger id) {
             this.id = id.longValue();
         }
     }
 }
