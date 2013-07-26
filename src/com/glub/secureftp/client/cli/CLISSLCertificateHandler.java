
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CLISSLCertificateHandler.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class CLISSLCertificateHandler extends SSLCertificateHandler
	                              implements SSLSessionManager {

  public void setCurrentCertificate( SSLCertificate cert ) {
    FTPSession session = SecureFTP.getFTPSession();
    session.setCurrentCertificate( cert ); 
  }

  public short newCertificateEncountered( SSLCertificate cert ) {
    short status = DENY_CERTIFICATE;

    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    out.println("New certifcate encountered:");

    printCert( cert );

    String msg = "Do you want to trust this certificate?";
    short result = CLIUtil.YNA_YES;

    if ( SecureFTP.scripted || SecureFTP.securityDisabled ) {
      out.println("Trusting the certificate.");
    }
    else {
      result = CLIUtil.yesNoAlwaysPrompt( msg, CLIUtil.YNA_YES );
    }

    switch ( result ) {
      case CLIUtil.YNA_YES:
        status = ALLOW_CERTIFICATE;
        break;

      case CLIUtil.YNA_ALWAYS:
        status = SAVE_CERTIFICATE;
        break;

      case CLIUtil.YNA_NO:
      default:
        status = DENY_CERTIFICATE;
        break;
    }

    return status;
  }

  public short replaceCertificate( SSLCertificate oldCert, 
                                   SSLCertificate newCert ) {
    short status = DENY_CERTIFICATE;

    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    out.println("This certificate already exists:");

    printCert( oldCert );

    out.println("This is the new certificate:");

    String msg = "Do you want to replace the old certificate?";
    short result = CLIUtil.YNA_YES;

    if ( SecureFTP.scripted || SecureFTP.securityDisabled ) {
      out.println("Replacing the certificate.");
    }
    else {
      result = CLIUtil.yesNoAlwaysPrompt( msg, CLIUtil.YNA_YES );
    }

    switch ( result ) {
      case CLIUtil.YNA_YES:
        status = ALLOW_CERTIFICATE;
        break;

      case CLIUtil.YNA_ALWAYS:
        status = SAVE_CERTIFICATE;
        break;

      case CLIUtil.YNA_NO:
      default:
        status = DENY_CERTIFICATE;
        break;
    }

    return status;
  }

  public boolean continueWithoutServerCertificate() {
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();
    String msg = "The server did not send certificate.";
    boolean status = true;
    if ( SecureFTP.scripted || SecureFTP.securityDisabled ) {
      out.println(msg + " Continue? <-- " + status);
    }
    else {
      status = CLIUtil.yesNoPrompt(msg + " Continue?");
    }
    return status;
  }

  public boolean continueWithExpiredCertificate( SSLCertificate cert ) {
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();
    String msg = "The server certificate has expired.";
    boolean status = SecureFTP.securityDisabled || CommandParser.ignoreErrors;
    if ( SecureFTP.scripted || status ) {
      out.println(msg + " Continue? <-- " + status);
    }
    else {
      status = CLIUtil.yesNoPrompt(msg + " Continue?");
    }
    return status;
  }

  public boolean continueWithInvalidCertificate( SSLCertificate cert ) {
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();
    String msg = "The server certificate is not yet valid.";
    boolean status = SecureFTP.securityDisabled || CommandParser.ignoreErrors;
    if ( SecureFTP.scripted || status ) {
      out.println(msg + " Continue? <-- " + status);
    }
    else {
      status = CLIUtil.yesNoPrompt(msg + " Continue?");
    }
    return status;
  }

  public boolean continueWithCertificateHostMismatch( SSLCertificate cert,
                                                      String actualHost,
                                                      String certHost ) {
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();
    out.println("host mismatch: " + cert.getCN());
    String msg = "The host mentioned in the certificate does not match" +
                 System.getProperty("line.separator") +
                 "the host you are connected to.";
    boolean status = SecureFTP.securityDisabled || CommandParser.ignoreErrors;
    if ( SecureFTP.scripted || status ) {
      out.println(msg + " Continue? <-- " + status);
    }
    else {
      status = CLIUtil.yesNoPrompt(msg + " Continue?");
    }
    return status;
  }

  public void randomSeedIsGenerating() {
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();
    out.print("Generating the random seed... ");
  }

  public void randomSeedGenerated() {
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();
    out.println("done.");
  }

  public static void printCert( SSLCertificate cert ) {
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();
    if ( cert == null ) {
      out.println("No certificate present.");
      return;
    }

    out.print("  Issued by: ");

    StringBuffer issueBuf = new StringBuffer();

    if ( cert.getIssuerOrg() != null && cert.getIssuerOrg().length() > 0 ) {
      issueBuf.append(cert.getIssuerOrg());
      issueBuf.append(", ");
    }

    if ( cert.getIssuerOU() != null && cert.getIssuerOU().length() > 0 ) {
      issueBuf.append(cert.getIssuerOU());
      issueBuf.append(", ");
    }

    if ( cert.getIssuerCN() != null && cert.getIssuerCN().length() > 0 ) {
      issueBuf.append(cert.getIssuerCN());
    }

    String issueStr = issueBuf.toString().trim();
    issueBuf.delete(0, issueBuf.capacity());

    if ( issueStr.endsWith(",") ) {
      issueStr = issueStr.substring(0, issueStr.length() - 1);
    }

    out.println(issueStr);

    out.print("  Issued to: ");


    if ( cert.getOrg() != null && cert.getOrg().length() > 0 ) {
      issueBuf.append(cert.getOrg());
      issueBuf.append(", ");
    }

    if ( cert.getOU() != null && cert.getOU().length() > 0 ) {
      issueBuf.append(cert.getOU());
      issueBuf.append(", ");
    }

    if ( cert.getCN() != null && cert.getCN().length() > 0 ) {
      issueBuf.append(cert.getCN());
    }

    issueStr = issueBuf.toString().trim();

    if ( issueStr.endsWith(",") ) {
      issueStr = issueStr.substring(0, issueStr.length() - 1);
    }

    out.println(issueStr);
  }
}
