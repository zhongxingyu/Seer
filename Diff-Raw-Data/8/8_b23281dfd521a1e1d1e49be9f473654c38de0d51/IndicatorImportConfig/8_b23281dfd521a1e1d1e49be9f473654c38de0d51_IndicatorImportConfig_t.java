 package org.ocha.hdx.persistence.entity.curateddata;
 
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 
 import org.ocha.hdx.model.validation.ValidationStatus;
 
 @Embeddable
 public class IndicatorImportConfig {
 
 	/**
 	 * Storing the initial value as represented in the resource before import. For troubleshooting
 	 */
 	@Column(name = "initial_value", nullable = false, updatable = false)
 	private String initialValue;
 
 	@Column(name = "lower_boundary", nullable = true, updatable = false)
	private Double lowerBoundary;
 	@Column(name = "upper_boundary", nullable = true, updatable = false)
	private Double upperBoundary;
 
 	/**
 	 * very likely to be something like 1000, but could also be a float (conversion, currencies ....)
 	 */
 	@Column(name = "multiplier", nullable = true, updatable = false)
	private Double multiplier;
 
 	/**
 	 * sample : YYYY
 	 */
 	@Column(name = "expected_time_format", nullable = true, updatable = false)
 	private String expectedTimeFormat;
 
 	/**
 	 * sample : if expectedTimeFormat is YYYY, interpretedTimeFormat could be YYYY-01-01
 	 */
 	@Column(name = "interpreted_time_format", nullable = true, updatable = false)
 	private String interpretedTimeFormat;
 
 	@Column(name = "validation_status", nullable = false, updatable = false)
 	@Enumerated(EnumType.STRING)
 	private ValidationStatus validationStatus;
 
 	@Column(name = "validation_message", nullable = true, updatable = false)
 	private String validationMessage;
 
 	public IndicatorImportConfig(final String initialValue, final ValidationStatus validationStatus) {
 		super();
 		this.initialValue = initialValue;
 		this.validationStatus = validationStatus;
 	}
 
 	public IndicatorImportConfig() {
 		super();
 	}
 
 	public String getInitialValue() {
 		return this.initialValue;
 	}
 
 	public void setInitialValue(final String initialValue) {
 		this.initialValue = initialValue;
 	}
 
 	public double getLowerBoundary() {
 		return lowerBoundary;
 	}
 
 	public void setLowerBoundary(final double lowerBoundary) {
 		this.lowerBoundary = lowerBoundary;
 	}
 
 	public double getUpperBoundary() {
 		return upperBoundary;
 	}
 
 	public void setUpperBoundary(final double upperBoundary) {
 		this.upperBoundary = upperBoundary;
 	}
 
 	public double getMultiplier() {
 		return multiplier;
 	}
 
 	public void setMultiplier(final double multiplier) {
 		this.multiplier = multiplier;
 	}
 
 	public String getExpectedTimeFormat() {
 		return expectedTimeFormat;
 	}
 
 	public void setExpectedTimeFormat(final String expectedTimeFormat) {
 		this.expectedTimeFormat = expectedTimeFormat;
 	}
 
 	public String getInterpretedTimeFormat() {
 		return interpretedTimeFormat;
 	}
 
 	public void setInterpretedTimeFormat(final String interpretedTimeFormat) {
 		this.interpretedTimeFormat = interpretedTimeFormat;
 	}
 
 	public ValidationStatus getValidationStatus() {
 		return validationStatus;
 	}
 
 	public void setValidationStatus(final ValidationStatus validationStatus) {
 		this.validationStatus = validationStatus;
 	}
 
 	public String getValidationMessage() {
 		return validationMessage;
 	}
 
 	public void setValidationMessage(final String validationMessage) {
 		this.validationMessage = validationMessage;
 	}
 
 }
