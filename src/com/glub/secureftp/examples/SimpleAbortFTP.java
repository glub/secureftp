//******************************************************************************
//*
//* (c) Copyright 2002, Glub Tech, Incorporated. All rights reserved.
//*
//* $Id: SimpleAbortFTP.java 37 2009-05-11 22:46:15Z gary $
//*
//******************************************************************************

import com.glub.secureftp.bean.*;
import java.io.File;

public class SimpleAbortFTP {

  public static void main( String[] args ) {
    String host = "ftp.globalscape.com";
    String user = "anonymous";
    String pass = "guest@";

    SimpleAbortFTP ftp = new SimpleAbortFTP(host, user, pass);
  }

  /**
   * This example will make an FTP connection to a Cute FTP Secure FTP
   * server, login, set the connection type to passive, 
   * change dir to /pub/cuteftp, set the transfer mode to binary, 
   * get the file "cuteftp.exe", abort the download midway, 
   * restart the download, and then logout.
   */
  public SimpleAbortFTP( String host, String user, String pass ) {
    HostInfo hostInfo = new HostInfo();

    try {
      hostInfo = new HostInfo( host, 21 );
    }
    catch ( java.net.UnknownHostException uhe ) {
      System.out.println(uhe.getMessage());
    }

    FTP ftp = new FTP( hostInfo, System.out, System.out );

    try {
      ftp.connect();
      ftp.login( user, pass, null );

      ftp.setConnectionType( FTP.PASV_CONNECTION_TYPE );

      ftp.chdir( "/pub/cuteftp" );

      ftp.binary();

      FTPAbortableTransfer abort = new FTPAbortableTransfer();
      (new FTPAbortThread(ftp, abort)).start();
      try {
        ftp.retrieve( "cuteftp.exe", new File("cuteftp.exe"), false, abort );
      }
      catch ( FTPAbortException fae ) {
        System.out.println("cuteftp.exe has been aborted");
      }
      catch ( FTPException fe ) {
        System.out.println("an unknown download exception has occured.");
      }

      ftp.retrieve( "cuteftp.exe", new File("cuteftp.exe"), true );
      ftp.logout();
    }
    catch ( Exception e ) {
      System.err.println("An error occured: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
