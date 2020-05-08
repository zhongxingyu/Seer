 package com.winvector.lstep;
 
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 public final class SimpleProblem implements Comparable<SimpleProblem> {
 	public final int dim;
 	public final int nrow;
 	public final double[][] x;
 	public final boolean[] y;
 	public final int[] wt;
 	
 	public SimpleProblem(final double[][] x,
 			final boolean[] y,
 			final int[] wt) {
 		this.x = x;
 		this.y = y;
 		dim = x[0].length;
 		nrow = x.length;
 		if(null!=wt) {
 			this.wt = wt;
 		} else {
 			this.wt = new int[nrow];
 			for(int i=0;i<nrow;++i) {
 				this.wt[i] = 1;
 			}
 		}
 	}
 	
 	private static class Row implements Comparable<Row> {
 		public double[] x;
 		public boolean y;
 		
 		public Row(final double[] x, final boolean y) {
 			this.x = x;
 			this.y = y;
 		}
 
 		@Override
 		public int compareTo(final Row o) {
 			final int n = x.length;
 			if(n!=o.x.length) {
 				if(n>=o.x.length) {
 					return 1;
 				} else {
 					return -1;
 				}
 			}
 			for(int i=0;i<n;++i) {
 				final double diff = x[i] - o.x[i];
 				if(Math.abs(diff)>1.0e-8) {
 					if(diff>=0) {
 						return 1;
 					} else {
 						return -1;
 					}
 				}
 			}
 			if(y!=o.y) {
 				if(y) {
 					return 1;
 				} else {
 					return -1;
 				}
 			}
 			return 0;
 		}
 		
 		@Override
 		public boolean equals(final Object o) {
 			return compareTo((Row)o)==0;
 		}
 	}
 	
 	public String toString() {
 		final StringBuilder b = new StringBuilder();
 		final int dim = x[0].length;
 		b.append("(");
 		for(int j=0;j<dim;++j) {
 			b.append(",x" + j + "=c(");
 			for(final double[] xi: x) {
 				b.append("," + xi[j]);
 			}
 			b.append(")");
 		}
 		b.append(",y=c(");
 		for(final boolean yi: y) {
 			b.append("," + (yi?"T":"F"));
 		}
 		b.append(")");
 		if(null!=wt) {
 			b.append(",w=c(");
 			for(final int wi: wt) {
 				b.append("," + wi);
 			}
 			b.append(")");
 		}
 		b.append(")");
 		return b.toString().replaceAll("\\(,","(");
 	}
 	
 	public static SimpleProblem cleanRep(final double[][] x, final boolean[] y, final int[] wt) {
 		final int om = x.length;
 		if(om<=0) {
 			return null;
 		}
 		final Map<Row,Integer> wCount = new TreeMap<Row,Integer>();
 		final int dim = x[0].length;
 		for(int i=0;i<om;++i) {
 			final int wti = (null==wt)?1:wt[i]; 
 			if(wti>0) {
 				final Row r = new Row(x[i],y[i]);
 				Integer oc = wCount.get(r);
 				if(null==oc) {
 					oc = wti;
 				} else {
 					oc = oc + wti;
 				}
 				wCount.put(r,oc);
 			}
 		}
 		final int nm = wCount.size();
 		if(nm<=0) {
 			return null;
 		}
 		final double[][] nx = new double[nm][dim];
 		final boolean[] ny = new boolean[nm];
 		final int[] nw = new int[nm];
 		int i = 0;
 		for(final Map.Entry<Row,Integer> me: wCount.entrySet()) {
 			final Row r = me.getKey();
 			final int c = me.getValue();
 			for(int j=0;j<dim;++j) {
 				nx[i][j] = r.x[j];
 			}
 			ny[i] = r.y;
 			nw[i] = c;
 			++i;
 		}
 		return new SimpleProblem(nx,ny,nw);
 	}
 
 	/**
 	 * assumes we are in cannonical form from cleanRep()
 	 */
 	@Override
 	public int compareTo(final SimpleProblem o) {
 		final int m = x.length;
 		if(m!=o.x.length) {
 			if(m>=o.x.length) {
 				return 1;
 			} else {
 				return -1;
 			}
 		}
 		final int dim = x[0].length;
 		if(dim!=o.x[0].length) {
 			if(dim>=o.x[0].length) {
 				return 1;
 			} else {
 				return -1;
 			}
 		}
 		for(int i=0;i<m;++i) {
 			if(y[i]!=o.y[i]) {
 				if(y[i]) {
 					return 1;
 				} else {
 					return -1;
 				}
 			}
 			if(wt[i]!=o.wt[i]) {
 				if(wt[i]>=o.wt[i]) {
 					return 1;
 				} else {
 					return -1;
 				}
 			}
 			for(int j=0;j<dim;++j) {
 				if(Math.abs(x[i][j]-o.x[i][j])>1.0e-8) {
 					if(x[i][j]>=o.x[i][j]) {
 						return 1;
 					} else {
 						return -1;
 					}
 				}
 			}
 		}
 		return 0;
 	}
 	
 	@Override
 	public boolean equals(final Object o) {
 		return compareTo((SimpleProblem)o)==0;
 	}
 	
 	public static Set<SimpleProblem> mutations(SimpleProblem p) {
 		p = new SimpleProblem(p.x,p.y,p.wt); // copy so any alterations we make are not visible outside here (thread safety)
 		final Set<SimpleProblem> mutations = new TreeSet<SimpleProblem>();
 		final int m = p.nrow;
 		for(int i=0;i<m;++i) {
 			final int wi = p.wt[i];
 			final boolean yi = p.y[i];
 			// row deletion
 			{
 				p.wt[i] = 0;
 				final SimpleProblem np = SimpleProblem.cleanRep(p.x,p.y,p.wt);
 				if((null!=np)&&(np.nrow>1)) {
 					mutations.add(np);
 				}
 			}
 			// weight changes
 			{
 				p.wt[i] = wi + 1;
 				final SimpleProblem np = SimpleProblem.cleanRep(p.x,p.y,p.wt);
 				if((null!=np)&&(np.nrow>1)) {
 					mutations.add(np);
 				}
 			}
 			{
 				p.wt[i] = wi - 1;
 				final SimpleProblem np = SimpleProblem.cleanRep(p.x,p.y,p.wt);
 				if((null!=np)&&(np.nrow>1)) {
 					mutations.add(np);
 				}
 			}
 			p.wt[i] = wi;
 			// y-flip
 			{
 				p.y[i] = !yi;
 				final SimpleProblem np = SimpleProblem.cleanRep(p.x,p.y,p.wt);
 				if((null!=np)&&(np.nrow>1)) {
 					mutations.add(np);
 				}
 			}
 			p.y[i] = yi;
 			// x-changes
 			for(int j=1;j<p.dim;++j) {
 				final double xij = p.x[i][j];
 				{
 					p.x[i][j] = xij + 1.0;
 					final SimpleProblem np = SimpleProblem.cleanRep(p.x,p.y,p.wt);
 					if((null!=np)&&(np.nrow>1)) {
 						mutations.add(np);
 					}
 				}
 				{
 					p.x[i][j] = xij - 1.0;
 					final SimpleProblem np = SimpleProblem.cleanRep(p.x,p.y,p.wt);
 					if((null!=np)&&(np.nrow>1)) {
 						mutations.add(np);
 					}
 				}
 				{
 					p.x[i][j] = -xij;
 					final SimpleProblem np = SimpleProblem.cleanRep(p.x,p.y,p.wt);
 					if((null!=np)&&(np.nrow>1)) {
 						mutations.add(np);
 					}
 				}
 				{
 					p.x[i][j] = 1.1*xij;
 					final SimpleProblem np = SimpleProblem.cleanRep(p.x,p.y,p.wt);
 					if((null!=np)&&(np.nrow>1)) {
 						mutations.add(np);
 					}
 				}
 				{
 					p.x[i][j] = 0.9*xij;
 					final SimpleProblem np = SimpleProblem.cleanRep(p.x,p.y,p.wt);
 					if((null!=np)&&(np.nrow>1)) {
 						mutations.add(np);
 					}
 				}
 				p.x[i][j] = xij;
 			}
 			// make sure all restored
 			p.wt[i] = wi;
 			p.y[i] = yi;
 		}
 		return mutations;
 	}
 	
 	public static Set<SimpleProblem> breed(final SimpleProblem p0, final SimpleProblem p1,
 			final Random rand) {
 		final Set<SimpleProblem> children = new TreeSet<SimpleProblem>();
 		final int dim = p0.dim;
 		final int m = p0.nrow + p1.nrow;
 		final double[][] x = new double[m][dim];
 		final boolean[] y = new boolean[m];
 		final int[] ow = new int[m];
 		for(int i=0;i<p0.nrow;++i) {
 			for(int j=0;j<dim;++j) {
 				x[i][j] = p0.x[i][j];
 			}
 			y[i] = p0.y[i];
 			ow[i] = p0.wt[i];
 		}
 		for(int i=0;i<p1.nrow;++i) {
 			for(int j=0;j<dim;++j) {
 				x[p0.nrow+i][j] = p1.x[i][j];
 			}
 			y[p0.nrow+i] = p1.y[i];
 			ow[p0.nrow+i] = p1.wt[i];
 		}
 		final int[] w = new int[m];
 		for(int rep=0;rep<10;++rep) {
 			for(int i=0;i<m;++i) {
 				w[i] = rand.nextBoolean()?ow[i]:0;
				final SimpleProblem np = SimpleProblem.cleanRep(x,y,w);
				if((null!=np)&&(np.nrow>1)) {
					children.add(np);
				}
 			}
 		}
 		return children;
 	}
 }
