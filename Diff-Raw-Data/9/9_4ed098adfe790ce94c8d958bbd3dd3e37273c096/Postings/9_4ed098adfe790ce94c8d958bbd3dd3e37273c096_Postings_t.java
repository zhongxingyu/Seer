 package edu.nyu.cs.cs2580;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Vector;
 
 /**
  * Sorted List of doc id's
  * @author kunal
  *
  */
 public class Postings extends Vector<Integer> implements Serializable
 {
 	//doc id maps to count
 	private HashMap<Integer,Integer> _countTerm = new HashMap<Integer,Integer>();
 	private static final long serialVersionUID = 5790192283947925472L;
 	private Integer cachedIndex;
 
 
 	@Override
 	public boolean add(Integer e){
 		
 		if(!_countTerm.containsKey(e)){
 			_countTerm.put(e, 0);//initalize to zero
 		}else{
 			//increment count
 			_countTerm.put(e,_countTerm.get(e)+1);
 		}
 		return super.add(e);
 	}
 
 	public Integer getCachedIndex() {
 		return cachedIndex;
 	}
 
 	public void setCachedIndex(Integer cachedIndex) {
 		this.cachedIndex = cachedIndex;
 	}
 
 	public void set_countTerm(HashMap<Integer,Integer> _countTerm) {
 		this._countTerm = _countTerm;
 	}
 
 	public HashMap<Integer,Integer> get_countTerm() {
 
 		HashMap<Integer,Integer> _ct_terms = new HashMap<Integer,Integer>();
 		for(Integer termID : this){
 
 			if(!_ct_terms.containsKey(termID)){
				_ct_terms.put(termID, 1);//initalize to zero
 			}else{
 				//increment count
 				_ct_terms.put(termID,_ct_terms.get(termID)+1);
 			}
 		}
 		return _ct_terms;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result
 		+ ((_countTerm == null) ? 0 : _countTerm.hashCode());
 		result = prime * result
 		+ ((cachedIndex == null) ? 0 : cachedIndex.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Postings other = (Postings) obj;
 		if (_countTerm == null) {
 			if (other._countTerm != null)
 				return false;
 		} else if (!_countTerm.equals(other._countTerm))
 			return false;
 		if (cachedIndex == null) {
 			if (other.cachedIndex != null)
 				return false;
 		} else if (!cachedIndex.equals(other.cachedIndex))
 			return false;
 		return true;
 	}
 }
 
