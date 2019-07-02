
package cryptography_primitives;

import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author yuzo
 */
public class CMAC {
    
    private static SecureRandom random = new SecureRandom();

    public byte[] MAC(byte[] input, byte[] byteKey) throws Exception {
        SecretKey key = new SecretKeySpec(byteKey, 0, byteKey.length, "AES");
        Mac mac = Mac.getInstance("AESCMAC", "BC");
        mac.init(key);
        mac.update(input, 0, input.length);
        byte[] output = new byte[mac.getMacLength()];
        mac.doFinal(output, 0);
        System.out.println("[CMAC] Hashing.");
        return output;
    }
    
    public SecretKey generateAESKey() throws Exception {
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        return keySpec;
    }

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        CMAC c = new CMAC();
        byte[] randomMessageInBytes = new byte[7];
        random.nextBytes(randomMessageInBytes);
        SecretKey k = c.generateAESKey();
        byte[] output = c.MAC(randomMessageInBytes, k.getEncoded());
        System.out.println(output.length);
    }
    
}
