
package utils;

import cryptography_primitives.AES;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author yuzo
 */
public final class AuthenticatedStateConstants {
    
    public byte[] pi; //encrypt i with pm
    public byte[] ai;
    public byte[] tk; //permanent
    public byte[] i;
    public byte[] T;
    public byte[] pm; //permanent
    public byte[] g;
    public byte[] pr;
    public byte[] pu;
    public KeyPair ecdsaKeys;
    
    public AuthenticatedStateConstants(String component) {
        try {
            this.i = generateBytes(15); //15 bytes generates 16 bytes ciphertext, used as a key later on
            this.g = Hex.decode("202122232425262728292a7b7c7d7e7f");
            this.tk = Hex.decode("303132333435363738393a34b4c43e4f");
            this.pm = Hex.decode("202122232425262728292a2b2c2d2e2f"); //permanent
            this.pi = generatePI();
            this.ai = generateBytes(16);
            this.T = new AuthUtils().generateTicket(tk, component, i, ai);
            this.pu = new byte[] {48, 73, 48, 19, 6, 7, 42, -122, 72, -50, 61, 2, 1, 6, 8,
                       42, -122, 72, -50, 61, 3, 1, 2, 3, 50, 0, 4, -106, 103,
                       -112, 32, 101, 78, 10, -56, 112, 89, 108, 1, 57, 74, 68, 96,
                       -124, 113, -83, -60, -70, -75, -66, 52, -122, -107, 29, 29, 79,
                       -71, -35, 86, -128, -89, -35, 99, -125, 68, 84, 108, 118, -48,
                       104, 0, -55, -59, -116, 104};
            this.pr = new byte[] {48, 123, 2, 1, 0, 48, 19, 6, 7, 42, -122, 72, -50, 61, 2,
                       1, 6, 8, 42, -122, 72, -50, 61, 3, 1, 2, 4, 97, 48, 95, 2,
                       1, 1, 4, 24, -106, -32, -58, 41, -63, 105, -86, -14, 1, 88,
                       48, 117, -101, 127, 91, 88, -45, -61, 82, 74, -24, -72, 125,
                       -39, -96, 10, 6, 8, 42, -122, 72, -50, 61, 3, 1, 2, -95, 52,
                       3, 50, 0, 4, -106, 103, -112, 32, 101, 78, 10, -56, 112, 89,
                       108, 1, 57, 74, 68, 96, -124, 113, -83, -60, -70, -75, -66,
                       52, -122, -107, 29, 29, 79, -71, -35, 86, -128, -89, -35, 99,
                       -125, 68, 84, 108, 118, -48, 104, 0, -55, -59, -116, 104};
        } catch (Exception ex) {
            Logger.getLogger(AuthenticatedStateConstants.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public byte[] generatePI() {
        try {
            byte[] iv = "123456789abcdefg".getBytes("US-ASCII");
            byte[] pi = new AES().encryptData(this.i, this.pm, iv);
            return pi;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AuthenticatedStateConstants.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AuthenticatedStateConstants.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public KeyPair generateKeyPairECDSA() {
        try {
            ECNamedCurveParameterSpec ECCparam = ECNamedCurveTable.getParameterSpec("prime192v2");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            keyGen.initialize(ECCparam);
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey puKey = keyPair.getPublic();
            PrivateKey prKey = keyPair.getPrivate();
            
            //byte[] puke = puKey.getEncoded();
            
            
            return keyPair;
        } catch (NoSuchAlgorithmException 
                | NoSuchProviderException 
                | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(AuthenticatedStateConstants.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public byte[] generateBytes(int len) {
        SecureRandom sr = new SecureRandom();
        byte[] bytes = new byte[len];
        sr.nextBytes(bytes);
        return bytes;
    }
    
}
