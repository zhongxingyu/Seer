 package org.dejava.component.ejb.businessrule;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 
 import org.dejava.component.validation.object.ValidationException;
 
 /**
  * An implementation generic entity business rule set.
  * 
  * @param <Entity>
  *            Any entity.
  */
 public abstract class AbstractGenericEntityBusinessRuleSet<Entity> implements
 		GenericEntityBusinessRuleSet<Entity> {
 
 	/**
 	 * Validates an entity in a given context.
 	 * 
 	 * @param entity
 	 *            Entity to be validated.
 	 * @param context
 	 *            Context from within the entity should be validated.
 	 * @return The violations for the entity in the context.
 	 */
 	private Set<ConstraintViolation<Entity>> validateNoExceptions(final Entity entity,
 			final Object... context) {
		// If the entity is not null.
		if (entity != null) {
 			// Returns an empty set of violations. FIXME Think about.
 			return new HashSet<>();
 		}
 		// If the entity is not null.
 		else {
 			// Validates the current entity (and returns the found violations).
 			return Validation.buildDefaultValidatorFactory().getValidator()
 					.validate(entity, (Class<?>[]) context);
 		}
 	}
 
 	/**
 	 * @see org.dejava.component.ejb.businessrule.GenericEntityBusinessRuleSet#validate(java.lang.Object,
 	 *      java.lang.Object[])
 	 */
 	@Override
 	public void validate(final Entity entity, final Object... context) {
 		// Validates the current entity (and throws an exception for the found violations).
 		ValidationException.throwViolationExceptions(validateNoExceptions(entity, context));
 	}
 
 	/**
 	 * @see org.dejava.component.ejb.businessrule.GenericEntityBusinessRuleSet#validate(java.util.Collection,
 	 *      java.lang.Object[])
 	 */
 	@Override
 	public void validate(final Collection<Entity> entities, final Object... context) {
 		// If there are entities to be added.
 		if (entities != null) {
 			// Creates a new violation set.
 			final HashSet<ConstraintViolation<?>> violations = new HashSet<>();
 			// For each entity.
 			for (final Entity currentEntity : entities) {
 				// Validates the current entity (and add the violations to the complete set).
 				validateNoExceptions(currentEntity, context);
 			}
 			// Throws an exception for the found violations.
 			ValidationException.throwViolationExceptions(violations);
 		}
 	}
 }
