 public abstract class Currency{
 	
 private String country;
 private double valueUSD;
 private double value;
 
 
 public Currency(String country,double value, double valueUSD){
    this.country = country;
    this.value = value;
    this.valueUSD = valueUSD;
 }
 
 public double getValue(){	
 return this.value;
 }
 
public double getValueUSD(){
return this.valueUSD;
}

 public void setValue(double value){
 	
 	this.value=value;
 }
 
 
 }
