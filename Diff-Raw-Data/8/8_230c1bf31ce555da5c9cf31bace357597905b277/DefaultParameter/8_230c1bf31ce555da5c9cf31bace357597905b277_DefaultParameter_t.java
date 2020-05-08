 package vara.app.startupargs.base;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import vara.app.startupargs.ArgsUtil;
 import vara.app.startupargs.exceptions.UnexpectedNumberOfArguments;
 import vara.app.startupargs.exceptions.UnexpectedValueException;
 import vara.app.startupargs.exceptions.ValidationObjectException;
 
 /**
  * Default implementation for parameters object 
  *
  * @author Grzegorz (vara) Warywoda
  *
  */
 public abstract class DefaultParameter implements AbstractParameter {
 	private static Logger log = LoggerFactory.getLogger(DefaultParameter.class);
 
 	private String symbol;
 	private String shortSymbol;
 
 	private String separator = null;
 
 	/**
 	 * Default constructor
 	 *
 	 * @param option
 	 * @param shortOption
 	 */
 	public DefaultParameter(String option,String shortOption){
 
 		this.symbol = ArgsUtil.check(option,false);
 		this.shortSymbol = ArgsUtil.check(shortOption,true);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getSymbol() {
 		return symbol;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getShortSymbol() {
 		return shortSymbol;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getOptionUsage() {
 		if(shortSymbol != null)
 			return getSymbol()+"|"+getShortSymbol();
 		return symbol;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getValueSeparator() {
 		return separator;
 	}
 
 	/**
 	 * Set individual values separator for this option.
 	 * Define new separator may to serve for special options
 	 * e.q. -option=val1:val2...
 	 * In this case separator should be defined as ':' for '-option' object.
 	 *
 	 * @param separator string represented the separator.
 	 */
 	public void setValueSeparator(String separator){
 		this.separator = separator;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		String str = new StringBuilder( symbol.length() + shortSymbol.length() + 8).
 			append('\'').append(symbol).append("':'").append(shortSymbol).append('\'').toString();
 		return str;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if(obj == null) return false;
 		if( !(obj instanceof DefaultParameter) ) return false;
 		if(obj == this) return true;
 
 		DefaultParameter other = ((DefaultParameter)obj);
 
 		return other.getSymbol().equals(symbol) || other.getShortSymbol().equals(shortSymbol);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		int hash = 3;
 		hash = 97 * hash + (this.symbol != null ? this.symbol.hashCode() : 0);
 		hash = 97 * hash + (this.shortSymbol != null ? this.shortSymbol.hashCode() : 0);
 		return hash;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void handleOption(String[] optionValues)  throws ValidationObjectException{
		//FIXED: When we can invoke any parameter without argument
		//e.q Parameters.getParameter("h").handleOption(null); //Before NPE has been generated
		
		if(optionValues == null){
			optionValues = new String[0];
		}
		int nOptions = optionValues.length;
 
 		RangeNumber nParams = getOptionValuesLength();
 
 		if (nOptions != nParams.intValue()){
 
 			if(!nParams.check(nOptions)){
 
 				String msg = new StringBuilder(256).
 										append("Wrong Number of input arguments. Expected: ").
 										append(nParams.toString2()).
 										append(" Got: ").append(nOptions).toString();
 				log.warn(msg);
 
 				//if(nOptions<nParams.intValue()){
 					throw new UnexpectedNumberOfArguments(this,msg);
 				//}
 
 			}
 		}
 
 		for (String optionValue : optionValues) {
 			if(optionValue == null){
 				//TODO: create message
 				String msg = "Value for option must be non-null !";
 				throw new UnexpectedValueException(this,msg);
 			}
 		}
 
 		safeOption(optionValues);
 	}
 
 	/**
 	 *	Method to allow exit from application after handling this option.
 	 *
 	 * @return true if should be exit
 	 */
 	public boolean isExit(){
 		return false;
 	}
 
 	public abstract void safeOption(String[] optionValues) throws ValidationObjectException;
 }
