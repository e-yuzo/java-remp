/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cps.security;

/**
 *
 * @author yuzo
 */
public interface IEncrytionOperations {
    
    public byte[] encryption(byte[] data); //add key argument: create another class
    public byte[] decryption(byte[] data);
}