
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RemoteHelpCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;
import java.util.*;

public class RemoteHelpCommand extends NetworkCommand {
  public RemoteHelpCommand() {
    super("remotehelp", CommandID.REMOTEHELP_COMMAND_ID, 0, 1, 
          "[command-name]", "get help from remote server");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    try {
      ArrayList args = getArgs();
      String helpArg = "";
      if ( args != null && args.size() == 1 ) {
        helpArg = (String)args.get(0);
      }
      //out.println(session.getFTPBean().help(helpArg));
      session.getFTPBean().help(helpArg);
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
    }

    return result;
  }
}

