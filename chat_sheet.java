import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class chat_sheet {
    static String host;
    static int port;
    static Socket s;
    static String username;

    static String privateKeyAlice;
    static String privateKeyBob;
    static String publicKeyAlice;
    static String publicKeyBob;
    static String aliceModulus;
    static String bobModulus;

    static String symmetricKeyInHex;


    public static void main(String[] args) throws Exception {

 //       String input1="--alice -a 5f13e6fa3e373b2680b86b79181026cc743ec350a2583a0f2449dde6a1c616d1d0229a5e5e78cc6c3eaff1e5ed7495eb12ca52b93bc688124daa4428ea93f18420d21751570f5346e8950e3e43ab42f23573fc7d563107f45d4560a55d3c5b40a55dfbfe0ff51e4450407cf0cef14e16d87738540c64f9a88edc01526f60ec2b -m 8e9dda775d52d8b9c114a135a4183a32ae5e24f8f3845716b66eccd9f2a9223ab833e78d8db532a25e07ead8e42ee0e09c2f7c15d9a9cc1b747f663d5fddea47af6f97afe8281c2d30a0e8e0211d7b1d27cbfa848397175919bc81f552a4db12230c0fbf8ef7406511aad550ba09f47d1de0ba1aa6952c96a2ef4496cd5f12fd -b 7 -n 832e0139ac6c5bb9ae025e21d25873b5a4de83cbe3d574f555c648507fd04bfaed1e20434d8deda4043bc45d2d1a684fd56e47186936c2105e0eff81f1f4c38ffeca3480e5e7e658ace1b6d2be8aa7f3014ded08a8282782d0802a2cfbf7546609ec0f45f6cd8d4957d9ea414b8b312e181056f89cc6948205bc2876e5f32935 -p 8989 -i 127.0.0.1";
 //       String[] parsedInput1=input1.split(" ");

 //       String input2="--bob -b 7070935601817331709450af4694f5771f9a27d3559264400060d044ffd71c8defd0adf08b9e396803a0f1746fcd7dfb4939f3cbc7e5cae975316d4acf6415558ec596b6fd148871587505a6ecf45b54f63795e29e2803ba979671604d845039ba3b50dfdadfb7cd507d946a505ed078824e20b15de67da6276c9997c72fedf7 -n 832e0139ac6c5bb9ae025e21d25873b5a4de83cbe3d574f555c648507fd04bfaed1e20434d8deda4043bc45d2d1a684fd56e47186936c2105e0eff81f1f4c38ffeca3480e5e7e658ace1b6d2be8aa7f3014ded08a8282782d0802a2cfbf7546609ec0f45f6cd8d4957d9ea414b8b312e181056f89cc6948205bc2876e5f32935 -a 3 -m 8e9dda775d52d8b9c114a135a4183a32ae5e24f8f3845716b66eccd9f2a9223ab833e78d8db532a25e07ead8e42ee0e09c2f7c15d9a9cc1b747f663d5fddea47af6f97afe8281c2d30a0e8e0211d7b1d27cbfa848397175919bc81f552a4db12230c0fbf8ef7406511aad550ba09f47d1de0ba1aa6952c96a2ef4496cd5f12fd -p 8989 -i 127.0.0.1";
 //       String[] parsedInput2=input2.split(" ");


        @SuppressWarnings("resource")
        Scanner keyboard = new Scanner(System.in);

//		Process command line arguments
        if(parseInput(args) == -1)
            return;
//		set up server, or join server
        setupServer();

//		Set up username
        System.out.println("Welcome to encrypted chat program.\nChat starting below:");

//		Make thread to print out incoming messages...
        ChatListenter chatListener = new ChatListenter();
        chatListener.start();

//		loop through sending and receiving messages
        PrintStream output = null;
        try {
            output = new PrintStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String input = "";
        while(true){

            input = keyboard.nextLine();
            if(!input.equals(""))
            {
                input = username + ": " + input;

                //encrypt the input
                EncryptionDecryption encryptionDecryption = new EncryptionDecryption(symmetricKeyInHex);
                String encryptedInput = encryptionDecryption.encrypt(input);

                output.println(encryptedInput);

                //output.println(input);

                output.flush();
            }
        }
    }


    /**
     * This function Processes the Command Line Arguments.
     */

    private static int parseInput(String[] args) {

        if(args[0].startsWith("--") || args.length!= 13)
        {
            if(args[0].equalsIgnoreCase("--alice"))
                username = "alice";
            else if(args[0].equalsIgnoreCase("--bob"))
                username = "bob";
            else
                return -1;

            //loop through each argument and load its value
            if(username.equals("alice"))
                for(int i = 1; i < args.length; i++)
                {
                    if (args[i].equals("-a"))
                        privateKeyAlice = args[i+1];

                    else if (args[i].equals("-m"))
                        aliceModulus = args[i+1];

                    else if (args[i].equals("-b"))
                        publicKeyBob = args[i+1];

                    else if (args[i].equals("-n"))
                        bobModulus = args[i+1];

                    else if (args[i].equals("-p"))
                        port = Integer.parseInt(args[i+1]);

                    else if (args[i].equals("-i"))
                        host = args[i+1];

                    else
                        return -1;

                    i++;

                }
            else if (username.equals("bob"))
                for(int i = 1; i < args.length; i++)
                {
                    if (args[i].equals("-a"))
                        publicKeyAlice = args[i+1];

                    else if (args[i].equals("-m"))
                        aliceModulus = args[i+1];

                    else if (args[i].equals("-b"))
                        privateKeyBob = args[i+1];

                    else if (args[i].equals("-n"))
                        bobModulus = args[i+1];

                    else if (args[i].equals("-p"))
                        port = Integer.parseInt(args[i+1]);

                    else if (args[i].equals("-i"))
                        host = args[i+1];

                    else
                        return -1;

                    i++;

                }

            return 0;
        }
        else
        {
            System.out.println("Usage:");
            System.out.println("-h");
            System.out.println("--alice -a <private key alice> -m <alice modulus> -b <public key bob> -n <bob modulus> -p port -i ip address");
            System.out.println("--bob -b <private key bob> -n <bob modulus> -a <public key alice> -m <alice modulus> -p port -i ip address:");

            return 0;
        }

    }



    /**
     * Upon running this function it first tries to make a connection on
     * the given ip:port pairing. If it find another client, it will accept
     * and leave function.
     * If there is no client found then it becomes the listener and waits for
     * a new client to join on that ip:port pairing.
     */
    private static void setupServer() {

        try {
            // This line will catch if there isn't a waiting port
            s = new Socket(host, port);

        } catch (IOException e1) {
            System.out.println("There is no other client on this IP:port pairing, waiting for them to join.");

            try {
                ServerSocket listener = new ServerSocket(port);
                s = listener.accept();	//blocks until it accepts

                listener.close();

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        try{
            BufferedReader input  = new BufferedReader(new InputStreamReader(s.getInputStream()));
            OutputStream output = s.getOutputStream();

            //right after the connection is accepted
            //if user is alice
            if (username.equalsIgnoreCase("alice"))
            {
                System.out.println("Connected to Bob");

                //read the first message, which will be the sym key
                System.out.println("Recieving enc sym key");
                String rsaEncryptedSymKey = input.readLine();	//NOTE: reaLine is blocking, so you'll always read the key
                System.out.println("Recieved enc sym key :" + rsaEncryptedSymKey);

                //decrypt it
                symmetricKeyInHex = RSA_Cipher.RSAdecrypt(privateKeyAlice, aliceModulus, rsaEncryptedSymKey);
                System.out.println("Decrypted Sym Key :"+symmetricKeyInHex);

                //Encrypt the message "OK"
                DES des = new DES(symmetricKeyInHex);
                String desEncryptedHandshake = des.encrypt("4f4b"); //4f4b == OK in hex

               // EncryptionDecryption en = new EncryptionDecryption(symmetricKeyInHex);
               // String desEncryptedHandshake = en.encrypt("4f4b");

                System.out.println("Number of blocks :" + desEncryptedHandshake.split("-")[0]);

                //send the handshake message
                System.out.println("Sending Handshake... " + desEncryptedHandshake);	//desEncryptedHandshake is split into blocks seperated by "\n"
                output.write((desEncryptedHandshake).getBytes());
                System.out.println("Handshake sent");

            }
            //if user is bob
            else if(username.equalsIgnoreCase("bob"))
            {
                System.out.println("Connected to Alice");

                //generate the symm key
                symmetricKeyInHex = DES.generateKey();
                System.out.println("Sym key :" + symmetricKeyInHex);

                //encrypt with alice's pub key
                String rsaEncryptedSymKey = RSA_Cipher.RSAencrypt(publicKeyAlice, bobModulus, symmetricKeyInHex);

                //send the encrypted symmetric key
                System.out.println("Sending enc sym key :" + rsaEncryptedSymKey);
                output.write((rsaEncryptedSymKey + "\n").getBytes());
                System.out.println("Sent enc sym key");

                //block till you get the handshake message
                System.out.println("Getting encrypted handshake...");
                String desEncryptedHandshake = input.readLine();
                int numBlocks = Integer.parseInt(desEncryptedHandshake.split("-")[0]);

                //read all blocks
                for (int i=1; i < numBlocks; i++)
                    desEncryptedHandshake = desEncryptedHandshake + "\n" + input.readLine();
                desEncryptedHandshake = desEncryptedHandshake + "\n";

                System.out.println("Got encrypted handshake :" +  desEncryptedHandshake);

                //decrypt it
                DES des = new DES(symmetricKeyInHex);
                String decryptedHandshake = des.decrypt(desEncryptedHandshake);

                //EncryptionDecryption en = new EncryptionDecryption(symmetricKeyInHex);
                //String decryptedHandshake = en.decrypt(desEncryptedHandshake);

                if(decryptedHandshake.equals("4f4b"))
                    System.out.println("Handshake Successful");
                else
                {
                    System.out.println("Handshake failed");
                    System.exit(1);
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Client Connected.");

    }


    /**
     * A private class which runs as a thread listening to the other
     * client. It prints out the message on screen.
     */
    static private class ChatListenter implements Runnable {
        private Thread t;
        ChatListenter(){
        }

        @Override
        public void run() {
            BufferedReader input = null;
            try {
                input = new BufferedReader(new InputStreamReader(s.getInputStream()));
            } catch (IOException e1) {
                e1.printStackTrace();
                System.err.println("System would not make buffer reader");
                System.exit(1);
            }
            String inputStr;
            while(true){
                try {
//					Read lines off the scanner
                    inputStr = input.readLine();

                    if (!inputStr.equals("")) {
                        //TODO: decrypt the des blocks
                        EncryptionDecryption encryptionDecryption = new EncryptionDecryption(symmetricKeyInHex);
                        String decryptedInputStr = encryptionDecryption.decrypt(inputStr);
                        System.out.println(decryptedInputStr);
                    }

//					System.out.println(inputStr);

//					if(inputStr == null){
//						System.err.println("The other user has disconnected, closing program...");
//						System.exit(1);
//					}

                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void start(){
            if (t == null){
                t = new Thread(this);
                t.start();
            }
        }
    }
}
