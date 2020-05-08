 package com.tda.model.socialworker;
 
 import java.util.Collection;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToOne;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.CollectionOfElements;
 
 import com.tda.model.patient.Patient;
 
 @Entity
 public class SocialWorkerForm {
 	private Long id;
 
 	private Patient patient;
 
 	private Date fillingDate;
 
 	// FAMILY GROUP
 	private String fatherFirstName;
 
 	private String fatherLastName;
 
 	private Integer fatherAge;
 
 	private String motherFirstName;
 
 	private String motherLastName;
 
 	private Integer motherAge;
 
 	private String tutorFirstName;
 
 	private String tutorLastName;
 
 	private Integer tutorAge;
 
 	private LivesWith livesWith;
 
 	private Integer peopleAtHome;
 
 	private Integer peopleAtHomeUnderTen;
 
 	private Integer peopleAtHomeOverTen;
 
 	// HOUSE
 	private Integer roomsExcludingKitchenAndBathroom;
 
 	private InteriorFloor interiorFloor;
 
 	private RoofType roofType;
 
 	private boolean hasCeiling;
 
 	private WaterSource waterSource;
 
 	private WaterSourceType waterSourceType;
 
 	private boolean hasBathroom;
 
 	private boolean isBathroomInside;
 
 	private BathroomSewerType bathroomSewerType;
 
 	private boolean isInSinkingZone;
 
 	private KitchenFuel kitchenFuel;
 
 	private Electricity electricity;
 
 	// SCHOLARITY
 
 	private boolean knowsHowToReadAndWrite;
 
 	private boolean isGoingToSchool;
 
 	private Scholarity scholarity;
 
 	private SchoolHours schoolHours;
 
 	private SchoolService schoolService;
 
 	// FAMILY GROUP SOCIAL AND ECONOMIC CONDITIONS
 
 	private Integer workingPeople;
 
 	private AdultEducationalLevel adultEducationalLevel;
 
 	private MainIncome mainIncome;
 
 	private boolean hasHealthCare;
 
 	private Collection<NBI> nbi;
 
 	private Addiction addiction;
 
 	private boolean hasProfessionalAssistanceForAddiction;
 
 	private Mistreatment mistreatment;
 
 	private boolean hasProfessionalAssistanceForMistreatment;
 
 	private boolean hasBeenDerivedToOtherSocialServices;
 
 	private String diagnose;
 
 	private String observations;
 
 	@ManyToOne
 	public Patient getPatient() {
 		return patient;
 	}
 
 	public void setPatient(Patient patient) {
 		this.patient = patient;
 	}
 
 	public Date getFillingDate() {
 		return fillingDate;
 	}
 
 	public void setFillingDate(Date fillingDate) {
 		this.fillingDate = fillingDate;
 	}
 
 	public String getFatherFirstName() {
 		return fatherFirstName;
 	}
 
 	public void setFatherFirstName(String fatherFirstName) {
 		this.fatherFirstName = fatherFirstName;
 	}
 
 	public String getFatherLastName() {
 		return fatherLastName;
 	}
 
 	public void setFatherLastName(String fatherLastName) {
 		this.fatherLastName = fatherLastName;
 	}
 
 	public Integer getFatherAge() {
 		return fatherAge;
 	}
 
 	public void setFatherAge(Integer fatherAge) {
 		this.fatherAge = fatherAge;
 	}
 
 	public String getMotherFirstName() {
 		return motherFirstName;
 	}
 
 	public void setMotherFirstName(String motherFirstName) {
 		this.motherFirstName = motherFirstName;
 	}
 
 	public String getMotherLastName() {
 		return motherLastName;
 	}
 
 	public void setMotherLastName(String motherLastName) {
 		this.motherLastName = motherLastName;
 	}
 
 	public Integer getMotherAge() {
 		return motherAge;
 	}
 
 	public void setMotherAge(Integer motherAge) {
 		this.motherAge = motherAge;
 	}
 
 	public String getTutorFirstName() {
 		return tutorFirstName;
 	}
 
 	public void setTutorFirstName(String tutorFirstName) {
 		this.tutorFirstName = tutorFirstName;
 	}
 
 	public String getTutorLastName() {
 		return tutorLastName;
 	}
 
 	public void setTutorLastName(String tutorLastName) {
 		this.tutorLastName = tutorLastName;
 	}
 
 	public Integer getTutorAge() {
 		return tutorAge;
 	}
 
 	public void setTutorAge(Integer tutorAge) {
 		this.tutorAge = tutorAge;
 	}
 
 	@Enumerated
 	public LivesWith getLivesWith() {
 		return livesWith;
 	}
 
 	public void setLivesWith(LivesWith livesWith) {
 		this.livesWith = livesWith;
 	}
 
 	public Integer getPeopleAtHome() {
 		return peopleAtHome;
 	}
 
 	public void setPeopleAtHome(Integer peopleAtHome) {
 		this.peopleAtHome = peopleAtHome;
 	}
 
 	public Integer getPeopleAtHomeUnderTen() {
 		return peopleAtHomeUnderTen;
 	}
 
 	public void setPeopleAtHomeUnderTen(Integer peopleAtHomeUnderTen) {
 		this.peopleAtHomeUnderTen = peopleAtHomeUnderTen;
 	}
 
 	public Integer getPeopleAtHomeOverTen() {
 		return peopleAtHomeOverTen;
 	}
 
 	public void setPeopleAtHomeOverTen(Integer peopleAtHomeOverTen) {
 		this.peopleAtHomeOverTen = peopleAtHomeOverTen;
 	}
 
 	public Integer getRoomsExcludingKitchenAndBathroom() {
 		return roomsExcludingKitchenAndBathroom;
 	}
 
 	public void setRoomsExcludingKitchenAndBathroom(
 			Integer roomsExcludingKitchenAndBathroom) {
 		this.roomsExcludingKitchenAndBathroom = roomsExcludingKitchenAndBathroom;
 	}
 
 	@Enumerated
 	public InteriorFloor getInteriorFloor() {
 		return interiorFloor;
 	}
 
 	public void setInteriorFloor(InteriorFloor interiorFloor) {
 		this.interiorFloor = interiorFloor;
 	}
 
 	@Enumerated
 	public RoofType getRoofType() {
 		return roofType;
 	}
 
 	public void setRoofType(RoofType roofType) {
 		this.roofType = roofType;
 	}
 
 	public boolean isHasCeiling() {
 		return hasCeiling;
 	}
 
 	public void setHasCeiling(boolean hasCeiling) {
 		this.hasCeiling = hasCeiling;
 	}
 
 	@Enumerated
 	public WaterSource getWaterSource() {
 		return waterSource;
 	}
 
 	public void setWaterSource(WaterSource waterSource) {
 		this.waterSource = waterSource;
 	}
 
 	@Enumerated
 	public WaterSourceType getWaterSourceType() {
 		return waterSourceType;
 	}
 
 	public void setWaterSourceType(WaterSourceType waterSourceType) {
 		this.waterSourceType = waterSourceType;
 	}
 
 	public boolean isHasBathroom() {
 		return hasBathroom;
 	}
 
 	public void setHasBathroom(boolean hasBathroom) {
 		this.hasBathroom = hasBathroom;
 	}
 
 	public boolean isBathroomInside() {
 		return isBathroomInside;
 	}
 
 	public void setBathroomInside(boolean isBathroomInside) {
 		this.isBathroomInside = isBathroomInside;
 	}
 
 	@Enumerated
 	public BathroomSewerType getBathroomSewerType() {
 		return bathroomSewerType;
 	}
 
 	public void setBathroomSewerType(BathroomSewerType bathroomSewerType) {
 		this.bathroomSewerType = bathroomSewerType;
 	}
 
 	public boolean isInSinkingZone() {
 		return isInSinkingZone;
 	}
 
 	public void setInSinkingZone(boolean isInSinkingZone) {
 		this.isInSinkingZone = isInSinkingZone;
 	}
 
 	@Enumerated
 	public KitchenFuel getKitchenFuel() {
 		return kitchenFuel;
 	}
 
 	public void setKitchenFuel(KitchenFuel kitchenFuel) {
 		this.kitchenFuel = kitchenFuel;
 	}
 
 	@Enumerated
 	public Electricity getElectricity() {
 		return electricity;
 	}
 
 	public void setElectricity(Electricity electricity) {
 		this.electricity = electricity;
 	}
 
 	public boolean isKnowsHowToReadAndWrite() {
 		return knowsHowToReadAndWrite;
 	}
 
 	public void setKnowsHowToReadAndWrite(boolean knowsHowToReadAndWrite) {
 		this.knowsHowToReadAndWrite = knowsHowToReadAndWrite;
 	}
 
 	public boolean isGoingToSchool() {
 		return isGoingToSchool;
 	}
 
 	public void setGoingToSchool(boolean isGoingToSchool) {
 		this.isGoingToSchool = isGoingToSchool;
 	}
 
 	@Enumerated
 	public Scholarity getScholarity() {
 		return scholarity;
 	}
 
 	public void setScholarity(Scholarity scholarity) {
 		this.scholarity = scholarity;
 	}
 
 	@Enumerated
 	public SchoolHours getSchoolHours() {
 		return schoolHours;
 	}
 
 	public void setSchoolHours(SchoolHours schoolHours) {
 		this.schoolHours = schoolHours;
 	}
 
 	@Enumerated
 	public SchoolService getSchoolService() {
 		return schoolService;
 	}
 
 	public void setSchoolService(SchoolService schoolService) {
 		this.schoolService = schoolService;
 	}
 
 	public Integer getWorkingPeople() {
 		return workingPeople;
 	}
 
 	public void setWorkingPeople(Integer workingPeople) {
 		this.workingPeople = workingPeople;
 	}
 
 	@Enumerated
 	public AdultEducationalLevel getAdultEducationalLevel() {
 		return adultEducationalLevel;
 	}
 
 	public void setAdultEducationalLevel(
 			AdultEducationalLevel adultEducationalLevel) {
 		this.adultEducationalLevel = adultEducationalLevel;
 	}
 
 	@Enumerated
 	public MainIncome getMainIncome() {
 		return mainIncome;
 	}
 
 	public void setMainIncome(MainIncome mainIncome) {
 		this.mainIncome = mainIncome;
 	}
 
 	public boolean isHasHealthCare() {
 		return hasHealthCare;
 	}
 
 	public void setHasHealthCare(boolean hasHealthCare) {
 		this.hasHealthCare = hasHealthCare;
 	}
 
	@CollectionOfElements(targetElement = NBI.class, fetch = FetchType.EAGER)
 	@JoinTable(name = "NBI", joinColumns = @JoinColumn(name = "SOCIAL_WORKER_ID"))
 	@Column(name = "NBI", nullable = true)
 	@Enumerated(EnumType.STRING)
 	public Collection<NBI> getNbi() {
 		return nbi;
 	}
 
 	public void setNbi(Collection<NBI> nbi) {
 		this.nbi = nbi;
 	}
 
 	@Enumerated
 	public Addiction getAddiction() {
 		return addiction;
 	}
 
 	public void setAddiction(Addiction addiction) {
 		this.addiction = addiction;
 	}
 
 	public boolean isHasProfessionalAssistanceForAddiction() {
 		return hasProfessionalAssistanceForAddiction;
 	}
 
 	public void setHasProfessionalAssistanceForAddiction(
 			boolean hasProfessionalAssistanceForAddiction) {
 		this.hasProfessionalAssistanceForAddiction = hasProfessionalAssistanceForAddiction;
 	}
 
 	@Enumerated
 	public Mistreatment getMistreatment() {
 		return mistreatment;
 	}
 
 	public void setMistreatment(Mistreatment mistreatment) {
 		this.mistreatment = mistreatment;
 	}
 
 	public boolean isHasProfessionalAssistanceForMistreatment() {
 		return hasProfessionalAssistanceForMistreatment;
 	}
 
 	public void setHasProfessionalAssistanceForMistreatment(
 			boolean hasProfessionalAssistanceForMistreatment) {
 		this.hasProfessionalAssistanceForMistreatment = hasProfessionalAssistanceForMistreatment;
 	}
 
 	public boolean isHasBeenDerivedToOtherSocialServices() {
 		return hasBeenDerivedToOtherSocialServices;
 	}
 
 	public void setHasBeenDerivedToOtherSocialServices(
 			boolean hasBeenDerivedToOtherSocialServices) {
 		this.hasBeenDerivedToOtherSocialServices = hasBeenDerivedToOtherSocialServices;
 	}
 
 	public String getDiagnose() {
 		return diagnose;
 	}
 
 	public void setDiagnose(String diagnose) {
 		this.diagnose = diagnose;
 	}
 
 	public String getObservations() {
 		return observations;
 	}
 
 	public void setObservations(String observations) {
 		this.observations = observations;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@Transient
 	public boolean isNew() {
 		return id == null;
 	}
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	public Long getId() {
 		return id;
 	}
 }
