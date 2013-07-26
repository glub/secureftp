
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: StatusCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class StatusCommand extends LocalCommand {
  public StatusCommand() {
    super("status", CommandID.STATUS_COMMAND_ID, "show current status");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    String securityType = "Insecurely ";
    if ( session.isSecure() ) {
      securityType = "Securely ";
    }

    if ( session.isLoggedIn() ) {
      out.println(securityType + "connected to " + 
                         session.getHostName() + 
                         " as " + session.getUserName() + ";");
    }
    else if ( session.isConnected() ) {
      out.println(securityType + "connected to " + 
                         session.getHostName() + "; "); 
    }
    else {
      out.println("Not connected.");   
    }

    if ( session.isSecure() ) {
      SSLFTP ftp = (SSLFTP)session.getFTPBean();

      // print cert information
      out.println("Server's certificate:");
      CLISSLCertificateHandler.printCert( session.getCurrentCertificate() );

      // data encryption
      String dataEncryptionOn = ( ftp.isDataEncryptionOn() ) ? "on;" : "off;";
      out.println("Data encryption: " + dataEncryptionOn);
    }

    FTP ftp = session.getFTPBean();

    if ( ftp != null && session.isConnected()) {
      out.print("Transfer mode: " );
      switch ( ftp.getTransferMode() ) {
        case FTP.ASCII_TRANSFER_MODE:
          out.println("ascii;");
          break;
        case FTP.BINARY_TRANSFER_MODE:
          out.println("binary;");
          break;
        case FTP.AUTO_TRANSFER_MODE:
          out.println("auto;");
          break;
      }
    }

    out.print("Verbose: " + ((session.reportVerbose()) ? "on" : "off") + "; ");
    out.print("Bell: " + ((session.getBeepWhenDone()) ? "on" : "off") + "; ");
    out.print("Prompting: " + ((session.isInteractiveOn()) ? "on" : "off") + 
              "; ");
    out.println("Globbing: " + ((session.isGlobOn()) ? "on" : "off") + "; ");

    out.print("Hash mark printing: " + 
              ((session.showProgress()) ? "on" : "off") + "; ");

    if ( ftp != null && session.isConnected() ) {
      out.println("Connection type: " + 
                  ((FTP.PASV_CONNECTION_TYPE == ftp.getConnectionType()) ? 
                    "passive" : "active") + "; ");
    } 
    else {
      out.println("");
    }

    return result;
  }
}

