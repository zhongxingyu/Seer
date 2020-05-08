 public class OperatorType extends Type {
    private int number_of_args = 0;
    private String takes_type = "NullType";
    private String out_type = "NullType";
    private Integer line = 0;
 
    public void setLine(Integer i) {
       this.line = i;
    }
 
    public Integer getLine() {
       return this.line;
    }
 
    public void setBinary() {
       this.number_of_args = 2;
    }
 
    public void setUnary() {
       this.number_of_args = 1;
    }
 
    public void setType(String in) {
       this.takes_type = in;
    }
 
    public void setOutType(String in) {
       this.out_type = in;
    }
 
    public boolean checkValid(Type a) {
       String aType = a.getClass().getName();
       return (number_of_args == 1) && (aType.equals(takes_type));
    }
 
    public boolean checkValid(Type a, Type b) {
       String aType = a.getClass().getName();
       String bType = b.getClass().getName();
       return (number_of_args == 2)
          && (aType.equals(takes_type))
          && (bType.equals(takes_type));
    }
 
    public String toString() {
       return "This operator requires " + this.number_of_args
          + " arguments and both must be " + this.takes_type;
    }
 
    public Type out() {
       // This is the dumb way to do it, but could get java abstraction working.
       if (this.out_type.equals("IntType")) {
          return new IntType();
       } else if (this.out_type.equals("BoolType")) {
          return new BoolType();
       } else {
          return new NullType();
       }
    }
 }
