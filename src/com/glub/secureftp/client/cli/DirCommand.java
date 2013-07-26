
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: DirCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.util.*;

public class DirCommand extends NetworkCommand {
  public DirCommand() {
    super("dir", CommandID.DIR_COMMAND_ID, 0, 9999, 
          "[remote-file ...]", 
          "list contents of remote directory");
    setBeepWhenDone( false );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();
    LsCommand lsCommand = new LsCommand();

    ArrayList args = getArgs();
    if ( args != null && args.size() > 0 ) {
      args.add(0, "-l");
    }
    else {
      args = new ArrayList(1);
      args.add("-l");
    }
    lsCommand.setArgs(args);

    SecureFTP.getCommandDispatcher().fireCommand(this, lsCommand);

    return result;
  }
}

