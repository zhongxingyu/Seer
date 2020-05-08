 /* Copyright (C) 2012 cloudbase.io
  
  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License, version 2, as published by
  the Free Software Foundation.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; see the file COPYING.  If not, write to the Free
  Software Foundation, 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
  */
 package com.cloudbase.datacommands;
 
 import java.util.Hashtable;
 import java.util.Vector;
 
 import net.rim.device.api.gps.BlackBerryLocation;
 
 import org.json.me.JSONArray;
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 
 import com.cloudbase.CBHelperSerializable;
 
 public class CBSearchCondition extends CBDataAggregationCommand implements CBHelperSerializable {
 
 	private Vector subConditions;
 	private Vector sortKeys;
 	/**
 	 * This property is the maximum number of results to be returned by the search
 	 */
 	private int limit;
 	private String field;
 	private Object value;
 	private String operator;
 	private String link;
 
 	public static final String CBSearchKey = "cb_search_key";
 	public static final String CBSortKey = "cb_sort_key";
 	public static final String CBLimitKey = "cb_limit";
 
 	/**
 	 * Creates an empty search condition object
 	 */
 	public CBSearchCondition() {
 		this.limit = -1;
 
 		this.setCommandType(CBDataAggregationCommandType.CBDataAggregationMatch);
 	}
 
 	/**
 	 * Creates a new "simple" search condition with the given values
 	 * @param fname The name of the field to run the search on
 	 * @param op The CBSearchConditionOperator to use in the search
 	 * @param value The value we are looking for in the field
 	 */
 	public CBSearchCondition(String fname, String op, Object value) {
 		this.setField(fname);
 		this.setOperator(op);
 		this.setValue(value);
 		this.limit = -1;
 
 		this.setCommandType(CBDataAggregationCommandType.CBDataAggregationMatch);
 	}
 
 	/**
 	 * Creates a new search condition for geographical searches. This looks for documents whose location data
 	 * places them near the given location.
 	 * @param nearLoc The location we are looking for
 	 * @param maxDistance The maximum distance in meters from the given location
 	 */
 	public CBSearchCondition(BlackBerryLocation nearLoc, int maxDistance) {
 		Vector points = new Vector();
 		
         points.addElement(Double.toString(nearLoc.getQualifiedCoordinates().getLatitude()));
         points.addElement(Double.toString(nearLoc.getQualifiedCoordinates().getLongitude()));
         
         Hashtable searchQuery = new Hashtable(); 
         this.setField("cb_location");
         this.setOperator(CBSearchConditionOperator.CBOperatorEqual);
         
         searchQuery.put("$near", points);
         if (maxDistance > 0)
         	searchQuery.put("$maxDistance", Integer.toString(maxDistance));
 
         this.setValue(searchQuery);
         this.limit = -1;
         
         this.setCommandType(CBDataAggregationCommandType.CBDataAggregationMatch);
 	}
 
 	/**
 	 * Creates a new search condition for geographical searches. This looks for documents within a given boundary box
 	 * defined by the coordinates of its North-Eastern and South-Western corners.
 	 * @param NECorner The coordinates for the north eastern corner
 	 * @param SWCorner The coordinates for the south western corner
 	 */
 	public CBSearchCondition(BlackBerryLocation NECorner, BlackBerryLocation SWCorner)
 	{
 		Vector box = new Vector();
 		Vector NECornerList = new Vector();
 		NECornerList.addElement(Double.toString((NECorner.getQualifiedCoordinates().getLatitude())));
 		NECornerList.addElement(Double.toString((NECorner.getQualifiedCoordinates().getLongitude())));
 		Vector SWCornerList = new Vector();
 		SWCornerList.addElement(Double.toString((SWCorner.getQualifiedCoordinates().getLatitude())));
 		SWCornerList.addElement(Double.toString((SWCorner.getQualifiedCoordinates().getLongitude())));
 		box.addElement(SWCornerList);
 		box.addElement(NECornerList);
 
 		Hashtable boxCondition = new Hashtable();
 		boxCondition.put("$box", box);
 
 		Hashtable searchQuery = new Hashtable();
 		searchQuery.put("$within", boxCondition);
 
 		this.setField("cb_location");
 		this.setOperator(CBSearchConditionOperator.CBOperatorEqual);
 		this.setValue(searchQuery);
 		this.limit = -1;
 
 		this.setCommandType(CBDataAggregationCommandType.CBDataAggregationMatch);
 	}
 
 	public void addAnd(String field, String op, Object value)
 	{
 		if (this.getSubConditions() == null)
 			this.setSubConditions(new Vector());
 
 		CBSearchCondition newCond = new CBSearchCondition();
 		newCond.setField(field);
 		newCond.setOperator(op);
 		newCond.setLink(CBSearchConditionLink.CBConditionLinkAnd);
 		newCond.setValue(value);
 
 		this.subConditions.addElement(newCond);
 	}
 
 	public void addOr(String field, String op, Object value)
 	{
 		if (this.getSubConditions() == null)
 			this.setSubConditions(new Vector());
 
 		CBSearchCondition newCond = new CBSearchCondition();
 		newCond.setField(field);
 		newCond.setOperator(op);
 		newCond.setLink(CBSearchConditionLink.CBConditionLinkOr);
 		newCond.setValue(value);
 
 		this.subConditions.addElement(newCond);
 	}
 
 	public void addNor(String field, String op, Object value)
 	{
 		if (this.getSubConditions() == null)
 			this.setSubConditions(new Vector());
 
 		CBSearchCondition newCond = new CBSearchCondition();
 		newCond.setField(field);
 		newCond.setOperator(op);
 		newCond.setLink(CBSearchConditionLink.CBConditionLinkNor);
 		newCond.setValue(value);
 
 		this.subConditions.addElement(newCond);
 	}
 
 	/**
 	 * Add a sorting condition to your search. You can add multiple fields to sort by.
 	 * It is only possible to sort on top level fields and not on objects.
 	 * @param field The name of the field in the collection
 	 * @param direction The direction of the sort (1 = ascending / -1 = descending)
 	 */
 	public void addSortField(String field, int direction) {
 		if (this.sortKeys == null)
 			this.sortKeys = new Vector();
 
 		Hashtable newSortField = new Hashtable();
 		newSortField.put(field, "" + direction);
 		this.sortKeys.addElement(newSortField);
 	}
 
 	public Vector getSubConditions() {
 		return subConditions;
 	}
 	public void setSubConditions(Vector subConditions) {
 		this.subConditions = subConditions;
 	}
 	public String getField() {
 		return field;
 	}
 	public void setField(String field) {
 		this.field = field;
 	}
 	public Object getValue() {
 		return value;
 	}
 	public void setValue(Object value) {
 		this.value = value;
 	}
 	public String getOperator() {
 		return operator;
 	}
 	public void setOperator(String operator) {
 		this.operator = operator;
 	}
 	public String getLink() {
 		return link;
 	}
 	public void setLink(String link) {
 		this.link = link;
 	}
 
 	public int getLimit() {
 		return limit;
 	}
 
 	public void setLimit(int limit) {
 		this.limit = limit;
 	}
 	
 	public JSONObject toJSONObject() throws JSONException {
 		JSONObject output = new JSONObject();
 
 		// This is not a condition but a collection of sub-conditions. loop over them
 		// and serialize them one by one
 		if (this.getField() == null)
 	    {
 	        if (this.getSubConditions().size() > 1) {
 	            JSONArray curObject = new JSONArray();
 
 	            String prevLink = null; // used to store the link from the previous condition
 	            
 	            int count = 0;
 	            for (int i = 0; i < this.getSubConditions().size(); i++) {
 	            	CBSearchCondition curGroup = (CBSearchCondition)this.getSubConditions().elementAt(i);
 	            
 	            	if (prevLink != null && prevLink != curGroup.getLink()) {
 	                	output.put(prevLink.toString(), curObject);
 	                    curObject = new JSONArray();
 	                }
 	            	curObject.put(curGroup.toJSONObject());
 	                prevLink = curGroup.getLink();
 	                count++;
 	                if (count == this.getSubConditions().size()) {
 	                	output.put(prevLink.toString(), curObject);
 	                }
 	            }
 	        }
 	        else if (this.getSubConditions().size() == 1)
 	        {
 	            output = ((CBSearchCondition)this.getSubConditions().elementAt(0)).toJSONObject();
 	        }
 	    }
 	    else // it's a single condition with a field. Generate the Map for it.s
 	    {
 	    	JSONObject cond = new JSONObject();
 	    	JSONArray modArray = new JSONArray();
 	        
 	    	if (this.getOperator() == CBSearchConditionOperator.CBOperatorEqual) {
 	    		output.put(this.getField(), this.value.toString());
 	    	}
 		    if (this.getOperator() == CBSearchConditionOperator.CBOperatorAll || 
		        	this.getOperator() == CBSearchConditionOperator.CBOperatorExists || 
 		        	this.getOperator() == CBSearchConditionOperator.CBOperatorNe ||
 		        	this.getOperator() == CBSearchConditionOperator.CBOperatorIn || 
 		        	this.getOperator() == CBSearchConditionOperator.CBOperatorNin || 
 		        	this.getOperator() == CBSearchConditionOperator.CBOperatorSize ||
 		        	this.getOperator() == CBSearchConditionOperator.CBOperatorType) {
 		    	cond.put(this.getOperator(), this.getValue().toString());
             	output.put(this.getField(), cond);
 	        }
 	        if (this.getOperator() == CBSearchConditionOperator.CBOperatorMod) {
 	        	modArray.put(this.getValue());
             	modArray.put(Integer.toString(1));
             	cond.put(this.getOperator(), modArray);
             	output.put(this.getField(), cond);
 	        }
 	           
 	    }
 
 	    return output;
 	}
 
 	public void fromJSONObject(JSONObject data) throws JSONException {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Object serializeAggregateConditions() {
 		try {
 			return this.toJSONObject();
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 }
