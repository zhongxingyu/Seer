 package petrinetze.impl;
 
 import petrinetze.IRenew;
 
 public class RenewCount implements IRenew {
 
 
     @Override
 	public String renew(String tlb) {
        return String.valueOf(Integer.parseInt(tlb) + 1);
 	}
 
 	@Override
 	public boolean isTlbValid(String tlb) {
 		try {
             Integer.parseInt(tlb);
             return true;
         }
         catch (Exception ex) {
             return false;
         }
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return getClass().hashCode();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		return this == obj || (obj != null && obj.getClass() == this.getClass());
 	}
 	
 
 }
