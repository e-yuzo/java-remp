/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cps.security.key;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;

/**
 *
 * @author yuzo
 */
public class KeyGenerator {

    private int size;
    
    public KeyGenerator(int size) {
        this.size = size;
    }

    public AsymmetricCipherKeyPair generateRSAKeyPair() {
        AsymmetricCipherKeyPairGenerator keyPairGenerated = new RSAKeyPairGenerator();
        keyPairGenerated.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x11), new SecureRandom(), 2048, 12));
        return keyPairGenerated.generateKeyPair();
    }
}
