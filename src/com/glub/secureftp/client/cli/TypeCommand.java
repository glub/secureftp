
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: TypeCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class TypeCommand extends NetworkCommand {
  public TypeCommand() {
    super("type", CommandID.TYPE_COMMAND_ID, 0, 1, "[ascii|ebcdic|binary]",
          "set transfer type");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    FTP ftp = session.getFTPBean();

    if ( getArgs().size() == 0 ) {
      out.println("Using " +
                  ((FTP.ASCII_TRANSFER_MODE ==
                    ftp.getTransferMode()) ? "ascii" : "binary") + 
                    " mode to transfer files.");
    }
    else {
      if ( "ascii".equalsIgnoreCase((String)getArgs().get(0)) ) {
        SecureFTP.getCommandDispatcher().fireCommand(this, new AsciiCommand());
      }
      else if ( "ebcdic".equalsIgnoreCase((String)getArgs().get(0)) ) {
        SecureFTP.getCommandDispatcher().fireCommand(this, new EbcdicCommand());
      }
      else if ( "binary".equalsIgnoreCase((String)getArgs().get(0)) ) {
        SecureFTP.getCommandDispatcher().fireCommand(this, new BinaryCommand());
      }
      else if ( "image".equalsIgnoreCase((String)getArgs().get(0)) ) {
        SecureFTP.getCommandDispatcher().fireCommand(this, new BinaryCommand());
      }
      else {
        out.println("Unknown type.");
      }
    }

    return result;
  }
}

