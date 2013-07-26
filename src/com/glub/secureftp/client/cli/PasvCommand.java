
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: PasvCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.PrintStream;

public class PasvCommand extends NetworkCommand {
  public PasvCommand() {
    super("pasv", CommandID.PASV_COMMAND_ID,
          "use passive (PASV) connection type for each data transfer");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    session.getFTPBean().setConnectionType( FTP.PASV_CONNECTION_TYPE );

    boolean force = GTOverride.getBoolean("forcePasvToUseControlIP");
    session.getFTPBean().forcePasvToUseControlIP( force );

    out.println("Connection type set to PASV.");

    return result;
  }
}

