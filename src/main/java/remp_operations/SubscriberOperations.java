
package remp_operations;

import cryptography_primitives.AES;
import cryptography_primitives.ECDSA;
import cryptography_primitives.KeyDerivation;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import utils.AuthenticatedStateConstants;
import utils.ByteUtils;
import utils.ProtocolStructureLength;

/**
 *
 * @author yuzo
 */
public class SubscriberOperations {

    public ProtocolStructureLength protocol;
    
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        
        //publisher
        String messageK = "lucid animal";
        PublisherOperations po = new PublisherOperations();
        System.out.println("AI from PUBLISHER: " + Arrays.toString(po.ai));
        System.out.println(po.ai.length);
        byte[] rn = po.generateRandomNumber();
        byte[] sessionKey = po.deriveSK(rn);
        byte[] encryptedMessageK = po.encryptMessageK(messageK, sessionKey, rn); //important messageK
        System.out.println("XK LENGTH: " + encryptedMessageK.length);
        byte[] dataX = po.createDataX(encryptedMessageK);
        System.out.println("DATAX: " + dataX.length);
        
        //broker
        //decrypting ticket done
        BrokerOperations bo = new BrokerOperations();
        bo.protocol = new ProtocolStructureLength("broker", dataX.length);
        byte[] ticket = new byte[bo.protocol.ticket];
        System.arraycopy(dataX, encryptedMessageK.length + bo.protocol.encryptedWithAI, ticket, 0, bo.protocol.ticket);
        bo.decryptTicket(ticket, bo.tk);
        //decrypting stuff that were encrypted with AI
        System.out.println("STATE OF IDENTITY (TICKET): " + Arrays.toString(bo.i));
        byte[] encryptedByAI = new byte[bo.protocol.encryptedWithAI];
        System.arraycopy(dataX, encryptedMessageK.length, encryptedByAI, 0, bo.protocol.encryptedWithAI);
        bo.decryptEncryptedDataByAuthKey(encryptedByAI);
        //create DATAy
        byte[] dataY = bo.createDataY(encryptedMessageK);
        
        //subscriber
        SubscriberOperations so = new SubscriberOperations();
        so.protocol = new ProtocolStructureLength("subscriber", dataY.length);
        byte[] id = so.extractIdentity(dataY);
        byte[] randomN = so.extracRandomNumber(dataY);
        byte[] sk = so.deriveSk(id, randomN);
        byte[] hash = so.extracHash(dataY);
        byte[] hashIdentity = ByteUtils.combine(id, hash);
        boolean signok = so.verifyData(so.extractSignature(dataY), hashIdentity);
        System.out.println("Verify returned: " + signok);
        System.out.println(so.extractClearMessage(sk, dataY));
    }
    
    private final byte[] pm;
    private final byte[] tj;
    private final byte[] pu;
    private final byte[] aj;
    
    public SubscriberOperations() {
        AuthenticatedStateConstants asc = new AuthenticatedStateConstants("subscriber");
        this.pm = asc.pm;
        this.tj = asc.T;
        this.pu = asc.pu;
        this.aj = asc.ai;
    }
    
    public byte[] extractIdentity(byte[] dataY) {
        int start = protocol.xk;
        byte[] extId = new byte[15];
        System.arraycopy(dataY, start, extId, 0, 15);
        return extId;
    }
    
    public String extractClearMessage(byte[] sk, byte[] dataY) {
        try {
            byte[] encMk = new byte[protocol.encryptedMk];
            System.arraycopy(dataY, 0, encMk, 0, protocol.encryptedMk);
            AES aes = new AES();
            byte[] iv = "123456789abcdefg".getBytes("US-ASCII");
            byte[] decMk = aes.decryptData(encMk, sk, iv);
            return new String(decMk, StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SubscriberOperations.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SubscriberOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public byte[] extracRandomNumber(byte[] dataY) {
        int start = protocol.encryptedMk;
        byte[] extRN = new byte[15];
        System.arraycopy(dataY, start, extRN, 0, 15);
        return extRN;
    }
    
    public byte[] extracHash(byte[] dataY) {
        int start = protocol.xk + protocol.identity;
        byte[] extHash = new byte[16];
        System.arraycopy(dataY, start, extHash, 0, 16);
        return extHash;
    }
    
    public byte[] deriveSk(byte[] identity, byte[] rn) {
        KeyDerivation kd = new KeyDerivation();
        byte[] pi = kd.deriveKey(identity, this.pm);
        byte[] sk = kd.deriveKey(rn, pi);
        return sk;
    }
    
    public byte[] extractSignature(byte[] dataY) {
        int start = protocol.xk + protocol.identity + protocol.hash;
        int signLength = dataY.length - start;
        byte[] extSign = new byte[signLength];
        System.arraycopy(dataY, start, extSign, 0, signLength);
        return extSign;
    }
    
    public boolean verifyData(byte[] signature, byte[] signedStuff) {
        ECDSA ecdsa = new ECDSA();
        System.out.println("SIGNED SIZE: " + signature.length);
        return ecdsa.verify(signedStuff, signature, pu);
    }
    
}
