 package models;
 
 import io.seq.Alphabet;
 import io.seq.Clustal;
 import io.seq.Fasta;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.regex.Pattern;
 
 import play.Logger;
 import play.data.validation.EmailCheck;
 import play.data.validation.Validation;
 import plugins.AutoBean;
 import util.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 @AutoBean
 @XStreamAlias("validation")
 public class ValidationCheck implements Serializable {
 
 	@XStreamAsAttribute
 	public boolean required;
 
 	/** Error message displayed when entered data does not match the specified {@link #required} constraint */
 	@XStreamAlias("required-error")
 	public String requiredError;
 	
 	
 	/** 
 	 * Specifies the format that must be used to entered the value in the field. The valid values are the following:
 	 * 	<li>TEXT</li> 
 	 * 	<li>EMAIL</li>
 	 * 	<li>INTEGER</li>
 	 * 	<li>DECIMAL</li>
 	 * 	<li>DATE</li>
 	 * 	<li>FASTA</li>
 	 * 	<li>CLUSTAL</li>
 	 *  
 	 */
 	@XStreamAsAttribute
 	public String format; 
 	
 	/** Error message displayed when entered data does not match the specified {@link format} */
 	@XStreamAlias("format-error")
 	public String formatError;
 	
 	/** 
 	 * Sub-type for format 'FASTA'. Valida types are: 
 	 * <li>amino-acid</li> (default)
 	 * <li>nucleic-acid</li>
 	 * <li>dna</li>
 	 * <li>rna</li>
 	 * 
 	 */
	@XStreamAsAttribute
 	public String type;
 	
 	/** minimum value accepted for numbers and date and string values */
 	@XStreamAsAttribute
 	public String min; 
 
 	/** Error message displayed when entered data does not match the specified {@link #min} constraint */
 	@XStreamAlias("min-error")
 	public String minError;	
 	
 	/** maximum value accepted for numbers and date and string values */
 	@XStreamAsAttribute
 	public String max;
 	
 	/** Error message displayed when entered data does not match the specified {@link #max} constraint */
 	@XStreamAlias("max-error")
 	public String maxError;	
 	
 	/** A regular expression to match */
 	@XStreamAsAttribute
 	public String pattern; 
 
 	/** Error message displayed when entered data does not match the specified {@link #pattern} constraint */
 	@XStreamAlias("pattern-error")
 	public String patternError;	
 	
 	
 	/** Max number of sequences (only for FASTA format) */
 	@XStreamAsAttribute
 	@XStreamAlias("maxnum")
 	public Integer maxNum;
 	
 	/** Error message displayed when entered data does not match the specified {@link #maxNum} constraint */
 	@XStreamAlias("maxnum-error")
 	public String maxNumError;	
 
 	/** Max number of sequences (only for FASTA format) */
 	@XStreamAsAttribute
 	@XStreamAlias("minnum")
 	public Integer minNum;
 	
 	/** Error message displayed when entered data does not match the specified {@link #maxNum} constraint */
 	@XStreamAlias("minnum-error")
 	public String minNumError;		
 	
 	/** Max length of sequence (only for FASTA format) */
 	@XStreamAsAttribute
 	@XStreamAlias("maxlen")
 	public Integer maxLength;
 	
 	/** Error message displayed when entered data does not match the specified {@link #maxLength} constraint */
 	@XStreamAlias("maxlen-error")
 	public String maxLengthError;	
 
 	/** Min length of sequence (only for FASTA format) */
 	@XStreamAsAttribute
 	@XStreamAlias("minlen")
 	public Integer minLength;
 	
 	/** Error message displayed when entered data does not match the specified {@link #maxLength} constraint */
 	@XStreamAlias("minlen-error")
 	public String minLengthError;		
 	
 	@XStreamOmitField 
 	String fErrorMessage;
 	
 	/** 
 	 * Default empty constructor 
 	 */
 	public ValidationCheck() { }
 	
 
 	
 	Integer getMinAsInteger() {
 		return Utils.parseInteger(min,null);
 	}
 	
 	Integer getMaxAsInteger() {
 		return Utils.parseInteger(max,null);
 	}
 	
 	Date getMinDate() {
 		return Utils.parseDate(min,null);
 	}
 	
 	Date getMaxDate() {
 		return Utils.parseDate(max,null);
 	}
 	
 	Double getMinDecimal() {
 		return Utils.parseDouble(min,null);
 	}
 
 	Double getMaxDecimal() {
 		return Utils.parseDouble(max,null);
 	}
 
 	public void apply(String name, String value) {
 
 		ErrorWrapper error = null;
 
 		/* 
 		 * apply REQUIRED constraint validation
 		 */
 		if( required && Utils.isEmpty(value)) 
 		{
 			error = applyRequiredValidation(name, value);
 		}
 		
 		/* 
 		 * if there are not error, check for 'format' constraint
 		 */
 		if( error == null ) { 
 			String[] all = this.format != null ? this.format.split("\\|") : null;
 			for( int i=0; all!=null && i<all.length ; i++ )
 			{ 
 				error = applyFormatValidation( all[i].trim(), name, value);
 				if( error == null ) { 
 					// the first that is NOT failing is good (- OR condition -) 
 					break;
 				}
 			}
 		}
 
 		/*
 		 * if any, report the error 
 		 */
 		if( error != null ) { 
			Logger.info("Failed validation for field '%s' with the following message: '%s' \n%s\n---- END ----\n\n", name, value);
 			Validation.addError(error.fieldName, error.message, error.variables);
 		}
 		
 	}
 
 	ErrorWrapper applyFormatValidation(String format, String name, String value) {
 
 		if( "TEXT".equalsIgnoreCase(format) ) 
 		{
 			return applyTextValidation(name, value);
 		}
 
 		
 		/*
 		 * EMAIL FIELDS 
 		 */
 		else if( "EMAIL".equalsIgnoreCase(format)  ) 
 		{ 
 			return applyEmailValidation(name, value);
 		}
 		
 		/*
 		 * DATE  validation
 		 */
 		else if( "DATE".equalsIgnoreCase(format) && Utils.isNotEmpty(value) ) 
 		{
 			return applyDateValidation(name, value);
 		}
 		/* 
 		 * INTEGER number validation
 		 */
 		else if( "INTEGER".equalsIgnoreCase(format) && Utils.isNotEmpty(value)) 
 		{
 			return applyIntegerValidation(name, value);
 		}
 		/*
 		 * DECIMAL number validation
 		 */
 		else if( "DECIMAL".equalsIgnoreCase(format) && Utils.isNotEmpty(value) ) 
 		{
 			return applyDecimalValidation(name, value);
 		}
 		
 		/*
 		 * FASTA format validation
 		 */
 		else if( "FASTA".equalsIgnoreCase(format) && Utils.isNotEmpty(value) ) 
 		{
 			return applyFastaValidation(name, value);
 		} 
 		
 		
 		/*
 		 * CLUSTAL format validation
 		 */
 		else if( "CLUSTAL".equalsIgnoreCase(format) && Utils.isNotEmpty(value) ) 
 		{
 			return applyClustalValidation(name, value);
 		} 		
 		else if( Utils.isNotEmpty(value) ) {
 			Logger.warn("Unknown validation format: '%s'", format);
 		}
 
 		// no error 
 		return null;
 	}
 
 
 
 	/**
 	 * Apply the clustal validation
 	 * @param name the field name
 	 * @param value the field value
 	 */
 	ErrorWrapper applyClustalValidation(String name, String value) {
 		Clustal clustal = new Clustal(type2alphabet(type));
 		
 		/* parse the sequences */
 		clustal.parse(value);
 		
 		/* check for validity */
         if ( !clustal.isValid() ) { 
 			String message = Utils.isNotEmpty(formatError) ? formatError : "validation.clustal.format";
         	return error(name, message, new String[0]);
         } 
         else if( minNum != null && clustal.count()<minNum ) { 
 			String message = Utils.isNotEmpty(minNumError) ? minNumError : "validation.clustal.minum";
         	return error(name, message, new String[0]);
         }
         else if( maxNum != null && clustal.count()>maxNum ) { 
 			String message = Utils.isNotEmpty(maxNumError) ? maxNumError : "validation.clustal.maxnum";
         	return error(name, message, new String[0]);
         }
         else if( minLength != null && clustal.minLength()<minLength ) { 
 			String message = Utils.isNotEmpty(minLengthError) ? minLengthError : "validation.clustal.minlen";
         	return error(name, message, new String[0]);
         }
         else if( maxLength != null && clustal.maxLength()>maxLength ) { 
 			String message = Utils.isNotEmpty(maxLengthError) ? maxLengthError : "validation.clustal.maxlen";
         	return error(name, message, new String[0]);
         }
    
 		return null;
 	}
 
 
 	private ErrorWrapper error(String fieldName, String message, String... variables) {
 		ErrorWrapper result = new ErrorWrapper();
 		result.fieldName = fieldName;
 		result.message = message;
 		result.variables = variables;
 		return result;
 	}
 
 	static class ErrorWrapper implements Serializable { 
 		String fieldName;
 		String message;
 		String[] variables;
 	}
 
 	/**
 	 * Apply the FASTA format validation
 	 * @param name the field name 
 	 * @param value the field value
 	 */
 	ErrorWrapper applyFastaValidation(String name, String value) {
 		Fasta fasta = new Fasta(type2alphabet(type));
 		
 		/* parse the sequences */
 		fasta.parse(value);
 		
 		/* check for validity */
         if ( !fasta.isValid() ) { 
 			String message = Utils.isNotEmpty(formatError) ? formatError : "validation.fasta.format";
         	return error(name, message, new String[0]);
         } 
         else if( minNum != null && fasta.count()<minNum ) { 
 			String message = Utils.isNotEmpty(minNumError) ? minNumError : "validation.fasta.minum";
         	return error(name, message, new String[0]);
         }
         else if( maxNum != null && fasta.count()>maxNum ) { 
 			String message = Utils.isNotEmpty(maxNumError) ? maxNumError : "validation.fasta.maxnum";
         	return error(name, message, new String[0]);
         }
         else if( minLength != null && fasta.minLength()<minLength ) { 
 			String message = Utils.isNotEmpty(minLengthError) ? minLengthError : "validation.fasta.minlen";
         	return error(name, message, new String[0]);
         }
         else if( maxLength != null && fasta.maxLength()>maxLength ) { 
 			String message = Utils.isNotEmpty(maxLengthError) ? maxLengthError : "validation.fasta.maxlen";
         	return error(name, message, new String[0]);
         }
         
         // no error
         return null;
 	}
 
 
 	/**
 	 * Apply the Decimal format validation 
 	 * @param name the field value 
 	 * @param value the field value
 	 */
 	ErrorWrapper applyDecimalValidation(String name, String value) {
 		Double num = Utils.parseDouble(value,null);
 		if( num == null ) {
 			String message = Utils.isNotEmpty(formatError) ? formatError : "validation.decimal.format";
 			return error(name, message, new String[] {value});			
 		}
 		
 		Double min = getMinDecimal();
 		if( min != null && num != null && num < min ) {
 			String message = Utils.isNotEmpty(minError) ? minError : "validation.decimal.min";
 			return error(name, message, new String[] {value});			
 		}
 		
 		Double max = getMaxDecimal();
 		if( max != null && num != null && num > max ) {
 			String message = Utils.isNotEmpty(maxError) ? maxError : "validation.decimal.max";
 			return error(name, message, new String[] {value});			
 		}
 
 		// no error 
 		return null;
 	}
 
 
 	/**
 	 * Apply the Integer format validation
 	 * @param name the field name 
 	 * @param value the field value
 	 */
 	ErrorWrapper applyIntegerValidation(String name, String value) {
 		Integer num = Utils.parseInteger(value,null);
 		if( num == null ) {
 			String message = Utils.isNotEmpty(formatError) ? formatError : "validation.integer.format";
 			return error(name, message, new String[] {value});			
 		}
 		
 		Integer min = getMinAsInteger();
 		if( min != null && num != null && num < min ) {
 			String message = Utils.isNotEmpty(minError) ? minError : "validation.integer.min";
 			return error(name, message, new String[] {value});			
 		}
 		
 		Integer max = getMaxAsInteger();
 		if( max != null && num != null && num > max ) {
 			String message = Utils.isNotEmpty(maxError) ? maxError : "validation.integer.max";
 			return error(name, message, new String[] {value});			
 		}
 		
 		// no error 
 		return null;
 		
 	}
 
 	
 	/**
 	 * Apply the Date format validation 
 	 * @param name the field name 
 	 * @param value the field value 
 	 */
 	ErrorWrapper applyDateValidation(String name, String value) {
 		
 		Date date = Utils.parseDate(value);
 		if( date == null ) {
 			String message = Utils.isNotEmpty(formatError) ? formatError : "validation.date.format";
 			return error(name, message, new String[] {value});			
 		}
 	
 		Date min = getMinDate();
 		if( date!=null && min!=null && date.getTime() < min.getTime()) {
 			String message = Utils.isNotEmpty(minError) ? minError : "validation.date.min";
 			return error(name, message, new String[] {value});			
 		}
 
 		Date max = getMaxDate();
 		if( date!=null && max!=null && date.getTime() > max.getTime()) {
 			String message = Utils.isNotEmpty(maxError) ? maxError : "validation.date.max";
 			return error(name, message, new String[] {value});			
 		}
 		
 		// no error 
 		return null;
 		
 	}
 
 
 	/** 
 	 * Apply the EMAIL format validation 
 	 * @param name the field name 
 	 * @param value the field value
 	 */
 	ErrorWrapper applyEmailValidation(String name, String value) {
 		
 		/* the value string can contains multiple addresses separated by a comma or a semicolon 
 		 * split the string to check email address syntax one-by-one 
 		 */
 		String sEmail = value != null ? value : "";
 		sEmail = sEmail.replace(",", ";"); // <-- normalize the comma 
 		String[] addresses = sEmail.split(";");
 		for( String addr : addresses ) { 
 			addr = addr.trim();
 			boolean isValid = new EmailCheck().isSatisfied(null, addr, null, null);
 			if( !isValid ) {
 				String message = Utils.isNotEmpty(formatError) ? formatError : "validation.email.format";
 				return error(name, message, new String[] {addr});
 			}
 		}
 		
 		/* optional 'min' and 'max' attribute can be entered to specify the numeber of accepted email addresses  */
 		Integer min = getMinAsInteger();
 		Integer max = getMaxAsInteger();
 		
 		if( min != null && ( addresses.length < min ) ) {
 			String message = Utils.isNotEmpty(minError) ? minError : "validation.minSize";
 			return error(name, message, new String[] {value});
 		}
 		
 		if( max != null && ( addresses.length > max ) ) {
 			String message = Utils.isNotEmpty(maxError) ? maxError : "validation.maxSize";
 			return error(name, message, new String[] {value});
 		}
 		
 		// no error 
 		return null;
 		
 	}
 
 
 	/**
 	 * Apply the TEXT format validation 
 	 * @param name the field name 
 	 * @param value the field value 
 	 */
 	ErrorWrapper applyTextValidation(String name, String value) {
 		Integer min = getMinAsInteger();
 		Integer max = getMaxAsInteger();
 		if( min != null && ( value==null || value.trim().length()<min ) ) {
 			String message = Utils.isNotEmpty(minError) ? minError : "validation.minSize";
 			return error(name, message, new String[] {value});
 		}
 		
 		if( max != null && ( value==null || value.trim().length()>max ) ) {
 			String message = Utils.isNotEmpty(maxError) ? maxError : "validation.maxSize";
 			return error(name, message, new String[] {value});
 		}
 		
 		if( Utils.isNotEmpty(pattern) && !Pattern.matches(pattern, value) ) {
 			String message = Utils.isNotEmpty(patternError) ? patternError : "validation.match";
 			return error(name, message, new String[] {value});
 		}
 
 		// no error 
 		return null;
 		
 	}
 
 
 	/**
 	 * Apply the REQUIRED constraint validation 
 	 * @param name the field name 
 	 * @param value the field value 
 	 */
 	ErrorWrapper applyRequiredValidation( String name, String value ) { 
 		String message = Utils.isNotEmpty(requiredError) ? requiredError : "validation.required";
 		return error(name, message, new String[] {name});		
 	}
 	
 	/**
 	 * Translate the string 'type' value to the matching {@link Alphabet} 
 	 * 
 	 * @param type one of the following values: <code>amino-acid</code>, <code>nucleic-acid</code>, <code>dna</code>, <code>rna</code>
 	 * @return an instance of {@link Alphabet} corresponding to the specified string value, 
 	 * if an unknown value is specified the default {@link Alphabet.AminoAcid} is returned 
 	 */
 	static Alphabet type2alphabet( String type ) { 
 		if( Utils.isEmpty(type) || "amino-acid".equalsIgnoreCase(type) )  { 
 			return Alphabet.AminoAcid.INSTANCE;
 		}
 		else if( "nucleic-acid".equalsIgnoreCase(type) ) { 
 			 return Alphabet.NucleicAcid.INSTANCE;		
 		}
 		else if( "dna".equalsIgnoreCase(type) ) { 
 			 return Alphabet.Dna.INSTANCE;		
 		}
 		else if( "rna".equalsIgnoreCase(type) ) { 
 			 return Alphabet.Rna.INSTANCE;			
 		}
 		else { 
 			Logger.warn("Unknown format type: '%s'", type);
 			return Alphabet.AminoAcid.INSTANCE;
 		}
 	}
 
 }
