
package utils;

/**
 *
 * @author yuzo
 */
public class ProtocolStructureLength {
    
    //length of message structure sent from publisher to broker
    public int message;
    public int rn = 15;
    public int group = 16;
    public int word = 1;
    public int xk;
    public int ticket = 48;
    public int identity = 15;
    public int ai = 16;
    public int encryptedWithAI = 32;
    public int hash = 16;
    public int encryptedMk;
    //just round the value to the closest multiple of 16 value
    //in order to find the size of the encrypted message
    // LENGTH - 116 or 118
    public int signed = 55; //signed data varies slightly 54 - 56
    public int dataY;
    
    public ProtocolStructureLength(String component, int dataSize) {
        this.message = getMessageKSize(component, dataSize); //this has to be calculated on the broker and subscriber
        this.xk = message + rn + group;
        this.encryptedMk = message;
        this.signed = dataSize - (xk + identity + hash);
    }
    
    public int getMessageKSize(String component, int dataSize) {
        int messageSize = 0;
        if (component.equalsIgnoreCase("broker")) {
            messageSize = dataSize - (ticket + encryptedWithAI + rn + group);
        } else {
                messageSize = dataSize - (identity + hash + signed + rn + group);
                messageSize = roundToMultipleOf16(messageSize);
                System.out.println(messageSize);
        }
        return messageSize;
    }
    
    public int roundToMultipleOf16(int n) {
        return 16*(Math.round(n/16));
    }
    
    private int padThis(int padSize, int length) {
        int pad = (padSize - (length % padSize)) + length;
        return pad;
    }
    
}
