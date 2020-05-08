 package de.shop.kundenverwaltung.service;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 //import java.util.Locale;
 
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 import javax.validation.groups.Default;
 
 import org.jboss.logging.Logger;
 
 import de.shop.util.IdGroup;
 import de.shop.util.Log;
 import de.shop.util.ValidatorProvider;
 import de.shop.kundenverwaltung.domain.Adresse;
 
 @Log
 public class AdresseService implements Serializable {
 	private static final long serialVersionUID = 1650148163922645962L;
 	
 	@PersistenceContext
 	private transient EntityManager em;
 	
 	@Inject
 	private transient Logger logger;
 	
 	@Inject
 	private ValidatorProvider validationProvider;
 	
 	@PostConstruct
 	private void postConstruct() {
 		logger.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
 	}
 	
 	@PreDestroy
 	private void preDestroy() {
 		logger.debugf("CDI-faehiges Bean %s wird geloescht", this);
 	}
 	
 	public List<Adresse> findAlleAdressen() {
 		List<Adresse> adressen;
 		
 		adressen = em.createNamedQuery(Adresse.FIND_ADRESSEN, Adresse.class)
 					.getResultList();
 		
 		return adressen;
 	}
 	
 	public Adresse findAdresseById(Long id, Locale locale) {
 		validateAdresseId(id, locale);
 		Adresse adresse = null;
 		
 		adresse = em.find(Adresse.class, id);
 		
 		return adresse;
 	}
 	
 	public Adresse createAdresse(Adresse adresse, Locale locale) {
 		if (adresse == null)
 			return adresse;
 		
 		validateAdresse(adresse, locale, Default.class);
 		
 		adresse.setAdresseId(null);
 		em.persist(adresse);
 		return adresse;
 	}
 	
 	public Adresse updateAdresse(Adresse adresse, Locale locale) {
 		if (adresse == null)
 			return null;
 		
 		validateAdresse(adresse, locale, Default.class);
		em.detach(adresse);
 		
 		try {
 			final Adresse vorhandeneAdresse = em.find(Adresse.class, adresse.getAdresseId());
 			
 			em.detach(vorhandeneAdresse);
 			
 			if (vorhandeneAdresse.getAdresseId().longValue() != adresse.getAdresseId().longValue())
 				throw new AdresseExistsException(adresse.getAdresseId());
 		}
 		catch (NoResultException e) {
 			logger.debugf("Neue Adresse");
 		}
 		
		
 		
 		em.merge(adresse);
 		return adresse;
 	}
 	
 	private void validateAdresse(Adresse adresse, Locale locale, Class<?>... groups) {
 		// Werden alle Constraints beim Einfuegen gewahrt?
 		final Validator validator = validationProvider.getValidator(locale);
 		
 		final Set<ConstraintViolation<Adresse>> violations = validator.validate(adresse, groups);
 		if (!violations.isEmpty()) {
 			throw new AdresseValidationException(adresse, violations);
 		}
 	}
 	
 	private void validateAdresseId(Long adresseId, Locale locale) {
 		final Validator validator = validationProvider.getValidator(locale);
 		final Set<ConstraintViolation<Adresse>> violations = validator.validateValue(Adresse.class,
 				                                                                           "adresseId",
 				                                                                           adresseId,
 				                                                                           IdGroup.class);
 		if (!violations.isEmpty())
 			throw new InvalidAdresseIdException(adresseId, violations);
 	}
 
 }
