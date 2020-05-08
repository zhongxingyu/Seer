 package cz.cvut.fit.mi_mpr_dip.admission.domain;
 
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.roo.addon.dod.RooDataOnDemand;
 
import cz.cvut.fit.mi_mpr_dip.admission.domain.study.Programme;
import cz.cvut.fit.mi_mpr_dip.admission.domain.study.ProgrammeDataOnDemand;

 @RooDataOnDemand(entity = Term.class)
 public class TermDataOnDemand {

	@Autowired
	private ProgrammeDataOnDemand programmeDataOnDemand;

	public Term getNewTransientTerm(int index) {
		Term obj = new Term();
		setApologyTo(obj, index);
		setCapacity(obj, index);
		setDateOfTerm(obj, index);
		setRegisterFrom(obj, index);
		setRegisterTo(obj, index);
		setRoom(obj, index);
		setTermType(obj, index);
		setPrograms(obj, index);

		return obj;
	}

	public void setPrograms(Term obj, int index) {
		Programme programme = programmeDataOnDemand.getRandomProgramme();
		Set<Programme> programms = new HashSet<Programme>();
		programms.add(programme);
		obj.setPrograms(programms);
	}
 }
