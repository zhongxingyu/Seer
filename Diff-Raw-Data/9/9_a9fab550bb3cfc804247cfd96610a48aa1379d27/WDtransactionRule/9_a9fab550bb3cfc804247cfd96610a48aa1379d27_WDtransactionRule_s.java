 /**
  * 
  */
 package com.grimesco.gcodataport.fi;
 
 import java.util.ArrayList;
 
 import com.grimesco.gcocore.act.Account;
 import com.grimesco.gcocore.axys.model.AxysBlotter;
 import com.grimesco.gcocore.axys.model.AxysSymbol;
 import com.grimesco.translateFidelity.model.POJO.FIcomment;
 import com.grimesco.translateFidelity.model.POJO.FItransaction;
 
 /**
  * @author GentleHawk
  *
  */
 
 public class WDtransactionRule extends A_FIBlotterTransactionRule {
 
 	//-- constructor
 	public WDtransactionRule(Account _account, FItransaction _fitx, AxysSymbol _asymbol, boolean cancelFlag, String _dvsource) {
 	
 		super(_account, _fitx, _asymbol);
 			
 		
 		if (_dvsource.trim().equals("client"))  { //-- if the WD transaction initiated by client
 			
 			//===================================================================================
 			//-- Blotter Col #2 set Transaction Code
 			//===================================================================================
 			//-- Check Cancel Flag 
 			if (cancelFlag) {
 				txBlotter.setTRANSACTION_CODE("LO".toCharArray());
 			} else {
 				txBlotter.setTRANSACTION_CODE("lo".toCharArray());	
 			}
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #4 set Security Type
 			//===================================================================================
 			txBlotter.setSECURITY_TYPE("caus".toCharArray());
 			//===================================================================================
 					
 			//===================================================================================
 			//-- Blotter Col #5 set Security Symbol
 			//===================================================================================
 			if (String.valueOf(fitx.getSYMBOL()).equals("na"))
 				txBlotter.setSECURITY_SYMBOL("fcash".toCharArray());
 			else
 				txBlotter.setSECURITY_SYMBOL(fitx.getSYMBOL());
 			//===================================================================================
 		
 			//===================================================================================
 			//-- Blotter Col #6 set Trade Date
 			//===================================================================================
 			txBlotter.setSQL_TRADE_DATE(fitx.getSQL_TRANSACTION_DATE());	
 			//===================================================================================
 		
 		
 			//===================================================================================
 			//-- Blotter Col #18 set Trade Amount
 			//===================================================================================
 			txBlotter.setTRADE_AMOUNT(fitx.getAMOUNT());
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #42 set Source
 			//===================================================================================
 			txBlotter.setSOURCE("1".toCharArray());
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #45 set Recon
 			//===================================================================================
 			txBlotter.setRECON('n');
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #46 set Post
 			//===================================================================================
 			txBlotter.setPOST('y');
 			//===================================================================================
 		
 		
 		
 		} else if (_dvsource.trim().equals("xxxxxxx") )  { //-- if the WD transaction initiated by client
 
 			//-- NOTE:
 			//-- assumption: WD with xxxxxxx transaction will always have a comment.
 			//-- So check the comment to determine which case rule to apply.
 			int xxxxxxxCaseNumber = 0;
 
 			//-- [original]648026506     wd*060513 ex $tefra        231.23 xxxxxxx     0.00000           0.00000 060513     
 			//-- [avdemt]2vanrira,wd,,exus,tefra,06052013,06052013,,,,,caus,$cash,,,,,231.23,,,,,,,,,,,,,,,,,,,,,,,,24,,,n,y,,,,,,,,,,,,,
 			//-- reversal tefra rule	
 			if (String.valueOf(fitx.getSYMBOL()).trim().equals("tefra")) { 
 			
 				xxxxxxxCaseNumber = 5;
 			
 			} else {  //-- not tefra 
 				
 				//-- Get the first comment if there is comment
 				if( _fitx.txcommentList.size() > 0) {	
 					
 					commentFlag = true;
 					FIcomment ficomment = _fitx.txcommentList.get(0);
 					
 					if (String.valueOf(ficomment.getCOMMENT_TEXT()).startsWith("NORMAL", 11) == true || String.valueOf(ficomment.getCOMMENT_TEXT()).startsWith("DEATH", 11) == true || String.valueOf(ficomment.getCOMMENT_TEXT()).startsWith("EARLY DIST", 11) == true || String.valueOf(ficomment.getCOMMENT_TEXT()).startsWith("IRA-TFR", 11) == true ) {	
 						//-- Normal distribution case
 						xxxxxxxCaseNumber = 1;
 						
 					} else if (String.valueOf(ficomment.getCOMMENT_TEXT()).startsWith("FED TAX W/H", 11) == true) {
 						//-- Fed tax holding case	
 						xxxxxxxCaseNumber = 2;
 						
 					} else if (String.valueOf(ficomment.getCOMMENT_TEXT()).startsWith("STATE TAX W/H", 11) == true) {
 						//-- State tax holding case
 						xxxxxxxCaseNumber = 3;	
 						
 					} else if (String.valueOf(ficomment.getCOMMENT_TEXT()).startsWith("TRANSFERRED TO", 11) == true) {
 						//-- Transfer case (has nothing to do with above the distribution case
 						xxxxxxxCaseNumber = 1;	
 					
					//	673530514     wd*111814 ex $cash          75.00 xxxxxxx     0.00000           0.00000 111814                               
					//	673530514     ; *111814 sd:        FEE CHARGED          IWS ACCOUNT CLOSEOUT
					} else if(String.valueOf(ficomment.getCOMMENT_TEXT()).startsWith("FEE CHARGED          IWS ACCOUNT CLOSEOUT", 41) == true) {
						//: BREADY-131 : account closing charge transaction
 						xxxxxxxCaseNumber = 1;	
 					
 						
 					//	Z42932485     wd*062813 ex $cash          45.00 xxxxxxx     0.00000           0.00000 062813                               
 					//	Z42932485     ; *062813 sd:        FEE CHARGED          LATE SETTLMNTS (3)  LATE SETT
 					} else if(String.valueOf(ficomment.getCOMMENT_TEXT()).startsWith("FEE CHARGED", 11) == true) {
 						xxxxxxxCaseNumber = 4;	
 						
 					} else {
 						xxxxxxxCaseNumber = 1;
 					}
 					
 				} else {
 						//-- if there is no comment with WD xxxxxxx: violation but treat it like CaseNumber 1 to prevent an error
 						xxxxxxxCaseNumber = 1;
 				}
 
 			}//-- end of not tefra if-statement
 			
 			
 			//===================================================================================
 			//-- Blotter Col #2 set Transaction Code
 			//===================================================================================
 			//-- Check Cancel Flag 
 			if (xxxxxxxCaseNumber == 1) {
 				if (cancelFlag) {
 					txBlotter.setTRANSACTION_CODE("LO".toCharArray());
 				} else {
 					txBlotter.setTRANSACTION_CODE("lo".toCharArray());	
 				}
 			} else if(xxxxxxxCaseNumber == 2 || xxxxxxxCaseNumber == 3) {
 				if (cancelFlag) {	
 					txBlotter.setTRANSACTION_CODE("DP".toCharArray());
 				} else {
 					txBlotter.setTRANSACTION_CODE("dp".toCharArray());	
 				}
 			} else if(xxxxxxxCaseNumber == 4 || xxxxxxxCaseNumber == 5) {
 				if (cancelFlag) {	
 					txBlotter.setTRANSACTION_CODE("WD".toCharArray());
 				} else {
 					txBlotter.setTRANSACTION_CODE("wd".toCharArray());	
 				}
 			}
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #4 set Security Type
 			//===================================================================================
 			if (xxxxxxxCaseNumber == 1) {
 				txBlotter.setSECURITY_TYPE("caus".toCharArray());
 			} else if(xxxxxxxCaseNumber == 2 || xxxxxxxCaseNumber == 3 || xxxxxxxCaseNumber == 5) {
 				txBlotter.setSECURITY_TYPE("exus".toCharArray());
 			} else if(xxxxxxxCaseNumber == 4 ) {
 				txBlotter.setSECURITY_TYPE("caus".toCharArray());
 			}
 				
 			//===================================================================================
 			
 			
 			
 			//===================================================================================
 			//-- Blotter Col #5 set Security Symbol
 			//===================================================================================
 			if (xxxxxxxCaseNumber == 1 || xxxxxxxCaseNumber == 5) {
 				txBlotter.setSECURITY_SYMBOL(fitx.getSYMBOL()); //--fcash
 			} else if (xxxxxxxCaseNumber == 2) {
 				txBlotter.setSECURITY_SYMBOL("fedtaxwhld".toCharArray()); //--fedtaxwhld
 			} else if (xxxxxxxCaseNumber == 3) {
 				txBlotter.setSECURITY_SYMBOL("stataxwhld".toCharArray()); //--stataxwhld
 			} else if (xxxxxxxCaseNumber == 4) {
 				txBlotter.setSECURITY_SYMBOL("fcash".toCharArray()); //--stataxwhld
 			}
 			//===================================================================================
 			
 			
 			
 			
 			//===================================================================================
 			//-- Blotter Col #6 set Trade Date
 			//===================================================================================
 			txBlotter.setSQL_TRADE_DATE(fitx.getSQL_TRANSACTION_DATE());	
 			//===================================================================================
 
 			
 			if(xxxxxxxCaseNumber == 2 || xxxxxxxCaseNumber == 3 || xxxxxxxCaseNumber == 4 || xxxxxxxCaseNumber == 5) {
 				
 				//===================================================================================
 				//-- Blotter Col #7 set Settle Date
 				//===================================================================================
 				txBlotter.setSQL_SETTLE_DATE(fitx.getSQL_SETTLEMENT_DATE());
 				//===================================================================================
 				
 				//===================================================================================
 				//-- Blotter Col #12 set Source Type to "caus" (type2)
 				//===================================================================================
 				txBlotter.setSECURITY_TYPE2("caus".toCharArray());
 				//===================================================================================
 			
 				//===================================================================================
 				//-- Blotter Col #13 set Source Symbol to  (symbol2)
 				//===================================================================================
 				txBlotter.setSECURITY_SYMBOL2("fcash".toCharArray());
 				//===================================================================================
 			
 			}
 			
 			//===================================================================================
 			//-- Blotter Col #18 set Trade Amount
 			//===================================================================================
 			txBlotter.setTRADE_AMOUNT(fitx.getAMOUNT());
 			//===================================================================================
 			
 
 			//===================================================================================
 			//-- Blotter Col #42 set Source
 			//===================================================================================
 
 			if (xxxxxxxCaseNumber == 5) {
 				txBlotter.setSOURCE("24".toCharArray());
 			} else {
 				txBlotter.setSOURCE("1".toCharArray());
 			}
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #45 set Recon
 			//===================================================================================
 			txBlotter.setRECON('n');
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #46 set Post
 			//===================================================================================
 			txBlotter.setPOST('y');
 			//===================================================================================
 
 		
 		} else if (_dvsource.trim().equals("cash") && String.valueOf(_fitx.getTRANSACTION_SECURITY_TYPE_CODE()).trim().toLowerCase().equals("ex") && (!String.valueOf(_fitx.getSYMBOL()).trim().toLowerCase().equals("tefra") && !String.valueOf(_fitx.getSYMBOL()).trim().toLowerCase().equals("chgcard") ) )  { //-- if the WD transaction initiated by client
 			
 			//===================================================================================
 			//-- Blotter Col #2 set Transaction Code
 			//===================================================================================
 			//-- Check Cancel Flag
 			//-- TODO: need to check with Cheryl when the actual cancel happen !!!
 			if (cancelFlag) {
 				txBlotter.setTRANSACTION_CODE("DP".toCharArray());
 			} else {
 				txBlotter.setTRANSACTION_CODE("dp".toCharArray());
 			}
 			
 			//===================================================================================
 			//-- Blotter Col #4 set Security Type
 			//===================================================================================
 			txBlotter.setSECURITY_TYPE("exus".toCharArray());
 			//===================================================================================
 		
 			//===================================================================================
 			//-- Blotter Col #5 set Security Symbol
 			//===================================================================================
 			txBlotter.setSECURITY_SYMBOL("miscfee".toCharArray());
 			//===================================================================================
 		
 			//===================================================================================
 			//-- Blotter Col #6 set Trade Date
 			//===================================================================================
 			txBlotter.setSQL_TRADE_DATE(_fitx.getSQL_TRANSACTION_DATE());	
 			//===================================================================================
 		
 			//===================================================================================
 			//-- Blotter Col #7 set Settle Date
 			//===================================================================================
 			txBlotter.setSQL_SETTLE_DATE(_fitx.getSQL_SETTLEMENT_DATE());
 			//===================================================================================
 		
 			//===================================================================================
 			//-- Blotter Col #12 set Source Type to "caus"
 			//===================================================================================
 			txBlotter.setSECURITY_TYPE2("caus".toCharArray());
 			//===================================================================================
 		
 			//===================================================================================
 			//-- Blotter Col #13 set Source Symbol2 "$cash"
 			//===================================================================================
 			txBlotter.setSECURITY_SYMBOL2("$cash".toCharArray());
 			
 
 			//===================================================================================
 			//-- Blotter Col #18 set Trade Amount
 			//===================================================================================
 			txBlotter.setTRADE_AMOUNT(_fitx.getAMOUNT());
 			//===================================================================================	
 
 			//===================================================================================
 			//-- Blotter Col #29 set Pledge
 			//===================================================================================
 				txBlotter.setPLEDGE('n');
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #30 set Lot Location
 			//===================================================================================				
 			txBlotter.setLOT_LOCATION("254".toCharArray());
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #42 set Source
 			//===================================================================================
 			txBlotter.setSOURCE("24".toCharArray());
 			//===================================================================================
 			
 			//-- Blotter Col #45 set Recon
 			//===================================================================================
 			txBlotter.setRECON('n');
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #46 set Post
 			//===================================================================================
 			txBlotter.setPOST('y');
 			//===================================================================================
 		
 
 		
 			
 		} else if ( (_dvsource.trim().equals("cash")) && ( String.valueOf(asymbol.getSYMBOL()).trim().toLowerCase().equals("tefra") ||  String.valueOf(asymbol.getSYMBOL()).trim().toLowerCase().equals("chgcard")) ) { 
 			
 			//===================================================================================
 			//-- Blotter Col #2 set Transaction Code
 			//===================================================================================
 			//-- Check Cancel Flag 
 			if (cancelFlag) {
 				txBlotter.setTRANSACTION_CODE("WD".toCharArray());
 			} else {
 				txBlotter.setTRANSACTION_CODE("wd".toCharArray());	
 			}
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #4 set Security Type
 			//===================================================================================
 			txBlotter.setSECURITY_TYPE("exus".toCharArray());
 			//===================================================================================
 					
 			//===================================================================================
 			//-- Blotter Col #5 set Security Symbol
 			//===================================================================================
 			if (String.valueOf(fitx.getSYMBOL()).equals("na"))
 				txBlotter.setSECURITY_SYMBOL("fcash".toCharArray());
 			else
 				txBlotter.setSECURITY_SYMBOL(fitx.getSYMBOL());
 			//===================================================================================
 		
 			//===================================================================================
 			//-- Blotter Col #6 set Trade Date
 			//===================================================================================
 			txBlotter.setSQL_TRADE_DATE(fitx.getSQL_TRANSACTION_DATE());	
 			//===================================================================================
 		
 			//===================================================================================
 			//-- Blotter Col #7 set Settle Date
 			//===================================================================================
 			txBlotter.setSQL_SETTLE_DATE(fitx.getSQL_SETTLEMENT_DATE());
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #12 set Source Type to "caus" (type2)
 			//===================================================================================
 			txBlotter.setSECURITY_TYPE2("caus".toCharArray());
 			//===================================================================================
 		
 			//===================================================================================
 			//-- Blotter Col #13 set Source Symbol to  (symbol2)
 			//===================================================================================
 			txBlotter.setSECURITY_SYMBOL2("$cash".toCharArray());
 			//===================================================================================
 			
 			//===================================================================================
 			//-- Blotter Col #18 set Trade Amount
 			//===================================================================================
 			txBlotter.setTRADE_AMOUNT(fitx.getAMOUNT());
 			//===================================================================================
 				
 	
 		} else { //-- anything other than above two rules will be ignored such as Margin rule (corresponding DP-WD)
 		
 			//--	
 		}
 	}
 }
