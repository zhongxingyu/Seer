 package com.orangeleap.tangerine.domain.paymentInfo;
 
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import javax.xml.bind.annotation.XmlType;
 
 import com.orangeleap.tangerine.util.TangerineMessageAccessor;
 @XmlType (namespace="http://www.orangeleap.com/orangeleap/schemas")
 public class Pledge extends Commitment {
 	private static final long serialVersionUID = 1L;
 
     private Date pledgeDate = Calendar.getInstance().getTime();
     private Date pledgeCancelDate;
     private String pledgeCancelReason;
     private String pledgeStatus = STATUS_PENDING;
     private boolean recurring = false;
     private Date projectedDate;
     private BigDecimal amountPaid;
     private BigDecimal amountRemaining;
 
 	public Pledge() {
 	}
 
 	public Pledge(Long id) {
 		setId(id);
 	}
 
 	public boolean isRecurring() {
 		return recurring;
 	}
 
 	public void setRecurring(boolean recurring) {
 		this.recurring = recurring;
 	}
 
 	public Date getProjectedDate() {
 		return projectedDate;
 	}
 
 	public void setProjectedDate(Date projectedDate) {
 		this.projectedDate = projectedDate;
 	}
 
 	public void setPledgeStatus(String pledgeStatus) {
 		this.pledgeStatus = pledgeStatus;
 	}
 
 	public String getPledgeStatus() {
 		return pledgeStatus;
 	}
 
 	public void setPledgeDate(Date pledgeDate) {
 		this.pledgeDate = pledgeDate;
 	}
 
 	public Date getPledgeDate() {
 		return pledgeDate;
 	}
 
 	public void setPledgeCancelDate(Date pledgeCancelDate) {
 		this.pledgeCancelDate = pledgeCancelDate;
 	}
 
 	public Date getPledgeCancelDate() {
 		return pledgeCancelDate;
 	}
 
 	public void setPledgeCancelReason(String pledgeCancelReason) {
 		this.pledgeCancelReason = pledgeCancelReason;
 	}
 
 	public String getPledgeCancelReason() {
 		return pledgeCancelReason;
 	}
 
     @Override
     public BigDecimal getAmountPaid() {
         return amountPaid;
     }
 
     @Override
 	public void setAmountPaid(BigDecimal amountPaid) {
         this.amountPaid = amountPaid;
     }
 
     @Override
     public BigDecimal getAmountRemaining() {
         return amountRemaining;
     }
 
     @Override
 	public void setAmountRemaining(BigDecimal amountRemaining) {
         if (amountRemaining != null && amountRemaining.compareTo(BigDecimal.ZERO) < 0) {
             amountRemaining = BigDecimal.ZERO;
         }
         this.amountRemaining = amountRemaining;
     }
 
 	public String getShortDescription() {
 		StringBuilder sb = new StringBuilder();
 		if (isRecurring()) {
 			sb.append(getAmountPerGift()).append(", ");
 		}
 		else {
 			sb.append(getAmountTotal()).append(", ");
 		}
 		sb.append(DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(pledgeDate)).append(", ");
 		if (isRecurring()) {
 			sb.append(TangerineMessageAccessor.getMessage("recurring"));
 		}
 		else {
 			sb.append(TangerineMessageAccessor.getMessage("notRecurring"));
 		}
 		return sb.toString();
 	}
 
 	@Override
 	public void prePersist() {
 		super.prePersist();
 
 		if (getAmountPaid() == null) {
 			setAmountPaid(BigDecimal.ZERO);
 		}
 		if (isRecurring()) {
 			setProjectedDate(null);
 			if (getEndDate() != null) {
 				if (getAmountTotal() != null && getAmountPaid() != null) {
 					setAmountRemaining(getAmountTotal().subtract(getAmountPaid()));
 				}
 				else {
 					setAmountRemaining(getAmountTotal());
 				}
 			}
 			else {
 				setAmountRemaining(null);
 				setAmountTotal(null);
 			}
 		}
 		else {
 			setStartDate(null);
 			setEndDate(null);
 			setAmountPerGift(null);
 			setFrequency(null);
 			if (getAmountTotal() != null && getAmountPaid() != null) {
 				setAmountRemaining(getAmountTotal().subtract(getAmountPaid()));
 			}
 			else {
 				setAmountRemaining(getAmountTotal());
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
 		return super.toString(); // TODO
 	}
 }
