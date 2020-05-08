 import java.util.HashSet;
 import java.util.Set;
 
 
 public class Main {
 	@SuppressWarnings("unused")
 	public static void main(String[] args) {
 		// Einfaches Testprogramm für den erarbeiteten Algorithmus a1 für die Trust-Prüfung
 		// Diese Variante enthält als vereinfachung keine Notare, da diese ja nur der Anonymisierung dienen.
 		
 		// == Initialisierung der Testwerte
 		Blacklist blacklist = new Blacklist();
 		
 		OmpId rootX = new OmpId("rootx.onion");
 		OmpId rootI = new OmpId("rooti.onion");
 		OmpId rootR = new OmpId("rootr.onion");
 		
 		HashSet<OmpId> rootIds = new HashSet<OmpId>();
 		rootIds.add(rootX);
 		rootIds.add(rootI);
 		rootIds.add(rootR);
 		
 		// Test Users
 		OmpId userA = new OmpId("a.onion");
 		OmpId userB = new OmpId("b.onion");
 		OmpId userC = new OmpId("c.onion");
 		OmpId userD = new OmpId("d.onion");
 		OmpId userE = new OmpId("e.onion");
 		OmpId userF = new OmpId("f.onion");
 		
 		if(false) {
 			userA.requestSignatureAt(rootX, "finger-a");
 		}
 		
 		if(false) {
 			userA.requestSignatureAt(rootX, "finger-a");
 			userA.signHonestFingerprintingContract(rootI);
 		}
 		
 		if(false) {
 			userA.signHonestFingerprintingContract(rootI);
 		}
 		
 		if(false) {
 			userA.requestSignatureAt(rootX, "finger-a");
 			userA.signHonestFingerprintingContract(rootX);
 		}
 		
 		if(false) {
 			userA.signHonestFingerprintingContract(rootX);
 		}
 		
 		if(false) {
 			userA.requestSignatureAt(rootX, "finger-a");
 			userA.signHonestFingerprintingContract(rootX);
 			
 			userB.requestSignatureAt(userA, "finger-b");
 			userC.requestSignatureAt(userB, "finger-c");
 			userD.requestSignatureAt(userC, "finger-d");
 		}
 		
 		if(false) {
 			userA.requestSignatureAt(rootX, "finger-a");
 			userA.signHonestFingerprintingContract(rootX);
 			
 			userB.requestSignatureAt(userA, "finger-b");
 			userB.signHonestFingerprintingContract(userA);
 			
 			userC.requestSignatureAt(userB, "finger-c");
 			userD.requestSignatureAt(userC, "finger-d");
 		}
 		
 		if(false) {
 			userA.requestSignatureAt(rootX, "finger-a");
 			userA.signHonestFingerprintingContract(rootX);
 			
 			userB.requestSignatureAt(userA, "finger-b");
 			userB.signHonestFingerprintingContract(userA);
 			
 			userC.requestSignatureAt(userB, "finger-c");
 			userD.requestSignatureAt(userC, "finger-d");
 			
 			blacklist.addBlackListEntry(new BlacklistEntry("finger-b", userA, "bad-signing"));
 		}
 		
 		if(false) {
 			// Branch 1
 			userA.requestSignatureAt(rootX, "finger-a");
 			userA.signHonestFingerprintingContract(rootX);
 			
 			userB.requestSignatureAt(userA, "finger-b");
 			userB.signHonestFingerprintingContract(userA);
 			
 			userC.requestSignatureAt(userB, "finger-c");
 			userC.signHonestFingerprintingContract(userB);
 			
 			userD.requestSignatureAt(userC, "finger-d");
 			userD.signHonestFingerprintingContract(userC);
 			
 			// Branch 2
 			userE.requestSignatureAt(rootI, "finger-e");
 			userE.signHonestFingerprintingContract(rootI);
 			
 			userF.requestSignatureAt(userE, "finger-f");
 			userF.signHonestFingerprintingContract(userE);
 			
 			// Blacklist
 			blacklist.addBlackListEntry(new BlacklistEntry("finger-c", userF, "bad-signing"));
 		}
 		
 		if(false) {
 			// Branch 1
 			userA.requestSignatureAt(rootX, "finger-a");
 			userA.signHonestFingerprintingContract(rootX);
 			
 			userB.requestSignatureAt(userA, "finger-b");
 			userB.signHonestFingerprintingContract(userA);
 			
 			userC.requestSignatureAt(userB, "finger-c");
 			userC.signHonestFingerprintingContract(userB);
 			
 			userD.requestSignatureAt(userC, "finger-d");
 			userD.signHonestFingerprintingContract(userC);
 			
 			// Branch 2
 			userE.requestSignatureAt(rootI, "finger-e");
 			userE.signHonestFingerprintingContract(rootI);
 			
 			userF.requestSignatureAt(userE, "finger-f");
 			userF.signHonestFingerprintingContract(userE);
 			
 			// Blacklist
 			blacklist.addBlackListEntry(new BlacklistEntry("finger-e", userF, "bad-signing"));
 			// check if it is ok, to let this blacklist both E and F
 		}
 		
 		if(false) { // Abbildung N-6
 			// Branch 1
 			userA.requestSignatureAt(rootX, "finger-a");
 			userA.signHonestFingerprintingContract(rootX);
 			
 			userB.requestSignatureAt(userA, "finger-b");
 			userB.signHonestFingerprintingContract(userA);
 			
 			userC.requestSignatureAt(userB, "finger-c");
 			userC.signHonestFingerprintingContract(userB);
 			
 			userD.requestSignatureAt(userC, "finger-d");
 			userD.signHonestFingerprintingContract(userC);
 			
 			// Branch 2
 			userE.requestSignatureAt(rootI, "finger-e");
 			userE.signHonestFingerprintingContract(rootI);
 			
 			userF.requestSignatureAt(userE, "finger-f");
 			userF.signHonestFingerprintingContract(userE);
 			
 			// Blacklist
 			blacklist.addBlackListEntry(new BlacklistEntry("finger-c", userF, "bad-signing"));
 			blacklist.addBlackListEntry(new BlacklistEntry("finger-e", userC, "bad-signing"));
 			
 			// lower layer is able to blacklist higher layer,
 			// but not here, because higher layer comes first
 		}
 		
 		if(true) {
 			userA.requestSignatureAt(rootX, "finger-a");
 			userA.signHonestFingerprintingContract(userB);
 			
 			userB.requestSignatureAt(rootI, "finger-b");
 			userB.signHonestFingerprintingContract(rootI);
 			
 			blacklist.addBlackListEntry(new BlacklistEntry("finger-b", rootI, "bad-signing"));
 			// here, bad-signing also means, that he cant be arbiter for signing any more
			// BUG :-(
 		}
 		
 		HashSet<OmpId> wot = new HashSet<OmpId>();
 		wot.add(rootX);
 		wot.add(rootI);
 		wot.add(rootR);
 		wot.add(userA);
 		wot.add(userB);
 		wot.add(userC);
 		wot.add(userD);
 		wot.add(userE);
 		wot.add(userF);
 		
 		// End of Test Users
 		
 		// == Der Trust Algorithmus
 		for (OmpId wurzel : rootIds) {
 			wurzel.setTrustedRoot();
 		}
 		
 		/*
 		 * Phase 2: L1 wird nach L2 kopiert. Es werden in L2 von oben nach unten alle schichten mit
 		 * ids gemäß signierung durchlaufen und dann geprüft, ob ein blacklist eintrag "bad-signing"
 		 * existiert, für den gilt, dass der fingerprint von der aktuellen ebene signiert wurde
 		 * (und der blacklistete muss den arbiter auch festgelegt haben).
 		 * --- Schließlich werden alle OmpIds die mindestens eine route ohne Blacklisteinträge
 		 * zur wurzel haben, als valid-hash bzw. valid-fingerprint markiert.
 		 */
 		final int MAX_SCHICHTEN = 20;
 		HashSet<OmpId> schichten[] = new HashSet[MAX_SCHICHTEN];
 		HashSet<String> verifiedBlacklistedFingerprints = new HashSet<String>();
 		HashSet<OmpId> currentSchicht;
 		
 		int anzahlSchichten = 0; // anzahl bereits befuellter Schichten
 		do {
 			currentSchicht = new HashSet<OmpId>(); // In diesem Schleifendurchgang zu fuellende Schicht
 			
 			// erste schicht?
 			if(anzahlSchichten==0) {
 				for (OmpId wurzel : rootIds) {
 					currentSchicht.add(wurzel);
 				}
 			} else
 			
 			// zweite oder weitere schicht?
 			{
 				// gehe durch alle ids der oberen schicht (obige ids sind gültig)
 				for(OmpId id : schichten[anzahlSchichten-1]) {
 					// gehe durch die kinder dieser id
 					Set<OmpId> childs = id.getSignedFingerprints();
 					for(OmpId child : childs)
 					{
 						// markiere es als valid-fingerprint
 						child.setValidFingerprint(true);
 						
 						// prüfe, ob das kind in der schrittweise aufgebauten verifizierten blacklist für "bad-signing" ist (1)
 						boolean blacklisted = verifiedBlacklistedFingerprints.contains(child.getFingerprint());
 						
 						if(blacklisted) {
 							// falls ja ignorieren
 						} else
 						// falls nein
 						{
 							// übernehme dieses kind in die aktuelle schicht
 							currentSchicht.add(child);
 							
 							// und markiere es vorübergehend als trusted-Identmanager
 							child.setTrustedIdentmanager(true);
 						}
 					}
 				}
 			}
 			
 			// gehe durch alle ids der aktuellen schicht
 			// prüfe, ob von dieser id ein blacklisteintrag kommt.
 			HashSet<String> badKeywords = new HashSet<String>();
 			badKeywords.add("bad-signing");
 			
 			HashSet<BlacklistEntry> bles =
 			blacklist.getBlacklistEntriesBy(
 					null,
 					currentSchicht,
 					badKeywords);
 			
 			for(BlacklistEntry badFingerprint : bles)
 			// falls ja
 			{
 				// nehme den eintrag in die lokale blacklist auf (1)
 				verifiedBlacklistedFingerprints.add(badFingerprint.fingerprint);
 				
 				// finde alle OmpIds mit diesem Fingerprint
 				for(OmpId checkOmpId : wot)
 				{
 					String fingerprint = checkOmpId.getFingerprint();
 					if(fingerprint!=null && fingerprint.equals(badFingerprint.fingerprint)) {
 						// falls der geblacklistete das flag trusted-identmanager
 						// besitzt
 						if(checkOmpId.isTrustedIdentmanager())
 						{
 							// das flag trusted-identmanager wird wieder entzogen.
 							checkOmpId.setTrustedIdentmanager(false);
 							
 							// Gehe rekursiv durch seine Kinder und entferne bei
 							// ihnen sowohl das trusted-identmanager flag, als auch
 							// das valid fingerprint flag (2)
 							removeChildTrust(checkOmpId);
 						}
 					}
 				}
 			}
 			
 			schichten[anzahlSchichten] = currentSchicht;
 			anzahlSchichten++;
 		} while(anzahlSchichten < MAX_SCHICHTEN);
 		
 		// TODO in späteren Schritten: prüfen, ob der blacklisteintrag legitim ist
 		// (also ob überhaupt ein solecher vertraug unterzeichnet wurde)
 				
 
 		// Phase 3
 		/*
 		 * Phase 3b: Kopiere L2 nach L3. Bei allen als valid-fingerprint oder valid-hash markierten
 		 * arbiter wird überprüft, ob sie sich auf einen trusted root arbiter zurückführen lassen.
 		 * falls das der fall ist, wird er in L3 als trusted-notary bzw. trusted-identmanager markiert.
 		 * falls nein, dann empfangen alle seine signierten kinder von ihm keinen valid-hash oder
 		 * valid-fingerprint mehr. Abbildung N-3 sollte hier funktionieren.
 		 */
 		
 		// gehe durch alle OmpIds
 		for (OmpId ompId : wot) {
 			// gehe rekursiv in diese omp id herein,
 			// um zu prüfen, ob es sich auf einen trusted arbiter zurückführen lässt
 			if( ompId.isTrustedIdentmanager() && checkForArbiter(ompId)) {
 				// als trusted arbiter markieren
 				ompId.setTrustedArbiter(true);
 				//ompId.setTrustedIdentmanager(true);
 			} else {
 				// kinder können kein valid-hash haben
 				//ompId.setTrustedArbiter(false);
 				ompId.setTrustedIdentmanager(false);
 				removeChildTrust(ompId);
 			}
 		}
 		
 
 		// === Das Ergebnis der Trust Berechnung ausgeben
 		for (OmpId ompId : wot) {
 			System.out.println(ompId.toString());
 		}
 	}
 	
 	// TODO: MAX DEPTH
 	private static boolean checkForArbiter(OmpId ompId) {
 		if(ompId==null) {
 			return false;
 		}
 		
 		if(ompId.isTrustedArbiter()) {
 			return true;
 		} else {
 			OmpId parentArbiter = ompId.getParentArbiter();
			if(parentArbiter==null) {
 				return false;
 			} else {
 				return checkForArbiter(parentArbiter);
 			}
 		}
 	}
 	
 	private static void removeChildTrust(OmpId parent) {
 		for(OmpId child : parent.getSignedFingerprints()) {
 			child.setTrustedIdentmanager(false);
 			child.setValidFingerprint(false);
 			removeChildTrust(child);
 		}
 	}
 }
