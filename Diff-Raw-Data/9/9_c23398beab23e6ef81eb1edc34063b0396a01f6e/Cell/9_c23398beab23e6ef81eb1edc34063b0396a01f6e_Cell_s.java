 package de.karfau.graxle.model;
 
 /**
  * A Field in a Matrix
  *
  * @see Matrix
  */
interface Cell {
	Object getContent();
 
	void setContent(Object value);
 }
