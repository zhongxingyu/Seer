 package module.geography.domain.task;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 
 import module.geography.domain.Country;
 import module.geography.domain.CountrySubdivision;
 import module.geography.util.StringsUtil;
 import module.organization.domain.Accountability;
 import myorg._development.PropertiesManager;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import pt.ist.fenixWebFramework.services.Service;
 import pt.utl.ist.fenix.tools.util.i18n.Language;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class PortugueseDistrictImportAuxiliaryServices {
 
     private static final String CTT_DISTRICTFILE = "/distritos.txt";
 
     Country portugal;
 
     protected int additions = 0;
 
     protected int deletions = 0;
 
     protected int modifications = 0;
 
     protected int touches = 0;
 
     private final MultiLanguageString districtLevelName = StringsUtil.makeName("Distrito", "District");;
 
     private static PortugueseDistrictImportAuxiliaryServices singletonHolder;
 
     private PortugueseDistrictImportAuxiliaryServices() {
 	super();
     }
 
     protected static PortugueseDistrictImportAuxiliaryServices getInstance() {
 	if (singletonHolder == null) {
 	    singletonHolder = new PortugueseDistrictImportAuxiliaryServices();
 	}
 	return singletonHolder;
     }
 
     @Service
     protected void executeTask(PortugueseDistrictImport originalTask) {
 	// let's retrieve Portugal, if it doesn't exist, we must create it
 	portugal = Country.getPortugal();
 	LineNumberReader reader = null;
 	ArrayList<CountrySubdivision> districtsOnFile = new ArrayList<CountrySubdivision>();
 	ArrayList<CountrySubdivision> existingDistricts = new ArrayList<CountrySubdivision>();
 	try {
 	    File file = new File(PropertiesManager.getProperty("modules.geography.file.import.location") + CTT_DISTRICTFILE);
 	    if (originalTask.getLastRun() == null || file.lastModified() > originalTask.getLastRun().getMillis()) {
 		DateTime lastReview = new DateTime(file.lastModified());
 		FileInputStream fileReader = new FileInputStream(file);
 		reader = new LineNumberReader(new InputStreamReader(fileReader, "ISO-8859-1"));
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 		    String[] parts = line.split(";");
 		    String districtCode = parts[0];
 		    String districtName = parts[1];
 
 		    CountrySubdivision district = portugal.getChildByCode(districtCode);
 		    if (district == null) {
 			district = createDistrict(districtCode, districtName, lastReview);
 		    } else {
 			modifyDistrict(district, districtCode, districtName, "", lastReview);
 		    }
 		    districtsOnFile.add(district);
 		} // 'remove' all of the districts in
 		  // excess i.e. make their // accountabilities end
 
 		// getting all of the existing districts
 		for (CountrySubdivision countrySubdivision : portugal.getChildren()) {
 		    if (countrySubdivision.getLevelName().getContent(Language.pt).equalsIgnoreCase(districtLevelName.getContent(Language.pt))) {
 			existingDistricts.add(countrySubdivision);
 		    }
 		} // let's assert
 		  // which ones are in excess and remove them
 		existingDistricts.removeAll(districtsOnFile);
 		for (CountrySubdivision countrySubdivision : existingDistricts) {
 		    removeDistrict(countrySubdivision);
 		}
 		originalTask.auxLogInfo("File last modification was: " + lastReview);
 		originalTask.auxLogInfo(additions + " districts added.");
 		originalTask.auxLogInfo(touches + " districts unmodified, but whose date has changed.");
 		originalTask.auxLogInfo(modifications + " districts modified");
 	    } else {
 		originalTask.auxLogInfo("File unmodified, nothing imported.");
 	    }
 	} catch (FileNotFoundException e) {
 	    e.printStackTrace();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	} finally {
 	    try {
 		if (reader != null) {
 		    reader.close();
 		}
 	    } catch (IOException e) {
 	    }
 	}
     }
 
 
     /**
      * It modifies the data or touches (sets the last review date to the date
      * that it got)
      * 
      * @param district
      *            the already-existing district
      * @param districtCode
      *            the code of the district
      * @param districtName
      *            the name of the district
      * @param lastReview
      *            the date of the last review of the file used to do this import
      */
 
     protected void modifyDistrict(CountrySubdivision district, String districtCode, String districtName, String districtAcronym,
 	    DateTime lastReview) {
 	String originalDistrictName = district.getName().getContent(Language.pt);
 	String originalDistrictAcronym = district.getAcronym();
 
 	// let's check if there are changes on the data:
 	if (!originalDistrictName.equals(districtName) || !originalDistrictAcronym.equals(districtAcronym)
 		|| !district.getLevelName().equals(districtLevelName)) {
 
 	    removeDistrict(district);
 	    deletions--;
 	    // create the new district!
 	    // lets correct the counters afterwards!
 	    createDistrict(districtCode, districtName, lastReview);
 	    additions--;
 	    modifications++;
 	}
 	// if not let's just 'touch' it
 	else {
 	    touchDistrict(district, lastReview);
 	}
     }
 
     protected void removeDistrict(CountrySubdivision district) {
 	// modify the district by setting the accountability of the last one
 	// end before the date of the last review
 	Accountability activeAccountability = null;
 	for (Accountability accountability : district.getUnit().getParentAccountabilities(
 		district.getOrCreateAccountabilityType())) {
 	    if (accountability.isActiveNow()) {
 		activeAccountability = accountability;
 	    }
 	}
 
 	if (activeAccountability != null) {
 	    // make the accountability expire a minimal measure of time before
 	    // of
 	    // the current date
 	    LocalDate endDate = new LocalDate();
 	    // endDate = endDate.minusDays(1);
	    activeAccountability.setEndDate(endDate);
 	}
 	deletions++;
     }
 
     protected void touchDistrict(CountrySubdivision district, DateTime lastReview) {
 	district.setLastReview(lastReview);
 	touches++;
     }
 
     protected CountrySubdivision createDistrict(String districtCode, String districtName, DateTime lastReview) {
 	CountrySubdivision district = new CountrySubdivision(portugal, districtName, "", districtCode);
 	district.setLevelName(districtLevelName, false);
 	district.setLastReview(lastReview);
 	additions++;
 	return district;
     }
 
 }
