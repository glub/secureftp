
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: HelpCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import java.awt.event.*;

public class HelpCommand extends LocalCommand {
  public HelpCommand() {
    this("help", CommandID.HELP_COMMAND_ID);
  }

  public HelpCommand( String commandName, short id ) {
    super(commandName, id, "help");
  }

  public SecureFTPError doIt() throws CommandException {
    Client.showHelpViewer((ActionEvent)(getArgs().get(0)));
    return new SecureFTPError();
  }
}

