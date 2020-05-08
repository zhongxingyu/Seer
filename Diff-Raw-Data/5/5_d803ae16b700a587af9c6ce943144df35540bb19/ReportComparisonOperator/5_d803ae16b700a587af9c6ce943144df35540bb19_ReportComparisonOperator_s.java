 package com.adaptc.mws.plugins;
 
 /**
  * This enumeration is used when Moab needs to compare items. Used in
  * Access Control Lists (ACLs).
  *
  * @author jpratt
  *
  */
 
 public enum ReportComparisonOperator {
 	/**
 	 * Valid values: "&gt;", "gt"
 	 */
 	GREATER_THAN(">", "gt"),
 	/**
 	 * Valid values: "&gt;=", "ge"
 	 */
 	GREATER_THAN_OR_EQUAL(">=", "ge"),
 	/**
 	 * Valid values: "&lt;", "lt"
 	 */
 	LESS_THAN("<", "lt"),
 	/**
 	 * Valid values: "&lt;=", "le"
 	 */
 	LESS_THAN_OR_EQUAL("<=", "le"),
 	/**
 	 * Valid values: "==", "eq", "="
 	 */
 	EQUAL("=", "eq", "=="),
 	/**
 	 * Valid values: "!=", "ne", "&lt;&gt;"
 	 */
 	NOT_EQUAL("!=", "ne", "<>"),
 	/**
 	 * Valid value: "%&lt;"
 	 */
 	LEXIGRAPHIC_SUBSTRING("%<"),
 	/**
 	 * Valid value: "%!"
 	 */
 	LEXIGRAPHIC_NOT_EQUAL("%!"),
 	/**
 	 * Valid value: "%="
 	 */
 	LEXIGRAPHIC_EQUAL("%=");
 
 	private String compare1;
 	private String compare2;
 	private String compare3;
 
 	private ReportComparisonOperator(String compare1) {
 		this.compare1 = compare1;
 		this.compare2 = null;
 		this.compare3 = null;
 	}
 
 	private ReportComparisonOperator(String compare1,
 									String compare2) {
 		this.compare1 = compare1;
 		this.compare2 = compare2;
 		this.compare3 = null;
 	}
 
 	private ReportComparisonOperator(String compare1,
 									String compare2,
 									String compare3) {
 		this.compare1 = compare1;
 		this.compare2 = compare2;
 		this.compare3 = compare3;
 	}
 
 	public String toString() {
 		return compare1;
 	}
 
 	public String toString2() {
 		return compare2;
 	}
 
 	public static ReportComparisonOperator parse(String string) {
 		for (ReportComparisonOperator compare : values()) {
 			if (compare.name().equalsIgnoreCase(string) ||
					compare.compare1 == string ||
 					compare.compare2 != null && compare.compare2.equalsIgnoreCase(string) ||
					compare.compare3 == string) {
 				return compare;
 			}
 		}
 		return null;
 	}
 }
