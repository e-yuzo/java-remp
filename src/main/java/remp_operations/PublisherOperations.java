
package remp_operations;

import cryptography_primitives.AES;
import cryptography_primitives.CMAC;
import cryptography_primitives.KeyDerivation;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import utils.AuthenticatedStateConstants;
import utils.ByteUtils;

/**
 * This class provides implementation of the Publisher component.
 * @author yuzo
 */
public class PublisherOperations {
    
    private byte[] pi; //128 bits
    public byte[] ai; //128 bits
    public byte[] Ti;
    private byte[] i; //128 bits
    private byte[] g;
    
    public byte[] iv;
    
    public byte[] encryptedWithAI;
    
    public PublisherOperations() {
        AuthenticatedStateConstants asc = new AuthenticatedStateConstants("publisher");
        this.pi = asc.pi;
        this.ai = asc.ai;
        this.Ti = asc.T;
        this.i = asc.i;
        this.g = asc.g;
    }
    
    /**
     * Random number rn generation.
     * @return 
     */
    public byte[] generateRandomNumber() {
        byte bytes[] = new byte[15];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Session key sk derivation per message Xk.
     * @param rn
     * @return 
     */
    public byte[] deriveSK(byte[] rn) {
        KeyDerivation kd = new KeyDerivation();
        //System.out.println("");
        byte[] derivedKey = kd.deriveKey(rn, this.pi);
        return derivedKey;
    }
    
    /**
     * Message Encryption and key concatenation according to protocol.
     * @param message
     * @param sessionKey
     * @param rn
     * @return 
     */
    public byte[] encryptMessageK(String message, byte[] sessionKey,
                                   byte[] rn) { try {
        AES aes = new AES();
        byte[] iv = "123456789abcdefg".getBytes("US-ASCII");
        byte[] encryptedMessage = aes.encryptData(message.getBytes(StandardCharsets.UTF_8),
                sessionKey, iv);
        byte[] messageK = ByteUtils.combine(encryptedMessage, ByteUtils.combine(rn, this.g));
        return messageK;
        } catch (Exception ex) {
            Logger.getLogger(PublisherOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Encryption of concatenated identity i and hashed Xk.
     * @param messageK return of encrypMessageK
     * @return 
     */
    public byte[] createDataX(byte[] messageK) {
        try {
            // encrypt i and hashed message
            byte[] hashedMessage = new CMAC().MAC(messageK, this.pi);
            byte[] toBeEncrypted = ByteUtils.combine(this.i, hashedMessage);
            byte[] IV = "123456789abcdefg".getBytes("US-ASCII");
            byte[] encryptedMessage = new AES().encryptData(toBeEncrypted, this.ai, IV);
            System.out.println("ENCRYPTEDWITHai LENGTH: " + encryptedMessage.length);
            // combine messageK, encrypted/hashed data, Ticket Ti
            System.out.println("TI LENGTH: " + Ti.length);
            byte[] dataX = ByteUtils.combine(messageK,
                    ByteUtils.combine(encryptedMessage, Ti));
            return dataX;
        } catch (Exception ex) {
            Logger.getLogger(PublisherOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        String messageK = "lucid animal";
        PublisherOperations po = new PublisherOperations();
        byte[] rn = po.generateRandomNumber();
        byte[] sessionKey = po.deriveSK(rn);
        byte[] encryptedMessageK = po.encryptMessageK(messageK, sessionKey, rn);
        System.out.println("XK LENGTH: " + encryptedMessageK.length);
        byte[] dataX = po.createDataX(encryptedMessageK);
        System.out.println("DATAX: " + dataX.length);
    }
    
}
