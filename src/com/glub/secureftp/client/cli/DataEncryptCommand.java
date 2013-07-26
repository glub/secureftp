
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: DataEncryptCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class DataEncryptCommand extends NetworkCommand {
  public DataEncryptCommand() {
    super("dataencrypt", CommandID.DATAENCRYPT_COMMAND_ID, 
          "toggle data encryption");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    if ( !session.isSecure() ) {
      out.println("This is not a secure connection.");
      throw new CommandException("This is not a secure connection.");
    }
    else {
      SSLFTP ftp = (SSLFTP)session.getFTPBean();
      try {
        ftp.setDataEncryptionOn( !ftp.isDataEncryptionOn() );
        String dataEncryptionOn = ( ftp.isDataEncryptionOn() ) ? "on." : "off.";
        out.println("Data encryption " + dataEncryptionOn);
      }
      catch ( FTPException fe ) {}
    }

    return result;
  }
}

