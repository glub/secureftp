
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: PEMConverter.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.common;

import java.io.*;

public class PEMConverter {
  public static void main( String args[] ) throws Exception {
    new PEMConverter( args );
  }

  public PEMConverter( String args[] ) throws Exception { 
    doIt(args); 
  }
  
  private void doIt( String args[] ) throws Exception {
    String inFile = null;
    String outFile = null;

    if ( args.length < 1 ) {
      System.out.println( "usage: PEMConverter <file.pem>" );
      System.exit( 1 );
    }

    try {
      inFile = args[0];
      outFile = args[0];

      if ( !inFile.toLowerCase().endsWith(".pem") ) {
        System.out.println( "The file must end with .pem" );
        System.exit( 1 );
      }
    }
    catch (ArrayIndexOutOfBoundsException aiobe) {
      System.out.println( "The file must end with .pem");
      System.exit( 1 );
    }
    catch ( StringIndexOutOfBoundsException siobe ) {
      System.out.println( "The file must end with .pem" );
      System.exit( 1 );
    }

    File in = new File(inFile);

    outFile = 
      in.getName().substring( 0, in.getName().lastIndexOf(".") ) + ".der";

    PEMInputStream pis = null;
    try {
      pis = new PEMInputStream(new FileInputStream(in));
    }
    catch (FileNotFoundException fnf) {
      System.err.println( fnf.getMessage() );
      System.exit( 1 );
    }

    String curDir = System.getProperty("user.dir");
    File out = new File(curDir, outFile);
    FileOutputStream fos = new FileOutputStream( out );

    System.out.print("Converting from PEM to DER... ");

    do {
      int i = pis.read();

      if (i != -1) {
        fos.write(i);
      } 
      else {
        System.out.println("done.");

        System.out.println("DER file written to: " + out);

        pis.close();
        fos.close();
        return;
      }
    } while (true);
  }
}
