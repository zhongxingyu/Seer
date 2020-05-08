 /**
  * Unisay — cowsay+ponysay rewritten in Java, with added features and full Unicode(!) support
  *
  * Copyright © 2012  Mattias Andrée (maandree@kth.se)
  *
 * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
 * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package se.kth.maandree.unisay;
 
 import java.util.*;
 import java.io.*;
 
 
 /**
  * Baloon class
  *
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class Baloon
 {
     /**
      * Constructor
      *
      * @param  lens    The length of each line
      * @param  maxlen  The width of the saying
      * @param  say     The saying
      * @param  nw      NW part of baloon
      * @param  n       N  part of baloon
      * @param  ne      NE part of baloon
      * @param  e       E  part of baloon
      * @param  se      SE part of baloon
      * @param  s       S  part of baloon
      * @param  sw      SW part of baloon
      * @param  w       W  part of baloon
      */
     @SuppressWarnings("hiding")
     public Baloon(final ArrayList<int[]> lens, final int maxlen, final ArrayList<ArrayList<byte[]>> say,
 		  final int[][] nw, final int[][] n, final int[][] ne, final int[] e,
 		  final int[][] se, final int[][] s, final int[][] sw, final int[] w)
     {
 	this.lens = lens;
 	this.maxlen = maxlen;
 	this.say = say;
 	this.nw = nw;
 	this.n  = n ;
 	this.ne = ne;
 	this. e =  e;
 	this.se = se;
 	this.s  = s ;
 	this.sw = sw;
 	this. w =  w;
     }
 	
 	
 	
     /**
      * The length of each line
      */
     private ArrayList<int[]> lens;
 	
     /**
      * The width of the saying
      */
     private int maxlen;
 	
     /**
      * The saying
      */
     private ArrayList<ArrayList<byte[]>> say;
 	
     /**
      * NW part of baloon
      */
     private int[][] nw;
 	
     /**
      * N part of baloon
      */
     private int[][] n;
 	
     /**
      * NE part of baloon
      */
     private int[][] ne;
 	
     /**
      * E part of baloon
      */
     private int[] e;
 	
     /**
      * SE part of baloon
      */
     private int[][] se;
 	
     /**
      * S part of baloon
      */
     private int[][] s;
 	
     /**
      * SW part of baloon
      */
     private int[][] sw;
 	
     /**
      * W part of baloon
      */
     private int[] w;
 	
 	
 	
     /**
      * Prints the baloon
      *
      * @param  width   The minimum width of the baloon
      * @param  height  The minimum height of the baloon
      * @param  indent  The indent of the baloon
      * 
      * @throws  IOException  On I/O exception
      */
     @SuppressWarnings("hiding")
     void print(final int width, final int height, final int indent) throws IOException
     {
 	int len = this.maxlen;
 	int inw = this.w.length + this.e.length;
 	len += inw;
 	if (len < width)
 	    len = width;
 	    
 	final byte[] ind = new byte[indent];
 	for (int i = 0, n = ind.length; i < n; i++)
 	    ind[i] = ' ';
 	    
 	for (int i = 0, n = this.n.length; i < n; i++)
         {
 	    final int nlen = len - this.nw[i].length - this.ne[i].length;
 	    if (i > 0)
 		System.out.write(ind);
 		
 	    System.out.write(Util.toBytes(this.nw[i]));
 		
 	    for (int j = 0, m = this.n[i].length; j < nlen; j++)
 		System.out.write(Util.toBytes(this.n[i][j % m]));
 		
 	    System.out.write(Util.toBytes(this.ne[i]));
 	    System.out.println();
 	}
 	    
 	final byte[] c = new byte[len - inw];
 	for (int i = 0, n = c.length; i < n; i++)
 	    c[i] = ' ';
 	    
 	int index = 0;
 	for (final ArrayList<byte[]> line : this.say)
         {
 	    final int linelen = this.lens.get(index++)[0];
 		
 	    System.out.write(ind);
 		
 	    System.out.write(Util.toBytes(this.w));
 		
 	    for (final byte[] bs : line)
 		System.out.write(bs);
 		
 	    System.out.write(c, 0, c.length - linelen);
 		
 	    System.out.write(Util.toBytes(this.e));
 	    System.out.println();
 	}
 	    
 	int h = this.n.length + this.s.length + this.say.size();
 	h = height - h;
 	if (h > 0)
 	    for (int i = 0; i < h; i++)
 	    {
 		System.out.write(ind);
 		    
 		System.out.write(Util.toBytes(this.w));
 		    
 		System.out.write(c);
 		    
 		System.out.write(Util.toBytes(this.e));
 		System.out.println();
 	    }
 	    
 	for (int i = 0, n = this.s.length; i < n; i++)
 	{
 	    final int slen = len - this.sw[i].length - this.se[i].length;
 	    System.out.write(ind);
 		
 	    System.out.write(Util.toBytes(this.sw[i]));
 		
 	    for (int j = 0, m = this.s[i].length; j < slen; j++)
 		System.out.write(Util.toBytes(this.s[i][j % m]));
 		
 	    System.out.write(Util.toBytes(this.se[i]));
 	    if (i + 1 < n)
 		System.out.println();
 	}
     }
     
     
     
     
     /**
      * Prints a test baloon
      * 
      * @param  args  Lines to say
      * 
      * @throws  IOException  On I/O exception
      */
     public static void main(final String... args) throws IOException
     {
 	final ArrayList<int[]> lens = new ArrayList<int[]>();
 	final ArrayList<ArrayList<byte[]>> say = new ArrayList<ArrayList<byte[]>>();
 	int maxlen = 0;
 	
 	for (final String arg : args)
 	{
 	    final int len;
 	    lens.add(new int[] { len = arg.length() });
 	    if (maxlen < len)
 		maxlen = len;
 	    
 	    final ArrayList<byte[]> line;
 	    say.add(line = new ArrayList<byte[]>());
 	    line.add(arg.getBytes("UTF-8"));
 	}
 	
 	final int[][] nw = {{'/', '/'}, {'|', ' '}};
 	final int[][] n  = {{'^'}, {'-'}};
 	final int[][] ne = {{'\\'}, {'|'}};
 	final int[]    e = {'<'};
 	final int[][] se = {{'/'}};
 	final int[][] s  = {{'v'}};
 	final int[][] sw = {{'\\'}};
 	final int[]    w = {'>'};
 	
 	final Baloon baloon = new Baloon(lens, maxlen, say, nw, n, ne, e, se, s, sw, w);
 	baloon.print(10, 4, 0);
     }
     
 }
