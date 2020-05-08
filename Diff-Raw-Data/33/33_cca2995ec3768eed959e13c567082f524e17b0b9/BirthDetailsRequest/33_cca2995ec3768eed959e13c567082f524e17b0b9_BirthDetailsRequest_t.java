 package fr.cg95.cvq.business.request.civil;
 
 import fr.cg95.cvq.business.request.*;
 import fr.cg95.cvq.business.users.*;
 import fr.cg95.cvq.business.authority.*;
 import fr.cg95.cvq.xml.common.*;
 import fr.cg95.cvq.xml.request.civil.*;
 
 import org.apache.xmlbeans.XmlOptions;
 import org.apache.xmlbeans.XmlObject;
 
 import fr.cg95.cvq.xml.common.RequestType;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.*;
 
 /**
  * Generated class file, do not edit !
  *
  * @hibernate.joined-subclass
  *  table="birth_details_request"
  *  lazy="false"
  * @hibernate.joined-subclass-key
  *  column="id"
  */
 public class BirthDetailsRequest extends Request implements Serializable { 
 
     private static final long serialVersionUID = 1L;
 
 
 
     public BirthDetailsRequest() {
         super();
        format = fr.cg95.cvq.business.request.civil.BirthCertificateFormatType.FULL_COPY;
     }
 
 
     public final String modelToXmlString() {
 
         BirthDetailsRequestDocument object = (BirthDetailsRequestDocument) this.modelToXml();
         XmlOptions opts = new XmlOptions();
         opts.setSavePrettyPrint();
         opts.setSavePrettyPrintIndent(4);
         opts.setUseDefaultNamespace();
         opts.setCharacterEncoding("UTF-8");
         return object.xmlText(opts);
     }
 
     public final XmlObject modelToXml() {
 
         Calendar calendar = Calendar.getInstance();
         Date date = null;
         BirthDetailsRequestDocument birthDetailsRequestDoc = BirthDetailsRequestDocument.Factory.newInstance();
         BirthDetailsRequestDocument.BirthDetailsRequest birthDetailsRequest = birthDetailsRequestDoc.addNewBirthDetailsRequest();
         super.fillCommonXmlInfo(birthDetailsRequest);
         if (this.format != null)
             birthDetailsRequest.setFormat(fr.cg95.cvq.xml.request.civil.BirthCertificateFormatType.Enum.forString(this.format.toString()));
         if (this.copies != null)
             birthDetailsRequest.setCopies(new BigInteger(this.copies.toString()));
         birthDetailsRequest.setBirthPostalCode(this.birthPostalCode);
         birthDetailsRequest.setComment(this.comment);
         birthDetailsRequest.setBirthFirstNames(this.birthFirstNames);
         if (this.motive != null)
             birthDetailsRequest.setMotive(fr.cg95.cvq.xml.request.civil.BirthCertificateMotiveType.Enum.forString(this.motive.toString()));
         birthDetailsRequest.setRequesterQualityPrecision(this.requesterQualityPrecision);
         date = this.birthDate;
         if (date != null) {
             calendar.setTime(date);
             birthDetailsRequest.setBirthDate(calendar);
         }
         if (this.requesterQuality != null)
             birthDetailsRequest.setRequesterQuality(fr.cg95.cvq.xml.request.civil.BirthRequesterQualityType.Enum.forString(this.requesterQuality.toString()));
         birthDetailsRequest.setBirthCity(this.birthCity);
         FatherInformationType fatherInformationTypeFatherInformation = birthDetailsRequest.addNewFatherInformation();
         fatherInformationTypeFatherInformation.setFatherLastName(this.fatherLastName);
         birthDetailsRequest.setBirthMarriageName(this.birthMarriageName);
         fatherInformationTypeFatherInformation.setFatherFirstNames(this.fatherFirstNames);
         MotherInformationType motherInformationTypeMotherInformation = birthDetailsRequest.addNewMotherInformation();
         motherInformationTypeMotherInformation.setMotherFirstNames(this.motherFirstNames);
         birthDetailsRequest.setBirthLastName(this.birthLastName);
         motherInformationTypeMotherInformation.setMotherMaidenName(this.motherMaidenName);
         return birthDetailsRequestDoc;
     }
 
     @Override
     public RequestType modelToXmlRequest() {
         BirthDetailsRequestDocument birthDetailsRequestDoc =
             (BirthDetailsRequestDocument) modelToXml();
         return birthDetailsRequestDoc.getBirthDetailsRequest();
     }
 
     public static BirthDetailsRequest xmlToModel(BirthDetailsRequestDocument birthDetailsRequestDoc) {
 
         BirthDetailsRequestDocument.BirthDetailsRequest birthDetailsRequestXml = birthDetailsRequestDoc.getBirthDetailsRequest();
         Calendar calendar = Calendar.getInstance();
         List list = new ArrayList();
         BirthDetailsRequest birthDetailsRequest = new BirthDetailsRequest();
         birthDetailsRequest.fillCommonModelInfo(birthDetailsRequest,birthDetailsRequestXml);
         if (birthDetailsRequestXml.getFormat() != null)
             birthDetailsRequest.setFormat(fr.cg95.cvq.business.request.civil.BirthCertificateFormatType.forString(birthDetailsRequestXml.getFormat().toString()));
         else
             birthDetailsRequest.setFormat(fr.cg95.cvq.business.request.civil.BirthCertificateFormatType.getDefaultBirthCertificateFormatType());
         birthDetailsRequest.setCopies(birthDetailsRequestXml.getCopies());
         birthDetailsRequest.setBirthPostalCode(birthDetailsRequestXml.getBirthPostalCode());
         birthDetailsRequest.setComment(birthDetailsRequestXml.getComment());
         birthDetailsRequest.setBirthFirstNames(birthDetailsRequestXml.getBirthFirstNames());
         if (birthDetailsRequestXml.getMotive() != null)
             birthDetailsRequest.setMotive(fr.cg95.cvq.business.request.civil.BirthCertificateMotiveType.forString(birthDetailsRequestXml.getMotive().toString()));
         else
             birthDetailsRequest.setMotive(fr.cg95.cvq.business.request.civil.BirthCertificateMotiveType.getDefaultBirthCertificateMotiveType());
         birthDetailsRequest.setRequesterQualityPrecision(birthDetailsRequestXml.getRequesterQualityPrecision());
         calendar = birthDetailsRequestXml.getBirthDate();
         if (calendar != null) {
             birthDetailsRequest.setBirthDate(calendar.getTime());
         }
         if (birthDetailsRequestXml.getRequesterQuality() != null)
             birthDetailsRequest.setRequesterQuality(fr.cg95.cvq.business.request.civil.BirthRequesterQualityType.forString(birthDetailsRequestXml.getRequesterQuality().toString()));
         else
             birthDetailsRequest.setRequesterQuality(fr.cg95.cvq.business.request.civil.BirthRequesterQualityType.getDefaultBirthRequesterQualityType());
         birthDetailsRequest.setBirthCity(birthDetailsRequestXml.getBirthCity());
         birthDetailsRequest.setFatherLastName(birthDetailsRequestXml.getFatherInformation().getFatherLastName());
         birthDetailsRequest.setBirthMarriageName(birthDetailsRequestXml.getBirthMarriageName());
         birthDetailsRequest.setFatherFirstNames(birthDetailsRequestXml.getFatherInformation().getFatherFirstNames());
         birthDetailsRequest.setMotherFirstNames(birthDetailsRequestXml.getMotherInformation().getMotherFirstNames());
         birthDetailsRequest.setBirthLastName(birthDetailsRequestXml.getBirthLastName());
         birthDetailsRequest.setMotherMaidenName(birthDetailsRequestXml.getMotherInformation().getMotherMaidenName());
         return birthDetailsRequest;
     }
 
     private fr.cg95.cvq.business.request.civil.BirthCertificateFormatType format;
 
     public final void setFormat(final fr.cg95.cvq.business.request.civil.BirthCertificateFormatType format) {
         this.format = format;
     }
 
 
     /**
      * @hibernate.property
      *  column="format"
      */
     public final fr.cg95.cvq.business.request.civil.BirthCertificateFormatType getFormat() {
         return this.format;
     }
 
     private java.math.BigInteger copies;
 
     public final void setCopies(final java.math.BigInteger copies) {
         this.copies = copies;
     }
 
 
     /**
      * @hibernate.property
      *  column="copies"
      *  type="serializable"
      */
     public final java.math.BigInteger getCopies() {
         return this.copies;
     }
 
     private String birthPostalCode;
 
     public final void setBirthPostalCode(final String birthPostalCode) {
         this.birthPostalCode = birthPostalCode;
     }
 
 
     /**
      * @hibernate.property
      *  column="birth_postal_code"
      *  length="2"
      */
     public final String getBirthPostalCode() {
         return this.birthPostalCode;
     }
 
     private String comment;
 
     public final void setComment(final String comment) {
         this.comment = comment;
     }
 
 
     /**
      * @hibernate.property
      *  column="comment"
      */
     public final String getComment() {
         return this.comment;
     }
 
     private String birthFirstNames;
 
     public final void setBirthFirstNames(final String birthFirstNames) {
         this.birthFirstNames = birthFirstNames;
     }
 
 
     /**
      * @hibernate.property
      *  column="birth_first_names"
      */
     public final String getBirthFirstNames() {
         return this.birthFirstNames;
     }
 
     private fr.cg95.cvq.business.request.civil.BirthCertificateMotiveType motive;
 
     public final void setMotive(final fr.cg95.cvq.business.request.civil.BirthCertificateMotiveType motive) {
         this.motive = motive;
     }
 
 
     /**
      * @hibernate.property
      *  column="motive"
      */
     public final fr.cg95.cvq.business.request.civil.BirthCertificateMotiveType getMotive() {
         return this.motive;
     }
 
     private String requesterQualityPrecision;
 
     public final void setRequesterQualityPrecision(final String requesterQualityPrecision) {
         this.requesterQualityPrecision = requesterQualityPrecision;
     }
 
 
     /**
      * @hibernate.property
      *  column="requester_quality_precision"
      */
     public final String getRequesterQualityPrecision() {
         return this.requesterQualityPrecision;
     }
 
     private java.util.Date birthDate;
 
     public final void setBirthDate(final java.util.Date birthDate) {
         this.birthDate = birthDate;
     }
 
 
     /**
      * @hibernate.property
      *  column="birth_date"
      */
     public final java.util.Date getBirthDate() {
         return this.birthDate;
     }
 
     private fr.cg95.cvq.business.request.civil.BirthRequesterQualityType requesterQuality;
 
     public final void setRequesterQuality(final fr.cg95.cvq.business.request.civil.BirthRequesterQualityType requesterQuality) {
         this.requesterQuality = requesterQuality;
     }
 
 
     /**
      * @hibernate.property
      *  column="requester_quality"
      */
     public final fr.cg95.cvq.business.request.civil.BirthRequesterQualityType getRequesterQuality() {
         return this.requesterQuality;
     }
 
     private String birthCity;
 
     public final void setBirthCity(final String birthCity) {
         this.birthCity = birthCity;
     }
 
 
     /**
      * @hibernate.property
      *  column="birth_city"
      *  length="32"
      */
     public final String getBirthCity() {
         return this.birthCity;
     }
 
     private String fatherLastName;
 
     public final void setFatherLastName(final String fatherLastName) {
         this.fatherLastName = fatherLastName;
     }
 
 
     /**
      * @hibernate.property
      *  column="father_last_name"
      *  length="38"
      */
     public final String getFatherLastName() {
         return this.fatherLastName;
     }
 
     private String birthMarriageName;
 
     public final void setBirthMarriageName(final String birthMarriageName) {
         this.birthMarriageName = birthMarriageName;
     }
 
 
     /**
      * @hibernate.property
      *  column="birth_marriage_name"
      *  length="38"
      */
     public final String getBirthMarriageName() {
         return this.birthMarriageName;
     }
 
     private String fatherFirstNames;
 
     public final void setFatherFirstNames(final String fatherFirstNames) {
         this.fatherFirstNames = fatherFirstNames;
     }
 
 
     /**
      * @hibernate.property
      *  column="father_first_names"
      */
     public final String getFatherFirstNames() {
         return this.fatherFirstNames;
     }
 
     private String motherFirstNames;
 
     public final void setMotherFirstNames(final String motherFirstNames) {
         this.motherFirstNames = motherFirstNames;
     }
 
 
     /**
      * @hibernate.property
      *  column="mother_first_names"
      */
     public final String getMotherFirstNames() {
         return this.motherFirstNames;
     }
 
     private String birthLastName;
 
     public final void setBirthLastName(final String birthLastName) {
         this.birthLastName = birthLastName;
     }
 
 
     /**
      * @hibernate.property
      *  column="birth_last_name"
      *  length="38"
      */
     public final String getBirthLastName() {
         return this.birthLastName;
     }
 
     private String motherMaidenName;
 
     public final void setMotherMaidenName(final String motherMaidenName) {
         this.motherMaidenName = motherMaidenName;
     }
 
 
     /**
      * @hibernate.property
      *  column="mother_maiden_name"
      *  length="38"
      */
     public final String getMotherMaidenName() {
         return this.motherMaidenName;
     }
 
 }
