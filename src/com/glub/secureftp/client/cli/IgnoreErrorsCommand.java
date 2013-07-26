
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: IgnoreErrorsCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class IgnoreErrorsCommand extends LocalCommand {
  public IgnoreErrorsCommand() {
    super("ignoreerrors", CommandID.IGNORE_ERRORS_COMMAND_ID, "toggle error handling from the scripted environment.");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    if ( SecureFTP.scripted ) {
      CommandParser.ignoreErrors = !CommandParser.ignoreErrors;
      if ( CommandParser.ignoreErrors ) {
        out.println("Ignoring errors: on");
      }
      else {
        out.println("Ignoring errors: off");
      }
    }
    else {
      out.println("This command is only available while running a script.");
    }

    return result;
  }
}

