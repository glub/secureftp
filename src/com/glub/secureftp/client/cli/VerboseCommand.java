
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: VerboseCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class VerboseCommand extends LocalCommand {
  public VerboseCommand() {
    super("verbose", CommandID.VERBOSE_COMMAND_ID, "toggle verbose mode");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    session.setReportVerbose( !session.reportVerbose() );
    if ( session.reportVerbose() ) {
      out.println("Verbose mode on.");
    }
    else {
      out.println("Verbose mode off.");
    }

    return result;
  }
}

