 import java.io.BufferedReader;
 import java.io.IOException;
 import java.math.BigInteger;
 
 public class ElGamal extends CryptographyMethod {
 
 	private BigInteger cyclicGroup, primitiveRoot, privateKey, publicKey,
 			sharedKey;
 
 	public ElGamal() {
 		generateNewData();
 	}
 
 	public BigInteger getCyclicGroup() {
 		return cyclicGroup;
 	}
 
 	public BigInteger getPublicKey() {
 		return publicKey;
 	}
 
 	public BigInteger getPrimitiveRoot() {
 		return primitiveRoot;
 	}
 
 	public void setPublicInfo(BigInteger cyclicGroup, BigInteger primitiveRoot) {
 		this.cyclicGroup = cyclicGroup;
 		this.primitiveRoot = primitiveRoot;
 		this.publicKey = FastExponentiation.fastExponentiation(primitiveRoot,
 				privateKey, cyclicGroup);
 	}
 
 	public void setPrivateInfo(BigInteger privateKey) {
 		this.privateKey = privateKey;
 		this.publicKey = FastExponentiation.fastExponentiation(primitiveRoot,
 				privateKey, cyclicGroup);
 	}
 
 	public void setSharedKey(BigInteger sharedKey) {
 		this.sharedKey = sharedKey;
 	}
 
 	public BigInteger encrypt(BigInteger msg) throws Exception {
 		// Make sure message is smaller than the group
 		if (msg.compareTo(cyclicGroup) != -1) {
 			throw new Exception("Message too large for public key!");
 		}
 		// Make sure public key is smaller than the group
 		if (sharedKey.compareTo(cyclicGroup) != -1) {
 			throw new Exception("Shared key too large for public n!");
 		}
 		// Make sure private key is smaller than the group
 		if (privateKey.compareTo(cyclicGroup) != -1) {
 			System.out.println(privateKey);
 			throw new Exception("Private key too large for public n!");
 		}
 
 		BigInteger encryptionKey = FastExponentiation.fastExponentiation(
 				sharedKey, privateKey, cyclicGroup);
 		return msg.multiply(encryptionKey).mod(cyclicGroup);
 	}
 
 	public BigInteger decrypt(BigInteger msg) {
 		BigInteger decryptionKey = FastExponentiation.fastExponentiation(
 				sharedKey,
 				cyclicGroup.subtract(BigInteger.ONE).subtract(privateKey),
 				cyclicGroup);
 		return msg.multiply(decryptionKey).mod(cyclicGroup);
 	}
 
 	@Override
 	public void encryptInput(BufferedReader in) throws Exception {
 		try {
 			System.out.println();
 			showPublicInfo();
 			System.out.print("To use the cyclic group, primitive group, "
 					+ "and public key above type anything but 'N':");
 			String input = in.readLine();
 
 			// Ask for new input data
 			if (input.equals("N")) {
 				System.out.print("Enter p (cyclic group):");
 				input = in.readLine();
 				BigInteger cyclicGroup = new BigInteger(input);
 
 				System.out.print("Enter primitive root:");
 				input = in.readLine();
 				BigInteger primitiveRoot = new BigInteger(input);
 
 				setPublicInfo(cyclicGroup, primitiveRoot);
 				setPrivateInfo(generateNewPrivateKey());
 			}
 
 			System.out.print("Enter shared public key:");
 			input = in.readLine();
 			BigInteger sharedKey = new BigInteger(input);
 			setSharedKey(sharedKey);
 
 			// Get the message
 			System.out.print("Enter in message: ");
 			input = in.readLine();
 
 			BigInteger message;
 			try {
 				message = new BigInteger(input);
 				// We found an integer lets see if user wants to use ASCII
 				System.out.print("Integer entered would you like to "
 						+ "convert to ASCII, type 'Y' for ASCII mode? ");
 				input = in.readLine();
 
 				// User wanted ASCII so lets convert
 				if (input.equals("Y")) {
 					message = Util.convertStringToBigInt(message.toString());
 					System.out.println("Converting string to integer"
 							+ " using ASCII - " + message);
 				}
 			} catch (Exception e) {
 				System.out.println("Converting string to integer using ASCII!");
 				message = Util.convertStringToBigInt(input);
 			}
 
 			// Encrypt and provide message and public key
 			System.out.println("\nNew public key = " + publicKey);
 			System.out.println("Encrypted message = " + encrypt(message));
 		} catch (IOException e) {
 			throw new Exception("Bad input");
 		}
 	}
 
 	@Override
 	public void decryptInput(BufferedReader in) throws Exception {
 		try {
 			System.out.println();
 			showPublicInfo();
 			System.out.print("To use the cyclic group, primitive root, "
 					+ "and public key above type anything but 'N':");
 			String input = in.readLine();
 
 			// Ask for new input data
 			if (input.equals("N")) {
 				// Get value of n (modulus)
 				System.out.print("Enter p (cyclic group):");
 				input = in.readLine();
 				BigInteger cyclicGroup = new BigInteger(input);
 
 				System.out.print("Enter b (primitive root):");
 				input = in.readLine();
 				BigInteger primitiveRoot = new BigInteger(input);
 
 				System.out.print("Enter private key:");
 				input = in.readLine();
 				BigInteger privateKey = new BigInteger(input);
 
 				setPublicInfo(cyclicGroup, primitiveRoot);
 				setPrivateInfo(privateKey);
 			}
 
 			System.out.print("Enter shared public key:");
 			input = in.readLine();
 			BigInteger sharedKey = new BigInteger(input);
 			setSharedKey(sharedKey);
 
 			// Get the message
 			System.out.print("Enter in encrypted message: ");
 			input = in.readLine();
 			BigInteger msg = new BigInteger(input);
 
 			BigInteger message = decrypt(msg);
 			System.out.println("\nDecrypted message: "
 					+ "\n\tMessage as number=" + message
 					+ "\n\tMessage as text using ASCII="
 					+ Util.convertBigIntToString(message));
 		} catch (IOException e) {
 			throw new Exception("Bad input");
 		}
 	}
 
 	@Override
 	public void attackInput(BufferedReader in) throws Exception {
 		try {
 			// Get value of n (modulus)
 			System.out.print("Enter p (cyclic group):");
 			String input = in.readLine();
 			BigInteger cyclicGroup = new BigInteger(input);
 
 			// Get value of pimitive root
 			System.out.print("Enter b (primitive root):");
 			input = in.readLine();
 			BigInteger primitiveRoot = new BigInteger(input);
 
 			// Get shared public key
 			System.out.print("Enter shared public key of person encrypting:");
 			input = in.readLine();
 			BigInteger publicKeyEnc = new BigInteger(input);
 
 			// Get public key
 			System.out.print("Enter shared public key of person decrypting:");
 			input = in.readLine();
 			BigInteger publicKeyDec = new BigInteger(input);
 
 			// Get the message
 			System.out.print("Enter in encrypted message: ");
 			input = in.readLine();
 			BigInteger msg = new BigInteger(input);
 
 			// Call baby-step giant-step algorithm
 			BigInteger privateKey = BabyStepGiantStep.babyStepGiantStep(
 					cyclicGroup, primitiveRoot, publicKeyDec);
 
 			// Make sure we found a private key
 			if (privateKey != null) {
 				this.cyclicGroup = cyclicGroup;
 				this.primitiveRoot = primitiveRoot;
 				this.sharedKey = publicKeyEnc;
 				this.publicKey = publicKeyDec;
 				this.privateKey = privateKey;
 
 				// Decrypt the message and attack
 				System.out.println("\nBest guess for private key = "
 						+ privateKey);
 				BigInteger message = decrypt(msg);
 				System.out.println("\nBest guess for decrypted message: "
 						+ "\n\tMessage as number=" + message
 						+ "\n\tMessage as text using ASCII="
 						+ Util.convertBigIntToString(message));
 			} else {
 				System.out.println("Could not find private key for attack!");
 			}
 		} catch (IOException e) {
 			throw new Exception("Bad input");
 		}
 	}
 
 	@Override
 	public void showPrivateInfo() {
 		System.out.println();
 		System.out.println("Private info:" + "\n\tl (private key) = "
 				+ privateKey);
 		System.out.println();
 	}
 
 	@Override
 	public void showPublicInfo() {
 		System.out.println();
 		System.out.println("Public info:" + "\n\tp (cyclic group) = "
 				+ cyclicGroup + "\n\tb (primitive root) = " + primitiveRoot
 				+ "\n\tmy public key = " + publicKey);
 		System.out.println();
 	}
 
 	private BigInteger generateNewPrivateKey() {
		return Util.randomBigInteger(Util.TWO,
				cyclicGroup.subtract(BigInteger.ONE));
 	}
 
 	@Override
 	public void generateNewData() {
 		boolean failed = true;
 		while (failed) {
 			cyclicGroup = BlumBlumShub.randomStrongPrime();
 			privateKey = generateNewPrivateKey();
 
 			// Find a primitive root in the group
 			try {
 				primitiveRoot = PrimitiveRootSearch
 						.primitiveRootSearch(cyclicGroup);
 
 				// Public key is primitive root to the power of private key
 				publicKey = FastExponentiation.fastExponentiation(
 						primitiveRoot, privateKey, cyclicGroup);
 				failed = false;
 			} catch (Exception e) {
 				// Do nothing lets just generate another cyclic group
 			}
 		}
 	}
 }
