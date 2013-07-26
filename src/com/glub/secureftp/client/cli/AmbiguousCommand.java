
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: AmbiguousCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

public class AmbiguousCommand extends Command {
  public AmbiguousCommand() {
    super("", CommandID.AMBIGUOUS_COMMAND_ID, "");
  }

  public SecureFTPError doIt() throws CommandException { 
    return new SecureFTPError(); 
  }
}

