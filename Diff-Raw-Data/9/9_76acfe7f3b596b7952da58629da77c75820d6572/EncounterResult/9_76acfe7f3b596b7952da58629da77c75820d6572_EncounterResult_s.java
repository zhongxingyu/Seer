 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.calculation.result;
 
 import java.util.Date;
 
 import org.openmrs.Encounter;
 import org.openmrs.calculation.patient.PatientCalculation;
 import org.openmrs.calculation.patient.PatientCalculationContext;
 
 /**
  * Represents a {@link CalculationResult} for an {@link Encounter}
  */
 public class EncounterResult extends SimpleResult implements DateBasedResult {
 	
 	/**
 	 * Convenience constructor that takes in an {@link Encounter} and {@link PatientCalculation}
 	 * 
 	 * @param encounter the encounter to set
 	 * @param calculation the calculation to set
 	 */
 	public EncounterResult(Encounter encounter, PatientCalculation calculation) {
 		this(encounter, calculation, null);
 	}
 	
 	/**
 	 * Convenience constructor that takes in an {@link Encounter}, {@link PatientCalculation} and
 	 * {@link PatientCalculationContext}
 	 * 
 	 * @param encounter the encounter to set
 	 * @param calculation the calculation to set
 	 * @param calculationContext the calculationContext to set
 	 */
 	public EncounterResult(Encounter encounter, PatientCalculation calculation, PatientCalculationContext calculationContext) {
 		super(encounter, calculation, calculationContext);
 	}
 	
 	/**
 	 * @see SimpleResult#getValue()
 	 */
 	@Override
 	public Encounter getValue() {
		return (Encounter)getValue();
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.result.DateBasedResult#getDateOfResult()
 	 */
 	@Override
 	public Date getDateOfResult() {
 		if (getValue() != null)
 			return getValue().getEncounterDatetime();
 		return null;
 	}
 	
 }
