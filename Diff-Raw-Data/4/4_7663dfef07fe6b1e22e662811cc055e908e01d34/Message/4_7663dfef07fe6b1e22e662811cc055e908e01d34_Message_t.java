 /**
  * Copyright (C) Posten Norge AS
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package no.digipost.api.client.representations;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import org.apache.commons.lang.StringUtils;
 
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlType(name = "message",
 		propOrder = {"messageId", "senderId", "preEncrypt", "subject", "recipient", "smsNotification", "authenticationLevel", "sensitivityLevel"})
 @XmlRootElement(name = "message")
 public class Message {
 	@XmlElement(name = "message-id", required = true)
 	protected String messageId;
 	@XmlElement(name = "sender-id")
 	protected Long senderId;
 	@XmlElement(name = "pre-encrypt")
 	protected Boolean preEncrypt;
 	@XmlElement(required = true)
 	protected String subject;
 	@XmlElement(name = "recipient")
 	protected RecipientIdentification recipient;
 	@XmlElement(name = "sms-notification")
 	protected SmsNotification smsNotification;
 	@XmlElement(name = "authentication-level")
 	protected AuthenticationLevel authenticationLevel;
 	@XmlElement(name = "sensitivity-level")
 	protected SensitivityLevel sensitivityLevel;
 
 	Message() {
 	}
 
 	public Message(final String messageId, final String subject, final PersonalIdentificationNumber id, final SmsNotification smsVarsling,
 				   final AuthenticationLevel authenticationLevel, final SensitivityLevel sensitivityLevel) {
 		this(messageId, subject, new RecipientIdentification(id), smsVarsling, authenticationLevel, sensitivityLevel);
 	}
 
 	public Message(final String messageId, final String subject, final DigipostAddress digipostAdress, final SmsNotification smsVarsling,
 				   final AuthenticationLevel authenticationLevel, final SensitivityLevel sensitivityLevel) {
 		this(messageId, subject, new RecipientIdentification(digipostAdress), smsVarsling, authenticationLevel, sensitivityLevel);
 	}
 
 	public Message(final String messageId, final String subject, final RecipientIdentification recipient, final SmsNotification smsVarsling,
 				   final AuthenticationLevel authenticationLevel, final SensitivityLevel sensitivityLevel) {
 		this.messageId = messageId;
 		this.subject = subject;
 		this.recipient = recipient;
 		smsNotification = smsVarsling;
 		this.authenticationLevel = authenticationLevel;
 		this.sensitivityLevel = sensitivityLevel;
 	}
 
 	public Message(final String messageId, final PrintDetails printDetails) {
 		this(messageId, null, new RecipientIdentification(printDetails), null, null, null);
 	}
 
 	public String getSubject() {
 		return subject;
 	}
 
 	public boolean hasSubject() {
 		return !StringUtils.isBlank(subject);
 	}
 
 	public SmsNotification getSmsNotification() {
 		return smsNotification;
 	}
 
 	public AuthenticationLevel getAuthenticationLevel() {
 		return authenticationLevel;
 	}
 
 	public SensitivityLevel getSensitivityLevel() {
 		return sensitivityLevel;
 	}
 
 	public RecipientIdentification getRecipient() {
 		return recipient;
 	}
 
 	public String getMessageId() {
 		return messageId;
 	}
 
	public boolean isPreEncrypt() {
		return preEncrypt != null && preEncrypt;
 	}
 
 	public boolean isSameMessageAs(final Message message) {
 		return messageId.equals(message.getMessageId());
 	}
 
 	public void setPreEncrypt() {
 		this.preEncrypt = true;
 	}
 
 	/**
 	 * Only neccessary when sending on behalf of another user.
 	 * In this case senderId must be the party you are sending on behalf of.
 	 * Your own user id should be set in the http header X-Digipost-UserId.
 	 */
 	public void setSenderId(final long senderId) {
 		this.senderId = senderId;
 	}
 
 	public void setPreEncrypt(final boolean preEncrypt) {
 		this.preEncrypt = preEncrypt;
 	}
 }
