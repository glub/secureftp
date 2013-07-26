
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CDUpCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class CDUpCommand extends NetworkCommand {
  public CDUpCommand() {
    super("cdup", CommandID.CDUP_COMMAND_ID, 
          "change remote working directory to parent directory");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    try {
      session.getFTPBean().cdup();
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
      result.setCode( SecureFTPError.CD_FAILED );
    }

    return result;
  }
}

