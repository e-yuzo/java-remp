/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import com.cps.security.key.KeyGenerator;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

/**
 *
 * @author yuzo
 */
public class Cryptography {
    
    public static void main(String[] args) {
        KeyGenerator key = new KeyGenerator(2048);
        AsymmetricCipherKeyPair keyPair = key.generateRSAKeyPair();
        System.out.println("hey:"+keyPair.getPrivate());
    }
    
}
