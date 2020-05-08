 package org.glite.authz.pap.papmanagement;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.glite.authz.pap.common.Pap;
 import org.glite.authz.pap.common.utils.Utils;
 import org.glite.authz.pap.distribution.DistributionConfiguration;
 import org.glite.authz.pap.distribution.DistributionConfigurationException;
 import org.glite.authz.pap.distribution.DistributionModule;
 import org.glite.authz.pap.repository.dao.DAOFactory;
 import org.glite.authz.pap.repository.dao.PapDAO;
 import org.glite.authz.pap.repository.exceptions.AlreadyExistsException;
 import org.glite.authz.pap.repository.exceptions.NotFoundException;
 import org.glite.authz.pap.repository.exceptions.RepositoryException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class provides methods to manage {@link Pap} objects (i.e. add, update, delete, etc). It
  * also provides methods to manage the <code>Pap</code> ordering. All of these methods affects both
  * the repository and the configuration.
  * <p>
  * Use the {@link PapManager#initialize()} method before doing anything with this class.
  * <p>
  * The PAP service has always one (special) pap defined. This pap is commonly identified in the code
  * with the word <i>default</i>. The alias of this special pap is defined by the constant
  * {@link Pap#DEFAULT_PAP_ALIAS} (its value is {@value Pap#DEFAULT_PAP_ALIAS}). The <i>default</i>
  * pap is local and public.
  * <p>
  * <b>Paps ordering</b><br>
  * For the policy evaluation process (performed by the <i>PDP</i>) the oder of the policies matters.
  * Therefore it is possible to specify an ordering for the <code>Pap</code> objects (which are
  * containers of policies). The policies of <code>Pap</code> <i>A</i> are evaluated before the
  * policies of <code>Pap</code> <i>B</i> if <i>A</i> comes before <i>B</i> in the order. There are
  * two points concerning the pap ordering that must be kept in mind:<br>
  * 1. if the <i>defdault</i> pap is <b>not</b> listed in the pap ordering then it is automatically
  * placed as the first pap in the order;<br>
  * 2. if the pap ordering does not contains all the defined paps then the paps not listed in the
  * order comes after (except the <i>default</i> pap, see point 1).
  * <p>
  * <b>Repository and configuration</b><br>
  * The information associated to a <code>Pap</code> object are stored both in the repository and in
  * the configuration. Since the configuration can be changed off-line (when the PAP service is not
  * running), synchronization between the configuration and the repository is needed at startup time.
  * See the method {@link PapManager#synchronizeRepositoryWithConfiguration()}.<br>
  * Information like the polling interval and the pap ordering are only in the configuration.
  * 
  * @see Pap
  * @see PapContainer
  * @see DistributionConfiguration
  * @see PapDAO
  * @see DistributionModule
  */
 public class PapManager {
 
     private static PapManager instance = null;
     private static final Logger log = LoggerFactory.getLogger(PapManager.class);
 
     private String[] configurationPapOrdering;
     private DistributionConfiguration distributionConfiguration;
     private PapDAO papDAO;
 
     /**
      * Constructor.
      * <p>
      * The <i>default</i> pap is created if it doesn't exists.
      */
     private PapManager() {
 
         papDAO = DAOFactory.getDAOFactory().getPapDAO();
 
         createDefaultPapIfNotExists();
 
         distributionConfiguration = DistributionConfiguration.getInstance();
 
         configurationPapOrdering = distributionConfiguration.getPapOrdering();
 
         synchronizeRepositoryWithConfiguration();
     }
 
     public static PapManager getInstance() {
 
         if (instance == null) {
             throw new PapManagerException("Please initialize configuration before calling the instance method!");
         }
         return instance;
     }
 
     /**
      * Initialization method. The main purpose of this method is to create (through the constructor)
      * the <i>default</i> pap it doesn't exits.
      */
     public static void initialize() {
         if (instance == null) {
             instance = new PapManager();
         }
     }
 
     /**
      * Add a pap to the repository and to the configuration.
      * 
      * @param pap the pap to be added.
      * 
      * @throws PapManagerException if the alias of the pap is equal to {@link Pap#DEFAULT_PAP_ALIAS}
      *             .
      * @throws AlreadyExistsException if the alias of the pap already exists.
      */
     public void addPap(Pap pap) {
 
         String papAlias = pap.getAlias();
 
         if (Pap.DEFAULT_PAP_ALIAS.equals(papAlias)) {
             throw new PapManagerException("Forbidden alias: " + papAlias);
         }
 
         if (exists(papAlias)) {
             throw new AlreadyExistsException("pap \"" + papAlias + "\" already exists");
         }
 
         distributionConfiguration.savePap(pap);
         papDAO.store(pap);
 
         PapContainer papContainer = new PapContainer(pap);
         papContainer.createRootPolicySet();
     }
 
     /**
      * Delete a pap from the configuration and from the repository.
      * 
      * @param papAlias the alias of the pap to remove.
      * 
      * @throws NotFoundException if a pap with the given alias does not exists.
      * @throws PapManagerException if the alias is equal to {@link Pap#DEFAULT_PAP_ALIAS}.
      */
     public void deletePap(String papAlias) {
 
         if (Pap.DEFAULT_PAP_ALIAS.equals(papAlias)) {
             throw new PapManagerException("Deleting the default pap is not allowed");
         }
 
         distributionConfiguration.removePap(papAlias);
         papDAO.delete(papAlias);
 
         // pap ordering could be modified.
         configurationPapOrdering = distributionConfiguration.getPapOrdering();
     }
 
     /**
      * Checks for the existence of a pap.
      * 
      * @param papAlias alias of the pap.
      * @return <code>true</code> if a pap with the given alias exists, <code>false</code> otherwise.
      */
     public boolean exists(String papAlias) {
         return papDAO.exists(papAlias);
     }
 
     /**
      * Returns a <code>List</code> of all the defined paps.<br>
      * The paps in the list are ordered following the pap ordering as specified in the
      * configuration.
      * 
      * @return an <i>ordered</i> <code>List</code> of all the defined paps.
      */
     public List<Pap> getAllPaps() {
         return getPapList();
     }
 
     /**
      * Returns a <code>List</code> of all the defined local paps.<br>
      * The paps in the list are ordered following the pap ordering as specified in the
      * configuration.
      * 
      * @return an <i>ordered</i> <code>List</code> of all the defined local paps.
      */
     public List<Pap> getLocalPaps() {
 
         List<Pap> resultList = new LinkedList<Pap>();
 
         for (Pap pap : getPapList()) {
             if (pap.isLocal()) {
                 resultList.add(pap);
             }
         }
         return resultList;
     }
 
     /**
      * Returns a <code>List</code> of all the defined remote paps.<br>
      * The paps in the list are ordered following the pap ordering as specified in the
      * configuration.
      * 
      * @return an <i>ordered</i> <code>List</code> of all the defined remote paps.
      */
     public List<Pap> getRemotePaps() {
 
         List<Pap> remotePapList = new LinkedList<Pap>();
 
         for (Pap pap : getPapList()) {
             if (pap.isRemote()) {
                 remotePapList.add(pap);
             }
         }
         return remotePapList;
     }
 
     /**
      * Returns the pap identified by the given alias.
      * 
      * @param papAlias the alias of the pap to retrieve.
      * @return the <code>Pap</code> associated to the given alias.
      * 
      * @throws NotFoundException if a pap with the given alias was not found.
      */
     public Pap getPap(String papAlias) {
         return papDAO.get(papAlias);
     }
 
     /**
      * Convenience method to get a <code>PapContainer</code> from a pap alias.
      * 
      * @param papAlias alias of the pap.
      * @return the <code>PapContainer</code> of the pap with the given alias.
      * 
      * @throws NotFoundException if a pap with the given alias was not found.
      */
     public PapContainer getPapContainer(String papAlias) {
         return new PapContainer(getPap(papAlias));
     }
 
     /**
      * Returns the pap ordering as defined in the configuration.
      * 
      * @return the pap ordering as defined in the configuration.
      */
     public String[] getPapOrdering() {
         return configurationPapOrdering;
     }
 
     /**
      * Returns a <code>List</code> of all the defined public paps.<br>
      * The paps in the list are ordered following the pap ordering as specified in the
      * configuration.
      * 
      * @return an <i>ordered</i> <code>List</code> of all the defined public paps.
      */
     public List<Pap> getPublicPaps() {
 
         List<Pap> resultList = new LinkedList<Pap>();
 
         for (Pap pap : getPapList()) {
             if (pap.isVisibilityPublic()) {
                 resultList.add(pap);
             }
         }
         return resultList;
     }
 
     /**
      * Set the pap ordering in the configuration.<br>
      * If the given array is <code>null</code> or <code>empty</code> then the previous order (if
      * any) is cleared.
      * 
      * @param aliasArray array of pap aliases defining the new pap ordering (can be
      *            <code>null</code> or <code>empty</code>).
      * 
      * @throws DistributionConfigurationException if the new ordering contains duplicated or unknown
      *             aliases.
      */
     public void setPapOrdering(String[] aliasArray) {
         distributionConfiguration.savePapOrdering(aliasArray);
 
         // update to the new order
         configurationPapOrdering = distributionConfiguration.getPapOrdering();
     }
 
     /**
      * Update the information associated to a pap.
      * 
      * @param newPap update the information of the pap with the same alias of the given one.
      * 
      * @throws NotFoundException if a pap with the same alias of the given one was not found.
      * @throws PapManagerException if the given pap is <code>null</code>.
      */
     public void updatePap(Pap newPap) {
 
         if (newPap == null) {
             throw new PapManagerException("pap cannot be null");
         }
 
         String alias = newPap.getAlias();
 
         if (Pap.DEFAULT_PAP_ALIAS.equals(alias)) {
             updateDefaultPap(newPap);
             return;
         }
 
         Pap oldPap = papDAO.get(alias);
 
         // id (and alias) cannot change
         newPap.setId(oldPap.getId());
 
         papDAO.update(newPap);
     }
 
     /**
      * Builds a list of alias of all the defined paps that follows the pap ordering defined in the
      * configuration.
      * 
      * @return a list of alias of all the defined paps that follows the pap ordering defined in the
      *         configuration.
      */
     private List<String> buildOrderedAliasList() {
 
         List<String> repositoryAliasList = papDAO.getAliasList();
 
         int configurationArraySize = configurationPapOrdering.length;
 
         if (configurationArraySize > repositoryAliasList.size()) {
             throw new PapManagerException("BUG: configuration contains more paps than the repository");
         }
 
         int defaultPapAliasIdx = repositoryAliasList.indexOf(Pap.DEFAULT_PAP_ALIAS);
 
         if (defaultPapAliasIdx == -1) {
             throw new PapManagerException("BUG: default pap does not exist in the repository");
         }
 
         /* enforce the order specified in the configuration file */
 
         List<String> configurationAliasOrderedList = new LinkedList<String>();
 
         // if the default pap is not specified in the order in configuration then it goes for first
         if (Utils.indexOf(Pap.DEFAULT_PAP_ALIAS, configurationPapOrdering) == -1) {
             configurationAliasOrderedList.add(Pap.DEFAULT_PAP_ALIAS);
         }
 
         for (int i = 0; i < configurationArraySize; i++) {
             configurationAliasOrderedList.add(configurationPapOrdering[i]);
         }
 
         // follow the order specified in the configuration file
         for (int i = 0; i < configurationAliasOrderedList.size(); i++) {
             String alias = configurationAliasOrderedList.get(i);
 
             int aliasIndex = repositoryAliasList.indexOf(alias);
 
             if (aliasIndex == -1) {
                 throw new PapManagerException("BUG: initialization error. pap defined in the configuration is not in the repository");
             }
 
             Collections.swap(repositoryAliasList, aliasIndex, i);
         }
 
         return repositoryAliasList;
     }
 
     /** Create the <i>default</i> pap if it doesn't exist. */
     private void createDefaultPapIfNotExists() {
 
         Pap defaultPap;
 
         if (!papDAO.exists(Pap.DEFAULT_PAP_ALIAS)) {
 
             defaultPap = Pap.makeDefaultPAP();
             papDAO.store(defaultPap);
 
         } else {
             defaultPap = papDAO.get(Pap.DEFAULT_PAP_ALIAS);
         }
 
         PapContainer defaultPapContainer = getPapContainer(Pap.DEFAULT_PAP_ALIAS);
 
         // check if the root policy set exists
         if (defaultPapContainer.hasPolicySet(defaultPapContainer.getRootPolicySetId())) {
             return;
         }
 
         // create the root policy set
         defaultPapContainer.createRootPolicySet();
     }
 
     /**
      * Returns a <code>List</code> of all the defined paps.<br>
      * The paps in the list are ordered following the pap ordering as specified in the
      * configuration.
      * 
      * @return an <i>ordered</i> <code>List</code> of all the defined paps.
      */
     private List<Pap> getPapList() {
         List<String> aliasOrderedList = buildOrderedAliasList();
         List<Pap> papList = new ArrayList<Pap>(aliasOrderedList.size());
 
         for (String alias : aliasOrderedList) {
             papList.add(papDAO.get(alias));
         }
         return papList;
     }
 
     /**
      * The information associated to a <code>Pap</code> object are stored both in the repository and
      * in the configuration. Since the configuration can be changed off-line (when the PAP service
      * is not running), synchronization between the configuration and the repository is needed. This
      * method synchronize these information doing the following steps:<br>
      * 1. add to the repository all the paps defined in the configuration that are not present in
      * the repository;<br>
      * 2. if a pap is defined both in the repository and in the configuration then update the
      * information in the repository with the one found in the configuration (if they differs);<br>
      * 3. delete from the repository the paps that are not in the configuration.
      * 
      */
     private void synchronizeRepositoryWithConfiguration() {
 
         List<Pap> papListFromConfiguration = DistributionConfiguration.getInstance().getPapList();
 
         if (papListFromConfiguration.isEmpty()) {
             log.info("No remote paps has been defined");
         }
 
         // make sure that all paps defined in the configuration are also in the repository
         for (Pap papFromConfiguration : papListFromConfiguration) {
             try {
 
                 String papAlias = papFromConfiguration.getAlias();
 
                 Pap papFromRepository = papDAO.get(papAlias);
 
                 if (papFromConfiguration.equals(papFromRepository)) {
                     continue;
                 }
 
                 // paps in the repository but must be updated with the information of the paps found
                 // in the configuration. Since the cache of a pap must be removed we can delete the
                 // pap and store it again
                 log.info("Settings for pap \"" + papAlias + "\" has been updated. Invalidating cache");
                 papDAO.delete(papAlias);
 
             } catch (NotFoundException e) {
                 // the pap is not in the repository therefore go on and store it
             }
 
             papDAO.store(papFromConfiguration);
             PapContainer papContainer = new PapContainer(papFromConfiguration);
             papContainer.createRootPolicySet();
         }
 
         // remove from the repository paps that are not in the configuration
         for (String alias : papDAO.getAliasList()) {
 
             // do not remove the default pap
             if (alias.equals(Pap.DEFAULT_PAP_ALIAS)) {
                 continue;
             }
 
             boolean notFoundInConfiguration = true;
             for (Pap papFromConfiguration : papListFromConfiguration) {
                 if (alias.equals(papFromConfiguration.getAlias())) {
                     notFoundInConfiguration = false;
                     break;
                 }
             }
 
             if (notFoundInConfiguration) {
                 papDAO.delete(alias);
                 log.info("Removed pap \"" + alias
                         + "\" from the repository because it hasn't been found in the configuration file.");
             }
         }
     }
 
     /**
      * Updates the <i>default</i> pap.
      * 
      * @param newDefaultPap
      */
     private void updateDefaultPap(Pap newDefaultPap) {
 
         if (!Pap.DEFAULT_PAP_ALIAS.equals(newDefaultPap.getAlias())) {
             throw new RepositoryException("Invalid alias for default pap. Cannot perform updateDefaultPap request.");
         }
 
         Pap oldDefaultPap = getPap(Pap.DEFAULT_PAP_ALIAS);
 
         newDefaultPap.setId(oldDefaultPap.getId());
 
         papDAO.update(newDefaultPap);
     }
 }
