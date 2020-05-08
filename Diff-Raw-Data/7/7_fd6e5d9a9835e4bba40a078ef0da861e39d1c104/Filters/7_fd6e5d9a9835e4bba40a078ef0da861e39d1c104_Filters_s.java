 package ca.softwareengineering.wesabetools.model;
 
 /**
  * A few {@link TransactionFilter} implementations.
  */
 public class Filters {
 	public static class DateBefore implements TransactionFilter {
 		long _date;
 
 		public DateBefore(long date) {
 			_date = date;
 		}
 
		@Override
 		public boolean accept(WesabeTransaction w) {
 			return w.getDate() <= _date;
 		}
 	}
 
 	public static class DateAfter implements TransactionFilter {
 		long _date;
 
 		public DateAfter(long date) {
 			_date = date;
 		}
 
		@Override
 		public boolean accept(WesabeTransaction w) {
 			return w.getDate() >= _date;
 		}
 	}
 
 	public static class NotTransfer implements TransactionFilter {
		@Override
 		public boolean accept(WesabeTransaction w) {
 			return !w.isTransfer();
 		}
 	}
 
 	public static class AmountLessThanOrEqual implements TransactionFilter {
 		final double cutoff;
 
 		public AmountLessThanOrEqual(double cutoff) {
 			this.cutoff = cutoff;
 		}
 
		@Override
 		public boolean accept(WesabeTransaction w) {
 			return w.getAmount() <= this.cutoff;
 		}
 	}
 }
