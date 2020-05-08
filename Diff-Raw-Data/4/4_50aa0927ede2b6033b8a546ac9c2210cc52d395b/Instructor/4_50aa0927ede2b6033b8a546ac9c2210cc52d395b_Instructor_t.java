 package myc;
 
 class Instructor {
	public String name;
	public String position;
 	public Instructor(String name, String position) {
 		this.name = name;
 		this.position = position;
 	}
 	public Instructor(String name) {
 		this(name, null);
 	}
 }
