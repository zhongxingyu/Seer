 package br.com.oncast.cieloacquirer.bean.transaction.payment;
 
 import javax.xml.bind.annotation.XmlEnum;
 import javax.xml.bind.annotation.XmlEnumValue;
 import javax.xml.bind.annotation.XmlType;
 
 @XmlType(name = "autorizar")
 @XmlEnum(Integer.class)
 public enum AuthorizationType {
 	/**
 	 * Skip authorization and authenticate only
 	 */
 	@XmlEnumValue("0")
 	SKIP,
 
 	/**
 	 * Authorize only if authentication succeeds
 	 */
 	@XmlEnumValue("1")
 	STRICT,
 
 	/**
 	 * Authorize with any results from authentication
 	 */
	@XmlEnumValue("1")
 	ANY,
 
 	/**
 	 * Authorize without authentication (only for credit function) also known as "Direct Authorization" <br/>
 	 * <b>This authorization type is mandatory for:</b>
 	 * <ul>
 	 * <li>{@link CreditCardFlag#AMERICAN_EXPRESS}</li>
 	 * <li>{@link CreditCardFlag#ELO}</li>
 	 * <li>{@link CreditCardFlag#DINERS}</li>
 	 * <li>{@link CreditCardFlag#DISCOVERY}</li>
 	 * <li>{@link CreditCardFlag#JCB}</li>
 	 * <li>{@link CreditCardFlag#AURA}</li>
 	 * </ul>
 	 */
	@XmlEnumValue("1")
 	DIRECT,
 
 	/**
 	 * For recurring transactions
 	 */
 	@XmlEnumValue("4")
 	RECURRING;
 
 }
