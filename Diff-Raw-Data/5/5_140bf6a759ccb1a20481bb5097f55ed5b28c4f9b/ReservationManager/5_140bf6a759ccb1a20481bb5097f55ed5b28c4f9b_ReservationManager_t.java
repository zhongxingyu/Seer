 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package projekt.teama.reservierung;
 
 import java.io.Serializable;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import projekt.fhv.teama.classes.leistungen.IZusatzleistung;
 import projekt.fhv.teama.classes.personen.IAdresse;
 import projekt.fhv.teama.classes.personen.IGast;
 import projekt.fhv.teama.classes.personen.ILand;
 import projekt.fhv.teama.classes.zimmer.*;
 import projekt.fhv.teama.hibernate.dao.leistungen.IZusatzleistungDao;
 import projekt.fhv.teama.hibernate.dao.leistungen.LeistungDao;
 import projekt.fhv.teama.hibernate.dao.leistungen.ZusatzleistungDao;
 import projekt.fhv.teama.hibernate.dao.personen.*;
 import projekt.fhv.teama.hibernate.dao.zimmer.*;
 import projekt.fhv.teama.hibernate.exceptions.DatabaseException;
 import projekt.fhv.teama.model.ModelZimmer;
 import projekt.teama.reservierung.wrapper.CategoryWrapper;
 import projekt.teama.reservierung.wrapper.CountryWrapper;
 import projekt.teama.reservierung.wrapper.PackageWrapper;
 
 /**
  *
  * @author mike
  */
 @ManagedBean
 @SessionScoped
 public class ReservationManager implements Serializable {
 
     //<editor-fold defaultstate="collapsed" desc="Fields und co">
     //Zeitraum
     private String arrival;
     private String departure;
     //Gastdaten
     private IGast guest;
     private IAdresse address;
     private Integer country;
     //Packete
     private Integer packageID = null;
     //Sonstiges
     private SimpleDateFormat dateformatter = new SimpleDateFormat("dd/MM/yyyy");
     // fuer alle kategorien ein element mit kategorienamen und anzahl der freien zimmer
     private List<CategoryWrapper> categories = null;
     //Fuer den Hund
     private boolean pet = false;
     // fuer schritt 3
     private double totalCosts;
     private long days;
     private HttpSession session;
 
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="Konstuktoren">
     public ReservationManager() {
         
         this.address = null;
         this.country = null;
         this.guest = null;
         this.arrival = "";
         this.departure = "";
        this.packageID = null;
        this.pet = false;
              
           
         FacesContext context = FacesContext.getCurrentInstance();
         HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
         this.session = ((HttpServletRequest) request).getSession();
 
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Timespan für den Aufenhalt">
     public String getArrival() {
         return arrival;
     }
 
     public void setArrival(String arrival) {
         this.arrival = arrival;
     }
 
     public String getDeparture() {
         return departure;
     }
 
     public void setDeparture(String departure) {
         this.departure = departure;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Zusatzleistungen">
     public Integer getPackageID() {
         return packageID;
     }
 
     public void setPackageID(Integer packageID) {
         this.packageID = packageID;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Dog">
     public boolean getPet() {
         return pet;
     }
 
     public void setPet(boolean pet) {
         this.pet = pet;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Schritte">
     public String stepOne() {
         clearAttributes();
         return "reservation";
     }
 
     public String stepTwo() {
         clearAttributes();
         if (checkDate()) {
             return "reservation2";
         } else {
             this.session.setAttribute("DateError", true);
             return "reservation";
         }
     }
 
     public String stepThree() {
         clearAttributes();
         if (testIfRoomSelected()) {
             calcTotalCosts();
             return "reservation3";
         } else {
             this.session.setAttribute("NoRoomSelected", true);
             return "reservation2";
         }
     }
 
     public String finish() {
         clearAttributes();
         if (saveReservationInDB()) {
             this.session.setAttribute("Confirmed", true);
             return "reservation3";
         } else {
             session.setAttribute("ErrorSave", true);
             return "reservation3";
         }
     }
 
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="Methode um Daten aus der DB zu holen">
     public List<CategoryWrapper> getCategories() {
         if (categories == null) {
             categories = new Vector<CategoryWrapper>();
             IZimmerpreisDao zpDao = ZimmerpreisDao.getInstance();
             try {
                 for (IKategorie category : KategorieDao.getInstance().getAll()) {
                     List<IZimmerpreis> zimmerpreise = new Vector<IZimmerpreis>(zpDao.getAll());
                     float preisOfKategorie = 0.0f;
                     for (IZimmerpreis preis : zimmerpreise) {
                         if (preis.getKategorie().equals(category)) {
                             preisOfKategorie = preis.getPreis();
                         }
                     }
                     categories.add(new CategoryWrapper(category, 0, getAvailableRooms(category), preisOfKategorie));
                 }
             } catch (DatabaseException ex) {
                 Logger.getLogger(ReservationManager.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         return categories;
     }
 
     public Integer getAvailableRooms(IKategorie category) {
         try {
             java.sql.Date ar = new java.sql.Date(dateformatter.parse(dateAdapter(getArrival())).getTime());
             java.sql.Date de = new java.sql.Date(dateformatter.parse(dateAdapter(getDeparture())).getTime());
             ModelZimmer modelzimmer = new ModelZimmer();
             return modelzimmer.getVerfuegbareZimmer(category, ar, de).size();
 
         } catch (ParseException ex) {
             return 0;
         } catch (DatabaseException e) {
             return 0;
         }
     }
 
     public List<CountryWrapper> getCountries() {
         ILandDao landDao = LandDao.getInstance();
         List<ILand> countriesInDatabase = new Vector<ILand>();
         List<CountryWrapper> countries = new Vector<CountryWrapper>();
         try {
             countriesInDatabase = new Vector<ILand>(landDao.getAll());
             for (ILand country : countriesInDatabase) {
                 countries.add(new CountryWrapper(country.getID(), country.getBezeichnung()));
             }
             return countries;
         } catch (DatabaseException ex) {
             return countries;
         }
     }
 
     public List<PackageWrapper> getPackages() {
         IZusatzleistungDao zbDao = ZusatzleistungDao.getInstance();
         List<IZusatzleistung> packagesInDatabase = new Vector<IZusatzleistung>();
         List<PackageWrapper> packages = new Vector<PackageWrapper>();
         try {
             packagesInDatabase = new Vector<IZusatzleistung>(zbDao.getAll());
             for (IZusatzleistung p : packagesInDatabase) {
                 if (p.getWarengruppe().getID() == 11) {
                     packages.add(new PackageWrapper(p, p.getID(), p.getBezeichnung()));
                 }
             }
             return packages;
         } catch (DatabaseException ex) {
             return packages;
         }
     }
 
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="Adapter">
     private String dateAdapter(String str) {
         String[] temp = new String[10];
         String delimiter = "/";
         temp = str.split(delimiter);
         if (temp.length == 3) {
             return temp[1] + "/" + temp[0] + "/" + temp[2];
         } else {
             return "20/10/1990";
         }
 
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Datums und Raumeingaben überprüfen">
     private boolean testIfRoomSelected() {
         int count = 0;
         for (CategoryWrapper entry : categories) {
             if (entry.getChosenRooms().equals(0)) {
                 count++;
             }
         }
 
         if (count >= categories.size()) {
             return false;
         }
 
         return true;
     }
 
     private boolean checkDate() {
 
         java.sql.Date today = new java.sql.Date(new java.util.Date().getTime());
 
         try {
             java.sql.Date ar = new java.sql.Date(dateformatter.parse(dateAdapter(getArrival())).getTime());
             java.sql.Date de = new java.sql.Date(dateformatter.parse(dateAdapter(getDeparture())).getTime());
 
             this.days = ((de.getTime() - ar.getTime()) / 1000 / 60 / 60 / 24) + 1;
 
             if (de.after(ar) && ar.after(today)) {
                 return true;
             } else {
                 return false;
             }
 
         } catch (ParseException ex) {
             return false;
         }
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Speicher Methode">
     private boolean saveReservationInDB() {
         if (guest != null) {
             try {
 
                 //Adresse Updaten  
                 ILand land = LandDao.getInstance().getById(this.country);
                 this.address.setLand(land);
 
                 List<IAdresse> adrs = new Vector<IAdresse>(this.guest.getAdressen());
                 IAdresse a1 = adrs.get(0);
                 a1 = this.address;
 
                 //DB aktion Adresse
                 IAdresseDao adressDao = AdresseDao.getInstance();
                 adressDao.update(this.address);
 
                 //DB aktion Gast
                 IGastDao gastDao = GastDao.getInstance();
                 gastDao.create(this.guest);
 
                 //Reservierung erstellen;
 
                 //Datum fuer die Reservierung
                 java.sql.Date ar = new java.sql.Date(dateformatter.parse(dateAdapter(getArrival())).getTime());
                 java.sql.Date de = new java.sql.Date(dateformatter.parse(dateAdapter(getDeparture())).getTime());
 
                 //Zusatzleistung
                 IZusatzleistungDao zlDao = ZusatzleistungDao.getInstance();
                 IZusatzleistung pack = zlDao.getById(this.packageID);
 
                 //Gäste der Reservierung hinzufügen
                 Set<IGast> gaeste = new HashSet<IGast>();
                 gaeste.add(this.guest);
 
                 //Reservierung
                 IReservierung res = new Reservierung(ar, de, guest, null, false, this.pet, pack, null, null, gaeste, null);
 
                 IReservierungDao resDao = ReservierungDao.getInstance();
                 resDao.create(res);
 
                 //Teilreservierungen
                 ITeilreservierungDao teilresDao = TeilreservierungDao.getInstance();
                 for (CategoryWrapper entry : this.categories) {
                     if (entry.getChosenRooms() > 0) {
                         ITeilreservierung tres = new Teilreservierung(entry.getCat(), res, entry.getChosenRooms());
                         try {
                             teilresDao.create(tres);
                         } catch (DatabaseException ex) {
                             return false;
                         }
                     }
                 }
 
                 return true;
 
             } catch (DatabaseException ex) {
                 return false;
             } catch (ParseException e) {
                 return false;
             }
 
         }
 
         return false;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Set/Get Gast">
     public IGast getGuest() {
         return guest;
     }
 
     public void setGuest(IGast gast) {
         this.guest = gast;
 
         List<IAdresse> adrs = new Vector<IAdresse>(this.guest.getAdressen());
         this.address = adrs.get(0);
 
         this.country = this.address.getLand().getID();
 
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="getter und setter">
     public Integer getCountry() {
         return this.country;
     }
     
     public void setCountry(Integer country) {
         this.country = country;
     }
     
     public long getDays() {
         return days;
     }
     
     public void setDays(long days) {
         this.days = days;
     }
     
     public double getTotalCosts() {
         return totalCosts;
     }
     
     public void setTotalCosts(double totalCosts) {
         this.totalCosts = totalCosts;
     }
     
     public IAdresse getAddress() {
         return address;
     }
     
     public void setAddress(IAdresse address) {
         this.address = address;
     }
     //</editor-fold>
 
     private void clearAttributes() {
 
         this.session.setAttribute("DateError", false);
         this.session.setAttribute("Confirmed", false);
         this.session.setAttribute("ErrorSave", false);
         this.session.setAttribute("NoRoomSelected", false);
     }
 
     private void calcTotalCosts() {
         float costs = 0;
 
         // Kosten pro Zimmer
         for (CategoryWrapper c : categories) {
             costs += c.getCost() * c.getChosenRooms();
         }
 
         // Package
         costs += 0;
 
         // Anzahl der Tage        
         this.totalCosts = costs * days;
     }
 
     //<editor-fold defaultstate="collapsed" desc="Bezeichnung für Land und Package holen">
     public String getLand() {
         if (this.country != null) {
             try {
                 return LandDao.getInstance().getById(this.country).getBezeichnung();
             } catch (Exception ex) {
                 return ex.getMessage();
             }
         }
         return "0";
     }
 
     public String getPackage() {
         if (this.packageID != null) {
             try {
                 return LeistungDao.getInstance().getById(this.packageID).getBezeichnung();
             } catch (Exception ex) {
                 return ex.getMessage();
             }
         }
         return "0";
     }
     //</editor-fold>
 }
