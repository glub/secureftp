
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LPWDCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class LPWDCommand extends LocalCommand {
  public LPWDCommand() {
    super("lpwd", CommandID.LPWD_COMMAND_ID,
          "print working directory on local machine");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    out.println("\"" + SecureFTP.getFTPSession().getLocalDir() +
                "\" is the current local directory.");

    return result;
  }
}

