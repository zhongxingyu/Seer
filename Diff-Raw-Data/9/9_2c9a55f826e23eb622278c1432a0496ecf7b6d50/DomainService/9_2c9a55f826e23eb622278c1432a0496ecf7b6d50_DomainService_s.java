 /**
  * ESUP-Portail Blank Application - Copyright (c) 2006 ESUP-Portail consortium
  * http://sourcesup.cru.fr/projects/esup-opiR1
  */
 package org.esupportail.opi.domain;
 
 
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.esupportail.commons.exceptions.ConfigException;
 import org.esupportail.commons.services.application.Version;
 import org.esupportail.commons.services.ldap.LdapUser;
 import org.esupportail.opi.domain.beans.NormeSI;
 import org.esupportail.opi.domain.beans.parameters.AutoListPrincipale;
 import org.esupportail.opi.domain.beans.parameters.Campagne;
 import org.esupportail.opi.domain.beans.parameters.PieceJustificative;
 import org.esupportail.opi.domain.beans.parameters.TypeDecision;
 import org.esupportail.opi.domain.beans.parameters.VetAutoLp;
 import org.esupportail.opi.domain.beans.parameters.accessRight.Profile;
 import org.esupportail.opi.domain.beans.pilotage.ArchiveTask;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.LinkTrtCmiCamp;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.references.rendezvous.CalendarRDV;
 import org.esupportail.opi.domain.beans.references.rendezvous.Horaire;
 import org.esupportail.opi.domain.beans.references.rendezvous.IndividuDate;
 import org.esupportail.opi.domain.beans.references.rendezvous.JourHoraire;
 import org.esupportail.opi.domain.beans.references.rendezvous.TrancheFermee;
 import org.esupportail.opi.domain.beans.references.rendezvous.VetCalendar;
 import org.esupportail.opi.domain.beans.user.Adresse;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.User;
 import org.esupportail.opi.domain.beans.user.candidature.Avis;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 import org.esupportail.opi.domain.beans.user.candidature.MissingPiece;
 import org.esupportail.opi.domain.beans.user.candidature.VersionEtpOpi;
 import org.esupportail.opi.domain.beans.user.indcursus.IndBac;
 import org.esupportail.opi.domain.beans.user.indcursus.IndCursus;
 import org.esupportail.opi.domain.beans.user.indcursus.IndCursusScol;
 import org.esupportail.opi.domain.beans.user.situation.IndSituation;
 import org.esupportail.opi.utils.primefaces.PFFilters;
 
 import fj.P2;
 import fj.data.Option;
 import fj.data.Stream;
 
 
 
 /**
  * The domain service interface.
  */
 public interface DomainService extends Serializable {
 
 	//////////////////////////////////////////////////////////////
 	// OBJECT
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * Permet de rendre persistant un proxy contenu dans args.
 	 * @param args
 	 * @param proxy
 	 * @param proxyClass
 	 */
 	void initOneProxyHib(Object args, Object proxy, Class< ? > proxyClass);
 	
 	/**
 	 * Return the student code regex.
 	 * @return the codStudentRegex
 	 */
 	String getCodStudentRegex();
 	/**
 	 * Return the student code regex.
 	 * @return the codStudentRegex
 	 */
 	
 	String getCodStudentPattern();
 	//////////////////////////////////////////////////////////////
 	// User
 	//////////////////////////////////////////////////////////////
 
 	/**
 	 * Return the individu that have the given numDosOpi.
 	 * @param numDosOpi
 	 * @param dateNaissance 
 	 * @return the individu
 	 */
 	Individu getIndividu(String numDosOpi, Date dateNaissance);
 	
 	/**
 	 * Return the individual that have the given codEtu.
 	 * @param codEtu
 	 * @return Individu
 	 */
 	Individu getIndividuCodEtu(String codEtu);
 	
 	/**
 	 * Return the individual for the given INE.
 	 * @param codeNNE
 	 * @param cleNNE
 	 * @return Individu
 	 */
 	Individu getIndividuINE(String codeNNE, String cleNNE);
 	
 	/**
 	 * withWishes if null return allIndividus in use.
 	 * @param withWishes if false Return the individuals without vows.
 	 * @param codeTypeTrt if withWishes true or null return the individuals with vows for the treatment type.
 	 * @return List< Individu>
 	 */
 	List<Individu> getIndividusWishes(Boolean withWishes, String codeTypeTrt);
 	
 	/**
 	 * Return the individu that have the given mail.
 	 * @param mail 
 	 * @return Individu
 	 */
 	Individu getIndividuByMail(String mail);
 
     /**
      * Return the ids of the {@link Individu}s whom {@link IndVoeu}s are managed by {@code commission}
      *
      * @param commission The {@link Commission} managing the {@link IndVoeu}s
      * @param validate Whether the {@link IndVoeu}s are valid and in service
      * @param listeRI The registration schemes of the {@link Campagne} the {@link IndVoeu}s belong to
      */
     List<String> getIndsIds(Commission commission, Boolean validate, Set<Integer> listeRI);
 
     /**
      * <b>Eagerly</b> (in hibernate sense) fetch an {@link Individu} from the DB by its id
      * @param id The id (i.e 'numDossierOpi') of the {@link Individu}
      * @param onlyValidWishes wether the {@link IndVoeu}s of the {@link Individu} should be filtered
      *                        with regard to the validity of their Avis(cf. {@link Avis#validation}).
      *                        A {@link Option#none()} value means no filtering.
      * @return The {@link Individu} of 'numDossierOpi' {@code id}
      */
     Individu fetchIndById(String id, Option<Boolean> onlyValidWishes);
 
 	/**
 	 * Return all individus with a codeEtu.
 	 * @return List< Individu>
 	 */
 	List<Individu> getAllIndividusEtu();
 	
 	/**
 	 * Return the individual with the wishes on this vet.
 	 * @param trt
 	 * @param state state of wish. (can be null). 
 	 * @return Individu
 	 */
 	List<Individu> getIndividusTrtCmiState(TraitementCmi trt, String state);
 	
 	/**
 	 * Return the individuqs for theTraitementCmi.
 	 * @param trt
 	 * @return Individu
 	 */
 	List<Individu> getIndividusTrtCmi(TraitementCmi trt);
 	
 	
 	/**
 	 * Return all individus of the campagne.
 	 * @param campagne
 	 * @return List< Individu>
 	 */
 	List<Individu> getIndividusByCampagne(Campagne campagne, Boolean temSve);
 	
 	/**
 	 * Return the individuals with the search module.
 	 * @param nomPatronymique
 	 * @param prenom
 	 * @param dateNaissance
 	 * @param codPayNaissance
 	 * @param codDepPaysNaissance
 	 * @return List
 	 */
 	List<Individu> getIndividuSearch(String nomPatronymique, String prenom,
 			Date dateNaissance, String codPayNaissance, String codDepPaysNaissance);
 	
 	/**
 	 * Retrieves a slice of {@link Individu} from the DB
 	 * 
 	 */
 	P2<Long, Stream<Individu>> sliceOfInd(PFFilters pfFilters,
                                           List<TypeDecision> typesDec,
                                           Option<Boolean> validWish,
                                           Option<Boolean> treatedWish,
                                           Option<Date> wishCreation,
                                           Collection<TypeTraitement> typeTrtmts,
                                           Option<Set<TraitementCmi>> trtCmis,
                                           Set<Integer> listCodesRI,
                                           Option<List<String>> typesTrtVet);
 
 	/**
 	 * @param individu
 	 * @return  false if the individual does not exist in the base
 	 * true otherwise
 	 */
 	Boolean canInsertIndividu(Individu individu);
 
 	/**
 	 * @param uid may be null
 	 * @return the Gestionnaire instance that corresponds to a uid.
 	 */
 	Gestionnaire getManager(String uid);
 	
 	/**
 	 * @param currentUserId may be null
 	 * @return the User.
 	 */
 	User getUser(String currentUserId);
 	
 	/**
 	 * Return managers if they ara not expiry.  
 	 * @param date if null return all managers
 	 * @return the list of all the managers.
 	 * @deprecated
 	 */
 	@Deprecated
 		//( le 27/03/2009) 
 	List<Gestionnaire> getManagers(Date date);
 
 	/**
 	 * Return not expired managers
 	 * @return the list of all the managers.
 	 */
 	List<Gestionnaire> getManagers();
 
 	/**
 	 * @param commission
 	 * @return the list of all the managers for the commission.
 	 */
 	List<Gestionnaire> getManagersByCmi(Commission commission);
 
 
 	/**
 	 * Update a user.
 	 * @param user
 	 */
 	void updateUser(User user);
 	
 	/**
 	 * Delete a user.
 	 * @param user
 	 */
 	void deleteUser(User user);
 	
 	/**
 	 * Delete the list of users.
 	 * @param users
 	 */
 	void deleteUserList(List<User> users);
 
 	/**
 	 * Add a user.
 	 * @param user
 	 */
 	void addUser(User user);
 	
 	/**
 	 * Only for Individu.
 	 * @param individu
 	 * @return true if email individu id unique
 	 */
 	Boolean emailIsUnique(Individu individu);
 	
 	/**
 	 * Test if gestionnaire login is unique.
 	 * @param gestionnaire
 	 * @return Boolean : true if login is unique
 	 */
 	Boolean gestionnaireLoginIsUnique(Gestionnaire gestionnaire);
 
 	/**
 	 * Find if the gestionnaire as rights on a sutdent.
 	 * @param lesVoeux : les voeux of the student
 	 * @param lesCommissions : les commssions oe le gestionnaire e des droits
 	 * @return Booelan : true if the gestionaire as rights false if not
 	 */
 	Boolean hasGestionnaireRightsOnStudent(Set<IndVoeu> lesVoeux,
                                            Set<Commission> lesCommissions);
 	
 
 	//////////////////////////////////////////////////////////////
 	// IndCursus
 	//////////////////////////////////////////////////////////////
 	/**
 	 * Add a indCursus.
 	 * @param indCursus
 	 */
 	void addIndCursus(IndCursus indCursus);
 
 	/**
 	 * Update a indCursus.
 	 * @param indCursus
 	 */
 	void updateIndCursus(IndCursus indCursus);
 
 	/**
 	 * Delete a indCursus.
 	 * @param indCursus
 	 */
 	void deleteIndCursus(IndCursus indCursus);
 	
 	//////////////////////////////////////////////////////////////
 	// IndCursusScol
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * Add a indCursusScol.
 	 * @param indCursusScol
 	 */
 	void addIndCursusScol(IndCursusScol indCursusScol);
 
 	/**
 	 * Update a indCursusScol.
 	 * @param indCursusScol
 	 */
 	void updateIndCursusScol(IndCursusScol indCursusScol);
 
 	/**
 	 * Delete a indCursusScol.
 	 * @param indCursusScol
 	 */
 	void deleteIndCursusScol(IndCursusScol indCursusScol);
 
 	
 	//////////////////////////////////////////////////////////////
 	// IndBac
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * Add a indBac.
 	 * @param indBac
 	 */
 	void addIndBac(IndBac indBac);
 
 	/**
 	 * Update a indBac.
 	 * @param indBac
 	 */
 	void updateIndBac(IndBac indBac);
 
 	/**
 	 * Delete a indBac.
 	 * @param indBac
 	 */
 	void deleteIndBac(IndBac indBac);
 
 	
 	//////////////////////////////////////////////////////////////
 	// Adresse
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * Add a adresse.
 	 * @param adresse
 	 */
 	void addAdresse(Adresse adresse);
 
 	/**
 	 * Update a adresse.
 	 * @param adresse
 	 */
 	void updateAdresse(Adresse adresse);
 
 	
 	/**
 	 * Set the information of a Gestionnaire from a ldapUser.
 	 * @param gest 
 	 * @param ldapUser 
 	 * @return Gestionnaire
 	 */
 	Gestionnaire setUserInfo(Gestionnaire gest, LdapUser ldapUser);
 	
 	/**
 	 * Add the first admin to the creation of the database.
 	 * @param firstAdministratorId
 	 * @param admin Profile
 	 */
 	void addFirstAdmin(String firstAdministratorId, Profile admin);
 	
 	
 	//////////////////////////////////////////////////////////////
 	// Voeu
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * Add a Voeu.
 	 * @param voeu
 	 */
 	void addIndVoeu(IndVoeu voeu);
 	
 	/**
 	 * Delete a Voeu.
 	 * @param voeu
 	 */
 	void deleteIndVoeu(IndVoeu voeu);
 	
 	/**
 	 * Update a Voeu.
 	 * @param voeu
 	 */
 	void updateIndVoeu(IndVoeu voeu);
 
 	/**
 	 * @return the list of accepted vows.
 	 */
 	List<IndVoeu> getVoeuxAcceptes();
 	
 	/**
 	 * Update the list of IndVoeu by setting the temEnServ.
 	 * @param link
 	 * @param temEnServ
 	 */
 	void updateIndVoeuTemEnServ(LinkTrtCmiCamp link, boolean temEnServ);
 	
 	//////////////////////////////////////////////////////////////
 	// Deep links
 	//////////////////////////////////////////////////////////////
 
 	/**
 	 * @param local 
 	 * @param params 
 	 * @return a link to a page.
 	 */
 	String getUrl(
 			boolean local, 
 			Map<String, String> params);
 
 	/**
 	 * @param local true if the URL is built for local users, false otherwise.
 	 * @param etape 
 	 * @param vet 
 	 * @return a link to the about page.
 	 */
 	String getFormationUrl(boolean local, String etape, String vet);
 
 
 
 	//////////////////////////////////////////////////////////////
 	// VersionManager
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * @return the database version.
 	 * @throws ConfigException when the database is not initialized
 	 */
 	Version getDatabaseVersion() throws ConfigException;
 	
 	/**
 	 * Set the database version.
 	 * @param version 
 	 */
 	void updateDatabaseVersion(Version version);
 	
 	/**
 	 * Set the database version.
 	 * @param version 
 	 */
 	void updateDatabaseVersion(String version);
 	
 	//////////////////////////////////////////////////////////////
 	// NormeSI
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * Set the attributes required for an addition.
 	 * @param norme 
 	 * @param codeCurrentUser 
 	 * @return NormeSI
 	 */
 	<T extends NormeSI> T add(T norme, String codeCurrentUser);
 	
 	/**
 	 * Set the attributes required for an update
 	 * @param norme 
 	 * @param codeCurrentUser 
 	 * @return NormeSI
 	 */
 	<T extends NormeSI> T update(T norme, String codeCurrentUser);
 	
 	
 	//////////////////////////////////////////////////////////////
 	// MissingPiece
 	//////////////////////////////////////////////////////////////
 
 	/**
 	 * All missingPiece for this individual.
 	 * @param individu
 	 * @param cmi
 	 * @return list de MissingPiece
 	 */
 	List<MissingPiece> getMissingPiece(Individu individu, Commission cmi);
 	
 	
 	/**
 	 * Save or update the missing piece list.
 	 * @param listMP
 	 * @param loginGest
 	 */
 	void saveOrUpdateMissingPiece(List<MissingPiece> listMP, String loginGest);
 	
 	/**
 	 * delete the missing piece list.
 	 * if all attributes are null, this method do nothing.
 	 * @param listMP can be null
 	 * @param piece can be null
 	 */
 	void deleteMissingPiece(List<MissingPiece> listMP, PieceJustificative piece);
 	
 	/**
 	 * Purge la table MISSING_PIECE pour la campagne.
 	 * @param camp
 	 */
 	void purgeMissingPieceCamp(Campagne camp);
 
 	//////////////////////////////////////////////////////////////
 	// Opinion
 	//////////////////////////////////////////////////////////////
 
 	/**
 	 * All the opinions pour un voeu.
 	 * @param indVoeu
 	 * @return liste de Avis
 	 */
 	List<Avis> getAvis(IndVoeu indVoeu);
 	
 	/**
 	 * All the opinions pour une etape.
 	 * @param codEtp
 	 * @param codVrsVet
 	 * @return liste de Avis
 	 */
 	List<Avis> getAvisByEtp(String codEtp, Integer codVrsVet);
 	
 	/**
 	 * Add an avis.
 	 * @param avis
 	 */
 	void addAvis(Avis avis);
 	
 	/**
 	 * Update an avis.
 	 * @param avis
 	 */
 	void updateAvis(Avis avis);
 
 	//////////////////////////////////////////////////////////////
 	// Nettoyage
 	//////////////////////////////////////////////////////////////
 
 	/**
 	 * 
 	 */
 	void deleteIndividusSansVoeux();
 
 
 	//////////////////////////////////////////////////////////////
 	// RENDEZ VOUS
 	//////////////////////////////////////////////////////////////
 
 	/**
 	 * @param indDate
 	 */
 	void addIndividuDate(IndividuDate indDate);
 
 	/**
 	 * @param indDate
 	 */
 	void updateIndividuDate(IndividuDate indDate);
 
 	/**
 	 * @param indDate
 	 */
 	void deleteIndividuDate(IndividuDate indDate);
 	
 	/**
 	 * @param indVoeu
 	 * @return the individuDate for his indVoeu
 	 */
 	IndividuDate getIndividuDate(IndVoeu indVoeu);
 	
 	/**
 	 * Delete the list of individuDate corresponding to the individual.
 	 * @param ind
 	 */
 	void deleteAllIndividuDate(Individu ind);
 	
 	/**
 	 * Purge la table RDV_INDIVIDU_DATE.
 	 * @param camp
 	 */
 	void purgeIndividuDateCamp(Campagne camp);
 	
 	/**
 	 * @param ho
 	 */
 	void addHoraire(Horaire ho);
 
 	/**
 	 * @param ho
 	 */
 	void updateHoraire(Horaire ho);
 
 	/**
 	 * @param ho
 	 */
 	void deleteHoraire(Horaire ho);
 	
 	/**
 	 * @param trFermee
 	 */
 	void addTrancheFermee(TrancheFermee trFermee);
 
 	/**
 	 * @param trFermee
 	 */
 	void updateTrancheFermee(TrancheFermee trFermee);
 
 	/**
 	 * @param trFermee
 	 */
 	void deleteTrancheFermee(TrancheFermee trFermee);
 	
 	/**
 	 * @param vetCalendar
 	 */
 	void addVetCalendar(VetCalendar vetCalendar);
 
 	/**
 	 * @param vetCalendar
 	 */
 	void updateVetCalendar(VetCalendar vetCalendar);
 
 	/**
 	 * @param vetCalendar
 	 */
 	void deleteVetCalendar(VetCalendar vetCalendar);
 
 	/**
 	 * @param jourHoraire
 	 */
 	void addJourHoraire(JourHoraire jourHoraire);
 
 	/**
 	 * @param jourHoraire
 	 */
 	void updateJourHoraire(JourHoraire jourHoraire);
 
 	/**
 	 * @param jourHoraire
 	 */
 	void deleteJourHoraire(JourHoraire jourHoraire);
 	
 	/**
 	 * @param jourHoraire
 	 */
 	void refreshJourHoraire(JourHoraire jourHoraire);
 	
 	/**
 	 * @param calendarRdv
 	 */
 	void refreshCalendarRdv(CalendarRDV calendarRdv);
 	
 	//////////////////////////////////////////////////////////////
 	// ARCHIVE TASK
 	//////////////////////////////////////////////////////////////
 
 	/**
 	 * Add a archive task.
 	 * @param archiveTask
 	 */
 	void addArchiveTask(ArchiveTask archiveTask);
 	
 	/**
 	 * Update a archive task.
 	 * @param archiveTask
 	 */
 	void updateArchiveTask(ArchiveTask archiveTask);
 	
 	/**
 	 * Delete a archive task.
 	 * @param archiveTask
 	 */
 	void deleteArchiveTask(ArchiveTask archiveTask);
 	
 	/**
 	 * Get all archive tasks.
 	 * @return List< ArchiveTask>
 	 */
 	List<ArchiveTask> getArchiveTasks();
 	
 	/**
 	 * Get all archive tasks to execute.
 	 * @return List< ArchiveTask>
 	 */
 	List<ArchiveTask> getArchiveTasksToExecute();
 	
 	
 	//////////////////////////////////////////////////////////////
 	// AUTOMATION SUPPLEMENTARY LISTS
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * @param autoLp
 	 */
 	void addAutoListPrincipale(AutoListPrincipale autoLp);
 
 	/**
 	 * @param autoLp
 	 */
 	void updateAutoListPrincipale(AutoListPrincipale autoLp);
 
 	/**
 	 * @param autoLp
 	 */
 	void deleteAutoListPrincipale(AutoListPrincipale autoLp);
 	
 	/**
 	 * @param vet
 	 */
 	void addVetAutoLp(VetAutoLp vet);
 
 	/**
 	 * @param vet
 	 */
 	void updateVetAutoLp(VetAutoLp vet);
 
 	/**
 	 * @param vet
 	 */
 	void deleteVetAutoLp(VetAutoLp vet);
 	
 	/**
 	 * @param autoLp
 	 * @param versionEtpOpi
 	 * @return IndVoeuPojo
 	 */
 	IndVoeu getRecupIndVoeuLc(AutoListPrincipale autoLp, VersionEtpOpi versionEtpOpi);
 
 	//////////////////////////////////////////////////////////////
 	// IndSituation
 	//////////////////////////////////////////////////////////////
 	
 	/**
 	 * Add a indSituation.
 	 * @param indSituation
 	 */
 	void addIndSituation(IndSituation indSituation);
 
 	/**
 	 * Update a indSituation.
 	 * @param indSituation
 	 */
 	void updateIndSituation(IndSituation indSituation);
 
 	/**
 	 * Delete a indSituation.
 	 * @param indSituation
 	 */
 	void deleteIndSituation(IndSituation indSituation);
 
 	/**
 	 * Get the indSituation for the ind.
 	 * @param ind
 	 */
 	IndSituation getIndSituation(Individu ind);
 
 }
