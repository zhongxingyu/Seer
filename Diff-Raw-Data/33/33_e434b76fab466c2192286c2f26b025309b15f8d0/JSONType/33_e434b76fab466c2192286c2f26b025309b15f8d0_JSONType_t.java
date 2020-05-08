 package tdanford.json.schema;
 
 import static java.lang.String.*;
 
 public interface JSONType { 
 
 	public boolean contains(Object obj);
 	public java.lang.String explain(Object obj);
	public boolean isOptional();
 	
 	public static abstract class AbstractType implements JSONType { 
 		
 		public java.lang.String explain(Object obj) { return null; }
		public boolean isOptional() { return false; }
 	}
 	
 	public static class Everything extends AbstractType { 
 		public boolean contains(Object obj) { 
 			return obj != null;
 		}
 	}
 	
 	public static class Empty extends AbstractType { 
 		public boolean contains(Object obj) { 
 			return false;
 		}
 		public java.lang.String explain(Object obj) {
 			return java.lang.String.format(
 					"REJECT: \"%s\" not in Empty", java.lang.String.valueOf(obj));
 		}
 	}
 
 	public static class String extends AbstractType { 
 		public boolean contains(Object obj) { 
 			return obj != null && obj instanceof java.lang.String;
 		}
 
 		public java.lang.String explain(Object obj) {
 			java.lang.String typeName = "String";
 			if(contains(obj)) { 
 				return null;
 			} else if (obj == null) { 
 				return java.lang.String.format("REJECT: null value");
 			} else {  
 				return java.lang.String.format(
 						"REJECT: \"%s\" has type %s not in %s", 
 						java.lang.String.valueOf(obj), 
 						obj.getClass().getSimpleName(), 
 						typeName);
 			}
 		}
 	}
 
 	public static class Integer extends AbstractType { 
 		public boolean contains(Object obj) { 
 			if(obj == null) { return false; }
 			if(obj instanceof java.lang.Integer) { return true; }
 			try { 
 				java.lang.Integer.parseInt(obj.toString());
 				return true;
 			} catch(NumberFormatException e) { 
 				return false;
 			}
 		}
 		public java.lang.String explain(Object obj) {
 			if(obj == null) { return format("REJECT: null value"); } 
 			if(obj instanceof java.lang.Integer) { return null; }
 			try { 
 				java.lang.Integer.parseInt(obj.toString());
 				return null;
 			} catch(NumberFormatException e) { 
 				return format("REJECT: %s", e.getMessage());
 			}
 		}
 	}
 
 	public static class Double extends AbstractType { 
 		public boolean contains(Object obj) { 
 			if(obj == null) { return false; }
 			if(obj instanceof java.lang.Double) { return true; }
 			try { 
 				java.lang.Double.parseDouble(obj.toString());
 				return true;
 			} catch(NumberFormatException e) { 
 				return false;
 			}
 		}
 		public java.lang.String explain(Object obj) {
 			if(obj == null) { return format("REJECT: null value"); } 
 			if(obj instanceof java.lang.Double) { return null; }
 			try { 
 				java.lang.Double.parseDouble(obj.toString());
 				return null;
 			} catch(NumberFormatException e) { 
 				return format("REJECT: %s", e.getMessage());
 			}
 		}
 	}
 
 	public static class Boolean extends AbstractType { 
 		public boolean contains(Object obj) { 
 			if(obj == null) { return false; }
 			if(obj instanceof java.lang.Boolean) { return true; }
 			try { 
 				java.lang.Boolean.parseBoolean(obj.toString());
 				return true;
 			} catch(NumberFormatException e) { 
 				return false;
 			}
 		}
 		public java.lang.String explain(Object obj) {
 			if(obj == null) { return format("REJECT: null value"); } 
 			if(obj instanceof java.lang.Boolean) { return null; }
 			try { 
 				java.lang.Boolean.parseBoolean(obj.toString());
 				return null;
 			} catch(NumberFormatException e) { 
 				return format("REJECT: %s", e.getMessage());
 			}
 		}
 	}
 }
 
