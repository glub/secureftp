
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: PWDCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class PWDCommand extends NetworkCommand {
  public PWDCommand() {
    super("pwd", CommandID.PWD_COMMAND_ID,
          "print working directory on remote machine");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    try {
/*
      out.println("\"" + SecureFTP.getFTPSession().getFTPBean().pwd() +
                         "\" is current directory.");
*/
      SecureFTP.getFTPSession().getFTPBean().pwd();
    }
    catch ( FTPException fe ) {
      out.println( fe.getMessage() );
    }

    return result;
  }
}

