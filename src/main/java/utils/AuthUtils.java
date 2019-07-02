
package utils;

import cryptography_primitives.AES;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides long term ticket generation.
 * Notes: only the Broker has the key tk.
 * @author yuzo
 */
public class AuthUtils {
    
    /**
     * Publisher/Subscriber ticket generation.
     * @param tk
     * @param word 'W' for publishers or 'R' for subscribers
     * @param identity
     * @param authKey
     * @return 
     */
    public byte[] generateTicket(byte[] tk, String word,
                                   byte[] identity, byte[] authKey) {
        switch (word.toLowerCase()) {
            case "publisher":
                //fix this
                word = "W";
                break;
            case "subscriber":
                word = "R";
                break;
            default:
                return null;
        }
        AES aes = new AES();
        byte[] byteTicket = ByteUtils.combine(identity,
                            ByteUtils.combine(authKey, word
                                    .getBytes(StandardCharsets.UTF_8))); //fix this
        try {
            byte[] iv = "123456789abcdefg".getBytes("US-ASCII");
            byte[] ticket = aes.encryptData(byteTicket, tk, iv);
            return ticket;
        } catch (Exception ex) {
            Logger.getLogger(AuthUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
