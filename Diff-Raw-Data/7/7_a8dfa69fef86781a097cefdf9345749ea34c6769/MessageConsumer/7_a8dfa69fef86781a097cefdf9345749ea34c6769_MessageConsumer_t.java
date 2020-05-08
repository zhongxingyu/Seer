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
 @Table(name="message_consumer")
 public class MessageConsumer implements Serializable {
 
         /** Contains the serialized UID */
         private static final long serialVersionUID = 1L;
 
         /** Contains the createdBy. */
         @Column(name="created_by", nullable=false, length=30)
         private String createdBy;
 
         /** Contains the messageConsumerKey. */
         @Id
         @Column(name="message_consumer_key", nullable=false)
         private Long messageConsumerKey;
 
         /** Contains the createdOn. */
         @Column(name="created_on", nullable=false)
         private Date createdOn;
 
         /** Contains the consumerEmail. */
         @Column(name="consumer_email", nullable=true, length=256)
         private String consumerEmail;
 
         /** Contains the maximumRetries. */
         @Column(name="maximum_retries", nullable=true)
         private Long maximumRetries;
 
         /** Contains the consumerRegDate. */
         @Column(name="consumer_reg_date", nullable=false)
         private Date consumerRegDate;
 
         /** Contains the consumer. */
         @Column(name="consumer", nullable=false, length=50)
         private String consumer;
 
         /** Contains the maximumFailure. */
         @Column(name="maximum_failure", nullable=true)
         private Long maximumFailure;
 
         /** Contains the retryInterval. */
         @Column(name="retry_interval", nullable=true)
         private Long retryInterval;
 
         /** Contains the lastUpdateOn. */
         @Column(name="last_update_on", nullable=false)
         private Date lastUpdateOn;
 
         /** Contains the consumerDestination. */
         @Column(name="consumer_destination", nullable=true, length=128)
         private String consumerDestination;
 
         /** Contains the endDate. */
         @Column(name="end_date", nullable=true)
         private Date endDate;
 
         /** Contains the maximumDestinationSize. */
         @Column(name="maximum_destination_size", nullable=true)
         private Long maximumDestinationSize;
 
         /** Contains the suspendFlag. */
         @Column(name="suspend_flag", nullable=false, length=1)
         private String suspendFlag;
 
         /** Contains the lastUpdateBy. */
         @Column(name="last_update_by", nullable=false, length=30)
         private String lastUpdateBy;
 
         /** Contains the destinationType. */
         @Column(name="destination_type", nullable=true, length=30)
         private String destinationType;
 
         /** Contains the startDate. */
         @Column(name="start_date", nullable=false)
         private Date startDate;
 
         /**
          * Constructor
          */
         public MessageConsumer() {
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
          * @return the messageConsumerKey
          */
         public Long getMessageConsumerKey() {
                 return messageConsumerKey;
         }
 
         /**
          * @param messageConsumerKey the messageConsumerKey to set.
          */
         public void setMessageConsumerKey(Long messageConsumerKey) {
                 this.messageConsumerKey = messageConsumerKey;
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
          * @return the consumerEmail
          */
         public String getConsumerEmail() {
                 return consumerEmail;
         }
 
         /**
          * @param consumerEmail the consumerEmail to set.
          */
         public void setConsumerEmail(String consumerEmail) {
                 this.consumerEmail = consumerEmail;
         }
 
         /**
          * @return the maximumRetries
          */
         public Long getMaximumRetries() {
                 return maximumRetries;
         }
 
         /**
          * @param maximumRetries the maximumRetries to set.
          */
         public void setMaximumRetries(Long maximumRetries) {
                 this.maximumRetries = maximumRetries;
         }
 
         /**
          * @return the consumerRegDate
          */
         public Date getConsumerRegDate() {
                 return consumerRegDate;
         }
 
         /**
          * @param consumerRegDate the consumerRegDate to set.
          */
         public void setConsumerRegDate(Date consumerRegDate) {
                 this.consumerRegDate = consumerRegDate;
         }
 
         /**
          * @return the consumer
          */
         public String getConsumer() {
                 return consumer;
         }
 
         /**
          * @param consumer the consumer to set.
          */
         public void setConsumer(String consumer) {
                 this.consumer = consumer;
         }
 
         /**
          * @return the maximumFailure
          */
         public Long getMaximumFailure() {
                 return maximumFailure;
         }
 
         /**
          * @param maximumFailure the maximumFailure to set.
          */
         public void setMaximumFailure(Long maximumFailure) {
                 this.maximumFailure = maximumFailure;
         }
 
         /**
          * @return the retryInterval
          */
         public Long getRetryInterval() {
                 return retryInterval;
         }
 
         /**
          * @param retryInterval the retryInterval to set.
          */
         public void setRetryInterval(Long retryInterval) {
                 this.retryInterval = retryInterval;
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
 
         /**
          * @return the consumerDestination
          */
         public String getConsumerDestination() {
                 return consumerDestination;
         }
 
         /**
          * @param consumerDestination the consumerDestination to set.
          */
         public void setConsumerDestination(String consumerDestination) {
                 this.consumerDestination = consumerDestination;
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
          * @return the maximumDestinationSize
          */
         public Long getMaximumDestinationSize() {
                 return maximumDestinationSize;
         }
 
         /**
          * @param maximumDestinationSize the maximumDestinationSize to set.
          */
         public void setMaximumDestinationSize(Long maximumDestinationSize) {
                 this.maximumDestinationSize = maximumDestinationSize;
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
          * @return the destinationType
          */
         public String getDestinationType() {
                 return destinationType;
         }
 
         /**
          * @param destinationType the destinationType to set.
          */
         public void setDestinationType(String destinationType) {
                 this.destinationType = destinationType;
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
 
 }
