 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package novelang.common.tree;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.NoSuchElementException;
 
 import com.google.common.base.Preconditions;
 import novelang.common.SyntacticTree;
 
 /**
  * Manipulation of immutable {@link Tree}s through {@link Treepath}s.
  * <p>
  * Convention for diagrams: the star (*) marks the {@link Tree}
  * objects referenced by the {@code Treepath} object.
  * The apostrophe (') marks new {@link Tree} objects
  * created for reflecting the new state of a logically "modified" Tree.
  *
  * @author Laurent Caillette
  */
 public class TreepathTools {
 
   private TreepathTools() {
     throw new Error( "TreeTools" ) ;
   }
 
   /**
    * Returns the n<sup>th</sup> sibling starting from end's parent.
    * {@link Treepath}.
    * <pre>
    * getEndChildAt( t1, 2 ):
    *
    *     *t0                  *t0
    *    / |  \              /  |   \
    * *t1  t2  t3  -->     t1   t2  *t3
    * </pre>
    * @param treepath non-null object with minimum depth of 2.
    * @param index inside [ 0, child count of previous tree [
    * @return non-null object.
    */
   public static< T extends Tree > Treepath< T > getSiblingAt(
       Treepath< T > treepath,
       int index
   ) throws IllegalArgumentException
   {
     final T end = treepath.getTreeAtEnd() ;
     Preconditions.checkArgument(
         treepath.getLength() > 1,
         "length of treepath [%s] should be greater than 1"
     ) ;
     Preconditions.checkArgument(
         index >= 0,
         "index [%s] should be 0 or greater", index
     ) ;
     Preconditions.checkArgument(
         treepath.getPrevious().getTreeAtEnd().getChildCount() > index,
         "child count [%s] should be greater than index [%s]", end.getChildCount(), index
     ) ;
     return Treepath.create( treepath.getPrevious().getTreeAtEnd(), index ) ;
   }
 
 
 
 
   /**
    * Returns true if given {@code Treepath} has a previous sibling, false otherwise.
    * @param treepath a non-null {@code Treepath} with a minimum length of 2.
    * @throws IllegalArgumentException
    * @see #getPreviousSibling(Treepath)
    */
   public static< T extends Tree > boolean hasPreviousSibling( Treepath< T > treepath )
       throws IllegalArgumentException
   {
     if( treepath.getLength() < 2 ) {
       throw new IllegalArgumentException( "Treepath must have minimum length of 2" ) ;
     }
     return treepath.getIndexInPrevious() > 0 ;
   }
 
   /**
    * Returns true if given {@code Treepath} has a next sibling, false otherwise.
    * @param treepath a non-null {@code Treepath} with a minimum length of 2.
    *     This may seem an excessive constraint but helps to detect many logical problems.
    * @throws IllegalArgumentException
    * @see #getNextSibling(Treepath)
    */
   public static< T extends Tree > boolean hasNextSibling( Treepath< T > treepath )
       throws IllegalArgumentException
   {
     if( treepath.getLength() < 2 ) {
       throw new IllegalArgumentException( "Treepath must have minimum length of 2" ) ;
     }
     return treepath.getIndexInPrevious() < 
         treepath.getPrevious().getTreeAtEnd().getChildCount() - 1 ;
   }
 
   /**
    * Returns the sibling on the left of the bottom of given
    * {@link Treepath}.
    * <pre>
    * *t0               *t0
    *  |  \              |  \
    * t1  *t2    -->    *t1  t2
    * </pre>
    * @param treepath non-null object with minimum length of 2.
    * @return non-null object.
    * @see #hasPreviousSibling(Treepath)
    */
   public static< T extends Tree > Treepath< T > getPreviousSibling(
       Treepath< T > treepath
   ) throws IllegalArgumentException
   {
     Preconditions.checkArgument( hasPreviousSibling( treepath ) ) ;
     final Treepath< T > previousTreepath = treepath.getPrevious() ;
     return Treepath.create( previousTreepath, treepath.getIndexInPrevious() - 1 ) ;
   }
 
 
   /**
    * Returns the sibling on the left of the end of given {@link Treepath}.
    * <pre>
    *    *t0               *t0
    *   /  |              /  |
    * *t1  t2    -->    t1  *t2
    * </pre>
    * @param treepath non-null object with minimum length of 2.
    * @return non-null object.
    * @see #hasNextSibling(Treepath)
    */
   public static< T extends Tree > Treepath< T > getNextSibling( Treepath< T > treepath ) {
     Preconditions.checkArgument( hasNextSibling( treepath ) ) ;
     final Treepath< T > previousTreepath = treepath.getPrevious() ;
     return Treepath.create( previousTreepath, treepath.getIndexInPrevious() + 1 ) ;
   }
 
   /**
    * Adds a sibling on the right of end of given {@link Treepath}.
    * <pre>
    * *t0          *t0'
    *  |            |  \
    * *t1    -->  *t1   t2
    * </pre>
    *
    * @param treepath non-null object.
    * @param tree non-null object.
    * @return non-null {@code Treepath} with the same end but with updated parents.
    *
    */
   public static< T extends Tree > Treepath< T > addSiblingLast(
       Treepath< T > treepath,
       T tree
   ) {
     Preconditions.checkArgument( 
         treepath.getLength() > 1, 
         "Minimum length is 2, got %s", treepath.getLength() 
     ) ;
     
     final T oldParent = treepath.getTreeAtDistance( 1 ) ;
     final T newParent = TreeTools.addLast( oldParent, tree ) ;
 
     return Treepath.create(
         replaceTreepathEnd( treepath.getPrevious(), newParent ),
         newParent.getChildCount() - 1
     ) ;
   }
 
   /**
    * Adds a first child to the end of given {@link Treepath}.
    * <pre>
    * *t0               *t0'
    *  |                 |
    * *t1    -->        *t1'
    *  |               / |
    *  t2         *new   t2 
    * </pre>
    *
    * @param treepath non-null object.
    * @param tree non-null object.
    * @return non-null {@code Treepath} referencing updated trees.
    *
    */
   public static < T extends Tree > Treepath< T > addChildFirst(
       Treepath< T > treepath,
       T tree
   ) {
     Preconditions.checkArgument( 
        treepath.getLength() < 1,
         "Minimum length is 1, got %s",
         treepath.getLength() 
     ) ;
     final T newParent = TreeTools.addFirst( treepath.getTreeAtEnd(), tree ) ;
     return Treepath.create(
         replaceTreepathEnd( treepath, newParent ),
         newParent.getChildCount() - 1
     ) ;
   }
 
   /**
    * Adds a child at given position on the end of given {@link Treepath}.
    * <pre>
    *     *t0             *t0'
    *      |               |
    *    *t1    -->      *t1'
    *    /  \           /  | \
    *  t2   t3       t2 *new  t3
    * </pre>
    *
    * @param treepath non-null object.
    * @param tree non-null object.
    * @param position position of added {@code tree}.
    * @return non-null {@code Treepath} referencing updated trees.
    *
    */
   public static < T extends Tree > Treepath< T > addChildAt(
       Treepath< T > treepath,
       T tree,
       int position
   ) {
     Preconditions.checkArgument( 
        treepath.getLength() < 1,
         "Minimum length is 1, got ",
         treepath.getLength() 
     ) ;
     final T newParent = TreeTools.add( treepath.getTreeAtEnd(), tree, position ) ;
     return Treepath.create( replaceTreepathEnd( treepath, newParent ), position ) ;
   }
 
   /**
    * Adds a last child to the end of given {@link Treepath}.
    * <pre>
    * *t0            *t0'
    *  |              |
    * *t1    -->     *t1'
    *  |              |  \
    *  t2             t2  *new
    * </pre>
    *
    * @param treepath non-null object.
    * @param tree non-null object.
    * @return non-null {@code Treepath} referencing updated trees.
    *
    */
   public static < T extends Tree > Treepath< T > addChildLast(
       Treepath< T > treepath,
       T tree
   ) {
     Preconditions.checkArgument( 
         treepath.getLength() > 0,
         "Minimum length is 1, got %s",
         treepath.getLength() 
     ) ;
     final T newParent = TreeTools.addLast( treepath.getTreeAtEnd(), tree ) ;
     return Treepath.create( 
         replaceTreepathEnd( treepath, newParent ), newParent.getChildCount() - 1 ) ;
   }
 
   /**
    * Returns a {@link Treepath} corresponding to a replacement of the end of the
    * given {@link Treepath}.
    * <pre>
    * *t0          *t0'
    *  |            |
    * *t1    -->   *t1'
    *  |            |
    * *old        *new
    * </pre>
    *
    * @param treepath non-null object.
    * @param newTree non-null object.
    * @return non-null {@code Treepath} with the same end referencing updated trees.
    *
    */
   public static< T extends Tree > Treepath< T > replaceTreepathEnd(
       Treepath< T > treepath,
       T newTree
   ) {
     if( null == treepath.getPrevious() ) {
       return Treepath.create( newTree ) ;
     } else {
       final Treepath< T > parentTreepath = treepath.getPrevious() ;
       final T newParent = TreeTools.replace(
           parentTreepath.getTreeAtEnd(),
           treepath.getIndexInPrevious(),
           newTree
       ) ;
 
       return Treepath.create(
           replaceTreepathEnd( parentTreepath, newParent ),
           treepath.getIndexInPrevious()
       ) ;
     }
   }
 
   /**
    * Removes the end of a given {@code Treepath}.
    *
    * @param treepath a non-null object with a minimum height of 2.
    * @return a {@code Treepath} referencing updated trees.
    */
   public static< T extends Tree > Treepath< T > removeEnd( Treepath< T > treepath ) {
     Preconditions.checkArgument( 
         treepath.getLength() > 1,
         "Treepath length must be 2 or more" 
     ) ;
     
 
     final T removed = treepath.getTreeAtEnd() ;
     final T parentOfRemoved = treepath.getTreeAtDistance( 1 ) ;
 
     T newTree = null ;
 
     for( int i = 0 ; i < parentOfRemoved.getChildCount() ; i++ ) {
       final Tree child = parentOfRemoved.getChildAt( i ) ;
       if( child == removed ) {
         newTree = TreeTools.remove( parentOfRemoved, i ) ;
         break ;
       }
     }
     if( null == newTree ) {
       throw new Error( "Internal error: found no end" ) ;
     }
     return replaceTreepathEnd( treepath.getPrevious(), newTree ) ;
   }
 
 
   /**
    * Removes a {@code Tree} from its direct parent, and adds it as child of its former
    * previous sibling.
    * <pre>
    * *t0              *t0'
    *  |  \             |
    * t1  *t2    -->   *t1'
    *                   |
    *                  *t2
    * </pre>
    *
    * @param targetTreepath non-null, minimum depth of 2.
    * @return non-null object representing path to moved {@code Tree}.
    * @throws IllegalArgumentException if there was no previous sibling.
    */
   public static < T extends Tree > Treepath< T > becomeLastChildOfPreviousSibling(
       Treepath< T > targetTreepath
   )
       throws IllegalArgumentException
   {
     final T moving = targetTreepath.getTreeAtEnd() ;
     final Treepath< T > previousSibling = getPreviousSibling( targetTreepath ) ;
 
     final Treepath< T > afterRemoval = removeEnd( targetTreepath ) ;
     return addChildLast(
         Treepath.create( afterRemoval, previousSibling.getIndexInPrevious() ),
         moving
     ) ;
   }
 
 
   /**
    * Removes the next sibling at end of given {@code Treepath}.
    * <pre>
    * *t0              t0'
    *  | \             |
    * *t1 t2    -->   *t1'
    * </pre>
    *
    * @param treepath non-null, minimum depth of 2.
    * @return non-null object representing path to moved {@code Tree}.
    * @throws IllegalArgumentException if there was no previous sibling.
    */
   public static< T extends Tree > Treepath< T > removeNextSibling(
       final Treepath< T > treepath
   ) {
     if( treepath.getLength() < 2 ) {
       throw new IllegalArgumentException( "Treepath length must be 2 or more" ) ;
     }
     if( ! hasNextSibling( treepath )) {
       throw new IllegalArgumentException( "Tree at end of treepath must have a next sibling" ) ;
     }
 
     final int indexOfNextSibling = treepath.getIndexInPrevious() + 1 ;
     final T parentBeforeRemoval = treepath.getTreeAtDistance( 1 ) ;
 
     final T parentAfterRemoval = TreeTools.remove( parentBeforeRemoval, indexOfNextSibling ) ;
 
     final Treepath treepathToParentWithRemoval =
         replaceTreepathEnd( treepath.getPrevious(), parentAfterRemoval ) ;
 
     return Treepath.create( treepathToParentWithRemoval, treepath.getIndexInPrevious() ) ;
 
   }
 
   /**
    * Removes a subtree of a container tree, while retaining consistency of a {@code Treepath}
    * to somewhere in the container tree.
    * Both {@code Treepath} are supposed to have the same root (tested by reference equality).
    * <pre>
  -*t0       *t0'             -*t0       *t0'
    |         |                 |         |
  -*t1  -->  *t1'             -*t1  -->  *t1'
   /  \       |                /  \       |
 -t2  *t3    *t3             *t2  -t3    *t2
 
 
  -*t0       *t0'            -*t0
    |         |                |
  -*t1  -->  *t1'            -*t1  -->  IllegalArgumentException
    |                          |
   -t2                        *t2
 
    * : treepath to some element
    - : treepath representing subtree
    * </pre>
    *
    * @param containerTreepath a {@code Treepath} where the tree at start is the tree to remove from.
    * @param subTreepath a {@code Treepath} where the tree at end is the subtree to remove.
    * @return a {@code Treepath} with the same start and end tree, but with subtree removed.
    * @throws IllegalArgumentException if the subtree is containing the tree it is supposed to be
    *     removed from, or if both treepaths don't have the same tree at start.
    */
   public static< T extends Tree > Treepath< T > removeSubtree(
       final Treepath< T > containerTreepath,
       final Treepath< T > subTreepath
 
   ) {
     Preconditions.checkArgument(
         containerTreepath.getTreeAtStart() == subTreepath.getTreeAtStart() ) ;
     Preconditions.checkArgument( subTreepath.getLength() > 1 ) ;
 
     final Iterator< Treepath< T > > invertedPathForContainer = invert( containerTreepath ) ;
     final Iterator< Treepath< T > > invertedPathForSub = invert( subTreepath ) ;
     invertedPathForContainer.next() ;
     invertedPathForSub.next() ;
 
     RemovalProgress progress = RemovalProgress.UNSPLIT ;
     Treepath< T > result = TreepathTools.removeEnd( subTreepath ).getStart() ;
 
     while( invertedPathForContainer.hasNext() ) {
       final Treepath< T > currentTreepathInContainer = invertedPathForContainer.next() ;
       final Treepath< T > currentTreepathInSub = invertedPathForSub.next() ;
 
       if( currentTreepathInContainer.getTreeAtEnd() == currentTreepathInSub.getTreeAtEnd() ) {
         if( ! invertedPathForSub.hasNext() ) {
           throw new IllegalArgumentException(
               "The subtree entierely contains the containing treepath" ) ;
         }
         result = Treepath.create( result, currentTreepathInContainer.getIndexInPrevious() ) ;
       } else {
         if( RemovalProgress.UNSPLIT == progress ) {
           if( currentTreepathInContainer.getIndexInPrevious() >
               currentTreepathInSub.getIndexInPrevious()
           ) {
             progress = RemovalProgress.REMOVAL_ON_LEFT ;
           } else {
             progress = RemovalProgress.REMOVAL_ON_RIGHT ;
           }
         }
         switch( progress ) {
           case REMOVAL_ON_LEFT :
             result = Treepath.create(
                 result, currentTreepathInContainer.getIndexInPrevious() - 1 ) ;
             break ;
           case REMOVAL_ON_RIGHT :
           case SPLIT :
             result = Treepath.create( result, currentTreepathInContainer.getIndexInPrevious() ) ;
           case UNSPLIT:
             break ;
         }
         progress = RemovalProgress.SPLIT ;
 
       }
     }
 
     return result ;
   }
 
 
 
   private enum RemovalProgress {
     UNSPLIT,
     REMOVAL_ON_LEFT,
     REMOVAL_ON_RIGHT,
     SPLIT
   }
 
 
 
   private static< T extends Tree > Iterator< Treepath< T > > invert( Treepath< T > treepath ) {
     final List< Treepath< T > > treepaths = new ArrayList< Treepath< T > >( treepath.getLength() ) ;
     while( true ) {
       treepaths.add( treepath ) ;
       final Treepath< T > previous = treepath.getPrevious();
       if( null == previous ) {
         break ;
       } else {
         treepath = previous ;
       }
     }
     Collections.reverse( treepaths ) ;
     return treepaths.iterator() ; 
   }
 
   /**
    * Returns a {@code Treepath} object to the next tree in a
    * <a href="http://en.wikipedia.org/wiki/Tree_traversal">preorder</a> traversal.
    * <pre>
    *  *t0            *t0            *t0            *t0
    *   |      next    |      next    |     next     |      next
    *   t1     -->    *t1     -->    *t1     -->    *t1     -->    null
    *  /  \           /  \           /  \           /  \           
    * t2   t3        t2   t3       *t2   t3       t2   *t3
    * </pre>
    *
    * @param treepath a non-null object.
    * @return the treepath to the next tree, or null.
    */
   public static< T extends Tree > Treepath< T > getNextInPreorder( Treepath< T > treepath ) {
     final T tree = treepath.getTreeAtEnd();
     if( tree.getChildCount() > 0 ) {
       return Treepath.create( treepath, 0 ) ;
     }
     return getNextUpInPreorder( treepath ) ;
   }
 
   private static < T extends Tree > Treepath< T > getUpNextInPreorder( Treepath< T > treepath ) {
     Treepath< T > previousTreepath = treepath.getPrevious() ;
     while( previousTreepath != null && previousTreepath.getPrevious() != null ) {
       if( hasNextSibling( previousTreepath ) ) {
         return getNextSibling( previousTreepath ) ;
       } else {
         previousTreepath = previousTreepath.getPrevious() ;
       }
     }
     return null ;
   }
 
   public static < T extends Tree > Treepath< T > getNextUpInPreorder( Treepath< T > treepath ) {
     if( hasNextSibling( treepath ) ) {
       return getNextSibling( treepath ) ;
     } else {
       return getUpNextInPreorder( treepath ) ;
     }
   }
 
   /**
    * Returns if a {@code Treepath} has the same indices in its parenthood as another
    * {@code Treepath}, for the length they have in common.
    * 
    * @param maybeParent a non-null object.
    * @param maybeChild a non-null object with {@link Treepath#getLength()} greater than the
    *     one of {@code maybeparent}. 
    * @return true if index in each parent tree is the same.
    */
   public static < T extends Tree > boolean hasSameStartingIndicesAs( 
       Treepath< T > maybeParent,  
       Treepath< T > maybeChild  
   ) {
     Preconditions.checkNotNull( maybeParent ) ;
     Preconditions.checkNotNull( maybeChild ) ;
     Preconditions.checkArgument( maybeParent.getLength() <= maybeChild.getLength() ) ;
     
     while( maybeChild.getLength() > maybeParent.getLength() ) {
       maybeChild = maybeChild.getPrevious() ;
     }    
     return hasSameStartingIndicesAsWithoutCheck( maybeParent, maybeChild ) ;
   }
   
   private static < T extends Tree > boolean hasSameStartingIndicesAsWithoutCheck( 
       Treepath< T > maybeParent,  
       Treepath< T > maybeChild  
   ) {
     if( maybeParent.getPrevious() == null ) {
       return true ; // No check needed as both arguments are supposed to have the same length.
     } else {
       if( maybeParent.getIndexInPrevious() == maybeChild.getIndexInPrevious() ) {
         return hasSameStartingIndicesAsWithoutCheck( 
             maybeParent.getPrevious(), 
             maybeChild.getPrevious() 
         ) ;
       } else {
         return false ;
       }
     }
   }
 
 }
