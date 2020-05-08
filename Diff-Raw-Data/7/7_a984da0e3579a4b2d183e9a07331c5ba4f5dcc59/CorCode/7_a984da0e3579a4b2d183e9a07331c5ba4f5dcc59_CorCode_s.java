 /* This file is part of hritserver.
  *
  *  hritserver is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  hritserver is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with hritserver.  If not, see <http://www.gnu.org/licenses/>.
  */
 package hritserver.handler.get.compare;
 
 
 import hritserver.json.corcode.Annotation;
 import hritserver.json.corcode.Range;
 import edu.luc.nmerge.mvd.MVD;
 import edu.luc.nmerge.mvd.Pair;
 import java.util.ArrayList;
 import hritserver.constants.JSONKeys;
 import hritserver.constants.ChunkState;
 import hritserver.json.JSONDocument;
 import hritserver.json.corcode.RangeComplete;
 import hritserver.json.corcode.ProgressiveParser;
 import hritserver.exception.JSONException;
 /**
  * Construct a CorCode programmatically
  * @author desmond
  */
 public class CorCode extends JSONDocument implements RangeComplete
 {
     /** current range */
     Range current;
     /** ranges waiting to be completed */
     ArrayList<Range> pending;
     /** The ranges array */
     ArrayList<JSONDocument> ranges;
     /** the last offset added to ranges */
     int lastOffset;
     /** the current offset */
     int offset;
     /** the transposition id-generator */
     IdGenerator idGen;
     /**
      * Create a new CorCode
      * @param style the style name to use for this CorCode
      */
     public CorCode( String style )
     {
         ranges = new ArrayList<JSONDocument>();
         pending = new ArrayList<Range>();
         put( JSONKeys.RANGES, ranges );
         put( JSONKeys.STYLE, style );
         idGen = new IdGenerator();
     }
     /**
      * A range in the CorCode has been completed
      * @param offset the absolute offset
      * @param len the range's length
      */
     @Override
     public void rangeComplete( int offset, int len )
     {
         this.offset = offset;
         // flush pending
         for ( int i=0;i<pending.size();i++ )
         {
             Range r = pending.get(i);
             r.len += len;
             if ( r.offset == Range.UNSET )
                 r.offset = offset;
             if ( r.len > 0 )
                 addRange( r );
         }
         pending.clear();
         // update current
         if ( current != null )
         {
             if ( current.offset == Range.UNSET )
                 current.offset = offset;
             current.len += len;
         }
     }
     /**
      * Add a range to the CorCode
      * @param r the range to add
      * @return null to indicate that the passed-in range is now gone
      */
     private Range addRange( Range r ) 
     {
         try
         {
             JSONDocument doc = new JSONDocument();
             int reloff = r.offset - lastOffset;
             lastOffset = r.offset;
             doc.put( JSONKeys.NAME, r.name );
             doc.put( JSONKeys.RELOFF, reloff );
             doc.put( JSONKeys.LEN, r.len );
             if ( r.annotations != null && r.annotations.size() > 0 )
             {
                 ArrayList<Object> attrs = new ArrayList<Object>();
                 for ( int i=0;i<r.annotations.size();i++ )
                 {
                     Annotation a = r.annotations.get( i );
                     attrs.add( a.toJSONDocument() );
                 }
                 doc.put( JSONKeys.ANNOTATIONS, attrs );
             }
             ranges.add( doc );
         }
         catch ( JSONException je )
         {
             System.out.println(je.getMessage());
         }
         return null;
     }
     /**
      * Generate ranges in corcode by comparing one version with another.
      * @param text the MVD to get the versions from
      * @param v1 the first version
      * @param v2 the second version
      * @param state the state to label pairs in v1 that are not in v2
      */
     public void compareCode( MVD text, int v1, int v2, String state )
     {
         try
         {
             ProgressiveParser pp = new ProgressiveParser( this );
             ArrayList<Pair> pairs = text.getPairs();
             for ( int i=0;i<pairs.size();i++ )
             {
                 Pair p = pairs.get( i );
                 if ( p.versions.nextSetBit(v1)==v1 )
                 {
                     String str = new String(p.getData(),text.getEncoding());
                     char[] chars = str.toCharArray();
                     if ( p.versions.nextSetBit(v2)!=v2 )
                     {
                         if ( current == null )
                             current = new Range( state );
                         boolean hasText = pp.parseData(chars);
                         if ( !current.hasText && hasText )
                             current.hasText = true;
                     }
                     else 
                     {
                         if ( current != null )
                         {
                             if ( current.hasText )
                                 pending.add( current );
                             current = null;
                         }
                         pp.parseData( chars );
                     }
                 }   
             }
         }
         catch ( Exception e )
         {
         }
     }
     /**
      * Compute the child id
      * @param id the numeric value
      * @return a string representation of it to use as a target
      */
     String childId( int id )
     {
         return "#c"+Integer.toString(id);
     }
     /**
      * Compute the parent id
      * @param id the numeric value
      * @return a string representation of it to use as a target
      */
     String parentId( int id )
     {
         return "#p"+Integer.toString(id);
     }
     /**
      * Get the name of the current range from the pair and the default name
      * @param p the pair to test
      * @param defaultName if not a transposition return this
      * @return a String being the name of the new range
      */
     String getState( Pair p, String defaultName )
     {
         if ( p.isParent() )
             return ChunkState.PARENT;
         else if ( p.isChild() )
             return ChunkState.CHILD;
         else
             return defaultName;
     }
     /**
      * Create a new Range
      * @param p the pair it was gleaned from
      * @param name its name
      * @return the new Range
      */
     Range newCurrent( Pair p, String name )
     {
         Range r = new Range( name );
         if ( p.isParent() )
         {
             int id = p.ensureId();
             r.addAnnotation(JSONKeys.CHILDID, childId(id) );
         }
         else if ( p.isChild() )
         {
             int id = p.getParent().ensureId();
             r.addAnnotation(JSONKeys.PARENTID, parentId(id) );
         }
         r.offset = offset;
         r.len = p.length();
         return r;
     }
     /**
      * Process a merged pair. Successive calls may work within the same run.
      * @param p a merged pair
      * @param prefix prefix this to all generated IDs
      * @param split split this merged pair from the previous if any
      */
     void doMergedPair( Pair p, char prefix, boolean split )
     {
         if ( split )
         {
             if ( current != null )
                 current = addRange( current );
            current = new Range( ChunkState.MERGED, offset, 
                offset+p.length() );
             current.addAnnotation( "mergeid", prefix
                 +new Integer(idGen.next()).toString() );
         }
         else
         {
             if ( current != null )
             {
                 current.len += p.length();
             }
             else
             {
                current = new Range( ChunkState.MERGED, offset, 
                    p.length() );
                 current.addAnnotation( "mergeid", prefix
                     +new Integer(idGen.next()).toString() );
             }
         }
     }
     /**
      * Compare two versions of an MVD and store the result in this corcode
      * @param text the MVD
      * @param v1 the first version to compare with v2
      * @param v2 the second version to compare with v1
      * @param state the name of the ranges to create
      * @param runs an array of runs in v1 where it is permitted to merge spans
      */
     public void compareText( MVD text, int v1, int v2, String state, 
         Run[] runs )
     {
         try
         {
             int j = 0;
             ArrayList<Pair> pairs = text.getPairs();
             boolean split = false;
             for ( int i=0;i<pairs.size();i++ )
             {
                 Pair p = pairs.get( i );
                 boolean hasv1 = p.versions.nextSetBit(v1)==v1;
                 boolean hasv2 = p.versions.nextSetBit(v2)==v2;
                 split = split||(hasv1&&!hasv2)||(hasv2&&!hasv1);
                 if ( p.length()> 0 )
                 {
                     if ( hasv1 )
                     {
                         if ( !hasv2 )
                         {
                             String name = getState(p,state);
                             // save or extend current range
                             if ( current != null && !name.equals(current.getName()) )
                                 current = addRange( current );
                             // else create new range
                             if ( current == null )
                                 current = newCurrent( p, name );
                             else
                                 current.len += p.length();
                         }
                         else    // hasv1 && hasv2
                         {
                             if ( current != null 
                                 && !current.name.equals(ChunkState.MERGED) )
                                 current = addRange( current );
                             doMergedPair( p, state.charAt(0), split );
                             split = false;
                         }
                         offset += p.length();
                     }
                 }
             }
         }
         catch ( Exception e )
         {
             // shouldn't happen anyway
             e.printStackTrace(System.out);
         }
     }
 }
