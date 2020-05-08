 package models;
 
 import java.util.*;
 import javax.persistence.*;
 import javax.persistence.validation.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 @Entity
 @Table(name = "students")
 public class Student extends Model {
     @Column(name = "date_of_birth")
     public Date dateOfBirth;
     @Column(name = "graduation_date")
     public Date graduationDate;
     public static final long serialVersionUID = 1L;
     @Id
     @NotNull
     @Column(name = "user_ID")
     public Integer userID;
     @NotNull
     @Size(min = 1, max = 120)
     @Column(name = "first_name")
     public String firstName;
     @NotNull
     @Size(min = 1, max = 300)
     @Column(name = "last_name")
     public String lastName;
     @NotNull
     @Size(min = 1, max = 500)
     @Column(name = "full_name")
     public String fullName;
     @NotNull
     @Size(min = 1, max = 10)
     @Column(name = "phd_cycle")
     public String phdCycle;
     @NotNull
     @Column(name = "is_suspended")
     public boolean isSuspended;
     @NotNull
     @Column(name = "course_year")
     public int courseYear;
     @NotNull
     @Column(name = "admitted_conditionally")
     public boolean admittedConditionally;
     @NotNull
     @Size(min = 1, max = 500)
     @Column(name = "legal_residence")
     public String legalResidence;
     @NotNull
     @Size(min = 1, max = 500)
     @Column(name = "current_domicile")
     public String currentDomicile;
     @NotNull
     @Size(min = 1, max = 300)
     @Column(name = "place_of_birth")
     public String placeOfBirth;
     @NotNull
     @Size(min = 1, max = 100)
     @Column(name = "office_phone")
     public String officePhone;
     @NotNull
     @Size(min = 1, max = 100)
     @Column(name = "mobile_phone")
     public String mobilePhone;
     @NotNull
     @Size(min = 1, max = 200)
     @Column(name = "office_working_place")
     public String officeWorkingPlace;
     @NotNull
     @Size(min = 1, max = 60)
     @Column(name = "locker_number")
     public String lockerNumber;
     @Column(name = "phd_scholarship")
     public Boolean phdScholarship;
     @NotNull
     @Size(min = 1, max = 500)
     @Column(name = "scholarship_type")
     public String scholarshipType;
     @NotNull
     @Column(name = "yearly_fee_to_center")
     public int yearlyFeeToCenter;
     @NotNull
     @Column(name = "yearly_fee_to_school")
     public int yearlyFeeToSchool;
     @NotNull
     @Column(name = "has_pc_rights")
     public boolean hasPcRights;
     @Size(max = 500)
     @Column(name = "pre_doctoral_scholarship")
     public String preDoctoralScholarship;
     @NotNull
     @Column(name = "months_predoc_scholarship")
     public int monthsPredocScholarship;
     @NotNull
     @Size(min = 1, max = 100)
     @Column(name = "year_extension_scholarship")
     public String yearExtensionScholarship;
     @NotNull
     @Column(name = "months")
     public int months;
     @NotNull
     @Column(name = "personal_funds_available")
     public int personalFundsAvailable;
     @NotNull
     @Column(name = "is_graduated")
     public boolean isGraduated;
     @NotNull
     @Size(min = 1, max = 20)
     @Column(name = "commitee_members")
     public String commiteeMembers;
     // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
     @NotNull
     @Size(min = 1, max = 200)
     @Column(name = "email")
     public String email;
     @NotNull
     @Column(name = "deleted")
     public boolean deleted;
     @Column(name = "Italian_Taxpayer_Code")
     public Integer italianTaxpayerCode;
     @JoinColumn(name = "university_of_provenance", referencedColumnName = "university_ID")
     @ManyToOne(optional = false)
     public University universityOfProvenance;
     @JoinColumn(name = "university", referencedColumnName = "university_ID")
     @ManyToOne(optional = false)
     public University university;
     @JoinColumn(name = "funding_institution", referencedColumnName = "funding_institution_ID")
     @ManyToOne(optional = false)
     public FundingInstitution fundingInstitution;
     @JoinColumn(name = "country_of_provenance", referencedColumnName = "country_ID")
     @ManyToOne(optional = false)
     public Country countryOfProvenance;
     @JoinColumn(name = "citizenship", referencedColumnName = "country_ID")
     @ManyToOne(optional = false)
     public Country citizenship;
     @JoinColumn(name = "funds_owner", referencedColumnName = "supervisor_ID")
     @ManyToOne(optional = false)
     public Supervisor fundsOwner;
     @JoinColumn(name = "tutor", referencedColumnName = "supervisor_ID")
     @ManyToOne(optional = false)
     public Supervisor tutor;
     @JoinColumn(name = "current_advisor", referencedColumnName = "supervisor_ID")
     @ManyToOne(optional = false)
     public Supervisor currentAdvisor;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "student")
     public Collection<Trip> tripsCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    public Collection<UserCredentials> usersCredentialsCollection;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "student")
    public Collection<CourseEnrollment> coursesEnrollmentCollection;
 
 }
