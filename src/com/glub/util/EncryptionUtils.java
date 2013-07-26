
//*****************************************************************************
//*
//* (c) Copyright 2006. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: EncryptionUtils.java 309 2010-12-24 06:24:36Z gary $
//*
//*****************************************************************************

package com.glub.util;

import java.math.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
//import com.sun.crypto.provider.*;

public class EncryptionUtils {
  private static final byte[] desKeyData = {
    (byte)5, (byte)5, (byte)118, (byte)9, (byte)73, (byte)-60, (byte)-60,
    (byte)-113, (byte)-36, (byte)12, (byte)-48, (byte)-8, (byte)-110,
    (byte)123, (byte)-71, (byte)60, (byte)53
  };

  private static Cipher encryptCipher = null;
  private static Cipher decryptCipher = null;

  private final static String[] hexLookupTable = {
    "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0a", "0b",
    "0c", "0d", "0e", "0f", "10", "11", "12", "13", "14", "15", "16", "17",
    "18", "19", "1a", "1b", "1c", "1d", "1e", "1f", "20", "21", "22", "23",
    "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f",
    "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b",
    "3c", "3d", "3e", "3f", "40", "41", "42", "43", "44", "45", "46", "47",
    "48", "49", "4a", "4b", "4c", "4d", "4e", "4f", "50", "51", "52", "53",
    "54", "55", "56", "57", "58", "59", "5a", "5b", "5c", "5d", "5e", "5f",
    "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6a", "6b",
    "6c", "6d", "6e", "6f", "70", "71", "72", "73", "74", "75", "76", "77",
    "78", "79", "7a", "7b", "7c", "7d", "7e", "7f", "80", "81", "82", "83",
    "84", "85", "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f",
    "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9a", "9b",
    "9c", "9d", "9e", "9f", "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
    "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af", "b0", "b1", "b2", "b3",
    "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd", "be", "bf",
    "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb",
    "cc", "cd", "ce", "cf", "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7",
    "d8", "d9", "da", "db", "dc", "dd", "de", "df", "e0", "e1", "e2", "e3",
    "e4", "e5", "e6", "e7", "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef",
    "f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "fa", "fb",
    "fc", "fd", "fe", "ff"
  };

  private static final String[] charTable = {
    "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
    "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3",
    "4", "5", "6", "7", "8", "9"
  };

  protected String randomChar = charTable[(int)(Math.random()*charTable.length)];

  protected static int initCrypto = initCrypto();

  public static String encryptPassword( String plainPass ) {
    StringBuffer hexData = new StringBuffer();

    try {
      String randomChar = charTable[(int)(Math.random()*charTable.length)];
      String encryptString = randomChar + plainPass + randomChar;
      byte[] buffer = encryptCipher.doFinal( encryptString.getBytes() );
      int readBytes = buffer.length;
      for ( int i = 0; i < readBytes; i++ ) {
        hexData.append( hexLookupTable[0xff & buffer[i]] );
        //System.out.print(buffer[i] + ", ");
      }
    }
    catch ( Exception e ) {}

    //System.out.println("");

    return hexData.toString();
  }

  public static String decryptPassword( String encryptedPassword ) {
    String result = null;

    if ( encryptedPassword != null && encryptedPassword.trim().length() > 0 ) {
      BigInteger bi = new BigInteger( encryptedPassword, 16 );
      byte[] paddedBuffer = bi.toByteArray();

      int bufSize = paddedBuffer.length;

      if ( paddedBuffer[0] == (byte)0 ) {
        bufSize--;
      }

      byte[] buffer = new byte[ bufSize ];

      int bufIndex = buffer.length - 1;
      for ( int i = paddedBuffer.length - 1; bufIndex >= 0; i-- ) {
        buffer[ bufIndex-- ] = paddedBuffer[ i ];
      }

      /*
      for ( int i = 0; i < buffer.length; i++ ) {
        System.out.print( buffer[i] + ", ");
      }

      System.out.println("");
      */

      try {
        String decryptString = new String( decryptCipher.doFinal(buffer) );
        //String randomChar = decryptString.substring( 0, 1 );
        result = decryptString.substring( 1, decryptString.length() - 1 );
      }
      catch ( Exception e ) {}
    }

    return result;
  }

  private static int initCrypto() {
    try {
      //Security.addProvider( new SunJCE() );
      DESKeySpec desKeySpec = new DESKeySpec( desKeyData );
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance( "DES" );
      SecretKey secretKey = keyFactory.generateSecret( desKeySpec );
      encryptCipher = Cipher.getInstance( "DES/ECB/PKCS5Padding" );
      encryptCipher.init( Cipher.ENCRYPT_MODE, secretKey );
      decryptCipher = Cipher.getInstance( "DES/ECB/PKCS5Padding" );
      decryptCipher.init( Cipher.DECRYPT_MODE, secretKey );
    }
    catch ( Exception e ) {}

    return 1;
  }
}
