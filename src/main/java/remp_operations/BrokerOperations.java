
package remp_operations;

import cryptography_primitives.AES;
import cryptography_primitives.ECDSA;
import java.io.UnsupportedEncodingException;
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
public class BrokerOperations {
    
    public byte[] pr; //128 bits
    public byte[] pu; //128 bits
    public byte[] tk;
    
    //extracted i and ai values from Ticket Ti
    public byte[] i; //identity of publisher i
    public byte[] ai; //auth key of publisher i
    public byte[] word;
    
    //extracted message encrypted with ai
    public byte[] identity;
    public byte[] hashedXk;
    
    public static ProtocolStructureLength protocol;
    
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        //publisher
        String messageK = "lucid animal lucid animalsa non sznonwoas";
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
        BrokerOperations bo = new BrokerOperations();
        protocol = new ProtocolStructureLength("broker", dataX.length);
        byte[] ticket = new byte[protocol.ticket];
        System.arraycopy(dataX, encryptedMessageK.length + protocol.encryptedWithAI, ticket, 0, protocol.ticket);
        bo.decryptTicket(ticket, bo.tk);
        System.out.println("STATE OF IDENTITY (TICKET): " + Arrays.toString(bo.i));
        byte[] encryptedByAI = new byte[protocol.encryptedWithAI];
        System.arraycopy(dataX, encryptedMessageK.length, encryptedByAI, 0, protocol.encryptedWithAI);
        bo.decryptEncryptedDataByAuthKey(encryptedByAI);
        byte[] dataY = bo.createDataY(encryptedMessageK);
        System.out.println(Arrays.toString(dataY));
    }
    
    public BrokerOperations() {
        AuthenticatedStateConstants asc = new AuthenticatedStateConstants("broker");
        this.pr = asc.pr;
        this.pu = asc.pu;
        this.tk = asc.tk;
    }
    
    public void decryptTicket(byte[] ticket, byte[] tk) {
        try {
            this.i = new byte[protocol.identity];
            this.ai = new byte[protocol.ai];
            this.word = new byte[1]; //W or R
            AES aes = new AES();
            byte[] iv = "123456789abcdefg".getBytes("US-ASCII");
            byte[] decrypted = aes.decryptData(ticket, tk, iv);
            System.arraycopy(decrypted, 0, i, 0, i.length);
            System.arraycopy(decrypted, i.length, ai, 0, ai.length);
            System.arraycopy(decrypted, i.length + ai.length, word, 0, word.length);
            System.out.println(Arrays.toString(ai));
        } catch (Exception ex) {
            Logger.getLogger(BrokerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void decryptEncryptedDataByAuthKey(byte[] encrypted) { try {
        byte[] iv = "123456789abcdefg".getBytes("US-ASCII");
        AES aes = new AES();
        byte[] decryptedData = aes.decryptData(encrypted, ai, iv);
        //extract identity
        byte[] id = new byte[protocol.identity];
        byte[] hash = new byte[protocol.hash];
        System.arraycopy(decryptedData, 0, id, 0, protocol.identity);
        System.arraycopy(decryptedData, protocol.identity, hash, 0, protocol.hash);
        System.out.println("STATE OF IDENTITY (ENCRYPTED_BY_AI): " + Arrays.toString(id));
        this.hashedXk = hash;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BrokerOperations.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(BrokerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean compareIdentities(byte[] identityTicket, byte[] identityAI) {
        return Arrays.equals(identityTicket, identityAI);
    }
    
    public byte[] createDataY(byte[] Xk) {
        ECDSA eccSign = new ECDSA();
        byte[] toSign = ByteUtils.combine(i, hashedXk);
        System.out.println("Signed data size: " + toSign.length);
        byte[] sign = eccSign.sign(toSign, pr);
        System.out.println("Signed size: " + sign.length);
        byte[] dataY = ByteUtils.combine(Xk,
                       ByteUtils.combine(i,
                       ByteUtils.combine(hashedXk, sign)));
        return dataY;
    }
}
