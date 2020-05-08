 package nachos.vm;
 
 import nachos.machine.*;
 import nachos.threads.*;
 import nachos.userprog.*;
 import nachos.vm.*;
 
 /**
  * A <tt>UserProcess</tt> that supports demand-paging.
  */
 public class VMProcess extends UserProcess {
 	/**
 	 * Allocate a new process.
 	 */
 	public VMProcess() {
 		super();
 //		if (kernel == null) {
 //			kernel = (VMKernel) ThreadedKernel.kernel;
 //			if (kernel == null)// if it still is null we have a problem
 //			{
 //				// Deal with problem some how.
 //				// If this failes does it mean there was no more
 //				// space for it?
 //				System.out.println("VM kernel allocation failed");
 //			}
 //		}
 	}
 
 	/**
 	 * Save the state of this process in preparation for a context switch.
 	 * Called by <tt>UThread.saveState()</tt>.
 	 */
 	public void saveState() {
 		//super.saveState();
 		VMKernel.syncTLB(true);
 
 	}
 
 	/**
 	 * Restore the state of this process after a context switch. Called by
 	 * <tt>UThread.restoreState()</tt>.
 	 */
 	public void restoreState() {
 		// super.restoreState();
 		//VMKernel.syncTLB(false);
 	}
 
 	/**
 	 * Initializes page tables for this process so that the executable can be
 	 * demand-paged.
 	 * 
 	 * @return <tt>true</tt> if successful.
 	 */
 	protected boolean loadSections() {
 		for(int i=0; i < pageTable.length; i++){
 			pageTable[i] = new TranslationEntry();
 		}
 		
 		for(int i=0; i < coff.getNumSections(); i++){
 			CoffSection section = coff.getSection(i);
 			for(int j=0; j < section.getLength(); j++){
 				//set pageTable bits
 				//get First VPN
				int vpn = section.getFirstVPN() + j;
 				pageTable[vpn].used = false;
 				pageTable[vpn].dirty = false;
 				pageTable[vpn].valid = false;
				pageTable[vpn].readOnly = section.isReadOnly();
 				pageTable[vpn].vpn = j;
 			}
 		}
 		return super.loadSections();
 	}
 
 	/**
 	 * Release any resources allocated by <tt>loadSections()</tt>.
 	 */
 	protected void unloadSections() {
 		super.unloadSections();
 	}
 	
 	
 	/*
 	 * This will be in VMKernel I guess
 	 
 	private Integer findValidPPN( int vpn )
 	{
 		//try to find the translation entry for this vpn in physical memory
 		for( int i = 0; i < Machine.processor().getNumPhysPages(); i++ ){
 			if(VMKernel.coreMap[i].te.vpn == vpn)
 				return VMKernel.coreMap[i].te.ppn;
 		}
 			//if(coreMap[i].vpn == false ) //don't quite understand why you wan to compare a vpn to a boolean
 			//	return coreMap[i].ppn;
 			
 		//if it isnt in physical memory you may have to find it in the swap file
 		//or coff file
 		
 		//JUST RETURN -1 FOR NOW IF ENTRY ISNT IN MEMORY
 		return -1;
 	}
 	*/
 
 	private void handleTLBMiss(int vaddr) {
 		VMKernel.memoryLock.acquire();
 		int vpn = Machine.processor().pageFromAddress(vaddr);
 		Lib.assertTrue( vpn >= 0 );
 		int ppn = VMKernel.translatePage( this, vpn ); //assume this function works for now
 		//if vpn not in coremap we need to check if its in COFF or swap file
 		//if dirty bit in page table entry is false it must be in coff file
 		//if in coff we need to allocate page and load it from coff
 		//if it isnt in coff file it must be in swap file
 		Lib.assertTrue( ppn != -1 );
 		
 		TranslationEntry entry = null;
 		Integer TLBIndex = 0;
 		//try to find an open TLB entry
 	
 		for( int i = 0; i < Machine.processor().getTLBSize(); i++ )
 		{
 			TranslationEntry TLBEntry = 
 					Machine.processor().readTLBEntry(i);
 			if( TLBEntry.valid == false ) 
 				{
 					TLBIndex = i;
 					entry = TLBEntry;
 				}
 		}
 		
 		//if the there is no free tlb then remove a random tlb entry
 		//This might be bad cause the valid bit might make a difference here
 		//if valid is false can we still evict it from tlb?
 				
 		if( entry != null)
 			Machine.processor().writeTLBEntry(VMKernel.replacementPolicy(), entry);
 			
 		entry.dirty = false;
 		entry.used = false;
 				
 		else Machine.processor().writeTLBEntry( TLBIndex, entry );
 		
 		//write this new random entry into the tlb
 		
 		//BEFORE WE JUST WRITE A NEW TLB ENTRY WE MAY NEED TO STORE THE OLD TLB ENTRY INTO MEMORY
 		//WILL MAYBE NEED TO ASK DORIAN THIS
 		
 		
 		VMKernel.memoryLock.release();
 		
 		/*hints from Dorian
 		 =================
 		 If you are implementing handleTLBMiss, after you 
 		 choose which TLB entry you will replace, you will 
 		 copy the information from the TranslationEntry of 
 		 the memory page you are going to store in the TLB, 
 		 set the dirty/used bits to false, and then write this 
 		 to the TLB. 
 		 
 		 here is representative code of what should be going on:
 		 
 		 TranslationEntry coreEntry = coremap[ppn].entry;
 		 tlbEntry = new TranslationEntry(coreEntry);
 		 tlbEntry.used = false;
 		 tlbEntry.dirty = false;
 		 Machine.processor().writeTLBEntry(i, tlbEntry);
 		 
 		 Here, 'i' is the index of the TLB entry you are replacing.
 		 */
 	}
 
 	/**
 	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
 	 * . The <i>cause</i> argument identifies which exception occurred; see the
 	 * <tt>Processor.exceptionZZZ</tt> constants.
 	 * 
 	 * @param cause
 	 *            the user exception that occurred.
 	 */
 	public void handleException(int cause) {
 		Processor processor = Machine.processor();
 
 		switch (cause) {
 		case Processor.exceptionTLBMiss:
 			handleTLBMiss(Machine.processor().readRegister(Processor.regBadVAddr));
 			break;
 		default:
 			super.handleException(cause);
 			break;
 		}
 	}
 	
 	private static VMKernel kernel = null;
 	private static final int pageSize = Processor.pageSize;
 	private static final char dbgProcess = 'a';
 	private static final char dbgVM = 'v';
 }
