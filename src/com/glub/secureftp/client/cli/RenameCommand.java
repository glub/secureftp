
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RenameCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class RenameCommand extends NetworkCommand {
  public RenameCommand() {
    super("rename", CommandID.RENAME_COMMAND_ID, 2, 2, "from to",
          "rename remote-file");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    try {
      session.getFTPBean().rename( (String)getArgs().get(0),
                                   (String)getArgs().get(1) );
    }
    catch ( IllegalArgumentException iae ) {
      throw new CommandException( getUsage() );
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
      result.setCode( SecureFTPError.RENAME_FAILED ); 
    }

    return result;
  }
}

