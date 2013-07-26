
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: AutoCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class AutoCommand extends NetworkCommand {
  public AutoCommand() {
    super("auto", CommandID.AUTO_COMMAND_ID, "set auto transfer type");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();
    session.getFTPBean().auto();
    out.println("Type set to auto.");

    return result;
  }
}

