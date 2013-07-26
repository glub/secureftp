
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: DisconnectCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;

public class DisconnectCommand extends NetworkCommand {
  public DisconnectCommand() {
    this("disconnect", CommandID.DISCONNECT_COMMAND_ID);
  }

  public DisconnectCommand( String commandName, short id ) {
    super(commandName, id, "terminate ftp session");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();
    FTPSession session = SecureFTP.getFTPSession();
    try {
      session.getFTPBean().logout();
      session.clearSession();
    }
    catch ( IOException ioe ) {}
    catch ( FTPException ftpe ) {}

    return result;
  }
}

class CloseCommand extends DisconnectCommand {
  public CloseCommand() {
    super("close", CommandID.CLOSE_COMMAND_ID);
  }
}

