
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ModeZCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;

public class ModeZCommand extends NetworkCommand {
  public ModeZCommand() {
    super("compress", CommandID.MODEZ_COMMAND_ID,
          "enable mode z compression");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    try {
      SecureFTP.getFTPSession().getFTPBean().modeZ();
    }
    catch ( FTPException fe ) {
      out.println( fe.getMessage() );
    }

    return result;
  }
}

