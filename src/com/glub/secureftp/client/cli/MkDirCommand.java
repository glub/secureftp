
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: MkDirCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

public class MkDirCommand extends NetworkCommand {
  public MkDirCommand() {
    super("mkdir", CommandID.MKDIR_COMMAND_ID, 1, 1, "directory-name",
          "make directory on the remote machine");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();
    FTPSession session = SecureFTP.getFTPSession();
    try {
      session.getFTPBean().mkdir( (String)getArgs().get(0) );
    }
    catch ( IllegalArgumentException iae ) {
      throw new CommandException( getUsage() );
    }
    catch ( FTPException fe ) {
      System.out.println(fe.getMessage());
      result.setCode( SecureFTPError.MKDIR_FAILED );
    }

    return result;
  }
}

