//******************************************************************************
//*
//* (c) Copyright 2002, Glub Tech, Incorporated. All rights reserved.
//*
//* $Id: SimpleSSLFTP2.java 37 2009-05-11 22:46:15Z gary $
//*
//******************************************************************************

import com.glub.secureftp.bean.*;
import java.io.File;

public class SimpleSSLFTP2 implements SSLSessionManager {
  private SSLCertificate currentCert = null;

  public static void main( String[] args ) {
    // seed as early as possible.
    SSLFTP.preSeed();

    String host = "ftps.glub.com";
    String user = "anonymous";
    String pass = "guest@";

    SimpleSSLFTP2 ftp = new SimpleSSLFTP2(host, user, pass);
  }

  /**
   * This example will make an explicit SSL connection to a server running
   * Secure FTP Wrapper login, set the connection type to passive, do a dir 
   * list, enable data encryption, change dir to /pub/test, do another dir 
   * list, set the transfer mode to binary, get the file "test.jpg", 
   * abort the download midway, restart the download, and then logout.
   */
  public SimpleSSLFTP2( String host, String user, String pass ) {
    SSLFTP sslFTP = new SSLFTP( this, host, 21, SSLFTP.EXPLICIT_CONNECTION,
				System.out, System.out );
    try {
      sslFTP.connect();
      sslFTP.login( user, pass, null );

      sslFTP.setConnectionType( FTP.PASV_CONNECTION_TYPE );

      RemoteFileList rfl = sslFTP.list();
      for (int i = 0; i < rfl.size(); i++ ) {
        System.out.println(rfl.getFile(i).getFileName());
      }

      sslFTP.setDataEncryptionOn( true );

      sslFTP.chdir( "/pub/test" );

      rfl = sslFTP.list();
      for (int i = 0; i < rfl.size(); i++ ) {
        System.out.println(rfl.getFile(i).getFileName());
      }

      sslFTP.binary();
      FTPAbortableTransfer abort = new FTPAbortableTransfer();
      String fileName = "test.jpg";
      (new FTPAbortThread(sslFTP, abort)).start();
      try {
        sslFTP.retrieve( fileName, new File(fileName), false, abort );
      }
      catch ( FTPAbortException fae ) {
        System.out.println(fileName + " has been aborted");
      }
      catch ( FTPException fe ) {
        System.out.println("an unknown download exception has occured.");
      }
      sslFTP.retrieve( fileName, new File(fileName), true );
      sslFTP.logout();
    }
    catch ( Exception e ) {
      System.err.println("An error occured: " + e.getMessage());
    }
  }

  public boolean continueWithCertificateHostMismatch( SSLCertificate cert,
		                                      String actualHost,
						      String certHost ) {
    System.out.println("Certificate host mismatch.");
    return true;
  }

  public boolean continueWithExpiredCertificate( SSLCertificate cert ) {
    System.out.println("Certificate expired.");
    return true;
  }

  public boolean continueWithInvalidCertificate( SSLCertificate cert ) {
    System.out.println("Certificate invalid.");
    return true;
  }

  public boolean continueWithoutServerCertificate() {
    System.out.println("Certificate not sent from server.");
    return true;
  }

  public short newCertificateEncountered( SSLCertificate cert ) {
    System.out.println("New cert found.");
    return SSLSessionManager.ALLOW_CERTIFICATE;
  }

  public short replaceCertificate( SSLCertificate oldCert, 
		                   SSLCertificate newCert ) {
    System.out.println("Replace cert.");
    return SSLSessionManager.ALLOW_CERTIFICATE;
  }

  public void randomSeedIsGenerating() {
    System.out.print("The random seed is generating... ");
  }

  public void randomSeedGenerated() {
    System.out.println("done.");
  }

  public void setCurrentCertificate( SSLCertificate currentCert ) {
    this.currentCert = currentCert;
  }
}

class FTPAbortThread extends Thread {
  private FTP ftp = null;
  private FTPAbortableTransfer abortTransfer = null;

  public FTPAbortThread( FTP ftp, FTPAbortableTransfer abortTransfer ) {
    this.ftp = ftp;
    this.abortTransfer = abortTransfer;
  }

  public void run() {
    try {
      sleep(2000);
      System.out.println("aborting xfer");
      ftp.abort( abortTransfer );
    }
    catch ( FTPException fe ) { 
      System.err.println(fe.getMessage()); 
    }
    catch ( Exception e ) { e.printStackTrace(); }
  }
}
