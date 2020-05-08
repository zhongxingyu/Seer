 package examples.teeda.web.add;
 
 public class AddInputDoublePage {
 
 	public static final String arg1_TRequiredValidator = null;
 
	public static final String arg1_TDoubleRangeValidator = "maximum=99.9999, maximumMessageId=examples.teeda.web.add.AddInputDouble.DoubleRangeValidator.MAXIMUM";
 
 	public static final String arg2_TRequiredValidator = null;
 
	public static final String arg2_TDoubleRangeValidator = "maximum=99.9999, maximumMessageId=examples.teeda.web.add.AddInputDouble.DoubleRangeValidator.MAXIMUM";
 
 	private Double arg1;
 
 	private Double arg2;
 
 	public Double getArg1() {
 		return arg1;
 	}
 
 	public void setArg1(Double arg1) {
 		this.arg1 = arg1;
 	}
 
 	public Double getArg2() {
 		return arg2;
 	}
 
 	public void setArg2(Double arg2) {
 		this.arg2 = arg2;
 	}
 
 	public String initialize() {
 		arg1 = null;
 		arg2 = null;
 		return null;
 	}
 }
