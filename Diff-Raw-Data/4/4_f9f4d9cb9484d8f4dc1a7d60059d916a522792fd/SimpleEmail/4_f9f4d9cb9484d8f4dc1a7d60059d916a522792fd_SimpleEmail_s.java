 package xingu.email;
 
 import java.util.Date;
 
 public class SimpleEmail
     implements Email
 {
     protected long id;
     
     protected String type;
     
     protected String code;
     
     protected String toAddress;
     
     protected String bounceAddress;
     
     protected String toName;
     
     protected String subject;
     
     protected String fromName;
     
     protected String fromAddress;
 
     protected String htmlTemplate;
     protected String htmlLayoutTemplate;
     
     protected String textTemplate;
     protected String textLayoutTemplate;
     
     protected Date sent;
     
     public void setFrom(String fromName, String fromAddress)
     {
         this.fromName = fromName;
         this.fromAddress = fromAddress;
     }
 
     public void setTo(String toName, String toAddress)
     {
         this.toName = toName;
         this.toAddress = toAddress;
     }
 
     @Override
     public String getType()
     {
         if (type == null)
         {
             return getClass().getName();
         }
         return type;
     }
     
 	@Override
 	public int hashCode()
 	{
 		return getType().hashCode() + toAddress.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj)
 	{
 		if(!(obj instanceof Email))
 		{
 			return false;
 		}
 		Email other = (Email) obj;
 		return id == other.getId() 
				&& type == null ? false : type.equals(other.getType()) 
				&& toAddress == null ? false : toAddress.equals(other.getToAddress());
 	}
 
     public void setSent(Date sent) {this.sent = sent;}
     public Date getSent() {return this.sent;}
     @Override public String getToAddress() {return toAddress;}
     public void setToAddress(String toAddress) {this.toAddress = toAddress;}
     @Override public String getToName(){return toName;}
     public void setToName(String toName) {this.toName = toName;}
     @Override public String getFromAddress() {return fromAddress;}
     public void setFromAddress(String fromAddress) {this.fromAddress = fromAddress;}
     @Override public String getFromName() {return fromName;}
     public void setFromName(String fromName) {this.fromName = fromName;}
     @Override public long getId() {return id;}
     @Override public void setId(long id) {this.id = id;}
     @Override public String getSubject() {return subject;}
     public void setSubject(String subject) {this.subject = subject;}
     @Override public String getHtmlTemplate() {return htmlTemplate;}
     public void setHtmlTemplate(String htmlTemplate) {this.htmlTemplate = htmlTemplate;}
     @Override public String getTextTemplate(){return textTemplate;}
     public void setTextTemplate(String textTemplate) {this.textTemplate = textTemplate;}
 	@Override public String getHtmlLayoutTemplate() {return htmlLayoutTemplate;}
 	public void setHtmlLayoutTemplate(String template) {htmlLayoutTemplate = template;}
 	@Override public String getTextLayoutTemplate() {return textLayoutTemplate;}
 	public void setTextLayoutTemplate(String template) {textLayoutTemplate = template;}
 	@Override public String getBounceTo() {return bounceAddress;}
 	public void setBounceTo(String address) {this.bounceAddress = address;}
 	@Override public String getCode() {return code;}
 	public void setCode(String code) {this.code = code;}
 
 }
