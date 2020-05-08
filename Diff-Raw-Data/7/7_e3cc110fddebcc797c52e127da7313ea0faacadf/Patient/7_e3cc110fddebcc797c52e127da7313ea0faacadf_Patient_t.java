 package medoffice.entity;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.*;
 import org.eclipse.persistence.annotations.PrivateOwned;
 
 @Entity
 @Table(name = "patient")
 public class Patient implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "middle_initial")
    private Character middleInitial;
    @Column(name = "sex")
    private Character sex;
    @Column(name = "birth_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthDate;
    @Column(name = "ssn")
    private String ssn;
    @JoinColumn(name = "personal_doctor_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Provider personalDoctor;
    @Column(name = "primary_insurance_id")
    private Integer primaryInsuranceId;
    @Column(name = "billing_account_id")
    private Integer billingAccountId;   
    @Column(name = "address")
    private String address;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;
    @Column(name = "zip_code")
    private String zipCode;
    @Column(name = "home_phone")
    private String homePhone;
    @Column(name = "cell_phone")
    private String cellPhone;
    @Column(name = "emergency_phone")
    private String emergencyPhone;
    @Column(name = "tags")
    private String tags;   
    @Column(name = "notes")
    private String notes;
    @Column(name = "register_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date registerDate;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "email")
    private String email;
    @Column(name = "ethnicity_id")
    private Integer ethnicityId;
    @JoinColumn(name = "ethnicity_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Ethnicity ethnicity;
    @Column(name = "race_id")
    private Integer raceId;
    @JoinColumn(name = "race_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Race race;
    @Column(name = "preferred_language_code")
    private String preferredLanguageCode;
    @JoinColumn(name = "preferred_language_code", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Language preferredLanguage;
    @PrivateOwned
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patient")
    @OrderBy("accountIndex ASC")
    private List<PatientInsurance> insuranceList;
    @Column(name = "service_payment_type_id")
    private Integer servicePaymentTypeId;
    @JoinColumn(name = "service_payment_type_id", insertable = false, updatable = false)
    @ManyToOne
    private ServicePaymentType servicePaymentType;
    @ManyToMany
    @JoinTable(name = "patient_diagnosis",
    joinColumns =
    @JoinColumn(name = "id"),
    inverseJoinColumns =
    @JoinColumn(name = "code", referencedColumnName = "code"))
    private List<Diagnosis> diagnosisList;
    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "patient")
    @OrderBy("id DESC")
    private List<PatientDiagnosis> problemList;
    @PrivateOwned
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "patient")
    private List<PatientNote> noteList;
    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "patient")
    @OrderBy("created DESC")
    private List<PatientPastMedicalHistory> pastMedicalHistoryList;
    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "patient")
    @OrderBy("created DESC")
    private List<PatientFamilyHistory> familyHistoryList;
    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "patient")
    @OrderBy("created DESC")
    private List<PatientSocialHistory> socialHistoryList;
    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "patient")
    @OrderBy("date DESC")
    private List<PatientHistoricData> historicDataList;
    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "patient")
    @OrderBy("created DESC")
    private List<PatientImmunization> immunizationList;
    @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "patient")
    @OrderBy("created DESC")
    private List<PatientMedication> medicationList;
    @Column(name = "condition_id")
    private Integer conditionId;
    @Column(name = "condition_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date conditionDate;
    @JoinColumn(name = "id", insertable = false, updatable = false)
    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    private PatientStatus status;
    @Column(name = "marital_status_code")
    private Character maritalStatusCode;
    @JoinColumn(name = "marital_status_code", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private MaritalStatus maritalStatus;
    @Column(name = "work_phone")
    private String workPhone;
    @Column(name = "work_phone_ext")
    private String workPhoneExt;
    @Column(name = "work_address")
    private String workAddress;
    @Column(name = "work_city")
    private String workCity;
    @Column(name = "work_state")
    private String workState;
    @Column(name = "work_zip_code")
    private String workZipCode;
    @Column(name = "employer")
    private String employer;
 
    public Integer getId() {
       return id;
    }
 
    public void setId(Integer id) {
       this.id = id;
    }
 
    public String getLastName() {
       return lastName;
    }
 
    public void setLastName(String lastName) {
       this.lastName = lastName;
    }
 
    public String getFirstName() {
       return firstName;
    }
 
    public void setFirstName(String firstName) {
       this.firstName = firstName;
    }
 
    public Character getMiddleInitial() {
       return middleInitial;
    }
 
    public void setMiddleInitial(Character middleInitial) {
       this.middleInitial = middleInitial;
    }
 
    public String getName() {
       return lastName + ", " + firstName;
    }
 
    public Character getSex() {
       return sex;
    }
 
    public void setSex(Character sex) {
       this.sex = sex;
    }
 
    public Date getBirthDate() {
       return birthDate;
    }
 
    public void setBirthDate(Date birthDate) {
       this.birthDate = birthDate;
    }
 
    public int getAge() {
       Calendar dateOfBirth = new GregorianCalendar();
       dateOfBirth.setTime(birthDate);
       Calendar today = Calendar.getInstance();
       int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
       dateOfBirth.add(Calendar.YEAR, age);
       if (today.before(dateOfBirth)) {
          age--;
       }
       return age;
    }
 
    public boolean isChild() {
       return (getAge() < 18);
    }
    
    public String getSsn() {
       return ssn;
    }
 
    public void setSsn(String ssn) {
       this.ssn = ssn;
    }
 
    public Provider getPersonalDoctor() {
       return personalDoctor;
    }
 
    public void setPersonalDoctor(Provider personalDoctor) {
       this.personalDoctor = personalDoctor;
    }
 
    public Integer getPrimaryInsuranceId() {
       return primaryInsuranceId;
    }
 
    public void setPrimaryInsuranceId(Integer primaryInsuranceId) {
       this.primaryInsuranceId = primaryInsuranceId;
    }
 
    public Integer getServicePaymentTypeId() {
       return servicePaymentTypeId;
    }
 
    public void setServicePaymentTypeId(Integer servicePaymentTypeId) {
       this.servicePaymentTypeId = servicePaymentTypeId;
    }
 
    public ServicePaymentType getServicePaymentType() {
       return servicePaymentType;
    }
 
    public void setServicePaymentType(ServicePaymentType servicePaymentType) {
       this.servicePaymentType = servicePaymentType;
    }
    
    public String getPcpNpi() {
       for (PatientInsurance account : insuranceList) {
          if (account.getAccountIndex() == 1) {
             return account.getPcpNpi();
          }
       }
       return null;
    }
 
    public Integer getBillingAccountId() {
       return billingAccountId;
    }
 
    public void setBillingAccountId(Integer billingAccountId) {
       this.billingAccountId = billingAccountId;
    }
 
    public String getAddress() {
       return address;
    }
 
    public void setAddress(String address) {
       this.address = address;
    }
 
    public String getCity() {
       return city;
    }
 
    public void setCity(String city) {
       this.city = city;
    }
 
    public String getState() {
       return state;
    }
 
    public void setState(String state) {
       this.state = state;
    }
 
    public String getZipCode() {
       return zipCode;
    }
 
    public void setZipCode(String zipCode) {
       this.zipCode = zipCode;
    }
 
    public String getCityStateZip() {
       return city + ", " + state + " " + zipCode;
    }
 
    public String getHomePhone() {
       return homePhone;
    }
 
    public void setHomePhone(String homePhone) {
       this.homePhone = homePhone;
    }
 
    public String getCellPhone() {
       return cellPhone;
    }
 
    public void setCellPhone(String cellPhone) {
       this.cellPhone = cellPhone;
    }
 
    public String getEmergencyPhone() {
       return emergencyPhone;
    }
 
    public void setEmergencyPhone(String emergencyPhone) {
       this.emergencyPhone = emergencyPhone;
    }
 
    public String getTags() {
       return tags;
    }
 
    public void setTags(String tags) {
       this.tags = tags;
    }
 
    public String getNotes() {
       return notes;
    }
 
    public void setNotes(String notes) {
       this.notes = notes;
    }
 
    public Date getRegisterDate() {
       return registerDate;
    }
 
    public void setRegisterDate(Date registerDate) {
       this.registerDate = registerDate;
    }
 
    public Integer getUserId() {
       return userId;
    }
 
    public void setUserId(Integer userId) {
       this.userId = userId;
    }
 
    public String getEmail() {
       return email;
    }
 
    public void setEmail(String email) {
       this.email = email;
    }
 
    public Integer getEthnicityId() {
       return ethnicityId;
    }
 
    public void setEthnicityId(Integer ethnicityId) {
       this.ethnicityId = ethnicityId;
    }
 
    public Ethnicity getEthnicity() {
       return ethnicity;
    }
 
    public void setEthnicity(Ethnicity ethnicity) {
       this.ethnicity = ethnicity;
    }
 
    public Integer getRaceId() {
       return raceId;
    }
 
    public void setRaceId(Integer raceId) {
       this.raceId = raceId;
    }
 
    public Race getRace() {
       return race;
    }
 
    public void setRace(Race race) {
       this.race = race;
    }
 
    public String getPreferredLanguageCode() {
       return preferredLanguageCode;
    }
 
    public void setPreferredLanguageCode(String preferredLanguageCode) {
       this.preferredLanguageCode = preferredLanguageCode;
    }
 
    public Language getPreferredLanguage() {
       return preferredLanguage;
    }
 
    public void setPreferredLanguage(Language preferredLanguage) {
       this.preferredLanguage = preferredLanguage;
    }
 
    public List<PatientInsurance> getInsuranceList() {
       return insuranceList;
    }
 
    public void setInsuranceList(List<PatientInsurance> insuranceList) {
       this.insuranceList = insuranceList;
    }
 
    public List<Diagnosis> getDiagnosisList() {
       return diagnosisList;
    }
 
    public void setDiagnosisList(List<Diagnosis> diagnosisList) {
       this.diagnosisList = diagnosisList;
    }
    
    public List<Diagnosis> getActiveDiagnosisList() {
       if (problemList == null) {
          return null;
       }
       List activeDiagnosisList = new ArrayList();
       for (PatientDiagnosis problem : problemList) {
          if (problem.getActive()) {
             activeDiagnosisList.add(problem.getDiagnosis());
          }
       }
       return activeDiagnosisList;
    }
 
    public List<PatientDiagnosis> getProblemList() {
       return problemList;
    }
 
    public void setProblemList(List<PatientDiagnosis> problemList) {
       this.problemList = problemList;
    }
 
    public List<PatientDiagnosis> getActiveProblemList() {
       if (problemList == null) {
          return null;
       }
       List activeProblemList = new ArrayList();
       for (PatientDiagnosis problem : problemList) {
          if (problem.getActive()) {
             activeProblemList.add(problem);
          }
       }
       return activeProblemList;
    }
 
    public List<PatientNote> getNoteList() {
       return noteList;
    }
 
    public void setNoteList(List<PatientNote> noteList) {
       this.noteList = noteList;
    }
 
    public List<PatientPastMedicalHistory> getPastMedicalHistoryList() {
       return pastMedicalHistoryList;
    }
 
    public void setPastMedicalHistoryList(List<PatientPastMedicalHistory> pastMedicalHistoryList) {
       this.pastMedicalHistoryList = pastMedicalHistoryList;
    }
 
    public List<PatientFamilyHistory> getFamilyHistoryList() {
       return familyHistoryList;
    }
 
    public void setFamilyHistoryList(List<PatientFamilyHistory> familyHistoryList) {
       this.familyHistoryList = familyHistoryList;
    }
 
    public List<PatientSocialHistory> getSocialHistoryList() {
       return socialHistoryList;
    }
 
    public void setSocialHistoryList(List<PatientSocialHistory> socialHistoryList) {
       this.socialHistoryList = socialHistoryList;
    }
 
    public List<PatientHistoricData> getHistoricDataList() {
       return historicDataList;
    }
    
    public List<PatientHistoricData> historicDataList(String table) {
       if (table == null || historicDataList == null) {
          return null;
       }
       List<PatientHistoricData> filteredHistoricDataList = new ArrayList();
       for (PatientHistoricData patientHistoricData : historicDataList) {
          if (table.equals(patientHistoricData.getTable())) {
             filteredHistoricDataList.add(patientHistoricData);
          }
       }
       return filteredHistoricDataList;
    }
 
    public void setHistoricDataList(List<PatientHistoricData> historicDataList) {
       this.historicDataList = historicDataList;
    }
 
    public PatientHistoricData getLastHistoricData(String table, String field) {
       if (historicDataList == null || table == null || field == null) {
          return null;
       }
       for (PatientHistoricData historicData : historicDataList) {
          if (table.equals(historicData.getTable()) && field.equals(historicData.getField())) {
             return historicData;
          }
       }
       return null;
    }
 
    public List<PatientImmunization> getImmunizationList() {
       return immunizationList;
    }
 
    public void setImmunizationList(List<PatientImmunization> immunizationList) {
       this.immunizationList = immunizationList;
    }
 
    public List<PatientMedication> getMedicationList() {
       return medicationList;
    }
 
    public void setMedicationList(List<PatientMedication> medicationList) {
       this.medicationList = medicationList;
    }
 
    public PatientStatus getStatus() {
       if (id == null) {
          return null;
       }
       if (status == null) {
          status = new PatientStatus();
          status.setPatientId(id);
          status.setPatient(this);
          status.setPersisted(false);
       }
       return status;
    }
 
    public void setStatus(PatientStatus status) {
       this.status = status;
    }
 
    public Integer getConditionId() {
       return conditionId;
    }
 
    public void setConditionId(Integer conditionId) {
       this.conditionId = conditionId;
    }
 
    public Date getConditionDate() {
       return conditionDate;
    }
 
    public void setConditionDate(Date conditionDate) {
       this.conditionDate = conditionDate;
    }
 
    public Character getMaritalStatusCode() {
       return maritalStatusCode;
    }
 
    public void setMaritalStatusCode(Character maritalStatusCode) {
       this.maritalStatusCode = maritalStatusCode;
    }
 
    public MaritalStatus getMaritalStatus() {
       return maritalStatus;
    }
 
    public void setMaritalStatus(MaritalStatus maritalStatus) {
       this.maritalStatus = maritalStatus;
    }
 
    public String getWorkPhone() {
       return workPhone;
    }
 
    public void setWorkPhone(String workPhone) {
       this.workPhone = workPhone;
    }
 
    public String getWorkPhoneExt() {
       return workPhoneExt;
    }
 
    public void setWorkPhoneExt(String workPhoneExt) {
       this.workPhoneExt = workPhoneExt;
    }
 
    public String getWorkAddress() {
       return workAddress;
    }
 
    public void setWorkAddress(String workAddress) {
       this.workAddress = workAddress;
    }
 
    public String getWorkCity() {
       return workCity;
    }
 
    public void setWorkCity(String workCity) {
       this.workCity = workCity;
    }
 
    public String getWorkState() {
       return workState;
    }
 
    public void setWorkState(String workState) {
       this.workState = workState;
    }
 
    public String getWorkZipCode() {
       return workZipCode;
    }
 
    public void setWorkZipCode(String workZipCode) {
       this.workZipCode = workZipCode;
    }
 
    public String getEmployer() {
       return employer;
    }
 
    public void setEmployer(String employer) {
       this.employer = employer;
    }
    
    public String getField(String field) {
       try {
          Object valObj = Patient.class.getDeclaredField(field).get(this);
          return valObj == null ? null : valObj.toString();
       } catch (Exception ex) {
          Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, null, ex);
       }
       return null;
    }
   
   @Override
   public int hashCode() {
      int hash = 0;
      hash += (this.id != null ? this.id.hashCode() : 0);
      return hash;
   }
 
    @Override
    public boolean equals(Object object) {
       if (!(object instanceof Patient)) {
          return false;
       }
       Patient other = (Patient) object;
       if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
          return false;
       }
       return true;
    }
 
    @Override
    public String toString() {
       return "medoffice.entity.Patient[id=" + id + "]";
    }
 }
