
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RefreshUICommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

public class RefreshUICommand extends LocalCommand {
  public RefreshUICommand() {
    this("refresh", CommandID.REFRESH_UI_COMMAND_ID);
  }

  public RefreshUICommand( String commandName, short id ) {
    super(commandName, id, "refresh ui");
  }

  public SecureFTPError doIt() throws CommandException {
    return new SecureFTPError();
  }
}
