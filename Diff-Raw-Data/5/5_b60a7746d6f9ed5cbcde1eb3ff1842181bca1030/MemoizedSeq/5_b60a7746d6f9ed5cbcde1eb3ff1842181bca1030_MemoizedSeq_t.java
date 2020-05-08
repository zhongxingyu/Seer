 /*
  * Vitry, copyright (C) Hans Hoglund 2011
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * See COPYING.txt for details.
  */
 package vitry.runtime.struct;
 
 
 public class MemoizedSeq<T> extends AbstractSeq<T>
 {
     private static final int HEADED   = 0x1;
     private static final int TAILED   = 0x2;
     private static final int FINISHED = HEADED | TAILED;
 
     private Seq<T> seq;
     private T      x;
     private Seq<T> xs;
     private int    state = 0;
 
     public MemoizedSeq(Seq<T> s) {
         this.seq = s;
     }
 
     public T head()
     {
        if ((this.state & HEADED) != HEADED)
         {
             this.x = seq.head();
             this.state |= HEADED;
             maybeFinish();
         }
         return this.x;
     }
 
     public Seq<T> tail()
     {
        if ((this.state & TAILED) != TAILED)
         {
             this.xs = seq.tail();
             this.state |= TAILED;
             maybeFinish();
         }
         return this.xs;
     }
 
     public boolean hasTail()
     {
         if ( (this.state & TAILED) == TAILED)
         {
             return !Seqs.isNil(this.xs);
         }
         else
         {
             return seq.hasTail();
         }
     }
 
     private void maybeFinish()
     {
         if (this.state == FINISHED)
         {
             this.seq = null;
         }
     }
 }
