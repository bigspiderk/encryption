import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Encryption {

    public static void main(String[] args) {
        if (args.length > 0) {
            String file = null;
            if (args.length >= 2) {
                file = args[1];
            } else {
                System.err.println("Please Specify File");
                System.exit(1);
            }

            if (args[0].equalsIgnoreCase("lock")) {
                lock(file);
            }

            if (args[0].equalsIgnoreCase("unlock")) {
                unlock(file);
            }
        }
    }

    public static void lock(String file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot Find File");
            System.exit(1);
        }
        
        Scanner s = new Scanner(System.in);
        System.out.printf("Set password (at least 8 characters): ");
        String password;
        while ((password = s.nextLine()).length() < 8) {
            System.out.println("Password must be at least 8 characters");
        }
        s.close();

        String binaryHash = intToBin(hash(password));

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(String.format("%s.lock", file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            int passwordLength = binaryHash.length() / 8;
            out.write(passwordLength);
            for (int i = 0; i < passwordLength; i++) {
                String chunck = binaryHash.substring(binaryHash.length() - 8*(i+1), binaryHash.length() - 8*i);
                out.write(binToInt(chunck));
            }

            encryptFile(in, out, password);
            System.out.println("Successfully Locked File");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void unlock(String file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot Find File");
            System.exit(1);
        }

        try {
            byte[] passwordBytes = in.readNBytes(in.read());
            StringBuilder binaryHashBuilder = new StringBuilder();
            for (byte b : passwordBytes) {
                binaryHashBuilder.insert(0, intToBin(b & 0xff));
            }

            int passwordHash = binToInt(binaryHashBuilder.toString());

            Scanner s = new Scanner(System.in);
            String passwordAttempt = null;
            System.out.printf("Enter Password: ");
            while (hash((passwordAttempt = s.nextLine())) != passwordHash) {
                System.out.println("Incorrect Password Try Again");
            }
            s.close();
            String unlockedFilename = String.format("unlocked-%s", file.substring(0, file.indexOf(".lock")));
            encryptFile(in, new FileOutputStream(unlockedFilename), passwordAttempt);
            System.out.println("Succesfully Unlocked File");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void encryptFile(FileInputStream in, FileOutputStream out, String password) throws IOException{
        int length = in.available();
        for (int i = 0; i < length; i++) {
            out.write(encryptByte((byte) in.read(), password.charAt(i%password.length())) &0xff);
        }
        in.close();
        out.close();
    }

    public static byte encryptByte(byte b, char c) {
        String keyBits = intToBin(c);
        String bits = intToBin(b &0xff);
        int newByte = 0;
        for (int i = bits.length()-1; i >= 0; i--) {
            if (keyBits.charAt(i) == bits.charAt(i)) {
                newByte += Math.pow(2, i);
            }
        }
        return (byte) newByte;
    }

    public static byte[] encrypt(byte[] text, String key) {
        ByteArrayOutputStream encryptedText = new ByteArrayOutputStream();
        String keyBits;
        String bits;
        int currentByte = 0;
        for (int i = 0; i < text.length; i++) {
            currentByte = 0;
            keyBits = intToBin(key.charAt(i%key.length()));
            bits = intToBin(text[i] &0xff);
            for (int j = bits.length()-1; j >= 0; j--) {
                if (keyBits.charAt(j) == bits.charAt(j)) {
                    currentByte += Math.pow(2, j);
                }
            }
            encryptedText.write(currentByte);
        }
        return encryptedText.toByteArray();
    }

    public static int hash(String s) {
        int hash = 0;
        for (int i = 0; i < s.length(); i++) {
            hash = 31 * hash + s.charAt(i);
        }
        return Math.abs(hash);
    }

    public static String intToBin(int num) {
        StringBuilder binary = new StringBuilder();

        if (num == 0) {
            binary.append(0);
        }
    
        while (num > 0) {
            int remainder = num % 2;
            binary.append(remainder);
            num /= 2;
        }
    
        while (binary.length() % 8 != 0) {
            binary.append('0');
        }
    
        return binary.toString();
    }

    public static int binToInt(String bin) {
        int total = 0;
        for (int i = 0; i < bin.length(); i++) {
            total += Math.pow(2, i) * (bin.charAt(i) % '0');
        }
        return total;
    }
}
