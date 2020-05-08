 package cours.ulaval.glo4003.domain;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 
 public class Time {
 
 	private Integer hour;
 	private Integer minute;
 
 	// This constructor is for serialization purpose
 	public Time() {
 	}
 
 	public Time(Integer hour, Integer minute) {
 		this.hour = hour;
 		this.minute = minute;
 	}
 
 	public void addHours(Integer hoursToAdd) {
 		hour += hoursToAdd;
 	}
 
 	public boolean after(Time time) {
 		if (hour > time.getHour()) {
 			return true;
 		} else if (hour == time.getHour()) {
 			if (minute > time.getMinute()) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public boolean before(Time time) {
 		if (hour < time.getHour()) {
 			return true;
 		} else if (hour == time.getHour()) {
 			if (minute < time.getMinute()) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public Integer getHour() {
 		return hour;
 	}
 
 	public void setHour(Integer hour) {
 		this.hour = hour;
 	}
 
 	public Integer getMinute() {
 		return minute;
 	}
 
 	public void setMinute(Integer minute) {
 		this.minute = minute;
 	}
 
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder().append(hour).append(minute).toHashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Time) {
 			final Time other = (Time) obj;
 			return new EqualsBuilder().append(this.hour, other.getHour()).append(this.minute, other.getMinute()).isEquals();
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public String toString() {
		return this.hour + ":" + this.minute;
 	}
 }
