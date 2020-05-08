 package it.uninsubria.invoice.domain;
 
 public class LineItem {
 
 	private final String description;
 	private final Money amount;
 
 	public LineItem(String description, Money amount) {
 		this.description = description;
 		this.amount = amount;
 	}
 
 	public String description() {
 		return description;
 	}
 
 	public Money amount() {
 		return amount;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("%s: %s", description, amount);
 	}
 	
 	public String toJson() {
		String template = "{ description: '%s', amountInCents: %d, }";
 		return String.format(template, description, amount.toCents());
 	}
 
 	@Override
 	public int hashCode() {
 		return description.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		LineItem other = (LineItem) obj;
 		if (amount == null) {
 			if (other.amount != null)
 				return false;
 		} else if (!amount.equals(other.amount))
 			return false;
 		if (description == null) {
 			if (other.description != null)
 				return false;
 		} else if (!description.equals(other.description))
 			return false;
 		return true;
 	}
 	
 }
