import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA_Cipher {

	char programType='z';

	String inputValue="", outputFile="";
	String modulusValue ="";
	int[][] finalKeys = new int [16][48];

	String keyString = "";

	static int bitSize = 1024;      //Default

	static BigInteger N = new BigInteger("0");
	static BigInteger D = new BigInteger("0");
	static BigInteger E = new BigInteger("0");
	static String m = new String();

	private int parseInput(String args[]) {

		if (args[0].charAt(0)=='-') {
			switch (args[0].charAt(1)) {
			case 'h':
				if (args.length!=1) {
					return 0;
				}
				programType='h';
				return 1;

			case 'k':
				if (args.length == 1)
				{
					programType='k';
					return 1;

				}
				else if(args.length == 3)
				{

					if(args[1].equals("-b"))
					{
						bitSize = Integer.parseInt(args[2]);
					}
					else
						return 0;

					programType='k';

					return 1;
				}
				else
					return 0;

			case 'e':
			case 'd':
				if (args.length!=6) {
					return 0;
				}
				keyString = args[1];
				modulusValue = new String();
				modulusValue = args[3];

				for (int i=0; i<args.length; i++)
				{
					if (args[i].charAt(0)=='-')
					{
						switch (args[i].charAt(1))
						{
						case 'e':
							programType='e';
							break;
						case 'd':
							programType='d';
							break;
						case 'i':
							inputValue = new String();
							inputValue = args[i+1];
							break;
						default:
							break;
						}
					}
				}
				return 1;
			default:
				return 0;
			}
		}
		else {
			return 0;
		}
	}

	public static void main(String[] args){



		RSA_Cipher rsa = new RSA_Cipher();

		if (args.length<1 || (rsa.parseInput(args)!=1) ) {
			System.out.println("Invalid options. Use java RSA -h\"for list of options.");
			return;
		}
       //
		switch(rsa.programType)
		{
		case 'h':
			System.out.print("Generate key: java RSA -k\n");
			System.out.print("Encrypt file: java RSA -e <64_bit_key_in_hex> -i <input_file> -o <output_file>\n");
			System.out.print("Decrypt file: java RSA -d <64_bit_key_in_hex> -i <input_file> -o <output_file>\n");
			break;
		case 'k':
			genRSAkey();
			break;
		case 'e':
			RSAencrypt(rsa.keyString, rsa.modulusValue, rsa.inputValue);
			break;
		case 'd':
			RSAdecrypt(rsa.keyString, rsa.modulusValue, rsa.inputValue);
			break;
		default:
			break;
		}

	}

	public static String RSAencrypt(String keyString, String modulusValue, String inputValue)
	{
		RSA_Cipher rsa = new RSA_Cipher();

		//TODO: the keyString and modulusValue also will be in hex,
		// so do the necessary conversions

//		System.out.println("e:"+keyString);
//		System.out.println("n:"+modulusValue);

		//convert e and n to decimal, since they will be in hex
		BigInteger e = new BigInteger(keyString, 16);
		BigInteger n = new BigInteger(modulusValue, 16);

//		System.out.println("Plaintext: " + inputValue);

		//plaintext/input is in hex, so we convert it to decimal
		BigInteger plaintext = new BigInteger(inputValue,16);

		//encrypt plaintext
		BigInteger ciphertext = rsa.encrypt(plaintext,e,n);

		//encrypted value is in dec so we convert it back to hex
		String ciphertextInHex = ciphertext.toString(16);

//		System.out.println("Ciphertext: " + ciphertextInHex);
		
		return ciphertextInHex;
	}

	public static BigInteger encrypt(BigInteger message, BigInteger e, BigInteger n ) {
		return message.modPow(e, n);
	}

	public static String RSAdecrypt(String keyString, String modulusValue, String inputValue)
	{
		RSA_Cipher rsa = new RSA_Cipher();

//		System.out.println("d:"+keyString);
//		System.out.println("n:"+modulusValue);

		//convert e and n to decimal, since they will be in hex
		BigInteger d = new BigInteger(keyString, 16);
		BigInteger n = new BigInteger(modulusValue, 16);

//		System.out.println("Ciphertext: " + inputValue);

		//ciphertext/input is in hex, so we convert it to decimal
		BigInteger ciphertext = new BigInteger(inputValue,16);

		//decrypt ciphertext
		BigInteger plaintext = rsa.decrypt(ciphertext, d, n);

		//String plaintextInHex =  plaintext.toString(); //new String(plaintext.toByteArray());

		//decrypted value is in dec, so convert it into hex
		String plaintextInHex = plaintext.toString(16);

//		System.out.println("Plaintext: " + plaintextInHex);
		
		return plaintextInHex;

	}

	public synchronized String decrypt(String message, BigInteger d, BigInteger n) {
		return new String((new BigInteger(message)).modPow(d, n).toByteArray());
	}
	public synchronized BigInteger decrypt(BigInteger message, BigInteger d, BigInteger n) {
		return message.modPow(d, n);
	}
	public static void genRSAkey()
	{
		String time = Long.toString((System.nanoTime()/1000)%1000);    	
		SecureRandom r = new SecureRandom(time.getBytes());        

		BigInteger p = new BigInteger(bitSize / 2, 100, r);
		BigInteger q = new BigInteger(bitSize / 2, 100, r);

		N = p.multiply(q);

		BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

		E = new BigInteger("65537");
		while (m.gcd(E).intValue() > 1) {
			E = E.add(new BigInteger("2"));
		}

		D = E.modInverse(m);

		String DinHex = D.toString(16);
		String EinHex = E.toString(16);
		String NinHex = N.toString(16);

		System.out.println("e,n :"+EinHex +","+ NinHex);

		System.out.println("d :"+ DinHex);

	}

}