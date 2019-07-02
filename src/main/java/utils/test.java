
package utils;

import cryptography_primitives.ECDSA;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

/**
 *
 * @author yuzo
 */
public class test {
    public static void main(String[] args) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            ECNamedCurveParameterSpec ECCparam = ECNamedCurveTable.getParameterSpec("prime192v2");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
//            KeyPairGenerator keyPairGenerator = KeyPairGenerator
//                    .getInstance("ECDSA", "BC");
            keyGen.initialize(ECCparam);
            KeyPair keyPair = keyGen.generateKeyPair();
            
            PrivateKey privateKey = keyPair.getPrivate();
            System.out.println(privateKey.getFormat());
            PublicKey publicKey = keyPair.getPublic();
            System.out.println(publicKey.getFormat());
            
            // A KeyFactory is used to convert encoded keys to their actual Java classes
            KeyFactory ecKeyFac = KeyFactory.getInstance("ECDSA", "BC");
            
            // Now do a round-trip for a private key,
            byte [] encodedPriv = privateKey.getEncoded();
            // now take the encoded value and recreate the private key
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encodedPriv);
            PrivateKey privateKey2 = ecKeyFac.generatePrivate(pkcs8EncodedKeySpec);
            
            // And a round trip for the public key as well.
            byte [] encodedPub = publicKey.getEncoded();
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encodedPub);
            PublicKey publicKey2 = ecKeyFac.generatePublic(x509EncodedKeySpec);
            System.out.println(publicKey2);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(ECDSA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ECDSA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ECDSA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(ECDSA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
