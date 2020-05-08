 package pl.booone.iplay.models.users;
 
 import pl.booone.iplay.models.BaseDTO;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import java.util.Date;
 
 @Entity
 @Table(name = "USERS")
 public class UserDTO extends BaseDTO {
 
     @Id
     @Column(name = "ID")
     @GeneratedValue
     private Integer id;
 
     @Column(name = "NAME")
     private String name;
 
     @Column(name = "SURNAME")
     private String surname;
 
     @Column(name = "EMAIL")
     private String email;
 
     @Column(name = "PASSWORD")
     private String password;
 
     @Column(name = "BIRTHDAY")
     private Date birthday;
 
     @Column(name = "COUNTRY")
     private String country;
 
     public UserDTO() {
     }
 
     public UserDTO(String newName, String newSurname,
                    String newEmail, String newPassword,
                    Date newBirthday, String newCountry) {
         this.name = newName;
         this.surname = newSurname;
         this.email = newEmail;
         this.password = newPassword;
        setBirthday(newBirthday);
         this.country = newCountry;
     }
 
     public Integer getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String newName) {
         this.name = newName;
     }
 
     public String getSurname() {
         return surname;
     }
 
     public void setSurname(String newSurname) {
         this.surname = newSurname;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String newEmail) {
         this.email = newEmail;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String newPassword) {
         this.password = newPassword;
     }
 
     public Date getBirthday() {
         return birthday != null
                 ? (Date) birthday.clone()
                 : null;
     }
 
     public void setBirthday(Date newBirthday) {
         this.birthday = newBirthday != null
                 ? (Date) newBirthday.clone()
                 : null;
     }
 
     public String getCountry() {
         return country;
     }
 
     public void setCountry(String newCountry) {
         this.country = newCountry;
     }
 }
