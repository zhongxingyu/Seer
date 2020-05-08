 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.fhg.fokus.persistence;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement; import org.codehaus.jackson.map.annotate.JsonSerialize;
 import javax.xml.bind.annotation.XmlTransient;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author Hannes Gorges
  */
 @Entity
 @Table(name = "smpaccount")
 @XmlRootElement  @JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY)
 @NamedQueries({
     @NamedQuery(name = "Smpaccount.findAll", query = "SELECT s FROM Smpaccount s"),
     @NamedQuery(name = "Smpaccount.findByUsername", query = "SELECT s FROM Smpaccount s WHERE s.username = :username")})
 public class Smpaccount implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @NotNull
     @Column(name = "idSmpAccount")
     private Integer idSmpAccount;
     @Size(max = 255)
     @Column(name = "fullname")
     private String fullname;
     @Size(max = 255)
     @Column(name = "provider")
     private String provider;
     @Size(max = 255)
     @Column(name = "providerId")
     private String providerId;
     @Size(max = 255)
     @Column(name = "authMethod")
     private String authMethod;
     @Size(max = 255)
     @Column(name = "accessToken")
     private String accessToken;
     @Size(max = 255)
     @Column(name = "secret")
     private String secret;
     @Size(max = 255)
     @Column(name = "token")
     private String token;
     @Size(max = 255)
     @Column(name = "profileUrl")
     private String profileUrl;
     @Size(max = 255)
     @Column(name = "profilePhoto")
     private String avatarUrl;
     @Size(max = 255)
     @Column(name = "username")
     private String username;
     @JoinColumn(name = "idUserData", referencedColumnName = "idUserData")
     @ManyToOne(optional = false)
     private Userdata idUserData;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idSmpAccount")
     private List<Publishchannel> publishchannelList;    
 
     public Smpaccount() {
     }
 
     public Smpaccount(Integer idSmpAccount) {
         this.idSmpAccount = idSmpAccount;
     }
 
     public Integer getIdSmpAccount() {
         return idSmpAccount;
     }
 
     public void setIdSmpAccount(Integer idSmpAccount) {
         this.idSmpAccount = idSmpAccount;
     }
 
     public String getFullname() {
         return fullname;
     }
 
     public void setFullname(String fullname) {
         this.fullname = fullname;
     }
 
     public String getProvider() {
         return provider;
     }
 
     public void setProvider(String network) {
         this.provider = network;
     }
 
     public String getProviderId() {
         return providerId;
     }
 
     public void setProviderId(String providerId) {
         this.providerId = providerId;
     }
 
     public String getAccessToken() {
         return accessToken;
     }
 
     public void setAccessToken(String accessToken) {
         this.accessToken = accessToken;
     }
 
     public String getSecret() {
         return secret;
     }
 
     public void setSecret(String secret) {
         this.secret = secret;
     }
 
     public String getToken() {
         return token;
     }
 
     public void setToken(String token) {
         this.token = token;
     }
 
     public String getProfileUrl() {
         return profileUrl;
     }
 
     public void setProfileUrl(String profileUrl) {
         this.profileUrl = profileUrl;
     }
 
     public String getAvatarUrl() {
         return avatarUrl;
     }
 
     public void setAvatarUrl(String avatarUrl) {
         this.avatarUrl = avatarUrl;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public Userdata getIdUserData() {
         return idUserData;
     }
 
     public void setIdUserData(Userdata idUserData) {
         this.idUserData = idUserData;
     }
 
        @JsonIgnore     @XmlTransient
     public List<Publishchannel> getPublishchannelList() {
         return publishchannelList;
     }
 
     public void setPublishchannelList(List<Publishchannel> publishchannelList) {
         this.publishchannelList = publishchannelList;
     }    
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (idSmpAccount != null ? idSmpAccount.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         
         if (!(object instanceof Smpaccount)) {
             return false;
         }
         Smpaccount other = (Smpaccount) object;
         if ((this.idSmpAccount == null && other.idSmpAccount != null) || (this.idSmpAccount != null && !this.idSmpAccount.equals(other.idSmpAccount))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "de.fhg.fokus.persistence.Smpaccount[ idSmpAccount=" + idSmpAccount + " ]";
     }
 
     /**
      * @return the authMethod
      */
     public String getAuthMethod() {
         return authMethod;
     }
 
     /**
      * @param authMethod the authMethod to set
      */
     public void setAuthMethod(String authMethod) {
         this.authMethod = authMethod;
     }
     
 }
