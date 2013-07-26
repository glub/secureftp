
//*****************************************************************************
//*
//* (c) Copyright 2006. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: EncryptPassword.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.util.*;

public class EncryptPassword {
  public static void main( String[] args ) {
    if ( args.length != 1 ) {
      System.out.println("usage: EncryptPassword <plaintext pass>");
    }
    else {
      String output = EncryptionUtils.encryptPassword( args[0] );
      System.out.println("ENCRYPTED:" + output);
    }
  }
}
