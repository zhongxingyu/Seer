 package session;
 
 import misc.Util;
 
 import java.io.*;
 
 public class SessionInstrument
 {
 	// XXX: should IMPM 1.00 instruments be supported? --GM
 	
 	public static class Envelope
 	{
 		public static final int EFLG_ON = 0x01;
 		public static final int EFLG_LOOP = 0x02;
 		public static final int EFLG_SUS = 0x04;
 		public static final int EFLG_CARRY = 0x08;
 		public static final int EFLG_FILTER = 0x80;
 		public class Handle
 		{
 			private Envelope env;
 			private int def;
 			
 			private int pos_x = 0;
 			private int pos_idx = 0;
 			private boolean sustain = true;
 			
 			public Handle(Envelope env, int def)
 			{
 				this.env = env;
 				this.def = def; // TIL "default" is a reserved Java keyword --GM
 			}
 			
 			public Handle dup()
 			{
 				Handle other = new Handle(env, def);
 				other.pos_x = pos_x;
 				other.pos_idx = pos_idx;
 				other.sustain = sustain;
 				
 				return other;
 			}
 			
 			public int interp(int idx, int x)
 			{
 				if(env.points[idx][1] == x)
 					return env.points[idx][0];
 				
 				int x1 = env.points[idx][1];
 				int y1 = env.points[idx][0];
 				int x2 = env.points[idx+1][1];
 				int y2 = env.points[idx+1][0];
 				int dx = x2 - x1;
 				int dy = y2 - y1;
 				
 				//System.out.printf("%d %d / %d: %d < %d < %d\n"
 				//	,dx,dy,idx,x1,x,x2);
 				
 				assert(x >= x1);
 				assert(x <= x2);
 				
 				
 				return y1 + (dy*(x-x1)*2+1)/(dx*2);
 			}
 			
 			public boolean fadeCheck(boolean note_off)
 			{
 				//if(true)return false; // TODO GET THIS CRAP WORKING *PROPERLY*
 				
 				// Fade applied when:
 				
 				// 1) Note fade NNA is selected and triggered (by another note)
 				// [ NOTE: handled in PlayerChannel ]
 				
 				// 2) Note off NNA is selected with no volume envelope
 				//    or volume envelope loop
 				if(note_off)
 					return ((env.flg & EFLG_ON) == 0)
 						|| ((env.flg & EFLG_LOOP) != 0);
 				
 				// 3) Volume envelope end is reached
 				boolean useloop = (env.flg & ((sustain ? EFLG_SUS : 0)|EFLG_LOOP)) != 0;
 				return (!useloop) && ((env.flg & EFLG_ON) != 0) && pos_idx >= env.num-1;
 			}
 			
 			public int getFlags()
 			{
 				return env.flg;
 			}
 			
 			public boolean hasFilter()
 			{
 				return (env.flg & EFLG_FILTER) != 0;
 			}
 			
 			public boolean isOn()
 			{
 				return (env.flg & EFLG_ON) != 0;
 			}
 			
 			public int read()
 			{
 				if((env.flg & EFLG_ON) == 0)
 					return this.def;
 				
 				int ret = (pos_idx >= env.num-1) ? env.points[env.num < 1 ? 0 : env.num-1][0] : interp(pos_idx, pos_x);
 				
 				boolean useloop = (env.flg & ((sustain ? EFLG_SUS : 0)|EFLG_LOOP)) != 0;
 				int lpb = (sustain && ((env.flg & EFLG_SUS) != 0) ? env.slb : env.lpb);
 				int lpe = (sustain && ((env.flg & EFLG_SUS) != 0) ? env.sle : env.lpe);
 				
 				//System.out.printf("NOSUS %s %d %d %d\n", useloop ? "Y" : "N", lpb, lpe, env.flg);
 				
 				if(useloop && pos_idx >= lpe)
 				{
 					pos_idx = lpb;
 					pos_x = env.points[pos_idx][1]; 
 				} else {
 					pos_x++;
 					while(pos_idx < env.num-1 && pos_x >= env.points[pos_idx+1][1])
 						pos_idx++;
 				}
 				
 				//System.out.printf("env %d: %d [%d] / %d => %d\n"
 				//	, pos_idx, pos_x, env.points[pos_idx][0], env.num, ret);
 				
 				return ret;
 			}
 			
 			public void noteOff()
 			{
 				sustain = false;
 			}
 			
 			public void stop()
 			{
 				if((env.flg & EFLG_CARRY) != 0)
 					return;
 				
 				pos_x = 0;
 				pos_idx = 0;
 			}
 			
 			public void retrig()
 			{
 				if((env.flg & EFLG_CARRY) == 0)
 					stop();
 				
 				sustain = true;
 			}
 		}
 		
 		private int flg = 0;
 		private int num = 0, lpb = 0, lpe = 0, slb = 0, sle = 0;
 		private int points[][] = new int[25][2]; // Y, X
 		private byte[] volenv1 = null;
 		
 		public Envelope(RandomAccessFile fp, int format) throws IOException
 		{
 			switch(format)
 			{
 				case FORMAT_IT100:
 					loadITv1(fp);
 					break;
 				case FORMAT_IT200:
 					loadITv2(fp);
 					break;
 				case FORMAT_XM:
 					throw new RuntimeException("incorrect constructor for envelope format");
 				default:
 					throw new RuntimeException("instrument envelope format not supported");
 			}
 		}
 		
 		public Envelope(int[] data, int format) throws IOException
 		{
 			switch(format)
 			{
 				case FORMAT_XM:
 					loadXM(data);
 					break;
 				case FORMAT_IT100:
 				case FORMAT_IT200:
 					throw new RuntimeException("incorrect constructor for envelope format");
 				default:
 					throw new RuntimeException("instrument envelope format not supported");
 			}
 		}
 		
 		private void loadXM(int[] data)
 		{
 			// TODO some of the weird crap required because some things just suck
 			// (i recall browsing through MikMod's XM loader and some of the comments stuck out)
 			//  --GM
 			
 			this.num = data[48];
 			
 			for(int i = 0; i < 12; i++)
 			{
 				points[i][1] = (int)(short)(data[4*i+0]|(data[4*i+1]<<8));
 				points[i][0] = (int)(short)(data[4*i+2]|(data[4*i+3]<<8));
 			}
 			
 			this.lpb = data[50];
 			this.lpe = data[51];
 			this.slb = data[49];
 			this.sle = data[49];
 			
 			this.flg = 0x00;
 			int xmflg = data[52];
 			
 			// TODO look into the REALLY SCARY STUFF pertaining to sustain + loop --GM
 			
 			System.out.printf("%d %d %d %d %d\n",num,lpb,lpe,slb,xmflg);
 			if((xmflg & 0x01) != 0)
 				this.flg |= EFLG_ON;
 			if((xmflg & 0x02) != 0)
 				this.flg |= EFLG_SUS;
 			if((xmflg & 0x04) != 0)
 				this.flg |= EFLG_LOOP;
 			
			if(this.num == 0)
				this.flg &= ~EFLG_ON;
			
 			// schism does this, so i might as well try it and see what happens --GM
			
 			if((this.flg & EFLG_ON) != 0 && (this.flg & EFLG_LOOP) == 0)
 			{
 				this.flg |= EFLG_LOOP;
 				this.lpb = this.lpe = this.num-1;
 			}
 			System.out.printf("%d %d %d %d %d\n",num,lpb,lpe,slb,flg);
 		}
 		
 		private void loadITv1(RandomAccessFile fp) throws IOException
 		{
 			volenv1 = new byte[200];
 			fp.read(volenv1, 0, 200);
 			
 			for(int i = 0; i < 25; i++)
 			{
 				points[i][1] = fp.readByte();
 				points[i][0] = fp.readUnsignedByte();
 				// TODO: work out more accurately how this works
 				if(points[i][1] != -1)
 					num = i+1;
 			}
 		}
 		
 		private void loadITv2(RandomAccessFile fp) throws IOException
 		{
 			this.flg = fp.read();
 			this.num = fp.read();
 			this.lpb = fp.read();
 			this.lpe = fp.read();
 			this.slb = fp.read();
 			this.sle = fp.read();
 			
 			for(int i = 0; i < 25; i++)
 			{
 				points[i][0] = fp.readByte();
 				points[i][1] = 0xFFFF&(int)Short.reverseBytes(fp.readShort());
 			}
 			
 			fp.read(); // last, unused byte (i think)
 		}
 		
 		private void feedITv1(int flg, int lpb, int lpe, int slb, int sle)
 		{
 			this.flg = flg & 0x07;
 			//this.num = 25;
 			this.lpb = lpb;
 			this.lpe = lpe;
 			this.slb = slb;
 			this.sle = sle;
 		}
 		
 		public Handle getHandle(int def)
 		{
 			return new Handle(this, def);
 		}
 	}
 	
 	// format bollocks
 	
 	public static final int FORMAT_IT100 = 1;
 	public static final int FORMAT_IT200 = 2;
 	public static final int FORMAT_XM = 3;
 	
 	// IMPI bollocks
 	
 	public static final int NNA_CUT = 0;
 	public static final int NNA_CONTINUE = 1;
 	public static final int NNA_OFF = 2;
 	public static final int NNA_FADE = 3;
 	
 	public static final int DCT_OFF = 0;
 	public static final int DCT_NOTE = 1;
 	public static final int DCT_SAMPLE = 2;
 	public static final int DCT_INSTRUMENT = 3;
 	
 	public static final int DCA_CUT = 0;
 	public static final int DCA_OFF = 1;
 	public static final int DCA_FADE = 2;
 	
 	private String fname = "";
 	private int nna = 0, dct = 0, dca = 0;
 	private int fadeout = 0, pps = 0, ppc = 0;
 	private int gbv = 0, dfp = 0xC0, rv = 0, rp = 0;
 	// 4 bytes used only in standalone instrument files
 	private String name = "";
 	private int ifc = 0, ifr = 0, mch = 0, mpr = 0, midibnk = 0;
 	private int[][] nstab = new int[120][2];
 	private Envelope env_vol = null;
 	private Envelope env_pan = null;
 	private Envelope env_per = null;
 	
 	public SessionInstrument(RandomAccessFile fp, int format) throws IOException
 	{
 		switch(format)
 		{
 			case FORMAT_IT100:
 				loadITv1(fp);
 				break;
 			case FORMAT_IT200:
 				loadITv2(fp);
 				break;
 			case FORMAT_XM:
 				throw new RuntimeException("incorrect constructor for instrument format");
 			default:
 				throw new RuntimeException("instrument format not supported");
 		}
 	}
 	
 	public SessionInstrument(RandomAccessFile fp, int format, Session session) throws IOException
 	{
 		switch(format)
 		{
 			case FORMAT_XM:
 				loadXM(fp, session);
 				break;
 			case FORMAT_IT100:
 			case FORMAT_IT200:
 				throw new RuntimeException("incorrect constructor for instrument format");
 			default:
 				throw new RuntimeException("instrument format not supported");
 		}
 	}
 	
 	private void loadXM(RandomAccessFile fp, Session session) throws IOException
 	{
 		byte[] b = new byte[22];
 		
 		int ifblen = Integer.reverseBytes(fp.readInt());
 		
 		if(ifblen >= 4+22)
 			this.name = Util.readStringNoNul(fp, b, 22); // let's cheese it a bit
 		else
 			this.name = "";
 		
 		
 		InhibitedFileBlock ifb = new InhibitedFileBlock(fp, ifblen-4-22);
 		
 		int xminstype = ifb.read();
 		int inssmpnum = 0xFFFF&(int)Short.reverseBytes(ifb.readShort());
 		System.out.printf("instr [%d] type %d name \"%s\" smps %d\n", ifblen, xminstype, this.name, inssmpnum);
 		if(inssmpnum > 32)
 			throw new RuntimeException("inssmpnum is too large");
 		//if(xminstype != 0)
 		//	throw new RuntimeException(String.format("XM instrument format %d not supported", xminstype));
 		
 		// abusing properties of InhibitedFileBlock for this loader >:D
 		// (apparently you shouldn't be loading this if inssmpnum == 0)
 		//   --GM
 		int smpheadlen = Integer.reverseBytes(ifb.readInt());
 		System.out.printf("smpheadlen=%d\n", smpheadlen);
 		
 		for(int i = 0; i < 120; i++)
 		{
 			this.nstab[i][0] = i;
 			this.nstab[i][1] = -1;
 		}
 		
 		for(int i = 0; i < 96; i++)
 		{
 			int v = ifb.read();
 			this.nstab[i+12][1] = v;
 		}
 		
 		// OK, buffering volenv/panenv info.
 		
 		int[] volenv_data = new int[53];
 		int[] panenv_data = new int[53];
 		
 		for(int i = 0; i < 48; i++)
 			volenv_data[i] = ifb.read();
 		for(int i = 0; i < 48; i++)
 			panenv_data[i] = ifb.read();
 		
 		volenv_data[48] = ifb.read();
 		panenv_data[48] = ifb.read();
 		
 		for(int i = 0; i < 3; i++)
 			volenv_data[49+i] = ifb.read();
 		for(int i = 0; i < 3; i++)
 			panenv_data[49+i] = ifb.read();
 		
 		volenv_data[52] = ifb.read();
 		panenv_data[52] = ifb.read();
 		
 		int vit = ifb.read();
 		int vis = ifb.read();
 		int vid = ifb.read();
 		int vir = ifb.read();
 		
 		// yeah yeah, whatever, i'm supporting mega fadeouts OK? --GM
 		this.fadeout = ((0xFFFF&(int)Short.reverseBytes(ifb.readShort()))+16)>>5;
 		
 		//System.out.printf("%d %d %d %d %d %d\n", volenv_data[52], panenv_data[52], vit, vis, vid, vir);
 		ifb.done();
 		
 		// load those bloody envelopes
 		env_vol = new Envelope(volenv_data, FORMAT_XM);
 		env_pan = new Envelope(panenv_data, FORMAT_XM);
 		env_per = null;
 		
 		// TEST: disable some stuff for now
 		//this.fadeout = 0;
 		//env_vol = null;
 		//env_pan = null;
 		
 		// load samples
 		SessionSample[] smps = new SessionSample[inssmpnum];
 		
 		for(int i = 0; i < inssmpnum; i++)
 		{
 			smps[i] = new SessionSample(fp, SessionSample.FORMAT_XM, smpheadlen);
 			smps[i].setSampleVibrato(vis, vid, vir, vit);
 		}
 		
 		int[] smpidxlist = session.addSamples(smps);
 		
 		// correct note-sample table
 		for(int i = 0; i < 120; i++)
 		{
 			this.nstab[i][1] = (this.nstab[i][1] < 0 || this.nstab[i][1] >= inssmpnum
 				? 0
 				: smpidxlist[this.nstab[i][1]]
 					);
 			//System.out.printf("nstab %d %d %d\n", i, nstab[i][0], nstab[i][1]);
 		}
 		
 		// load sample data
 		for(int i = 0; i < inssmpnum; i++)
 		{
 			smps[i].loadSampleData(fp, null);
 			smps[i].unrollLoops();
 		}
 		//for(int i = 0; i < smps[i].getLength(); i++)
 		//	System.out.printf("%.3f\n", smps[i].getDataLoop()[0][i]);
 	}
 	
 	private void loadITv1(RandomAccessFile fp) throws IOException
 	{
 		// NOTE: not 100% balls accurate due to the way the volume envelope is stored
 		// (it pre-interpolates it and shoves it into some weird buffer,
 		//  so if someone decides to be a smartass then we're screwed)
 		//    --GM
 		
 		byte[] b = new byte[26];
 		fp.read(b, 0, 4);
 		if(b[0] != 'I' || b[1] != 'M' || b[2] != 'P' || b[3] != 'I')
 			throw new RuntimeException("not an IMPI v1 instrument");
 		
 		this.fname = Util.readString(fp, b, 13);
 		int veflg = fp.read();
 		int velpb = fp.read();
 		int velpe = fp.read();
 		int veslb = fp.read();
 		int vesle = fp.read();
 		fp.readShort(); // 2 unused bytes
 		
 		this.fadeout = 0xFFFF&(int)Short.reverseBytes(fp.readShort());
 		this.nna = fp.read();
 		int dnc = fp.read();
 		fp.readInt();
 		
 		this.dct = DCT_OFF;
 		this.dca = DCA_CUT;
 		if(dnc != 0)
 		{
 			// TODO: check which values are valid!
 			// TODO: check what we actually do here!
 			this.dct = DCT_NOTE;
 			this.dca = DCA_CUT;
 		}
 		
 		this.name = Util.readString(fp, b, 26);
 		
 		fp.readShort();
 		fp.readInt();
 		
 		for(int i = 0; i < 120; i++)
 		{
 			nstab[i][0] = fp.read();
 			nstab[i][1] = fp.read();
 		}
 		
 		this.env_vol = new Envelope(fp, FORMAT_IT100);
 		env_vol.feedITv1(veflg, velpb, velpe, veslb, vesle);
 		this.env_pan = null;
 		this.env_per = null;
 	}
 	
 	private void loadITv2(RandomAccessFile fp) throws IOException
 	{
 		byte[] b = new byte[26];
 		fp.read(b, 0, 4);
 		if(b[0] != 'I' || b[1] != 'M' || b[2] != 'P' || b[3] != 'I')
 			throw new RuntimeException("not an IMPI v2 instrument");
 		
 		this.fname = Util.readString(fp, b, 13);
 		this.nna = fp.read();
 		this.dct = fp.read();
 		this.dca = fp.read();
 		this.fadeout = 0xFFFF&(int)Short.reverseBytes(fp.readShort());
 		this.pps = fp.read();
 		this.ppc = fp.read();
 		this.gbv = fp.read();
 		this.dfp = fp.read();
 		this.rv = (int)fp.readByte();
 		this.rp = (int)fp.readByte();
 		fp.readInt();
 		this.name = Util.readString(fp, b, 26);
 		
 		this.ifc = fp.read();
 		this.ifr = fp.read();
 		this.mch = fp.read();
 		this.mpr = fp.read();
 		this.midibnk = 0xFFFF&(int)Short.reverseBytes(fp.readShort());
 		
 		for(int i = 0; i < 120; i++)
 		{
 			nstab[i][0] = fp.read();
 			nstab[i][1] = fp.read();
 		}
 		
 		this.env_vol = new Envelope(fp, FORMAT_IT200);
 		this.env_pan = new Envelope(fp, FORMAT_IT200);
 		this.env_per = new Envelope(fp, FORMAT_IT200);
 	}
 	
 	// getters
 	
 	public int getSampleAndNote(int note)
 	{
 		assert(note >= 0 && note < 120);
 		return nstab[note][0] | (nstab[note][1]<<8);
 	}
 	
 	public String getName()
 	{
 		return this.name;
 	}
 	
 	public int getGlobalVol()
 	{
 		return this.gbv;
 	}
 	
 	public int getDefaultPan()
 	{
 		return this.dfp;
 	}
 	
 	public int getDefaultCutoff()
 	{
 		return this.ifc;
 	}
 	
 	public int getDefaultResonance()
 	{
 		return this.ifr;
 	}
 	
 	public int getPitchPanSep()
 	{
 		return this.pps;
 	}
 	
 	public int getPitchPanCentre()
 	{
 		return this.ppc;
 	}
 	
 	public int getVolSwing()
 	{
 		return this.rv;
 	}
 	
 	public int getPanSwing()
 	{
 		return this.rp;
 	}
 	
 	public Envelope.Handle getVolEnvHandle()
 	{
 		return this.env_vol == null ? null : this.env_vol.getHandle(64);
 	}
 	
 	public Envelope.Handle getPanEnvHandle()
 	{
 		return this.env_pan == null ? null : this.env_pan.getHandle(0);
 	}
 	
 	public Envelope.Handle getPerEnvHandle()
 	{
 		return this.env_per == null ? null : this.env_per.getHandle(0);
 	}
 	
 	public int getFadeout()
 	{
 		return fadeout;
 	}
 	
 	public int getNNA()
 	{
 		return nna;
 	}
 	
 	public int getDCT()
 	{
 		return dct;
 	}
 	
 	public int getDCA()
 	{
 		return dca;
 	}
 }
