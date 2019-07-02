
package cryptography_primitives;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author yuzo 
 */
public class AES {

    private static final int keyLength = 16;
    private static final SecureRandom random = new SecureRandom();
    
    public byte[] encryptData(byte[] plaintext, byte[] symKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        SecretKey key = new SecretKeySpec(symKey, 0, symKey.length, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        System.out.println("[AES] Encrypting.");
        return cipher.doFinal(plaintext);
    }
    
    public byte[] decryptData(byte[] ciphertext, byte[] symKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        SecretKey key = new SecretKeySpec(symKey, 0, symKey.length, "AES");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        System.out.println("[AES] Decrypting.");
        return cipher.doFinal(ciphertext);
    }

    public SecretKey generateKey() throws Exception {
        byte[] keyBytes = new byte[keyLength];
        random.nextBytes(keyBytes);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        return keySpec;
    }

    public IvParameterSpec generateIV(Cipher cipher) throws Exception {
        byte[] ivBytes = new byte[cipher.getBlockSize()];
        random.nextBytes(ivBytes);
        return new IvParameterSpec(ivBytes);
    }
    
}
