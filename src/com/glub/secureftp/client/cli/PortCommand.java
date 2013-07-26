
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: PortCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class PortCommand extends NetworkCommand {
  public PortCommand() {
    super("port", CommandID.PORT_COMMAND_ID,
          "use active (PORT) connection type for each data transfer");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    session.getFTPBean().setConnectionType( FTP.ACTIVE_CONNECTION_TYPE );
    out.println("Connection type set to PORT.");

    return result;
  }
}

