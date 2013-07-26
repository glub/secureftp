
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: BellCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class BellCommand extends LocalCommand {
  public BellCommand() {
    super("bell", CommandID.BELL_COMMAND_ID, "beep when command completed");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();
    session.setBeepWhenDone( !session.getBeepWhenDone() );
    if ( session.getBeepWhenDone() ) {
      out.println("Bell mode on.");
    }
    else {
      out.println("Bell mode off.");
    }

    return result;
  }
}

