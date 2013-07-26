
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: GUISSLCertificateHandler.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.util.*;
import javax.swing.*;

public class GUISSLCertificateHandler extends SSLCertificateHandler
	                              implements SSLSessionManager {

  public void setCurrentCertificate( SSLCertificate cert ) {
    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
    if ( null != session ) {
      session.setCurrentCertificate( cert ); 
    }
    else {
      showWarningDialog(LString.getString("SSLCertificate.null_cert", 
                                 "The certificate is null. Continue anyway?"));
    }
  }

  public short newCertificateEncountered( SSLCertificate cert ) {
    short status = DENY_CERTIFICATE;

    SSLCertificateCommand certCommand = new SSLCertificateCommand();
    ArrayList args = new ArrayList(3);
    args.add( cert );
    args.add( new Short(SSLCertificateCommand.NEW_CERTIFICATE) );
    certCommand.setArgs( args );

    SecureFTPError result = 
      SecureFTP.getCommandDispatcher().fireCommand( this, certCommand );

    if ( result.getCode() == SecureFTPError.ACCEPT_CERTIFICATE_ALWAYS ) {
      status = SAVE_CERTIFICATE;
    }
    else if ( result.getCode() == SecureFTPError.ACCEPT_CERTIFICATE_ONCE ) {
      status = ALLOW_CERTIFICATE;
    }
    else {
      status = DENY_CERTIFICATE;
    }

    return status;
  }

  public short replaceCertificate( SSLCertificate oldCert, 
                                   SSLCertificate newCert ) {
    short status = DENY_CERTIFICATE;

    SSLCertificateCommand certCommand = new SSLCertificateCommand();
    ArrayList args = new ArrayList(3);
    args.add( newCert );
    args.add( new Short(SSLCertificateCommand.REPLACE_CERTIFICATE) );
    certCommand.setArgs( args );

    SecureFTPError result = 
      SecureFTP.getCommandDispatcher().fireCommand( this, certCommand );

    if ( result.getCode() == SecureFTPError.ACCEPT_CERTIFICATE_ALWAYS ) {
      status = SAVE_CERTIFICATE;
    }
    else if ( result.getCode() == SecureFTPError.ACCEPT_CERTIFICATE_ONCE ) {
      status = ALLOW_CERTIFICATE;
    }
    else {
      status = DENY_CERTIFICATE;
    }

    return status;
  }

  public boolean continueWithoutServerCertificate() {
    String msg = "The server did not send certificate. Continue anyway?";
    boolean status = 
      showWarningDialog(LString.getString("SSLCertificate.no_cert", msg));
    return status;
  }

  public boolean continueWithExpiredCertificate( SSLCertificate cert ) {
    String msg = "The server certificate has expired. Continue anyway?";
    boolean status = 
      showWarningDialog(LString.getString("SSLCertificate.expired_cert", msg));
    return status;
  }

  public boolean continueWithInvalidCertificate( SSLCertificate cert ) {
    String msg = "The server certificate is not yet valid. Continue anyway?";
    boolean status = 
      showWarningDialog(LString.getString("SSLCertificate.invalid_cert", msg));
    return status;
  }

  public boolean continueWithCertificateHostMismatch( SSLCertificate cert,
                                                      String actualHost,
                                                      String certHost ) {
    String msg = "The host mentioned in the certificate does not match " +
                 "the host you are connected to. Continue anyway?";
    boolean status = 
      showWarningDialog(LString.getString("SSLCertificate.host_mismatch", msg));
    return status;
  }

  public void randomSeedIsGenerating() {
    //System.out.print("Generating the random seed... ");
  }

  public void randomSeedGenerated() {
    //System.out.println("done.");
  }

  private boolean showWarningDialog( String msg ) {
    boolean result = false;

    String title = LString.getString( "SSLCertificate.warning.title",
                                      "Certificate Warning" );
    int r = JOptionPane.showConfirmDialog( SecureFTP.getBaseFrame(),
                                           msg,
                                           title,
                                           JOptionPane.OK_CANCEL_OPTION );

    if ( r == JOptionPane.OK_OPTION ) {
      result = true; 
    }

    return result;
  }
}
