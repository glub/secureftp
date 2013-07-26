//******************************************************************************
//*
//* (c) Copyright 2003, Glub Tech, Incorporated. All rights reserved.
//*
//* $Id: SimpleProgressFTP.java 37 2009-05-11 22:46:15Z gary $
//*
//******************************************************************************

import com.glub.secureftp.bean.*;
import java.io.*;
import java.util.*;
import java.text.*;

public class SimpleProgressFTP {

  public static void main( String[] args ) {
    String host = null;
    String user = null;
    String pass = null;
    String dir  = null;

    if ( args.length < 4 ) {
      host = "ftp.adobe.com";
      user = "anonymous";
      pass = "guest@";
      dir  = "/pub/adobe/acrobat/win/5.x";
    }
    else {
      host = args[0];
      user = args[1];
      pass = args[2];
      dir  = args[3];
    }

    SimpleProgressFTP ftp = new SimpleProgressFTP(host, user, pass, dir);
  }

  public SimpleProgressFTP( String host, String user, String pass,
                            String dir ) {

    FTP client = new FTP( host, 21 );

    try {
      client.connect();
      client.login( user, pass );

      client.setConnectionType( FTP.PASV_CONNECTION_TYPE );

      client.binary();

      client.chdir( dir );
      System.out.println( "Working directory = " + client.pwd() );

      RemoteFileList rfl = client.list();
      System.out.println( "Files in this directory:" );
      for (int i = 0; i < rfl.size(); i++ ) {
        System.out.println("  " + rfl.getFile(i).getFileName());
      }

      ProgressThread pt = null;
      pt = new ProgressThread();
      pt.start();
 
      try {
        System.out.println( "Downloading " +  rfl.getFile(0).getFileName() );
        client.retrieve( rfl.getFile(0).getFileName(), 
                         new File("downloaded.file"), false, pt );
      }
      finally {
        if ( pt != null && pt.isRunning() ) {
          pt.finishProgress();
        }
      }

      client.logout();
    }
    catch ( Exception e ) {
      System.err.println("An error occured: " + e.getMessage());
    }
  }

}

class ProgressThread extends Thread implements Progress {
  private boolean stop = false;
  private int index;
  private String progress = "";
  private long total = 0;
  private int indeterminateState = -1;

  public void run() {
    while (!stop) {
      try {
        System.out.print("     \b\r" + progress);
        System.out.flush();
        this.sleep(5);
      }
      catch (InterruptedException ie) {}
    }
  }

  public boolean isRunning() { return !stop; }

  public void startProgress() {}

  public void finishProgress() { 
    stop = true; 
    System.out.println("     \b\r" + total + 
                       " of " + total + " bytes transferred." );
  }

  public void updateProgress( long pos, long total ) {
    StringBuffer buf = new StringBuffer();

    buf.append(pos);
    if ( indeterminateState < 0 ) {
      buf.append(" of ");
      buf.append(total);
    }

    buf.append(" bytes transferred." );

    progress = buf.toString();
    this.total = total;
  }
}
