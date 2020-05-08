 /* SVN FILE: $Id: BuildBean.java 5970 2013-01-04 15:50:31Z jvuccolo $ */
 package edu.psu.iam.cpr.utility.beans;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  *
  * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 United States License. To
  * view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/us/ or send a letter to Creative
  * Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  *
  * @package edu.psu.iam.cpr.core.database.beans
  * @author $Author: jvuccolo $
  * @version $Rev: 5970 $
  * @lastrevision $Date: 2013-01-04 10:50:31 -0500 (Fri, 04 Jan 2013) $
  */
 
 @Entity
 @Table(name="cpr_access_groups")
 public class CprAccessGroups implements Serializable {
 
         /** Contains the serialized UID */
         private static final long serialVersionUID = 1L;
 
         /** Contains the createdBy. */
         @Column(name="created_by", nullable=false, length=30)
         private String createdBy;
 
         /** Contains the endDate. */
         @Column(name="end_date", nullable=true)
         private Date endDate;
 
         /** Contains the accessGroup. */
         @Column(name="access_group", nullable=false, length=60)
         private String accessGroup;
 
         /** Contains the suspendFlag. */
         @Column(name="suspend_flag", nullable=false, length=1)
         private String suspendFlag;
 
         /** Contains the cprAccessGroupsKey. */
         @Id
         @Column(name="cpr_access_groups_key", nullable=false)
         private Long cprAccessGroupsKey;
 
         /** Contains the createdOn. */
         @Column(name="created_on", nullable=false)
         private Date createdOn;
 
         /** Contains the lastUpdateBy. */
         @Column(name="last_update_by", nullable=false, length=30)
         private String lastUpdateBy;
 
         /** Contains the raServerPrincipalKey. */
         @Column(name="ra_server_principal_key", nullable=false)
         private Long raServerPrincipalKey;
 
         /** Contains the startDate. */
         @Column(name="start_date", nullable=false)
         private Date startDate;
 
         /** Contains the lastUpdateOn. */
         @Column(name="last_update_on", nullable=false)
         private Date lastUpdateOn;
 
         /**
          * Constructor
          */
         public CprAccessGroups() {
             super();
         }
 
         /**
          * @return the createdBy
          */
         public String getCreatedBy() {
                 return createdBy;
         }
 
         /**
          * @param createdBy the createdBy to set.
          */
         public void setCreatedBy(String createdBy) {
                 this.createdBy = createdBy;
         }
 
         /**
          * @return the endDate
          */
         public Date getEndDate() {
                 return endDate;
         }
 
         /**
          * @param endDate the endDate to set.
          */
         public void setEndDate(Date endDate) {
                 this.endDate = endDate;
         }
 
         /**
          * @return the accessGroup
          */
         public String getAccessGroup() {
                 return accessGroup;
         }
 
         /**
          * @param accessGroup the accessGroup to set.
          */
         public void setAccessGroup(String accessGroup) {
                 this.accessGroup = accessGroup;
         }
 
         /**
          * @return the suspendFlag
          */
         public String getSuspendFlag() {
                 return suspendFlag;
         }
 
         /**
          * @param suspendFlag the suspendFlag to set.
          */
         public void setSuspendFlag(String suspendFlag) {
                 this.suspendFlag = suspendFlag;
         }
 
         /**
          * @return the cprAccessGroupsKey
          */
         public Long getCprAccessGroupsKey() {
                 return cprAccessGroupsKey;
         }
 
         /**
          * @param cprAccessGroupsKey the cprAccessGroupsKey to set.
          */
         public void setCprAccessGroupsKey(Long cprAccessGroupsKey) {
                 this.cprAccessGroupsKey = cprAccessGroupsKey;
         }
 
         /**
          * @return the createdOn
          */
         public Date getCreatedOn() {
                 return createdOn;
         }
 
         /**
          * @param createdOn the createdOn to set.
          */
         public void setCreatedOn(Date createdOn) {
                 this.createdOn = createdOn;
         }
 
         /**
          * @return the lastUpdateBy
          */
         public String getLastUpdateBy() {
                 return lastUpdateBy;
         }
 
         /**
          * @param lastUpdateBy the lastUpdateBy to set.
          */
         public void setLastUpdateBy(String lastUpdateBy) {
                 this.lastUpdateBy = lastUpdateBy;
         }
 
         /**
          * @return the raServerPrincipalKey
          */
         public Long getRaServerPrincipalKey() {
                 return raServerPrincipalKey;
         }
 
         /**
          * @param raServerPrincipalKey the raServerPrincipalKey to set.
          */
         public void setRaServerPrincipalKey(Long raServerPrincipalKey) {
                 this.raServerPrincipalKey = raServerPrincipalKey;
         }
 
         /**
          * @return the startDate
          */
         public Date getStartDate() {
                 return startDate;
         }
 
         /**
          * @param startDate the startDate to set.
          */
         public void setStartDate(Date startDate) {
                 this.startDate = startDate;
         }
 
         /**
          * @return the lastUpdateOn
          */
         public Date getLastUpdateOn() {
                 return lastUpdateOn;
         }
 
         /**
          * @param lastUpdateOn the lastUpdateOn to set.
          */
         public void setLastUpdateOn(Date lastUpdateOn) {
                 this.lastUpdateOn = lastUpdateOn;
         }
 
 }
