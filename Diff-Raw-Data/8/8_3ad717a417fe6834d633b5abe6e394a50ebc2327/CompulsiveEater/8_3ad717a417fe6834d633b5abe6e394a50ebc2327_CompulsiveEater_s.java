 package skittles.g6;
 
 import skittles.g6.strategy.CompulsiveOfferEvaluator;
 import skittles.g6.strategy.OfferEvaluator;
 import skittles.g6.strategy.OfferGenerator;
 import skittles.g6.strategy.OfferGeneratorImplementer;
 import skittles.g6.strategy.PreferenceEvaluator;
 import skittles.g6.strategy.PreferenceEvaluatorImpl;
 import skittles.sim.*;
 
 public class CompulsiveEater extends Player 
 {
 	
 	private PreferenceEvaluator prefEval;
 	private OfferEvaluator offerEval;
 	private OfferGenerator offerGen;
 	private int turnCounter;
 	private boolean discovery;
 	private int turnsEatenSame;
 	private int lastEatInv;
 	private int colorsRemaining;
 	
 	//===== EVERYTHING BELOW CAME FROM DumpPlayer ====
 	private int[] aintInHand;
 	private int intColorNum;
 	double dblHappiness;
 	String strClassName;
 	int intPlayerIndex;
 	
 	private double[] adblTastes;
 	private int intLastEatIndex;
 	private int intLastEatNum;
 	
 //	public DumpPlayer( int[] aintInHand )
 //	{
 //		this.aintInHand = aintInHand;
 //		intColorNum = aintInHand.length;
 //		dblHappiness = 0;
 //	}
 
 	@Override
 	public void eat( int[] aintTempEat )
 	{
 		printInHand();
 		int eatIndex = scanForLeastValuable();
 		//eat all of last color
 		
 		aintTempEat[ eatIndex ] = aintInHand[ eatIndex ];
 		aintInHand[ eatIndex ] = 0;
 		intLastEatIndex = eatIndex;
 		intLastEatNum = aintTempEat[ eatIndex ];
 		return;
 		
 		/*System.out.print("aintHand: ");
 		for (int i=0; i<intColorNum; i++){
 			System.out.print(aintInHand[i] + ", ");
 		}
 		System.out.println();
 		
 		int eatIndex = scanForLeastValuable();
 		//eat all of last color
 		if(colorsRemaining == 1){
 			aintTempEat[ eatIndex ] = aintInHand[ eatIndex ];
 			aintInHand[ eatIndex ] = 0;
 			return;
 		}
 		//try to eat one of every color
 		while(discovery && intLastEatIndex < intColorNum - 1){
 			intLastEatIndex++;
 			if(aintInHand[intLastEatIndex] == 0){
 				intLastEatIndex++;
 				continue;
 			}
 			aintInHand[intLastEatIndex]--;
 			aintTempEat[intLastEatIndex] = 1;
 			intLastEatNum = 1;
 			return;
 		}
 		discovery = false;
 		
 		
 		//TODO: Test threshold
 		if(adblTastes[eatIndex] > .5 && turnsEatenSame > 2 && eatIndex == intLastEatIndex){
 			aintTempEat[ eatIndex ] = aintInHand[ eatIndex ];
 			aintInHand[ eatIndex ] = 0;
 		}
 		else{
 			aintTempEat[ eatIndex ] = 1;
 			aintInHand[ eatIndex ]--;
 		}
 		intLastEatIndex = eatIndex;
 		intLastEatNum = aintTempEat[ eatIndex ];
 		
 		if(eatIndex == intLastEatIndex)
 			turnsEatenSame++;
 		else
 			turnsEatenSame = 1;*/
 	}
 	/*
 	 * Returns the index of the lowest value skittle which we have
 	 */
 	private int scanForLeastValuable(){
 		double minTasteValue = 2;
 		int minTasteIndex = 0;
 		colorsRemaining = intColorNum;
 		for(int i = 0; i < intColorNum; i++){
 			if(aintInHand[i] == 0){
 				colorsRemaining--;
 				continue;
 			}
 			if(adblTastes[i] < minTasteValue){
 				minTasteValue = adblTastes[i]; 
 				minTasteIndex = i;
 			}
 		}
 		return minTasteIndex;
 	}
 	
 	@Override
 	public void offer( Offer offTemp )
 	{
 		/*//Offer ourOffer = offerGen.getOffer();
 		Offer nullOffer = new Offer(intPlayerIndex, intColorNum);
 		//offTemp.setOffer( ourOffer.getOffer(), ourOffer.getDesire() );
 		offTemp.setOffer(nullOffer.getOffer(), nullOffer.getDesire());*/
 
		Offer ourOffer = offerGen.getOffer();
 		//System.out.println("our offer is" + ourOffer.toString());
 		//offTemp.setOffer( ourOffer.getOffer(), ourOffer.getDesire() );
 		int[] zeros = {0,0,0,0,0};
 		offTemp.setOffer(zeros, zeros);
 
 	}
 
 	@Override
 	public void syncInHand(int[] aintInHand) 
 	{
 		// TODO Auto-generated method stub
 		for (int i = 0; i < this.aintInHand.length; i++) {
 			this.aintInHand[i] = aintInHand[i];
 		}
 		
 	}
 
 	@Override
 	public void happier(double dblHappinessUp) 
 	{
 		double dblHappinessPerCandy = dblHappinessUp / Math.pow( intLastEatNum, 2 );
 		if ( adblTastes[ intLastEatIndex ] == -1 )
 		{
 			adblTastes[ intLastEatIndex ] = dblHappinessPerCandy;
 		}
 		else
 		{
 			if ( adblTastes[ intLastEatIndex ] != dblHappinessPerCandy )
 			{
 				System.out.println( "Error: Inconsistent color happiness!" );
 			}
 		}
 	}
 
 	@Override
 	public Offer pickOffer(Offer[] aoffCurrentOffers) 
 	{
 		return null;
 		/*prefEval.examineIncomeOffers(aoffCurrentOffers);
 		offerGen.setCurrentOffers(aoffCurrentOffers);
 		Offer gonnaPick = offerEval.getBestOffer(aoffCurrentOffers);
<<<<<<< HEAD
		if (gonnaPick==null)
=======
 		if(gonnaPick == null)
>>>>>>> 3b59ad51c1525d9474ced6afe982fdf9d503cb26
 			return null;
 		int[] aintOffer = gonnaPick.getOffer();
 		int[] aintDesire = gonnaPick.getDesire();
 		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
 		{
 			aintInHand[ intColorIndex ] += aintOffer[ intColorIndex ] - aintDesire[ intColorIndex ];
 		}
 		return gonnaPick;*/
 	}
 
 	@Override
 	public void offerExecuted(Offer offPicked) 
 	{
 		int[] aintOffer = offPicked.getOffer();
 		int[] aintDesire = offPicked.getDesire();
 		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
 		{
 			aintInHand[ intColorIndex ] += aintDesire[ intColorIndex ] - aintOffer[ intColorIndex ];
 		}
 	}
 
 	@Override
 	public void updateOfferExe(Offer[] aoffCurrentOffers) 
 	{
 		//prefEval.examineAcceptedOffers(aoffCurrentOffers);
 	}
 
 	@Override
 	public void initialize(int intPlayerNum, int intPlayerIndex, String strClassName,	int[] aintInHand) 
 	{
 		this.intPlayerIndex = intPlayerIndex;
 		this.strClassName = strClassName;
 		this.aintInHand = aintInHand;
 		intColorNum = aintInHand.length;
 		turnsEatenSame = 0;
 		intLastEatIndex = -1;
 		lastEatInv = 0;
 		dblHappiness = 0;
 		discovery = true;
 		adblTastes = new double[ intColorNum ];
 		
 		offerGen = new OfferGeneratorImplementer(intColorNum);
 		offerGen.setPlayer(this);
 		
 		prefEval = new PreferenceEvaluatorImpl(intColorNum);
 		prefEval.setPlayer(this);
 		
 		offerEval = new CompulsiveOfferEvaluator();
 		offerEval.setPlayer(this);
 		
 		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
 		{
 			adblTastes[ intColorIndex ] = -1;
 		}
 	}
 	
 	private boolean checkEnoughInHand( int[] aintTryToUse )
 	{
 		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
 		{
 			if ( aintTryToUse[ intColorIndex ] > aintInHand[ intColorIndex ] )
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public void printInHand(){
 		System.out.print("InHand: ");
 		for (int i = 0; i < aintInHand.length; i++) {
 			System.out.print(aintInHand[i] + " ");
 		}
 		System.out.println();
 	}
 
 	@Override
 	public String getClassName() 
 	{
 		return "CompulsiveEater";
 	}
 
 	@Override
 	public int getPlayerIndex() 
 	{
 		return intPlayerIndex;
 	}
 	
 	public OfferGenerator getOfferGenerator() {
 		return offerGen;
 	}
 	
 	public OfferEvaluator getOfferEvaluator() {
 		return offerEval;
 	}
 	
 	public PreferenceEvaluator getPreferenceEavluator() {
 		return prefEval;
 	}
 	
 	public int getTurnCounter() {
 		return turnCounter;
 	}
 	
 	public double[] getPreferences() {
 		return adblTastes;
 	}
 	
 	public int[] getAIntInHand() {
 		return aintInHand;
 	}
 	
 	public int getIntLastEatIndex() {
 		return intLastEatIndex;
 	}
 }
