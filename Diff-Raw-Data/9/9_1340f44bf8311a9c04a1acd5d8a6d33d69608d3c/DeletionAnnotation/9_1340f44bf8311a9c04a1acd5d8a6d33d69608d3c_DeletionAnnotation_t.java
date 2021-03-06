 package exomizer.annotation;
 
 import exomizer.reference.KnownGene;
 import exomizer.reference.Annotation;
 import exomizer.reference.Translator;
 
 
 /**
  * This class is intended to provide a static method to generate annotations for deletion
  * mutations. This method is put in its own class only for convenience and to at least
  * have a name that is easy to find.
  * @version 0.01 (November 15, 2012)
  * @author Peter N Robinson
  */
 
 public class DeletionAnnotation {
 
     /**
      * Creates annotation for a single-nucleotide deletion on the plus strand.
      * @param kgl The known gene that corresponds to the deletion caused by the variant.
      * @param frame_s 0 if deletion begins at first base of codon, 1 if it begins at second base, 2 if at third base
      * @param wtnt3 Nucleotide sequence of wildtype codon
      * @param wtnt3_after Nucleotide sequence of codon following that affected by variant
      * @param ref sequence of wildtype sequence
      * @param var alternate sequence (should be '-')
      * @param refvarstart Position of the variant in the CDS of the known gene
      * @param exonNumber Number of the affected exon (one-based: TODO chekc this).
      * @return An annotation corresponding to the deletion.
      */
     public static Annotation getAnnotationSingleNucleotidePlusStrand(KnownGene kgl,int frame_s, String wtnt3,String wtnt3_after,
 		String ref, String var,int refvarstart,int exonNumber){
 	String annotation = null;
 	Translator translator = Translator.getTranslator(); /* Singleton */
 	char deletedNT=' ';
 	String varnt3=null;
 	if (frame_s == 1) {
 	    deletedNT = wtnt3.charAt(1);
 	    varnt3 = String.format("%c%c%s",wtnt3.charAt(0), wtnt3.charAt(2),wtnt3_after);
 	    /* $deletent = $wtnt3[1];
 	       $varnt3 = $wtnt3[0].$wtnt3[2].$wtnt3_after; */
 	} else if (frame_s == 2) {
 	    deletedNT = wtnt3.charAt(2);
 	    varnt3 = String.format("%c%c%s",wtnt3.charAt(0), wtnt3.charAt(1),wtnt3_after);
 	    /* $deletent = $wtnt3[2];
 	       $varnt3 = $wtnt3[0].$wtnt3[1].$wtnt3_after; */
 	} else {
 	    deletedNT = wtnt3.charAt(0);
 	    varnt3 = String.format("%c%c%s",wtnt3.charAt(1), wtnt3.charAt(2),wtnt3_after);
 	    /*$deletent = $wtnt3[0];
 	      $varnt3 = $wtnt3[1].$wtnt3[2].$wtnt3_after; */
 	}
 	String wtaa = translator.translateDNA(wtnt3);
 	String varaa = translator.translateDNA(varnt3);
 	int aavarpos = (int)Math.floor((refvarstart-kgl.getRefCDSStart())/3)+1;
 	/*$varpos) =   int(($refvarstart-$refcdsstart)/3)+1; */
 	/* The following gives us the cDNA annotation */
 	String canno = String.format("c.%ddel%c",(refvarstart-kgl.getRefCDSStart()+1),deletedNT);
 	/* $canno = "c." . ($refvarstart-$refcdsstart+1) . "del$deletent"; */
 	/* Now create amino-acid annotation */
 	if (wtaa.equals("*")) { /* #mutation on stop codon */ 
 	    if (varaa.startsWith("*")) { /* #stop codon is still stop codon 	if ($varaa =~ m/\* /)   */
 		String nfsdel_ann = String.format("%s:%s:exon%d:%s:p.X%dX,",kgl.getName2(),kgl.getName(),
 						     exonNumber,canno,aavarpos);
 		Annotation ann = Annotation.createNonFrameshiftDeletionAnnotation(nfsdel_ann);
 		return ann;
 		/* Annovar: "$geneidmap->{$seqid}:$seqid:exon$exonpos:$canno:p.X$varpos" . "X,";	
 		   #changed fsdel to nfsdel on 2011feb19 */
 	    } else {	 /* stop codon is lost */
 		String stoploss_ann = String.format("%s:%s:exon%d:%s:p.X%d%s,",kgl.getName2(),kgl.getName(),
 						    exonNumber,canno,aavarpos,varaa);
 		/* $function->{$index}{stoploss} .= 
 		   "$geneidmap->{$seqid}:$seqid:exon$exonpos:$canno:p.X$varpos" . "$varaa,"; */
 		Annotation ann = Annotation.createStopLossAnnotation(stoploss_ann);
 		return ann;
 	    }
 	} else {
 	    if (varaa.contains("*")) { /* new stop codon created */
 		String stopgain_ann = String.format("%s:%s:exon%d:%s:p.%s%dX,",kgl.getName2(),kgl.getName(),
 						  exonNumber,canno,wtaa, aavarpos);
 		/* $function->{$index}{stopgain} .= 
 		   "$geneidmap->{$seqid}:$seqid:exon$exonpos:$canno:p.$wtaa$varpos" . "X,"; */
 		Annotation ann = Annotation.createStopGainAnnotation(stopgain_ann);
 		return ann;
 	    } else {
 		String fsdel_ann = String.format("%s:%s:exon%d:%s:p.%s%dfs,",kgl.getName2(),kgl.getName(),
 						  exonNumber,canno,wtaa,aavarpos);
 		/*  $function->{$index}{fsdel} .= 
 		    "$geneidmap->{$seqid}:$seqid:exon$exonpos:$canno:p.$wtaa$varpos" . "fs,"; */
 	
		Annotation ann = Annotation.createFrameshiftDeletionAnnotation(fsdel_ann);
 		return ann;
 	    }
 	}
 
     }
 
 
      /**
      * Creates annotation for a block deletion on the plus strand.
      * This is recognized by the fact that the ref sequence has a lenght greater than one
      * and the variant sequence is "-".
      * Note that with the $firstcodondel option set to true, annovar reports deletions that
      * affect the first amino acid as ABC:uc001ab:wholegene (FSDEL). We will not follow annovar
      * here, but rather repoprt such as deletion as with any other amino acid.
      * @param kgl The known gene that corresponds to the deletion caused by the variant.
      * @param frame_s 0 if deletion begins at first base of codon, 1 if it begins at second base, 2 if at third base
      * @param wtnt3 Nucleotide sequence of wildtype codon
      * @param wtnt3_after Nucleotide sequence of codon following that affected by variant
      * @param ref sequence of wildtype sequence
      * @param var alternate sequence (should be '-')
      * @param refvarstart Position of the variant in the CDS of the known gene
      * @param exonNumber Number of the affected exon (one-based: TODO chekc this).
      * @return An annotation corresponding to the deletion.
      */
     public static Annotation getAnnotationBlockPlusStrand(KnownGene kgl,int frame_s, String wtnt3,String wtnt3_after,
 							  String ref, String var,int refvarstart,int refvarend, int exonNumber){
 	String annotation = null;
 	Translator translator = Translator.getTranslator(); /* Singleton */
 	char deletedNT=' ';
 	String varnt3=null;
 	String canno = null;
 	String panno = null;
 	String wtaa = translator.translateDNA(wtnt3);
 	int refcdsstart = kgl.getRefCDSStart();
 	int cdslen = kgl.getCDSLength();
 
 	int aavarpos = (int)Math.floor((refvarstart-kgl.getRefCDSStart())/3)+1;
 	int varposend = -1; // 	the position of the last amino acid in the deletion
 
 	if (refvarstart <=refcdsstart) { /* first amino acid deleted */
 	    if (refvarend >= cdslen  + refcdsstart) { // i.e., 3' portion of the gene is deleted
 		varposend = (int)Math.floor( cdslen / 3); //int ($cdslen->{$seqid}/3);
 		//$canno = "c." . ($refvarstart-$refcdsstart+1) . "_" . ($cdslen->{$seqid}+$refcdsstart-1) . "del";
 		canno = String.format("c.%d_%ddel",refvarstart-refcdsstart,cdslen+refcdsstart-1);
 					
 	    } else { /* deletion encompasses less than entire CDS */
 		//$varposend = int (($refvarend-$refcdsstart)/3) + 1;
 		varposend = (int)Math.floor(( refvarend-refcdsstart)/3) + 1;
 		//$canno = "c." . 1 . "_" . ($refvarend-$refcdsstart+1) . "del";	#added 20120618
 		canno = String.format("c.1_%ddel",refvarend-refvarstart+1);
 		//($refvarend-$refvarstart+1) % 3 == 0 or $is_fs++;
 	    }
 	    //$function->{$index}{fsdel} .= "$geneidmap->{$seqid}:$seqid:exon$exonpos:$canno:p.${varpos}_${varposend}del,";
 	    panno = String.format("%s:%s:exon%d:%s:p.%d_%ddel,",kgl.getName2(),kgl.getName(),
 				  exonNumber,canno,aavarpos,varposend);
 	    Annotation ann = Annotation.createFrameShiftSubstitionAnnotation(panno);
 	    return ann;
 	} else if (refvarend >= cdslen + refcdsstart) { // 3' portion of the gene is deleted
 	    varposend = (int)Math.floor(cdslen/3);	
 	    //$canno = "c." . ($refvarstart-$refcdsstart+1) . "_" . ($cdslen->{$seqid}+$refcdsstart-1) . "del";
 	    canno = String.format("c.%d_%ddel",refvarstart - refcdsstart +1, cdslen + refcdsstart -1);
 	    //$function->{$index}{fsdel} .= "$geneidmap->{$seqid}:$seqid:exon$exonpos:$canno:p.${varpos}_${varposend}del,";
 	    panno = String.format("%s:%s:exon%d:%s:p.%d_%ddel,",kgl.getName2(),kgl.getName(),
 				  exonNumber,canno,aavarpos,varposend);
 	    Annotation ann = Annotation.createFrameShiftSubstitionAnnotation(panno);
 	    return ann;
 	    //$is_fs++;
 	} else if ((refvarend-refvarstart+1) % 3 == 0) { /* Non-frameshift deletion */
 	    varposend = (int)Math.floor(( refvarend- refcdsstart)/3) + 1;
 	    //$canno = "c." . ($refvarstart-$refcdsstart+1) . "_" . ($refvarend-$refcdsstart+1) . "del";
 	    canno = String.format("c.%d_%ddel",refvarstart-refcdsstart+1,refvarend-refcdsstart+1);
 	    //$function->{$index}{nfsdel} .= "$geneidmap->{$seqid}:$seqid:exon$exonpos:$canno:p.${varpos}_${varposend}del,";
 	    panno =  String.format("%s:%s:exon%d:%s:p.%d_%ddel,",kgl.getName2(),kgl.getName(),
 				  exonNumber,canno,aavarpos,varposend);
 	    Annotation ann = Annotation.createNonFrameShiftSubstitionAnnotation(panno);
 	    return ann;
 	} else { /** frameshift deletion within the CDS */
 	    varposend = (int)Math.floor(( refvarend- refcdsstart)/3) + 1;// int (($refvarend-$refcdsstart)/3) + 1;
 	    //$canno = "c." . ($refvarstart-$refcdsstart+1) . "_" . ($refvarend-$refcdsstart+1) . "del";
 	    canno = String.format("c.%d_%ddel",refvarstart-refcdsstart+1,refvarend-refcdsstart+1);
 	    //$function->{$index}{fsdel} .= "$geneidmap->{$seqid}:$seqid:exon$exonpos:$canno:p.${varpos}_${varposend}del,";
 	     panno =  String.format("%s:%s:exon%d:%s:p.%d_%ddel,",kgl.getName2(),kgl.getName(),
 				  exonNumber,canno,aavarpos,varposend);
 	     Annotation ann = Annotation.createFrameShiftSubstitionAnnotation(panno);
 	    return ann;
 	     
 	}
     }
 
 
 	
 }
