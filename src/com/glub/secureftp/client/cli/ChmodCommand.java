
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ChmodCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.util.*;

public class ChmodCommand extends NetworkCommand {
  public ChmodCommand() {
    super("chmod", CommandID.CHMOD_COMMAND_ID, 0, 2, 
          "mode remote-file", "change file permissions of remote file");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    ArrayList args = getArgs();

    try {
      StringBuffer rawCommands = new StringBuffer();
      rawCommands.append( "SITE CHMOD " );

      BufferedReader stdin = SecureFTP.getInput();

      String mode = ""; 
      String fileName = "";

      try {
        if ( args.size() == 0 ) {
          out.print("(mode) ");
          mode = stdin.readLine().trim();
          if ( mode.length() == 0 ) {
            throw new CommandException( getUsage() );
          }
          out.print("(remote-file) ");
          fileName = stdin.readLine().trim();
          if ( fileName.length() == 0 ) {
            throw new CommandException( getUsage() );
          }
        }
        else if ( args.size() == 1 ) {
          mode = (String)args.get(0);
          out.print("(remote-file) ");
          fileName = stdin.readLine().trim();
          if ( fileName.length() == 0 ) {
            throw new CommandException( getUsage() );
          }
        }
        else {
          mode = (String)args.get(0);
          fileName = (String)args.get(1);
        }
      }
      catch ( IOException ioe ) {}

      if ( mode.length() > 0 && fileName.length() > 0 ) {
        rawCommands.append( mode + " " + fileName );
        session.getFTPBean().raw( rawCommands.toString().trim() );
      }
      else {
        throw new CommandException( getUsage() );
      }
    }
    catch ( FTPException fe ) {
      out.println( fe.getMessage() );
      result.setCode( SecureFTPError.CHMOD_FAILED );
    } 

    return result;
  }
}

