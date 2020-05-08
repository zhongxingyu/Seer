 package fr.cg95.cvq.service.request.social.impl;
 
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.social.DomesticHelpRequest;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.service.request.impl.RequestService;
 import fr.cg95.cvq.service.request.social.IDomesticHelpRequestService;
 
 /**
  * Implementation of the domestic help request service.
  * 
  * @author Rafik Djedjig (rdj@zenexity.fr)
  */
 public class DomesticHelpRequestService extends RequestService implements
         IDomesticHelpRequestService {
 
     static Logger logger = Logger.getLogger(DomesticHelpRequestService.class);
 
     public Long create(final Request request) throws CvqException,
             CvqObjectNotFoundException {
 
         DomesticHelpRequest dhr = (DomesticHelpRequest) request;
         performBusinessChecks(dhr);
 
         processTotals(dhr);
 
         return finalizeAndPersist(dhr);
     }
 
     public void modify(Request request) throws CvqException {
 
         processTotals((DomesticHelpRequest) request);
         super.modify(request);
     }
 
     private void processTotals(DomesticHelpRequest dhr) {
 //        int subjectTotalIncomes = (dhr.getRequesterPensions() == null ? 0 : dhr
 //                .getRequesterPensions().intValue())
 //                + (dhr.getRequesterAllowances() == null ? 0 : dhr.getRequesterAllowances()
 //                        .intValue())
 //                + (dhr.getRequesterFurnitureInvestmentIncome() == null ? 0 : dhr
 //                        .getRequesterFurnitureInvestmentIncome().intValue())
 //                + (dhr.getRequesterRealEstateInvestmentIncome() == null ? 0 : dhr
 //                        .getRequesterRealEstateInvestmentIncome().intValue())
 //                + (dhr.getRequesterNetIncome() == null ? 0 : dhr.getRequesterNetIncome().intValue());
 //        dhr.setRequesterIncomesAnnualTotal(BigInteger.valueOf(subjectTotalIncomes));
 //
 //        if (dhr.getSpouseInformation() != null) {
 //            int spouseTotalIncomes = (dhr.getSpousePensions() == null ? 0 : dhr.getSpousePensions()
 //                    .intValue())
 //                    + (dhr.getSpouseAllowances() == null ? 0 : dhr.getSpouseAllowances().intValue())
 //                    + (dhr.getSpouseFurnitureInvestmentIncome() == null ? 0 : dhr
 //                            .getSpouseFurnitureInvestmentIncome().intValue())
 //                    + (dhr.getSpouseRealEstateInvestmentIncome() == null ? 0 : dhr
 //                            .getSpouseRealEstateInvestmentIncome().intValue())
 //                    + (dhr.getSpouseNetIncome() == null ? 0 : dhr.getSpouseNetIncome().intValue());
 //            dhr.setSpouseIncomesAnnualTotal(BigInteger.valueOf(spouseTotalIncomes));
 //        }
 //        int realAssetsTotal = 0;
 //        List<DhrRealAsset> realAssets = dhr.getRealAssets();
 //        for (DhrRealAsset realAsset : realAssets) {
 //            realAssetsTotal += realAsset.getRealAssetValue() == null ? 0 : realAsset
 //                    .getRealAssetValue().intValue();
 //        }
 //        dhr.setRealAssetsValuesTotal(BigInteger.valueOf(realAssetsTotal));
 //
 //        int notRealAssetsTotal = 0;
 //        List<DhrNotRealAsset> notRealAssets = dhr.getNotRealAssets();
 //        for (DhrNotRealAsset notRealAsset : notRealAssets) {
 //            notRealAssetsTotal += notRealAsset.getAssetValue() == null ? 0 : notRealAsset
 //                    .getAssetValue().intValue();
 //        }
 //        dhr.setNotRealAssetsValuesTotal(BigInteger.valueOf(notRealAssetsTotal));
 //
 //        int taxesTotal = (dhr.getIncomeTax() == null ? 0 : dhr.getIncomeTax().intValue())
 //                + (dhr.getLocalRate() == null ? 0 : dhr.getLocalRate().intValue())
 //                + (dhr.getPropertyTaxes() == null ? 0 : dhr.getPropertyTaxes().intValue())
 //                + (dhr.getProfessionalTaxes() == null ? 0 : dhr.getProfessionalTaxes().intValue());
 //        dhr.setTaxesTotal(BigInteger.valueOf(taxesTotal));
     } 
     
     public boolean accept(Request request) {
         return request instanceof DomesticHelpRequest;
     }
 
     public Request getSkeletonRequest() throws CvqException {
         return new DomesticHelpRequest();
     }
     
     public boolean checkIsCoupleRequest(final Map<String,String> inputs){
         if (inputs.get("dhrRequestKind").equals("fr.cg95.cvq.business.request.social.DhrRequestKindType_Couple"))
             return true;
         return false;
     }
     
     public boolean checkHaveFamilyReferent(final Map<String,String> inputs){
         if (inputs.get("dhrHaveFamilyReferent").equals("true"))
             return true;
         return false;
     }
     
     public boolean checkIsMadam(final Map<String,String> inputs){
         if (inputs.get("dhrRequesterTitle").equals("fr.cg95.cvq.business.users.TitleType_Madam"))
             return true;
         return false;
     }
     
     public boolean checkIsNonEuropean(final Map<String,String> inputs){
         if (inputs.get("dhrRequesterNationality").equals("fr.cg95.cvq.business.users.NationalityType_OutsideEuropeanUnion"))
             return true;
         return false;
     }
     
     public boolean checkIsOtherPensionPlan(final Map<String,String> inputs){
         if (inputs.get("dhrPrincipalPensionPlan").equals("fr.cg95.cvq.business.request.social.DhrPrincipalPensionPlanType_Other"))
             return true;
         return false;
     }
     
     public boolean checkHaveGuardian(final Map<String,String> inputs){
         if (inputs.get("dhrRequesterHaveGuardian").equals("true"))
             return true;
         return false;
     }
     
     public boolean checkIsSpouseMadam(final Map<String,String> inputs){
         if (inputs.get("dhrSpouseTitle").equals("fr.cg95.cvq.business.users.TitleType_Madam"))
             return true;
         return false;
     }
     
     public boolean checkIsSpouseNonEuropean(final Map<String,String> inputs){
         if (inputs.get("dhrSpouseNationality").equals("fr.cg95.cvq.business.users.NationalityType_OutsideEuropeanUnion"))
             return true;
         return false;
     }
     
     public boolean checkIsSpouseRetired(final Map<String,String> inputs){
         if (inputs.get("dhrIsSpouseRetired").equals("true"))
             return true;
         return false;
     }
     
     public boolean checkIsSpouseOtherPensionPlan(final Map<String,String> inputs){
         if (inputs.get("dhrSpousePrincipalPensionPlan").equals("fr.cg95.cvq.business.request.social.DhrPrincipalPensionPlanType_Other"))
             return true;
         return false;
     }
     
     public boolean checkIsCurrentDwellingPlaceOfResidence(final Map<String,String> inputs){
         if (inputs.get("dhrCurrentDwellingKind").equals("fr.cg95.cvq.business.request.social.DhrDwellingKindType_placeOfResidence"))
             return true;
         return false;
     }
     
     public boolean checkIsPreviousDwellingPlaceOfResidence(final Map<String,String> inputs){
         if (inputs.get("dhrPreviousDwellingKind").equals("fr.cg95.cvq.business.request.social.DhrDwellingKindType_placeOfResidence"))
             return true;
         return false;
     }
     
     public boolean checkIsRealEstate(final Map<String,String> inputs){
         if (inputs.get("dhrNotRealAssetKind").equals("fr.cg95.cvq.business.request.social.DhrAssetKindType_RealEstate"))
             return true;
         return false;
     }
     
 }
