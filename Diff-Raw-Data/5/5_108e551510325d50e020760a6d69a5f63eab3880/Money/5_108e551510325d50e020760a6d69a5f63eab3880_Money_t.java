 package edu.ibs.core.entity;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Comparator;
 
 /**
  *
  * @date Nov 5, 2012
  *
  * @author Vadim Martos
  */
 public class Money implements Comparable<Money>, Comparator<Money>, Serializable {
 
 	private static final long serialVersionUID = 47892347148517205L;
 	private static final Comparator<Money> comparator = new Comparator<Money>() {
 
 		@Override
 		public int compare(Money o1, Money o2) {
 			return o1.defaultAmount().compareTo(o2.defaultAmount());
 		}
 	};
 	private final BigDecimal amount;
 	private final Currency currency;
 
 	public Money(long integer, int fraction, Currency currency) throws IllegalArgumentException, NullPointerException {
 		if (integer < 0 || fraction < 0) {
 			throw new IllegalArgumentException("Positive value expected on money");
 		}
 		int multiply = currency.getFraction().multiply();
 		if (fraction / multiply != 0) {
 			throw new IllegalArgumentException(String.format("Fraction %s has more digits than allowed(%s)", fraction, currency.getFraction()));
 		}
 		this.amount = BigDecimal.valueOf(multiply * integer + fraction);
 		this.currency = currency;
 	}
 
 	public Money(long amount, Currency currency) throws NullPointerException, IllegalArgumentException {
 		if (amount < 0) {
 			throw new IllegalArgumentException("Positive value expected on money");
 		}
 		this.amount = BigDecimal.valueOf(amount);
 		this.currency = currency;
 	}
 
 	private Money(BigDecimal amount, Currency currency) {
 		this.amount = amount;
 		this.currency = currency;
 	}
 
 	public static Money parseMoney(String integer, String fraction, Currency curr) throws IllegalArgumentException, NumberFormatException {
 		long l = Long.parseLong(integer);
 		int f = Integer.parseInt(fraction);
 		int digits = (int) Math.log10(curr.getFraction().multiply());
 		int fractionLen = fraction.length();
		if (f == 0) {
			fractionLen = 0;
		}
		if (fractionLen > digits) {
 			throw new IllegalArgumentException(String.format("Fraction [.%s] is too long, expeced %s digits", fraction, digits));
 		} else {
 			if (fractionLen < digits) {
 				int mul = (digits - fractionLen) * 10;
 				f *= mul;
 			}
 			return new Money(l, f, curr);
 		}
 	}
 
 	public long integer() {
 		return amount.longValue() / currency.getFraction().multiply();
 	}
 
 	public int fraction() {
 		return (int) (amount.longValue() % currency.getFraction().multiply());
 	}
 
 	public Currency currency() {
 		return currency;
 	}
 
 	public Money convert(Currency curr) {
 		BigDecimal bd = this.amount.multiply(this.currency.getFactor()).divide(curr.getFactor(), 0, RoundingMode.DOWN);
 		return new Money(bd, curr);
 	}
 
 	private BigDecimal defaultAmount() {
 		return amount.multiply(currency.getFactor());
 	}
 
 	public long balance() {
 		return amount.longValue();
 	}
 
 	public Money add(Money other) {
 		other = other.convert(this.currency);
 		BigDecimal bd = this.amount.add(other.amount);
 		return new Money(bd, this.currency);
 	}
 
 	public Money subtract(Money other) {
 		other = other.convert(this.currency);
 		BigDecimal bd = this.amount.subtract(other.amount);
 		return new Money(bd, this.currency);
 	}
 
 	/**
 	 * Greater or equals than
 	 *
 	 * @param other
 	 * @return
 	 */
 	public boolean ge(Money other) {
 		return compareTo(other) >= 0;
 	}
 
 	/**
 	 * Greater than
 	 *
 	 * @param other
 	 * @return
 	 */
 	public boolean gt(Money other) {
 		return compareTo(other) > 0;
 	}
 
 	/**
 	 * Lower than
 	 *
 	 * @param other
 	 * @return
 	 */
 	public boolean lt(Money other) {
 		return compareTo(other) < 0;
 	}
 
 	/**
 	 * Lower or equals than
 	 *
 	 * @param other
 	 * @return
 	 */
 	public boolean le(Money other) {
 		return compareTo(other) <= 0;
 	}
 
 	@Override
 	public int compareTo(Money o) {
 		return compare(this, o);
 	}
 
 	public static Comparator<Money> comparator() {
 		return comparator;
 	}
 
 	@Override
 	public int compare(Money o1, Money o2) {
 		return comparator.compare(o1, o2);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final Money other = (Money) obj;
 		if (this.amount != other.amount) {
 			return false;
 		}
 		if (this.currency != other.currency && (this.currency == null || !this.currency.equals(other.currency))) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 7;
 		hash = 97 * hash + (this.amount != null ? this.amount.hashCode() : 0);
 		hash = 97 * hash + (this.currency != null ? this.currency.hashCode() : 0);
 		return hash;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("%s.%s of %s", integer(), fraction(), currency.getName());
 	}
 }
