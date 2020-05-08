 package com.businessclasses;
 
 
 
 public enum Route {
 	FLY,
 	IN,
 	OUT,
 	SLANT,
 	POST,
 	SKINNY_POST, 
 	FLAG,
 	STOP_AND_GO, 
 	ARROW,
 	WHEEL,
 	FADE;
	
	@Override
	public String toString(){
		if(this.equals(STOP_AND_GO)){
			return "STOP AND GO";
		}if(this.equals(SKINNY_POST)){
			return "SKINNY POST";
		}
		return super.toString();
	}
 }
