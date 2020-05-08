 package org.cotrix.repository;
 
 import org.cotrix.domain.codelist.Codelist;
 
 
 /**
  * 
  * A {@link Repository} of {@link Codelist}s
  * 
  * @author Fabio Simeoni
  *
  */
 public interface CodelistRepository extends Repository<Codelist> {
 	
 	@Override
 	public void remove(String id) throws UnremovableCodelistException;
 	
 	
 	
 	@SuppressWarnings("serial")
 	static class UnremovableCodelistException extends IllegalStateException {
 		
 		public UnremovableCodelistException(String msg) {
 			super(msg);
 		}
 	}
 }
