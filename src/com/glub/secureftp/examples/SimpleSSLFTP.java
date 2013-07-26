//******************************************************************************
//*
//* (c) Copyright 2002, Glub Tech, Incorporated. All rights reserved.
//*
//* $Id: SimpleSSLFTP.java 37 2009-05-11 22:46:15Z gary $
//*
//******************************************************************************

import com.glub.secureftp.bean.*;
import java.io.File;

public class SimpleSSLFTP implements SSLSessionManager {
  private SSLCertificate currentCert = null;

  public static void main( String[] args ) {
    // seed as early as possible.
    SSLFTP.preSeed();

    String host = null;
    String user = null;
    String pass = null;

    if ( args.length < 3 ) {
      host = "localhost";
      user = "anonymous";
      pass = "guest@";
    }
    else {
      host = args[0];
      user = args[1];
      pass = args[2];
    }

    SimpleSSLFTP ftp = new SimpleSSLFTP(host, user, pass);
  }

  /**
   * This example will make an implicit SSL connection to a secure FTP
   * server, login, set the connection type to active, do a dir list, 
   * enable data encryption, change dir to /etc, set the connection type to 
   * passive, do another dir list, set the transfer mode to ascii, get
   * the file "motd", and then logout.
   */
  public SimpleSSLFTP( String host, String user, String pass ) {
    SSLFTP sslFTP = new SSLFTP( this, host, 990, SSLFTP.IMPLICIT_CONNECTION,
				System.out, System.out );
    try {
      sslFTP.connect();
      sslFTP.login( user, pass, null );

      sslFTP.setConnectionType( FTP.ACTIVE_CONNECTION_TYPE );

      RemoteFileList rfl = sslFTP.list();
      for (int i = 0; i < rfl.size(); i++ ) {
        System.out.println(rfl.getFile(i).getFileName());
      }

      sslFTP.setDataEncryptionOn( true );

      sslFTP.chdir( "/etc" );

      sslFTP.setConnectionType( FTP.PASV_CONNECTION_TYPE );

      rfl = sslFTP.list();
      for (int i = 0; i < rfl.size(); i++ ) {
        System.out.println(rfl.getFile(i).getFileName());
      }

      sslFTP.ascii();
      sslFTP.retrieve( "motd", new File("motd"), false );
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
