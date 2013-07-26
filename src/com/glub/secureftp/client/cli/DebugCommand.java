
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: DebugCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class DebugCommand extends LocalCommand {
  public DebugCommand() {
    super("debug", CommandID.DEBUG_COMMAND_ID, "toggle/set debugging mode");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    session.setDebugOn( !session.isDebugOn() );

    System.setProperty( "glub.debug", "" + session.isDebugOn() );

    if ( session.isDebugOn() ) {
      out.println("Debugging on.");
    }
    else {
      out.println("Debugging off.");
    }

    return result;
  }
}

