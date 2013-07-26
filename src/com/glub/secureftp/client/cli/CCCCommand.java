
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CCCCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;

public class CCCCommand extends NetworkCommand {
  public CCCCommand() {
    super("ccc", CommandID.CCC_COMMAND_ID,
          "switch to a clear command channel (explicit-ssl only)");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    try {
      if ( FTPSession.EXPLICIT_SSL == 
           SecureFTP.getFTPSession().getSecurityMode() ) {
        ((SSLFTP)SecureFTP.getFTPSession().getFTPBean()).setClearCommandChannel();
        SecureFTP.getFTPSession().setUseCCC( true );
      }
      else {
        out.println( "This command is only available during an explicit SSL connection." );
      }
    }
    catch ( FTPException fe ) {
      out.println( fe.getMessage() );
    }
    catch ( IOException ioe ) {
      out.println( ioe.getMessage() );
    }

    return result;
  }
}

