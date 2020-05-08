 public class SlimBanner {
 	public static void main(String[]a) throws Exception {
 	    int w=100,h=40,dw = 25, dh = 10, l=0, i, j, p, zc=500,
 	        d[] = {0xf,0xc000fb6,0xc183f,0xc60dbff3,0xe6dff8fb,0x6ccc0db6,0x667edb33,0x0};
	    String f = "22-METsaysTHANKS!", b, z=""; boolean v=false;
 	    while(zc-->0) z+="\n";
 	    for(b=z;;System.out.print(b),Thread.sleep(2000),v=!v,b=z)
 	        for(i=0;i<h;i++,b+='\n')
 	            for(j=0;j<w;j++){
 	                p = i*dh/h*dw+j*dw/(w+1);
 	                b +=
 	                (((d[p/32] & ( 1L << (31 - (p%32)))) > 0) ^ v)?
	                    ' ': f.charAt(l++); l%=f.length()-1;
 	            }
 	}
 
 }
