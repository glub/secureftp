
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ByeCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;

public class ByeCommand extends Command {
  public ByeCommand() {
    this("bye", CommandID.BYE_COMMAND_ID);
  }

  public ByeCommand( String commandName, short id ) {
    super(commandName, id, "terminate ftp session and exit");
  }

  public SecureFTPError doIt() throws CommandException {
    FTPSession session = SecureFTP.getFTPSession();
    if ( session.isConnected() ) {
      try {
        session.getFTPBean().logout();
      }
      catch ( IOException ioe ) {}
      catch ( FTPException ftpe ) { System.out.println(ftpe.getMessage()); }
    }

    return new SecureFTPError();
  }
}

class QuitCommand extends ByeCommand {
  public QuitCommand() {
    super("quit", CommandID.QUIT_COMMAND_ID);
  }
}

