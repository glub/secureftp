
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LCDCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.*;

public class LCDCommand extends LocalCommand {
  public LCDCommand() {
    super("lcd", CommandID.LCD_COMMAND_ID, 0, 1, 
          "[directory]", "change local working directory");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    if ( getArgs().size() == 0 ) {
      try {
        session.setLocalDir( (String)System.getProperty("user.home") );
        out.println("Local directory now \"" + session.getLocalDir() +
                           "\"");
      }
      catch (FileNotFoundException fnfe) {
        out.println("No such directory: " + 
                     System.getProperty("user.home")); 
      }
    }
    else {
      String newDirStr = (String)getArgs().get(0);

      File newDir = new File( newDirStr );

      if ( !newDir.isAbsolute() ) {
        newDir = new File( session.getLocalDir(), newDirStr );
      }

      try {
        if ( session.isGlobOn() ) {
          newDir = CLIUtil.globLocalPathForDir( newDir.getAbsolutePath() );
        }

        if ( newDir.exists() ) {
          session.setLocalDir( newDir.getCanonicalPath() );
          out.println("Local directory now \"" + session.getLocalDir() + "\"");
        }
        else {
          out.println("No such directory: " + newDirStr); 
          result.setCode( SecureFTPError.NOT_A_DIRECTORY );
        }
      } 
      catch (FileNotFoundException fnfe) {
        out.println("No such directory: " + newDirStr); 
        result.setCode( SecureFTPError.NOT_A_DIRECTORY );
      }
      catch (IOException ioe) {} 
    }

    return result;
  }
}

