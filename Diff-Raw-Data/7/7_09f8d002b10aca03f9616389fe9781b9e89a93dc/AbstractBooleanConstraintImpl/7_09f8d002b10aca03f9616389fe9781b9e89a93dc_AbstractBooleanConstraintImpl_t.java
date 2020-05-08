 package org.genericsystem.systemproperties.constraints;
 
 import org.genericsystem.core.Generic;
 import org.genericsystem.exception.ConstraintViolationException;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.map.ConstraintsMapProvider;
 
 public abstract class AbstractBooleanConstraintImpl extends AbstractConstraintImpl {
 	@Override
 	public void check(Holder valueBaseComponent, AxedConstraintClass key) throws ConstraintViolationException {
		if (valueBaseComponent.getValue(getCurrentCache().<Holder> find(ConstraintsMapProvider.ConstraintValue.class)))
 			check(valueBaseComponent.<Attribute> getBaseComponent().getBaseComponent(), key);
 	}
 
 	public abstract void check(Generic baseComponent, AxedConstraintClass key) throws ConstraintViolationException;
 }
