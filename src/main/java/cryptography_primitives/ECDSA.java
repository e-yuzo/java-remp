
package cryptography_primitives;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

public class ECDSA {

    public byte[] sign(byte[] message, byte[] prKey) {

        try {
            KeyFactory ecKeyFac = KeyFactory.getInstance("EC", "BC");
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(prKey);
            PrivateKey key = ecKeyFac.generatePrivate(pkcs8EncodedKeySpec);
            Signature ecdsa = Signature.getInstance("ECDSA", "BC");
            ecdsa.initSign(key);
            ecdsa.update(message);
            byte[] signature = ecdsa.sign();
            System.out.println("[ECDSA] Signing.");
            return signature;
        } catch (NoSuchAlgorithmException
                | InvalidKeySpecException
                | NoSuchProviderException
                | InvalidKeyException
                | SignatureException ex) {
            Logger.getLogger(ECDSA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public boolean verify(byte[] message, byte[] signature, byte[] puKey) {
        try {
            KeyFactory ecKeyFac = KeyFactory.getInstance("ECDSA", "BC");
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(puKey);
            PublicKey key = ecKeyFac.generatePublic(x509EncodedKeySpec);
            System.out.println(key);
            Signature ecdsa = Signature.getInstance("ECDSA", "BC");
            ecdsa.initVerify(key);
            ecdsa.update(message);
            boolean check = ecdsa.verify(signature);
            System.out.println("[ECDSA] Verifying: " + check);
            return check;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | InvalidKeySpecException | SignatureException ex) {
            Logger.getLogger(ECDSA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static void main(String[] args) throws Exception{
        Security.addProvider(new BouncyCastleProvider());
        String m = "sign me";
        ECNamedCurveParameterSpec ECCparam = ECNamedCurveTable.getParameterSpec("prime192v2");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
        keyGen.initialize(ECCparam);
        KeyPair keyPair = keyGen.generateKeyPair();
        PublicKey puKey = keyPair.getPublic();
        PrivateKey prKey = keyPair.getPrivate();
        Signature ecdsa = Signature.getInstance("ECDSA", "BC");
        ecdsa.initSign(prKey);
        ecdsa.update(m.getBytes());
        byte[] sig = ecdsa.sign();
        ecdsa.initVerify(puKey);
        m = "sign me";
        ecdsa.update(m.getBytes());
        boolean sigok = ecdsa.verify(sig);
        System.out.println("ECDSA way 1 returned: " + sigok);
    }
    
}
