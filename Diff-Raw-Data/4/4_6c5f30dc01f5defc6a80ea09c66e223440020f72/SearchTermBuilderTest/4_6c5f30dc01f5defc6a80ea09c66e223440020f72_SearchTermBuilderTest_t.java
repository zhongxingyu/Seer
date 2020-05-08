 package com.redshape.search.serializers;
 
 import com.redshape.search.query.SearchTermBuilder;
 import com.redshape.search.query.terms.*;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.search.serializers
  * @date 10/28/11 9:41 PM
  */
 public class SearchTermBuilderTest {
 
 	@Test
 	public void testMain() {
 		SearchTermBuilder builder = new SearchTermBuilder();
 
 		IFieldTerm fieldTermA = builder.field( "afla", builder.literal("bfla") );
 		IFieldTerm fieldTermB = builder.field("bfla?", builder.literal("dfla") );
 		IFieldTerm fieldTermC = builder.field("cfla?", builder.literal("dfla") );
 
 		IFieldTerm groupFieldTerm1 = builder.field("g1", builder.literal("g2"));
 		IFieldTerm groupFieldTerm2 = builder.field("g2", builder.literal("g2"));
 
 		IBinaryTerm term = builder.and(
 			builder.and(
 				builder.not(fieldTermA),
 				builder.group( Operation.OR, groupFieldTerm1, groupFieldTerm2 )
 			),
 			builder.and(
 					builder.or(fieldTermB, fieldTermC),
 					builder.to(
 							groupFieldTerm1,
 							groupFieldTerm2,
 							RangeType.INCLUSIVE
 					)
 			)
 		);
 
 		assertEquals( term.getOperation(), Operation.AND );
		assertEquals(
            ( (FieldTerm) term.<IBinaryTerm>getLeft().<IUnaryTerm>getLeft().getTerm() ).getField(),
            fieldTermA.getField() );
 		assertEquals( term.<IBinaryTerm>getLeft().<IGroupingTerm>getRight().getOperation(), Operation.OR  );
 
 		for ( ISearchTerm listItem : term.<IBinaryTerm>getLeft().<IGroupingTerm>getRight().getList() ) {
 			assertTrue( listItem instanceof IFieldTerm );
 			assertTrue( listItem.equals( groupFieldTerm1 )
 				|| listItem.equals( groupFieldTerm2 ) );
 		}
 
 		assertEquals( Operation.OR, term.<IBinaryTerm>getRight().<IBinaryTerm>getLeft().getOperation() );
 		assertEquals( Operation.TO, term.<IBinaryTerm>getRight().<IBinaryTerm>getRight().getOperation() );
 		assertEquals( RangeType.INCLUSIVE, term.<IBinaryTerm>getRight().<ToTerm>getRight().getType() );
 	}
 
 }
