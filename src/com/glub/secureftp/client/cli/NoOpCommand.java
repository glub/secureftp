
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: NoOpCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

public class NoOpCommand extends Command {
  public NoOpCommand() {
    super("", CommandID.NOOP_COMMAND_ID, "");
  }

  public SecureFTPError doIt() throws CommandException { 
    return new SecureFTPError();
  }
}

