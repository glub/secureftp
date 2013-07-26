
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LogStream.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import java.io.*;

public class LogStream extends FileOutputStream {
 
  public LogStream( FileDescriptor fd ) throws FileNotFoundException {
    super( fd );
  }

  public LogStream( File file ) throws FileNotFoundException {
    super( file );
  }
 
  public void write( byte[] b ) throws IOException {
    super.write( b );
    System.out.write( b );
  }

  public void write( byte[] b, int off, int len ) throws IOException {
    super.write( b, off, len );
    System.out.write( b, off, len );
  }

  public void write( int b ) throws IOException {
    super.write( b );
    System.out.write( b );
  }

}
