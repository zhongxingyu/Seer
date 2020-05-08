 package fr.cg95.cvq.service.payment.external.impl;
 
 import java.io.StringReader;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import au.com.bytecode.opencsv.CSVReader;
 import fr.cg95.cvq.business.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.payment.ExternalDepositAccountItemDetail;
 import fr.cg95.cvq.business.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.payment.ExternalInvoiceItemDetail;
 import fr.cg95.cvq.business.payment.ExternalTicketingContractItem;
 import fr.cg95.cvq.business.payment.external.ExternalApplication;
 import fr.cg95.cvq.business.payment.external.ExternalHomeFolder;
 import fr.cg95.cvq.business.payment.external.ExternalIndividual;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.IndividualRole;
 import fr.cg95.cvq.business.users.RoleType;
import fr.cg95.cvq.business.users.external.HomeFolderMapping;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.jpa.IGenericDAO;
 import fr.cg95.cvq.dao.users.IAdultDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.service.payment.external.IExternalApplicationService;
 import fr.cg95.cvq.service.users.external.IExternalHomeFolderService;
 import fr.cg95.cvq.util.DateUtils;
 
 public class ExternalApplicationService implements IExternalApplicationService {
 
     private static Logger logger = Logger.getLogger(ExternalApplicationService.class);
 
     private IGenericDAO genericDAO;
     private IAdultDAO adultDAO;
 
     private IExternalHomeFolderService externalHomeFolderService;
 
     @Override
     public ExternalApplication getExternalApplicationById(final Long id)
         throws CvqObjectNotFoundException {
         return genericDAO.simpleSelect(ExternalApplication.class).and("id", id).unique();
     }
 
     @Override
     public List<ExternalApplication> allExternalApplications() {
         return genericDAO.simpleSelect(ExternalApplication.class).list();
     }
 
     @Override
     public void createExternalApplication(ExternalApplication externalApplication)
             throws CvqModelException {
         genericDAO.create(externalApplication);
     }
 
     @Override
     public void modifyExternalApplication(ExternalApplication externalApplication)
             throws CvqModelException {
         genericDAO.update(externalApplication);
     }
 
     @Override
     public void deleteExternalApplication(Long id) throws CvqModelException, CvqObjectNotFoundException {
         ExternalApplication ea = getExternalApplicationById(id);
         if (ea.getExternalHomeFolders() != null) {
 //            List<ExternalAccountItem> externalAccountItems = paymentService.getAllExternalAccountItems();
 //            for (ExternalAccountItem eai : externalAccountItems) {
 //                if (eai.getExternalServiceSpecificDataByKey(ExternalServiceUtils.EXTERNAL_APPLICATION_ID_KEY).equals(String.valueOf(ea.getId()))) {
 //                    externalAccountItems.remove(eai);
 //                    eai.setExternalServiceSpecificData(null);
 //                    genericDAO.delete(eai);
 //                }
 //            }
             for (ExternalHomeFolder externalHomeFolder : ea.getExternalHomeFolders()) {
                HomeFolderMapping homeFolderMapping =
                    externalHomeFolderService.getHomeFolderMapping(ea.getLabel(), 
                            externalHomeFolder.getExternalId());
                // a external home folder not yet mapped to a CapDemat account has no home folder mapping
                if (homeFolderMapping != null)
                    externalHomeFolderService.deleteHomeFolderMappings(ea.getLabel(), 
                            homeFolderMapping.getHomeFolderId());
                 genericDAO.delete(externalHomeFolder);
             }
             ea.setExternalHomeFolders(null);
         }
         genericDAO.delete(ea);
     }
 
     /*
         External Individual Id
         Last Name
         First Name
         External Family Account Id
         Address
         Is Responsible
         Email
         Phone
     */
     @Override
     public Map<String, Integer> importHomeFolders(Long externalApplicationId,
             byte[] csvDatas) throws CvqModelException, CvqObjectNotFoundException {
         ExternalHomeFolder ehf;
         ExternalIndividual ei;
         ExternalApplication ea = getExternalApplicationById(externalApplicationId);
         Boolean created = false;
         Map<String,Integer> report = new HashMap<String,Integer>();
         report.put("created", 0);
         report.put("updated", 0);
         try {
             CSVReader csvReader = new CSVReader(new StringReader(new String(csvDatas)),',','"',1);
             for (Object o : csvReader.readAll()) {
                 String[] line = (String[])o;
                 ehf = genericDAO.simpleSelect(ExternalHomeFolder.class)
                         .and("externalId", line[3])
                         .and("externalApplication", ea).unique();
                 if (ehf == null) {
                     ehf = new ExternalHomeFolder(line[3], ea, line[4]);
                     ea.getExternalHomeFolders().add(ehf);
                     genericDAO.create(ehf);
                     modifyExternalApplication(ea);
                     report.put("created",report.get("created") + 1);
                     created = true;
                 }
                 ei = genericDAO.simpleSelect(ExternalIndividual.class)
                         .and("externalId", line[0])
                         .and("externalHomeFolder", ehf).unique();
                 if (ei == null) {
                     ei = new ExternalIndividual(line[0], line[1], line[2], line[6], line[7] ,"1".equals(line[5]) ? true : false);
                     ei.setExternalHomeFolder(ehf);
                     ehf.getIndividuals().add(ei);
                     if (ei.isResponsible())
                         ehf.setAddress(line[4]);
                     genericDAO.create(ei);
                     genericDAO.update(ehf);
                     if (!created)
                         report.put("updated",report.get("updated") + 1);
                 } else if (!ei.getLastName().equals(line[1]) || !ei.getFirstName().equals(line[2]) 
                             || (!ehf.getAddress().equals(StringUtils.upperCase(line[4])) && ei.isResponsible())) {
                     ei.setLastName(line[1]);
                     ei.setFirstName(line[2]);
                     if (ei.isResponsible())
                         ehf.setAddress(line[4]);
                     genericDAO.update(ei);
                     genericDAO.update(ehf);
                     report.put("updated",report.get("updated") + 1);
                 }
                 HibernateUtil.getSession().flush();
             }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            HibernateUtil.getSession().getTransaction().rollback();
            throw  new CvqModelException("external.error.csvImport");
        }
         return report;
     }
 
     /*
         Invoice Id
         Invoice Value
         Invoice Date
         Invoice Label
         Invoice Expiration Date
         Invoice Payment Date
         Invoice Paid
         External Family Account Id
      */
     @Override
     public Map<String, Integer> importInvoices(Long externalApplicationId, String broker, byte[] csvDatas)
             throws CvqModelException, CvqObjectNotFoundException {
         ExternalInvoiceItem eii;
         ExternalHomeFolder ehf;
         ExternalApplication ea = getExternalApplicationById(externalApplicationId);
         Map<String,Integer> report = new HashMap<String,Integer>();
         report.put("created", 0);
         report.put("updated", 0);
         report.put("ignored", 0);
         try {
             CSVReader csvReader = new CSVReader(new StringReader(new String(csvDatas)),',','"',1);
             for (Object o : csvReader.readAll()) {
                 boolean updated = false;
                 String[] line = (String[])o;
                 ehf = genericDAO.simpleSelect(ExternalHomeFolder.class)
                         .and("externalId", line[7])
                         .and("externalApplication", ea).unique();
                 if (ehf == null) {
                     report.put("ignored",report.get("ignored") + 1);
                     continue;
                 }
                 eii = genericDAO.simpleSelect(ExternalInvoiceItem.class)
                         .and("externalItemId", line[0])
                         .and("externalApplicationId", String.valueOf(externalApplicationId)).unique();
                 if (eii != null) {
                     genericDAO.delete(eii);
                     report.put("updated",report.get("updated") + 1);
                     updated = true;
                 }
                 eii = new ExternalInvoiceItem(
                         line[3],
                         Double.valueOf(line[1]),
                         Double.valueOf(line[1]),
                         EXTERNAL_APPLICATION_LABEL,
                         line[0],
                         DateUtils.parseIso(line[2]),
                         DateUtils.parseIso(line[4]),
                         DateUtils.parseIso(line[5]),
                         "1".equals(line[6]) ? true : false,
                         broker);
                 eii.setExternalApplicationId(ea.getId().toString());
                 eii.setExternalHomeFolderId(ehf.getExternalId());
                 genericDAO.create(eii);
                 if (!updated)
                     report.put("created",report.get("created") + 1);
             }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            HibernateUtil.getSession().getTransaction().rollback();
            throw new CvqModelException("external.error.csvImport");
        }
         return report;
     }
 
     /*
         Invoice Id
         Subject Name
         Subject Surname
         Label
         Unit Price
         Quantity
         Value
         External Family Account Id
     */
     @Override
     public Map<String, Integer> importInvoicesDetails(Long externalApplicationId, byte[] csvDatas)
             throws CvqModelException, CvqObjectNotFoundException {
         ExternalInvoiceItemDetail eiid;
         ExternalInvoiceItem eii;
         Map<String,Integer> report = new HashMap<String,Integer>();
         report.put("created", 0);
         report.put("ignored", 0);
         List<ExternalInvoiceItem> allInvoices = genericDAO.simpleSelect(ExternalInvoiceItem.class).list();
         for (ExternalInvoiceItem invoice : allInvoices) {
             invoice.getInvoiceDetails().clear();
             genericDAO.update(invoice);
         }
         try {
             CSVReader csvReader = new CSVReader(new StringReader(new String(csvDatas)),',','"',1);
             for (Object o : csvReader.readAll()) {
                 String[] line = (String[])o;
                 eii = genericDAO.simpleSelect(ExternalInvoiceItem.class)
                         .and("externalItemId", line[0])
                         .and("externalApplicationId", String.valueOf(externalApplicationId)).unique();
                 if (eii == null) {
                     report.put("ignored",report.get("ignored") + 1);
                     continue;
                 }
                 eiid = new ExternalInvoiceItemDetail(
                         line[1],
                         line[2],
                         line[3],
                         Integer.valueOf(line[4]),
                         BigDecimal.valueOf(Long.valueOf(line[5])),
                         Integer.valueOf(line[6]));
                 eii.addInvoiceDetail(eiid);
                 eiid.setExternalInvoiceItem(eii);
                 genericDAO.update(eii);
                 report.put("created",report.get("created") + 1);
             }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            HibernateUtil.getSession().getTransaction().rollback();
            throw new CvqModelException("external.error.csvImport");
        }
         return report;
     }
 
     /*
         Account Id
         Account Value
         Account Date
         Account Label
         External Family Account Id
      */
     @Override
     public Map<String, Integer> importDepositAccounts(Long externalApplicationId, String broker, byte[] csvDatas)
             throws CvqModelException, CvqObjectNotFoundException {
         ExternalDepositAccountItem edai;
         ExternalHomeFolder ehf;
         ExternalApplication ea = getExternalApplicationById(externalApplicationId);
         Map<String,Integer> report = new HashMap<String,Integer>();
         report.put("created", 0);
         report.put("updated", 0);
         report.put("ignored", 0);
         try {
             CSVReader csvReader = new CSVReader(new StringReader(new String(csvDatas)),',','"',1);
             for (Object o : csvReader.readAll()) {
                 boolean updated = false;
                 String[] line = (String[])o;
                 ehf = genericDAO.simpleSelect(ExternalHomeFolder.class)
                         .and("externalId", line[4])
                         .and("externalApplication", ea).unique();
                 if (ehf == null) {
                     report.put("ignored",report.get("ignored") + 1);
                     continue;
                 }
                 edai = genericDAO.simpleSelect(ExternalDepositAccountItem.class)
                         .and("externalItemId", line[0])
                         .and("externalApplicationId", String.valueOf(externalApplicationId)).unique();
                 if (edai != null) {
                     genericDAO.delete(edai);
                     report.put("updated",report.get("updated") + 1);
                     updated = true;
                 }
                 edai = new ExternalDepositAccountItem(
                         line[3],
                         Double.valueOf(line[1]),
                         EXTERNAL_APPLICATION_LABEL,
                         line[0],
                         DateUtils.parseIso(line[2]),
                         Double.valueOf(line[1]),
                         broker);
                 edai.setExternalApplicationId(ea.getId().toString());
                 edai.setExternalHomeFolderId(ehf.getExternalId());
                 genericDAO.create(edai);
                 if (!updated)
                     report.put("created",report.get("created") + 1);
             }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            HibernateUtil.getSession().getTransaction().rollback();
            throw new CvqModelException("external.error.csvImport");
        }
        return report;
     }
 
     /*
         Account Id
         Holder Name
         Holder Surname
         Value
         Payment Date
         Payment Type
         Payment Ack
         Cvq Ack
         External Family Account Id
      */
     @Override
     public Map<String, Integer> importDepositAccountsDetails(Long externalApplicationId,
             byte[] csvDatas) throws CvqModelException, CvqObjectNotFoundException {
         ExternalDepositAccountItemDetail edaid;
         ExternalDepositAccountItem edai;
         Map<String,Integer> report = new HashMap<String,Integer>();
         report.put("created", 0);
         report.put("ignored", 0);
         List<ExternalDepositAccountItem> allDepositAccounts = genericDAO.simpleSelect(ExternalDepositAccountItem.class).list();
         for (ExternalDepositAccountItem depositAccount : allDepositAccounts) {
             depositAccount.getAccountDetails().clear();
             genericDAO.update(depositAccount);
         }
         try {
             CSVReader csvReader = new CSVReader(new StringReader(new String(csvDatas)),',','"',1);
             for (Object o : csvReader.readAll()) {
                 String[] line = (String[])o;
                 edai = genericDAO.simpleSelect(ExternalDepositAccountItem.class)
                         .and("externalItemId", line[0])
                         .and("externalApplicationId", String.valueOf(externalApplicationId)).unique();
                 if (edai == null) {
                     report.put("ignored",report.get("ignored") + 1);
                     continue;
                 }
                 edaid = new ExternalDepositAccountItemDetail(
                         DateUtils.parseIso(line[4]),
                         line[1],
                         line[2],
                         Integer.valueOf(line[3]),
                         line[5],
                         line[0],
                         null);
                 edai.addAccountDetail(edaid);
                 edaid.setExternalDepositAccountItem(edai);
                 genericDAO.update(edai);
                 report.put("created",report.get("created") + 1);
             }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            HibernateUtil.getSession().getTransaction().rollback();
            throw new CvqModelException("external.error.csvImport");
        }
        return report;
     }
 
     /*
         Contract Id
         Contract Value
         Contract Date
         Contract Label
         Buy Price
         Min Buy
         Max Buy
         External Family Account Id
         External Individual Id
      */
     @Override
     public Map<String, Integer> importTicketingContracts(Long externalApplicationId, String broker, byte[] csvDatas)
             throws CvqModelException, CvqObjectNotFoundException {
         ExternalTicketingContractItem etci;
         ExternalHomeFolder ehf;
         ExternalIndividual ei;
         ExternalApplication ea = getExternalApplicationById(externalApplicationId);
         Map<String,Integer> report = new HashMap<String,Integer>();
         report.put("created", 0);
         report.put("updated", 0);
         report.put("ignored", 0);
         try {
             CSVReader csvReader = new CSVReader(new StringReader(new String(csvDatas)),',','"',1);
             for (Object o : csvReader.readAll()) {
                 boolean updated = false;
                 String[] line = (String[])o;
                 ehf = genericDAO.simpleSelect(ExternalHomeFolder.class)
                         .and("externalId", line[7])
                         .and("externalApplication", ea).unique();
                 if (ehf == null) {
                     report.put("ignored",report.get("ignored") + 1);
                     continue;
                 }
                 ei = genericDAO.simpleSelect(ExternalIndividual.class)
                         .and("externalId",line[8])
                         .and("externalHomeFolder", ehf).unique();
                 if (ei == null) {
                     report.put("ignored",report.get("ignored") + 1);
                     continue;
                 }
                 etci = genericDAO.simpleSelect(ExternalTicketingContractItem.class)
                         .and("externalItemId", line[0])
                         .and("externalApplicationId", String.valueOf(externalApplicationId)).unique();
                 if (etci != null) {
                     genericDAO.delete(etci);
                     report.put("updated",report.get("updated") + 1);
                     updated = true;
                 }
                 etci = new ExternalTicketingContractItem(
                         line[3],
                         Double.valueOf(line[1]),
                         EXTERNAL_APPLICATION_LABEL,
                         line[0],
                         null,
                         Double.valueOf(line[4]),
                         Integer.valueOf(line[5]),
                         Integer.valueOf(line[6]),
                         DateUtils.parseIso(line[2]),
                         broker);
                 etci.setExternalApplicationId(ea.getId().toString());
                 etci.setExternalHomeFolderId(ehf.getExternalId());
                 etci.setExternalIndividualId(line[8]);
                 genericDAO.create(etci);
                 if (!updated)
                     report.put("created",report.get("created") + 1);
             }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            HibernateUtil.getSession().getTransaction().rollback();
            throw new CvqModelException("external.error.csvImport");
        }
         return report;
     }
 
     @Override
     public List<ExternalHomeFolder> getHomeFolders(Integer offset, Integer max)
             throws CvqException {
         return genericDAO.simpleSelect(ExternalHomeFolder.class).offset(offset).max(max).list();
     }
 
     @Override
     public ExternalHomeFolder getHomeFolder(Long id) throws CvqException {
         return genericDAO.simpleSelect(ExternalHomeFolder.class).and("id", id).unique();
     }
 
     @Override
     public List<ExternalHomeFolder> getHomeFolders(Long externalApplicationId, Integer offset, Integer max)
             throws CvqException {
         return genericDAO.simpleSelect(ExternalHomeFolder.class)
                 .and("externalApplication", getExternalApplicationById(externalApplicationId))
                 .max(max).offset(offset).list();
     }
 
     @Override
     public List<ExternalInvoiceItem> getExternalInvoiceItems(String externalApplicationId) {
         return genericDAO.simpleSelect(ExternalInvoiceItem.class)
                 .and("externalApplicationId", externalApplicationId)
                 .list();
     }
 
     @Override
     public List<ExternalDepositAccountItem> getExternalDepositAccountItems(String externalApplicationId) {
         return genericDAO.simpleSelect(ExternalDepositAccountItem.class)
                 .and("externalApplicationId", externalApplicationId)
                 .list();
     }
 
     @Override
     public List<ExternalTicketingContractItem> getExternalTicketingContractItems(String externalApplicationId) {
         return genericDAO.simpleSelect(ExternalTicketingContractItem.class)
                 .and("externalApplicationId", externalApplicationId)
                 .list();
     }
 
     @Override
     public void modifyHomeFolder(ExternalHomeFolder eh) throws CvqException {
         genericDAO.update(eh);
     }
 
     @Override public List<Adult> matchAdults(Long externalHomeFolderId) {
         ExternalHomeFolder ehf = (ExternalHomeFolder) genericDAO.findById(ExternalHomeFolder.class, externalHomeFolderId);
         ExternalIndividual ehfr =  ehf.getResponsible();
 
         if (ehfr == null)
             return new ArrayList<Adult>();
 
         Map<String,String> parameters = new HashMap<String, String>();
         parameters.put("firstName", ehfr.getFirstName());
         parameters.put("lastName", ehfr.getLastName());
         parameters.put("email", ehfr.getEmail());
         parameters.put("homePhone", ehfr.getHomePhone());
         parameters.put("address", ehf.getAddress());
         List<Adult> adults = adultDAO.matchAdults(parameters);
         List<Adult> responsibles = new ArrayList<Adult>();
         for (Adult adult : adults) {
             for (IndividualRole ir : adult.getIndividualRoles())
                 if (ir.getRole().equals(RoleType.HOME_FOLDER_RESPONSIBLE))
                     responsibles.add(adult);
         }
         return responsibles;
     }
 
     public void setGenericDAO(IGenericDAO genericDAO) {
         this.genericDAO = genericDAO;
     }
 
     public void setAdultDAO(IAdultDAO adultDAO) {
         this.adultDAO = adultDAO;
     }

    public void setExternalHomeFolderService(IExternalHomeFolderService externalHomeFolderService) {
        this.externalHomeFolderService = externalHomeFolderService;
    }
 }
