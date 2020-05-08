 package com.redshape.validators.impl.common;
 
 import com.redshape.utils.range.IRange;
import com.redshape.utils.range.RangeBuilder;
import com.redshape.utils.range.RangeUtils;
 import com.redshape.validators.AbstractValidator;
 
 /**
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.validators.impl.common
  * @date 8/16/11 8:49 PM
  */
 public class RangeValidator extends AbstractValidator<String, ValidationResult> {
 	private IRange<Integer> range;
 
 	public RangeValidator( IRange<Integer> range ) {
 		if ( range == null ) {
 			throw new IllegalArgumentException("<null>");
 		}
 
 		this.range = range;
 	}
 
 	@Override
 	public boolean isValid(String value) {
 		return value == null || value.isEmpty()
				/**
				 * @FIXME: needs to use isSubRange() check
				 */
				|| RangeUtils.checkIntersections( range, RangeBuilder.fromString(value) );
 	}
 
 	@Override
 	public ValidationResult validate(String value) {
 		return new ValidationResult( this.isValid(value),
 				"Given value not fit acceptable values range: " + this.range.getStart() + " - " + this.range.getEnd() );
 	}
 
 }
