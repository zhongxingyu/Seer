 package models.main;
 
 import ext.Ext;
 import ext.ExtINS;
 import models.conge.Holiday;
 import models.ins.Contract;
 import models.ins.Professional;
 import models.ma.*;
 import models.mj.MJRoom;
 import models.mj.MJStay;
 import models.rh.Department;
 import models.rh.Division;
 import models.sa.Shop;
 import models.security.Role;
 import models.security.User;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 import utils.Dates;
 import utils.Utils;
 
 import javax.persistence.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 @Entity
 public class Person extends Model implements Comparable<Person> {
 
     @Required
     public String name;
     @Required
     public String firstname;
     public int birthDay;
     public int birthMonth;
     public int birthYear;
     public String gsm;
     public String phone;
     public String email;
     public String nationality;
     public String nationalNumber;
     public String securityNumber;
     public String birthPlace;
     public int civilStatus;
     public int driverLicence;
     public String talkLanguage;
     public String writeLanguage;
     @Lob
     public String otherTalkLanguage;
     @Lob
     public String otherWriteLanguage;
     @Embedded
     public Address address;
     @ManyToOne
     public Department department;
     public Blob picture;
     // travailleur maison accueil
     public int nbFolders;
     @ManyToOne
     public Floor floor;
     @OneToOne(mappedBy = "person")
     public User user;
     @OneToOne
     public Division division;
     public boolean enabled;
     // Données résidents
     public int nbStays;
     public long folderNumber;
     public long folderNumberMJ;
     public String contactPerson;
     public boolean withPig;
     public int docIDType;
     @Temporal(TemporalType.DATE)
     public Date readmissionDate;
     @ManyToOne
     public Person referent1;
     @ManyToOne
     public Person referent2;
     @ManyToOne
     public Person referent3;
     @OneToOne
     public Room room;
     @OneToOne
     public MJRoom roomMJ;
     public boolean redList;
     public boolean noReadmission;
     public boolean noForMA;
     // Données magasins
     @ManyToMany
     public List<Shop> shops;
     @ElementCollection(fetch = FetchType.EAGER)
     public Set<String> personStatus = new TreeSet<String>();
     // Insertion : volontaire, art 60, tig
     public int motivation;
     @Lob
     public String motivationDetails;
     public String comment;
     @ManyToOne
     public Locality locality;
     // Employé
     public boolean chiefDepartment;
     public boolean chiefDivision;
     public int nationality2;
     public int residencePermit;
     public int familySituation;
     public int school;
 
     public Person() {
         this.personStatus = new TreeSet<String>();
     }
 
     public static List<Person> byInfo() {
         List<Department> departments = new ArrayList<Department>();
         departments.add(Department.getInfo());
 
         List<Person> persons = Person.byDepartmentsAllStatus(departments);
 
         return persons;
     }
 
     public static List<Person> byUSAAndProvShop() {
         List<Department> departments = Department.getUSAAndProvShop();
         departments.add(Department.getMagasinsChef());
 
         List<String> status = Arrays.asList(PersonStatus.employe.value,
                 PersonStatus.worker.value, PersonStatus.art60.value, PersonStatus.volunteer.value);
 
         List<Person> persons = Person.byStatusAndDepartments(departments, status);
 
         return persons;
     }
 
     public static List<Person> byWorkShop() {
         List<Department> departments = new ArrayList<Department>();
         departments.add(Department.getElectro());
         departments.add(Department.getHorizon());
 
         List<Person> persons = Person.byDepartmentsAllStatus(departments);
 
         return persons;
     }
 
     private static List<Person> byStatusAndDepartments(List<Department> departments, List<String> status) {
         List<Person> persons = Person.find("from Person p, in(p.personStatus) ps "
                 + "where p.department in (:department) "
                 + "and ps in (:status) "
                 + "and p.enabled = true "
                 + "order by p.name").
                 bind("department", departments).
                 bind("status", status).fetch();
         return persons;
     }
 
     private static List<Person> byDepartmentsAllStatus(List<Department> departments) {
         List<Person> persons = Person.find("select distinct(p) from Person p, in(p.personStatus) ps "
                 + "where p.department in (:department) "
                 + "and p.enabled = true "
                 + "order by p.name").
                 bind("department", departments).fetch();
         return persons;
     }
 
     public static List<Person> getPersons(String name, String firstname, String birthDate) throws ParseException {
         Date date = new SimpleDateFormat("dd-MM-yyyy").parse(birthDate);
 
         List<Person> persons = Person.find("(name = ? "
                 + "and firstname = ? "
                 + "or (birthDay = ? "
                 + "and birthMonth = ? "
                 + "and birthYear = ?)) "
                 + "and enabled = true ",
                 name.toLowerCase(), firstname.toLowerCase(),
                 date.getDate(),
                 date.getMonth(),
                 date.getYear() + 1900).fetch();
 
         return persons;
     }
 
     public static Long checkDuplicate(String name, String firstname, String birthDate) throws ParseException {
         Long nbPerson = 0L;
         if (birthDate.length() == 10) {
             Date date = new SimpleDateFormat("dd-MM-yyyy").parse(birthDate);
 
             nbPerson = Person.count("(name = ? "
                     + "and firstname = ? "
                     + "or (birthDay = ? "
                     + "and birthMonth = ? "
                     + "and birthYear = ?)) "
                     + "and enabled = true ",
                     name.toLowerCase(), firstname.toLowerCase(),
                     date.getDate(),
                     date.getMonth(),
                     date.getYear() + 1900);
         }
 
 
         return nbPerson;
     }
 
     public static Person getDirector() {
         User user = User.find("username = ?", "dir01").first();
         if (user == null) {
             return null;
         }
 
         return user.person;
     }
 
     public static List<Person> byReferent1And2And3(Person ref1, Person ref2,
                                                    Person ref3, String status) {
         List<Person> residents = Person.find("from Person p,in(p.personStatus) ps "
                 + "where (p.referent1 = ? "
                 + "or p.referent2 = ? "
                 + "or p.referent3 = ?) "
                 + "and p.enabled = true "
                 + "and  ps = ? "
                 + "order by p.name,p.firstname",
                 ref1, ref2, ref3, status).fetch();
         return residents;
     }
 
     public void moveToServiceX() {
         Department dep = Department.getX();
         this.department = dep;
         this.save();
     }
 
     public void setToOldResident() {
         this.personStatus.clear();
         this.personStatus.add(PersonStatus.oldResident.value);
     }
 
     public void setToVoluntaar() {
         this.personStatus.add(PersonStatus.volunteer.value);
     }
 
     public static List<Person> birthDaysByStatus(List<String> status) {
         Calendar today = new GregorianCalendar();
         Calendar yesterday = Dates.getYesterday();
         Calendar tomorrow = Dates.getTomorrow();
 
         return Person.find("select distinct(p) from Person p,in(p.personStatus) ps "
                 + "where ps in (:status)  "
                 + "and p.enabled = true "
                 + "and ((p.birthDay = :dayBefore and p.birthMonth = :monthBefore) "
                 + "or (p.birthDay = :today and p.birthMonth = :month) "
                 + "or (p.birthDay = :dayAfter and p.birthMonth = :monthAfter)) "
                 + "and p.enabled = true "
                 + "and p.department is not null "
                 + "order by p.birthDay ").
                 bind("status", status).query.setParameter("dayBefore", yesterday.get(Calendar.DAY_OF_MONTH)).
                 setParameter("today", today.get(Calendar.DAY_OF_MONTH)).
                 setParameter("dayAfter", tomorrow.get(Calendar.DAY_OF_MONTH)).
                 setParameter("monthBefore", yesterday.get(Calendar.MONTH)).
                 setParameter("month", today.get(Calendar.MONTH)).
                 setParameter("monthAfter", tomorrow.get(Calendar.MONTH)).getResultList();
     }
 
     public static List<Person> birthDaysResidentByFloor(Floor floor) {
         Calendar today = new GregorianCalendar();
         Calendar yesterday = Dates.getYesterday();
         Calendar tomorrow = Dates.getTomorrow();
 
         List<String> status = Arrays.asList(PersonStatus.resident.value, PersonStatus.beforeOutResident.value);
         return Person.find("from Person p,in(p.personStatus) ps "
                 + "where ps in (:status)  "
                 + "and ((p.birthDay = :dayBefore and p.birthMonth = :monthBefore) "
                 + "or (p.birthDay = :today and p.birthMonth = :month) "
                 + "or (p.birthDay = :dayAfter and p.birthMonth = :monthAfter)) "
                 + "and p.enabled = true "
                 + "and p.department is not null "
                 + "and (p.floor is null "
                 + "or p.floor = :floor) "
                 + "order by p.birthDay ").
                 bind("status", status).query.setParameter("dayBefore", yesterday.get(Calendar.DAY_OF_MONTH)).
                 setParameter("today", today.get(Calendar.DAY_OF_MONTH)).
                 setParameter("dayAfter", tomorrow.get(Calendar.DAY_OF_MONTH)).
                 setParameter("monthBefore", yesterday.get(Calendar.MONTH)).
                 setParameter("month", today.get(Calendar.MONTH)).
                 setParameter("monthAfter", tomorrow.get(Calendar.MONTH)).
                 setParameter("floor", floor).getResultList();
     }
 
     public static List<Person> birthDaysResident() {
         Calendar today = new GregorianCalendar();
         Calendar yesterday = Dates.getYesterday();
         Calendar tomorrow = Dates.getTomorrow();
 
         List<String> status = Arrays.asList(PersonStatus.resident.value, PersonStatus.beforeOutResident.value);
         return Person.find("from Person p,in(p.personStatus) ps "
                 + "where ps in (:status)  "
                 + "and ((p.birthDay = :dayBefore and p.birthMonth = :monthBefore) "
                 + "or (p.birthDay = :today and p.birthMonth = :month) "
                 + "or (p.birthDay = :dayAfter and p.birthMonth = :monthAfter)) "
                 + "and p.enabled = true "
                 + "and p.department is not null "
                 + "order by p.birthDay ").
                 bind("status", status).query.setParameter("dayBefore", yesterday.get(Calendar.DAY_OF_MONTH)).
                 setParameter("today", today.get(Calendar.DAY_OF_MONTH)).
                 setParameter("dayAfter", tomorrow.get(Calendar.DAY_OF_MONTH)).
                 setParameter("monthBefore", yesterday.get(Calendar.MONTH)).
                 setParameter("month", today.get(Calendar.MONTH)).
                 setParameter("monthAfter", tomorrow.get(Calendar.MONTH)).getResultList();
     }
 
     public static List<Person> personsActivated(String status) {
         return Person.find("from Person p,in(p.personStatus) ps "
                 + "where ps = ? "
                 + "and p.enabled = true "
                 + "and p.department is not null "
                 + "order by p.name", status).fetch();
     }
 
     public static Long nbResidents() {
         return Person.count("from Person p,in(p.personStatus) ps "
                 + "where ps = ? "
                 + "and p.enabled = true ", PersonStatus.resident.value);
     }
 
     public static long nbPersons(String status) {
         return Person.count("from Person p,in(p.personStatus) ps "
                 + "where ps = ? "
                 + "and p.department is not null "
                 + "and p.enabled = true ", status);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Person other = (Person) obj;
         if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
         return hash;
     }
 
     public int getPersonType() {
         if (this.personStatus.contains("000")) {
             return 0;
         }
         if (this.personStatus.contains("100")) {
             return 1;
         }
         if (this.personStatus.contains("200")) {
             return 2;
         }
         if (this.personStatus.contains("300")) {
             return 3;
         }
         if (this.personStatus.contains("400")) {
             return 4;
         }
         return -1;
     }
 
     public static List<Person> getSalers() {
         List<Person> persons = new ArrayList<Person>();
 
         List<User> users = User.find("select distinct u from User u "
                 + "join u.roles as r where r.name = ?", "userSA").fetch();
         for (User user : users) {
             if (!persons.contains(user.person)) {
                 persons.add(user.person);
             }
         }
 
         return persons;
     }
 
     public static List<Person> getTS() {
         List<Person> persons = new ArrayList<Person>();
 
         List<User> users = User.find("select distinct u from User u "
                 + "join u.roles as r "
                 + "where (r.name = ? or r.name = ?) "
                 + "and u.enabled = true "
                 + "order by u.person.name ", "adminMA", "userMA").fetch();
         for (User user : users) {
             if (!persons.contains(user.person)) {
                 persons.add(user.person);
             }
         }
 
         return persons;
     }
 
     public static List<Person> getRH() {
         List<Person> persons = new ArrayList<Person>();
 
         List<User> users = User.find("select distinct u from User u "
                 + "join u.roles as r "
                 + "where (r.name = ? or r.name = ?) "
                 + "and u.enabled = true "
                 + "order by u.person.name ", "adminRH", "userRH").fetch();
         for (User user : users) {
             if (!persons.contains(user.person)) {
                 persons.add(user.person);
             }
         }
 
         return persons;
     }
 
     public static List<Person> getInsMA() {
         List<Person> persons = new ArrayList<Person>();
 
         List<User> users = User.find("select distinct u from User u "
                 + "join u.roles as r "
                 + "where r.name = ? ", "insMA").fetch();
         for (User user : users) {
             if (!persons.contains(user.person)) {
 
                 persons.add(user.person);
             }
         }
 
         return persons;
     }
 
     public static List<Person> getSynerWorkers() {
         List<Person> persons = new ArrayList<Person>();
 
         List<User> users = User.find("select distinct u from User u "
                 + "join u.roles as r "
                 + "where r.name = ? or r.name = ?", "userSY", "adminSY").fetch();
         for (User user : users) {
             if (!persons.contains(user.person)) {
                 persons.add(user.person);
             }
         }
 
         return persons;
     }
 
     public static List<Person> getAllSalers() {
         List<Person> persons = new ArrayList<Person>();
 
         List<User> users = User.find("select distinct u from User u "
                 + "join u.roles as r "
                 + "where r.name = ? or r.name = ?", "userSA", "adminSA").fetch();
         for (User user : users) {
             if (!persons.contains(user.person)) {
 
                 persons.add(user.person);
             }
         }
 
         return persons;
     }
 
     public static long getNewFolderNumber() {
         Long folderNumber = (Long) Person.em().createQuery("select max(r.folderNumber) "
                 + "from Person r").getSingleResult();
         folderNumber++;
         return folderNumber;
     }
 
     public static void updateNBFolder(Person ts) {
         if (ts == null) {
             return;
         }
 
         long nbFolders = Person.count("from Person p,in(p.personStatus) ps "
                 + "where ps = ? "
                 + "and p.enabled = true "
                 + "and p.referent1 = ? ", PersonStatus.resident.value, ts);
 
         if (nbFolders == 0) {
             return;
         }
 
         ts.nbFolders = (int) nbFolders;
         ts.save();
 
     }
 
     public void checkStatus() {
         Date today = new Date();
         List<Activity> activities = Activity.find("actif = true and "
                 + "? >= startDate and ? <= endDate "
                 + "and resident = ?", today, today, this).fetch();
 
         for (Activity a : activities) {
             a.resident.personStatus.add("005");
             a.resident.save();
             return;
         }
 
         List<Training> trainings = Training.find("actif = true and "
                 + "? >= startDate and ? <= endDate "
                 + "and resident = ?", today, today, this).fetch();
 
         for (Training t : trainings) {
             t.resident.personStatus.add("006");
             t.resident.save();
             return;
         }
 
         List<Work> works = Work.find("actif = true and "
                 + "? >= startDate and ? <= endDate "
                 + "and resident = ?", today, today, this).fetch();
 
         for (Work w : works) {
             w.resident.personStatus.add("007");
             w.resident.save();
             return;
         }
 
         List<Itt> itts = Itt.find("actif = true and "
                 + "? >= fromDate and ? <= toDate "
                 + "and resident = ?", today, today, this).fetch();
 
         for (Itt i : itts) {
             i.resident.personStatus.add("008");
             i.resident.save();
             return;
         }
 
         this.personStatus.remove("005");
         this.personStatus.remove("006");
         this.personStatus.remove("007");
         this.personStatus.remove("008");
 
         this.save();
     }
 
     public void endContract() {
         this.referent1 = null;
         this.referent2 = null;
         this.referent3 = null;
         this.department = null;
         this.division = null;
         this.chiefDepartment = false;
         this.chiefDivision = false;
         this.personStatus.add(PersonStatus.oldPerson.value);
         this.save();
     }
 
     public void setBirthDate(String birthDate) {
         this.resetBirthDate();
         if (birthDate != null && !birthDate.isEmpty()) {
             try {
                 if (birthDate.length() == 10) {
                     Date d = new SimpleDateFormat("dd-MM-yyyy").parse(birthDate);
                     this.birthDay = d.getDate();
                     this.birthMonth = d.getMonth();
                     this.birthYear = d.getYear() + 1900;
                 }
             } catch (ParseException ex) {
             }
         }
     }
 
     private void resetBirthDate() {
         this.birthDay = 0;
         this.birthMonth = 0;
         this.birthYear = 0;
     }
 
     public void setMeatType(String meat) {
         if (meat == null || meat.equals("pig")) {
             this.withPig = true;
         } else {
             this.withPig = false;
         }
     }
 
     public void setTalkLanguage(Integer[] talkLanguage) {
         this.talkLanguage = "";
 
         if (talkLanguage != null && talkLanguage.length > 0) {
             for (int i = 0; i < talkLanguage.length; i++) {
                 if ((i + 1) == talkLanguage.length) {
                     this.talkLanguage += talkLanguage[i];
                 } else {
                     this.talkLanguage += talkLanguage[i] + ":";
                 }
 
             }
         }
     }
 
     public void setWriteLanguage(Integer[] writeLanguage) {
         this.writeLanguage = "";
 
         if (writeLanguage != null && writeLanguage.length > 0) {
             for (int i = 0; i < writeLanguage.length; i++) {
                 if ((i + 1) == writeLanguage.length) {
                     this.writeLanguage += writeLanguage[i];
                 } else {
                     this.writeLanguage += writeLanguage[i] + ":";
                 }
 
             }
         }
     }
 
     public void setFirstname(String firstname) {
         this.firstname = firstname.toLowerCase();
     }
 
     public void setName(String name) {
         this.name = name.toLowerCase();
     }
 
     public void changeStatus(String status) {
         if (this.isArt60() && PersonStatus.isRHStatus(status)) {
             Holiday.remove(this);
         }
 
         if (this.personStatus.isEmpty()) {
             this.personStatus.add(status);
             return;
         }
 
         this.removeINSStatus();
         this.removeRHStatus();
         this.personStatus.add(status);
 
         if (this.personStatus.contains("200") && this.personStatus.contains("003")) {
             this.personStatus.add("201");
         } else {
             this.personStatus.remove("201");
         }
     }
 
     private void removeINSStatus() {
         this.personStatus.remove("200");
         this.personStatus.remove("300");
         this.personStatus.remove("400");
     }
 
     private void removeRHStatus() {
         this.personStatus.remove("100");
         this.personStatus.remove("500");
     }
 
     public void updateAffectation(Professional professional) {
         this.removeNoWorkStatus();
         this.chiefDepartment = professional.chiefDepartment;
         this.chiefDivision = professional.chiefDivision;
 
         this.department = Department.findById(professional.getFirstDepartment().id);
 
         if (professional.division != null) {
             this.division = professional.division;
             this.division.person = this;
             this.division.save();
         }
 
         this.save();
 
         if (this.isResident()) {
             Stay stay = Stay.lastActif(this);
             stay.department = this.department;
             stay.save();
         }
     }
 
     private void removeNoWorkStatus() {
         this.personStatus.remove(PersonStatus.training.value);
         this.personStatus.remove(PersonStatus.work.value);
         this.personStatus.remove(PersonStatus.activity.value);
         this.personStatus.remove(PersonStatus.sick.value);
     }
 
     public void removeAffectation() {
         this.chiefDepartment = false;
         this.chiefDivision = false;
 
         this.department = null;
         this.division = null;
 
         this.save();
 
         if (this.isResident()) {
             Stay stay = Stay.lastActif(this);
             stay.department = Department.getX();
             stay.save();
         }
     }
 
     public int compareTo(Person o) {
         if (this.name.compareTo(o.name) == 0) {
             return this.firstname.compareTo(o.firstname);
         }
 
         return this.name.compareTo(o.name);
     }
 
     public List<Department> getHisDepartments() {
         if (this.chiefDivision) {
             return new ArrayList<Department>(this.division.departments);
         }
         if (this.chiefDepartment) {
             Division div = this.department.getDivision();
             if (div == null) {
                 return Arrays.asList(this.department);
             }
             return new ArrayList<Department>(div.departments);
         }
 
         return null;
     }
 
     public static HashMap<Department, List<Person>> byDepartments(List<Department> departments) {
         HashMap<Department, List<Person>> personByDepartments = new HashMap<Department, List<Person>>();
 
         for (Department department : departments) {
             List<Person> persons = Person.byDepartment(department);
 
             if (!persons.isEmpty()) {
                 personByDepartments.put(department, persons);
             }
         }
 
         return personByDepartments;
     }
 
     public static HashMap<Department, List<Person>> byDepartmentsAndStatusActiveContracts(
             List<Department> departments, List<String> status) {
         HashMap<Department, List<Person>> personByDepartments = new HashMap<Department, List<Person>>();
 
         for (Department department : departments) {
             List<Person> persons = Contract.byDepartmentAndStatus(department, status);
 
             if (!persons.isEmpty()) {
                 personByDepartments.put(department, persons);
             }
         }
 
         return personByDepartments;
     }
 
     public static List<Person> byDepartmentsAndStatusActiveContractsList(
             List<Department> departments, List<String> status) {
         List<Person> persons = new ArrayList<Person>();
 
         for (Department department : departments) {
             List<Person> list = Contract.byDepartmentAndStatus(department, status);
             persons.addAll(list);
         }
 
         Collections.sort(persons);
 
         return persons;
     }
 
     public static List<Person> byXAndResident() {
         List<Person> list = Person.find("from Person p,in(p.personStatus) ps "
                 + "where p.department = ? "
                 + "and p.enabled = true "
                 + "and ps = ? "
                 + "order by name,firstname", Department.getX(), PersonStatus.resident.value).fetch();
 
         List<Person> persons = new ArrayList<Person>();
         for (Person p : list) {
             if (p.personStatus.contains("005")
                     || p.personStatus.contains("006")
                     || p.personStatus.contains("007")
                     || p.personStatus.contains("008")) {
                 continue;
             }
 
             persons.add(p);
         }
 
         return persons;
     }
 
     public static List<Person> byXAndResidentbyFloor(Person person) {
         List<Person> list = Person.find("from Person p,in(p.personStatus) ps "
                 + "where p.department = ? "
                 + "and p.enabled = true "
                 + "and ps = ? "
                 + "and p.room.floor = ? "
                 + "and p.room is not null "
                 + "order by name,firstname", Department.getX(),
                 PersonStatus.resident.value, person.floor).fetch();
 
         List<Person> persons = new ArrayList<Person>();
         for (Person p : list) {
             if (p.personStatus.contains("005")
                     || p.personStatus.contains("006")
                     || p.personStatus.contains("007")
                     || p.personStatus.contains("008")) {
                 continue;
             }
 
             persons.add(p);
         }
 
         return persons;
     }
 
     public static List<Person> byDepartment(Department department) {
         return Person.find("department = ? "
                 + "and enabled = true "
                 + "and chiefDivision = false "
                 + "order by name,firstname", department).fetch();
     }
 
     public static List<Person> byXAndReferent(Person person) {
         Department depX = Department.getX();
         List<Person> list = Person.find("from Person p "
                 + "where (p.referent1 = ? "
                 + "or p.referent2 = ? "
                 + "or p.referent3 = ?) "
                 + "and department = ? "
                 + "and p.enabled = true "
                 + "order by p.name,p.firstname",
                 person, person, person, depX).fetch();
 
         List<Person> persons = new ArrayList<Person>();
         for (Person p : list) {
             if (p.personStatus.contains("005")
                     || p.personStatus.contains("006")
                     || p.personStatus.contains("007")
                     || p.personStatus.contains("008")) {
                 continue;
             }
 
             persons.add(p);
         }
 
         return persons;
     }
 
     public void setChiefDivisionRole() {
         if (Utils.isNotNull(this.user)) {
             this.user.roles.add(Role.getDivisionChief());
             this.user.save();
         }
     }
 
     public void removeChiefDivisionRole() {
         if (Utils.isNotNull(this.user)) {
             this.user.roles.remove(new Role("divisionChief"));
             this.user.save();
         }
     }
 
     public void setChiefDepartmentRole() {
         if (Utils.isNotNull(this.user)) {
             this.user.roles.add(Role.getDepartmentChief());
             this.user.save();
         }
     }
 
     public void removeChiefDepartmentRole() {
         if (Utils.isNotNull(this.user)) {
             this.user.roles.remove(new Role("departmentChief"));
             this.user.save();
         }
     }
 
     public static List<Person> findInsPersons() {
         List<Person> employes = new ArrayList<Person>();
 
         List<User> users = User.find("select distinct u from User u "
                 + "join u.roles as r "
                 + "where r.name in (:status) "
                 + "and u.enabled = true ").
                 bind("status", Arrays.asList("adminINS", "userINS")).fetch();
         for (User user : users) {
             if (!employes.contains(user.person)) {
                 employes.add(user.person);
             }
         }
 
         return employes;
     }
 
     public static List<Person> findRHPersons() {
         List<String> status = PersonStatus.getRHStatus();
         List<Person> employes = Person.find("from Person p,in(p.personStatus) ps "
                 + "where ps in (:status)  "
                 + "and p.enabled = true "
                 + "order by p.name,p.firstname").bind("status", status).fetch();
         return employes;
     }
 
     public static List<Person> getDivisionChiefs() {
         return Person.find("enabled = true "
                 + "and chiefDivision = true ").fetch();
     }
 
     public Blob getPicture() {
         return picture;
     }
 
     public void setPicture(Blob picture) {
         this.picture = picture;
     }
 
     @Override
     public String toString() {
         return this.name + " " + this.firstname;
     }
 
     public boolean isINSPerson() {
         return this.isVolunteer() || this.isArt60() || this.isTIG() || this.isTrainee();
     }
 
     public boolean isRHPerson() {
         return this.isEmployee() || this.isWorker();
     }
 
     public boolean isVolunteer() {
         return this.personStatus.contains("200");
     }
 
     public boolean isTIG() {
         return this.personStatus.contains("400");
     }
 
     public boolean isArt60() {
         return this.personStatus.contains("300");
     }
 
     public boolean isTrainee() {
         return this.personStatus.contains("600");
     }
 
     public boolean isEmployee() {
         return this.personStatus.contains("100");
     }
 
     public boolean isWorker() {
         return this.personStatus.contains("500");
     }
 
     public boolean isBeforeOutResident() {
         return this.personStatus.contains(PersonStatus.beforeOutResident.value);
     }
 
     public boolean isBeforeOutMJResident() {
         return this.personStatus.contains(PersonStatus.beforeOutResidentMJ.value);
     }
 
     public boolean isPostResident() {
         return this.personStatus.contains(PersonStatus.postResident.value);
     }
 
     public boolean isResidentAndOld() {
         return this.isResident() || this.isOldResident();
     }
 
     public boolean isMA() {
         return this.folderNumber > 0;
     }
 
     public boolean isMJ() {
         return this.folderNumberMJ > 0;
     }
 
     public boolean isResident() {
         return this.personStatus.contains(PersonStatus.resident.value);
     }
 
     public boolean isMJResident() {
         return this.personStatus.contains(PersonStatus.residentJ.value)
                 || this.isBeforeOutMJResident()
                 || this.isOldMJResident();
     }
 
     public boolean isBeforeMJResident() {
         return this.personStatus.contains(PersonStatus.beforeResidentJ.value);
     }
 
     public boolean isOldResident() {
         return this.personStatus.contains(PersonStatus.oldResident.value)
                 || this.personStatus.contains(PersonStatus.rejectedRequest.value);
     }
 
     public boolean isOldMJResident() {
         return this.personStatus.contains(PersonStatus.oldResidentMJ.value);
     }
 
     public boolean isOldPerson() {
         return this.personStatus.contains("011");
     }
 
     public void cancelOutStay() {
         this.personStatus.remove(PersonStatus.beforeOutResident.value);
         this.personStatus.add(PersonStatus.resident.value);
         this.save();
     }
 
     public void setToResident() {
         this.removeINSStatus();
         this.removeRHStatus();
         this.personStatus.add(PersonStatus.resident.value);
         this.personStatus.remove(PersonStatus.beforeResident.value);
         this.personStatus.remove(PersonStatus.oldResident.value);
         this.personStatus.remove(PersonStatus.postResident.value);
     }
 
     public static List<Person> suggestForV(String query) {
         List<String> s = Arrays.asList(PersonStatus.volunteer.value);
         List<Person> persons = suggestByStatus(s, query);
 
         return persons;
     }
 
     public static List<Person> suggestForShops(String q) {
         List<Department> departments = Department.getShops();
         List<Person> persons = Person.find("select distinct p "
                 + "from Person p "
                 + "where (p.name like :name or p.firstname like :firstname) "
                 + "and p.enabled = true "
                 + "and p.department in (:department) "
                 + "order by p.name, p.firstname").
                 bind("department", departments).query.setParameter("name", q + "%").setParameter("firstname", q + "%").getResultList();
         return persons;
     }
 
     public static List<Person> suggestForAll(String query) {
         List<Person> persons = Person.find("select distinct p "
                 + "from Person p "
                 + "where (p.name like ? or p.firstname like ?) "
                 + "and p.enabled = true "
                 + "order by p.name, p.firstname", "%" + query + "%","%" + query + "%")
                 .fetch(30);
 
         return persons;
     }
 
     public static List<Person> suggestForINS(String query) {
         List<String> s = PersonStatus.getINSStatus();
         List<Person> persons = suggestByStatus(s, query);
 
         return persons;
     }
 
     public static List<Person> suggestForMJ(String query) {
         List<String> s = PersonStatus.getMJStatus();
         List<Person> persons = suggestByStatus(s, query);
 
         return persons;
     }
 
     public static List<Person> suggestForRH(String query) {
         List<String> s = PersonStatus.getRHStatus();
         List<Person> persons = suggestByStatus(s, query);
 
         return persons;
     }
 
     public static List<Person> suggestForMA(String query) {
         List<String> s = PersonStatus.getResidentStatus();
         List<Person> list = suggestByStatus(s, query);
 
         List<Person> persons = new ArrayList<Person>();
         for (Person person : list) {
             if (person.folderNumber > 0) {
                 persons.add(person);
             }
         }
 
         return persons;
     }
 
     public static List<Person> suggestForMAResidentOnly(String query) {
         List<String> s = new ArrayList<String>();
         s.add(PersonStatus.resident.value);
         List<Person> list = suggestByStatus(s, query);
 
         return list;
     }
 
     private static List<Person> suggestByStatus(List<String> s, String query) {
         List<Person> persons = Person.find("select distinct p "
                 + "from Person p,in(p.personStatus) ps "
                 + "where (p.name like ? or p.firstname like ?) "
                 + "and p.enabled = true "
                 + "and ps in (:status) "
                 + "order by p.name, p.firstname", "%" + query + "%","%" + query + "%").
                 bind("status", s).fetch(30);
         return persons;
     }
 
     public static String createAutoCompleteJson(String query, List<Person> persons) {
         String json = "[";
         for (int i = 0; i < persons.size(); i++) {
            json += "{\"name\": \"" + Ext.personLabel(persons.get(i)).replace("'", "\\'");
             json += " (" + ExtINS.showStatus(persons.get(i)) + ")\"";
             json += ",\"id\": " +persons.get(i).id+"}";
             if ((i + 1) != persons.size()) {
                 json += ",";
             }
         }
         json += "]";
         return json;
     }
 
     public static String createAutoCompleteJsonNoStatus(String query, List<Person> persons) {
         String json = "[";
         for (int i = 0; i < persons.size(); i++) {
            json += "{\"name\": \"" + Ext.personLabel(persons.get(i)).replace("'", "\\'");
             json +=  "\"";
             json += ",\"id\": " +persons.get(i).id+"}";
             if ((i + 1) != persons.size()) {
                 json += ",";
             }
         }
         json += "]";
         return json;
     }
 
     public void changeToOldPerson() {
         this.personStatus.add(PersonStatus.oldPerson.value);
     }
 
     public int getStatusOrder() {
         if (this.isEmployee()) {
             return 1;
         }
 
         if (this.isWorker()) {
             return 2;
         }
 
         if (this.isVolunteer()) {
             return 3;
         }
 
         if (this.isArt60()) {
             return 4;
         }
 
         if (this.isTIG()) {
             return 5;
         }
 
         if (this.isTrainee()) {
             return 6;
         }
 
         if (this.isMJResident()) {
             return 7;
         }
 
         return 8;
     }
 
     public String getCurrentStatus() {
         if (this.isEmployee()) {
             return PersonStatus.employe.value;
         }
         if (this.isWorker()) {
             return PersonStatus.worker.value;
         }
 
         if (this.isVolunteer()) {
             return PersonStatus.volunteer.value;
         }
         if (this.isArt60()) {
             return PersonStatus.art60.value;
         }
         if (this.isTIG()) {
             return PersonStatus.tig.value;
         }
 
         return PersonStatus.resident.value;
     }
 
     public void residentOut() {
         this.personStatus.remove(PersonStatus.resident.value);
         this.personStatus.add(PersonStatus.beforeOutResident.value);
         this.department = null;
         this.save();
 
         if (this.room != null) {
             this.room.toEmpty();
         }
     }
 
     public void residentMJOut() {
         this.personStatus.remove(PersonStatus.residentJ.value);
         this.personStatus.add(PersonStatus.beforeOutResidentMJ.value);
         this.department = null;
         this.save();
 
         if (this.roomMJ != null) {
             this.roomMJ.toEmpty();
         }
     }
 
     public void cancelResidentMJOut() {
         this.personStatus.add(PersonStatus.residentJ.value);
         this.personStatus.remove(PersonStatus.beforeOutResidentMJ.value);
         this.save();
     }
 
     public void deleteResident() {
         this.enabled = false;
         this.personStatus.clear();
         this.save();
 
         if (this.room != null) {
             this.room.resident = null;
             this.room.status = 2;
             this.room.save();
         }
 
         InSummary.disableLast(this.id);
         Mutual.disableLast(this.id);
         CPAS.disableLast(this.id);
         Unemployment.disableLast(this.id);
         Pension.disableLast(this.id);
         OtherIncome.disableLast(this.id);
 
         Evaluation.disableLast(this.id);
         Partner.disableLast(this.id);
         Prescription.close(this.id);
         MedicalFolder.disableLast(this.id);
         Itt.disableLast(this.id);
         HandicapAllocation.disableLast(this.id);
         HandicapIntegration.disableLast(this.id);
         PastWork.disableLast(this.id);
         Work.disableLast(this.id);
         PastTraining.disableLast(this.id);
         Training.disableLast(this.id);
         Activity.disableLast(this.id);
         JusticeSummary.disableLast(this.id);
         Probation.disableLast(this.id);
         TigPta.disableLast(this.id);
         Tribunal.disableLast(this.id);
         Debt.disableLast(this.id);
         DebtSummary.disableLast(this.id);
         DebtMediator.disableLast(this.id);
         PropertyAdministrator.disableLast(this.id);
         RDVResident.disableLast(this.id);
 
         List<Activation> activations = Activation.find("person = ? and "
                 + "activationType != 5 and "
                 + "endDate is null", this).fetch();
         for (Activation a : activations) {
             a.delete();
         }
 
         List<RoomHistory> list = RoomHistory.find("resident = ?", this).fetch();
         for (RoomHistory rh : list) {
             rh.delete();
         }
     }
 
     public static List<Person> getTSMJ() {
         List<Person> persons = new ArrayList<Person>();
 
         List<User> users = User.getMJUsers();
         for (User user : users) {
             if (!persons.contains(user.person)) {
                 persons.add(user.person);
             }
         }
 
         return persons;
     }
 
     public static List<Person> getMJResidents() {
         List<Person> residents = Person.find("from Person p,in(p.personStatus) ps "
                 + "where p.enabled = true "
                 + "and  ps = ? "
                 + "order by p.name,p.firstname",
                 PersonStatus.residentJ.value).fetch();
         return residents;
     }
 
     public void removeMJ() {
         this.enabled = false;
         this.personStatus.clear();
         this.personStatus.add("703");
         this.save();
     }
 
     public void newMJStay() {
         this.folderNumberMJ = Person.nextFolderNumberMJ();
         this.referent1 = null;
         this.referent2 = null;
         this.room = null;
         this.save();
 
         MJStay.newStay(this);
     }
 
     private static long nextFolderNumberMJ() {
         long folderNumber = 0;
         try {
             folderNumber = Person.find("select max(folderNumberMJ) "
                     + "from Person ").first();
         } catch (NullPointerException npe) {
         }
 
         folderNumber++;
 
         return folderNumber;
     }
 
     public static long nbMJResident() {
         return Person.count("from Person p,in(p.personStatus) ps "
                 + "where p.enabled = true "
                 + "and  ps = ? ",
                 PersonStatus.residentJ.value);
     }
 
     public static Map<Floor, List<Person>> tsByFloor(List<Floor> floors) {
         Map<Floor, List<Person>> persons = new HashMap<Floor, List<Person>>();
         for (Floor f : floors) {
             List<Person> list = Person.find("from Person p,in(p.personStatus) ps "
                     + "where ps = ? "
                     + "and p.enabled = true "
                     + "and p.floor = ? "
                     + "order by p.nbFolders desc", "100", f).fetch();
             persons.put(f, list);
         }
 
         return persons;
     }
 
     public void setToOldResidentMJ() {
         this.personStatus.clear();
         this.personStatus.add(PersonStatus.oldResidentMJ.value);
     }
 
 }
