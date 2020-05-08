 class BranchedAlkanesTest extends AlkanesTest {
 
     private BranchedAlkanesTest( String name, int testsPlanned ) {
       super( name, testsPlanned );
     }
 
     private String inMiddleOf(int halfLength, String alkyl) {
       StringBuilder sb = new StringBuilder();
 
       for ( int i = 0; i < halfLength + 1; i++ )
         sb.append("C(");
 
       sb.append(alkyl.substring(1));
 
       for ( int i = 0; i < halfLength - 1; i++ )
         sb.append("C(");
       sb.append("C");
 
       for ( int i = 0; i < halfLength; i++ )
         sb.append("))");
 
       return sb.toString();
     }
 
     public void runTests() {
       is( new Alkane("C(C(C(CC(C))))").iupacName(),
           "3-Methylpentane",
           "Side chains and longest chain name the alkyl" );
          
       is( new Alkane("C(C(CC(C(CC(CC)))))").iupacName(),
           "2,3,5-Trimethylhexane",
           "Direction chosen so as to give lowest possible numbers I" );
          
       is( new Alkane("C(C(C(CC(CC(C(C(C(C(CC)))))))))").iupacName(),
           "2,7,8-Trimethyldecane",
           "Direction chosen so as to give lowest possible numbers II" );
 
       is( new Alkane("C(C(C(C(C(CC(C(C(C))C(C(C))))))))").iupacName(),
           "5-Methyl-4-propylnonane",
           "Direction chosen so as to give lowest possible numbers III" );
 
       is( new Alkane("C(C(C(C(C(C(C("
                      + "C(CC(C(C(C))))"
                      + "C(C(C(C(C(C))))))))))))").iupacName(),
           "7-(1-Methylpentyl)tridecane",
           "Branches are numbered from the trunk out along longest chain I"
         );
          
       is( new Alkane("C(C(C(C(C(C(C("
                      + "C(C(CC(C(C))))"
                      + "C(C(C(C(C(C))))))))))))").iupacName(),
           "7-(2-Methylpentyl)tridecane",
           "Branches are numbered from the trunk out along longest chain II"
         );
          
       is( new Alkane("C(C(C(C(C(C(C("
                      + "C(C(C(C(C(C(CC)))))"
                      + "C(C(C(C(C(C(C))))))))))))))").iupacName(),
           "8-(5-Methylhexyl)pentadecane",
           "Branches are numbered from the trunk out along longest chain III"
         );
 
       is( new Alkane( inMiddleOf(4, "-C(CC)") ).iupacName(),
           "5-Isopropylnonane",
           "Isopropyl" );
          
       is( new Alkane( inMiddleOf(4, "-C(C(CC))") ).iupacName(),
           "5-Isobutylnonane",
           "Isobutyl" );
          
       is( new Alkane( inMiddleOf(4, "-C(CC(C))") ).iupacName(),
          "5-sec-butylnonane",
           "sec-butyl" );
          
       is( new Alkane( inMiddleOf(5, "-C(CCC)") ).iupacName(),
          "6-tert-butylundecane",
           "tert-butyl" );
          
       is( new Alkane( inMiddleOf(6, "-C(C(C(CC)))") ).iupacName(),
           "7-Isopentyltridecane",
           "Isopentyl" );
          
       is( new Alkane( inMiddleOf(5, "-C(C(CCC))") ).iupacName(),
           "6-Neopentylundecane",
           "Neopentyl" );
          
       is( new Alkane( inMiddleOf(5, "-C(CCC(C))") ).iupacName(),
           "6-tert-Pentylundecane",
           "tert-Pentyl" );
          
       is( new Alkane( inMiddleOf(6, "-C(C(C(C(CC))))") ).iupacName(),
           "7-Isohexyltridecane",
           "Isohexyl" );
     }
 
     public static void main( String args[] ) {
       new BranchedAlkanesTest( "Branched alkanes",
                                15
         ).test();
     }
 }
