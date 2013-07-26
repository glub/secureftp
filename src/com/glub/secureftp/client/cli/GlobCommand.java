
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: GlobCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class GlobCommand extends LocalCommand {
  public GlobCommand() {
    super("glob", CommandID.GLOB_COMMAND_ID, 
          "toggle metacharacter expansion of local file names");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    session.setGlobOn( !session.isGlobOn() );
    if ( session.isGlobOn() ) {
      out.println("Globbing on.");
    }
    else {
      out.println("Globbing off.");
    }

    return result;
  }
}

