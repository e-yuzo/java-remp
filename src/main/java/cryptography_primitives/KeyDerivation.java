
package cryptography_primitives;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yuzo
 */
public class KeyDerivation {

    public byte[] deriveKey(byte[] rn, byte[] key) {
        try {
            AES aes = new AES();
            byte[] iv = "123456789abcdefg".getBytes("US-ASCII");
            byte[] derivedKey = aes.encryptData(rn, key, iv);
            return derivedKey;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(KeyDerivation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(KeyDerivation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(KeyDerivation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
