
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: AsciiCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class AsciiCommand extends NetworkCommand {
  public AsciiCommand() {
    super("ascii", CommandID.ASCII_COMMAND_ID, "set ascii transfer type");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    try {
      session.getFTPBean().ascii();
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
    }

    return result;
  }
}

