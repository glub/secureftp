
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LMkDirCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.*;

public class LMkDirCommand extends LocalCommand {
  public LMkDirCommand() {
    super("lmkdir", CommandID.LMKDIR_COMMAND_ID, 1, 1, "directory-name",
          "make directory on the local machine");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    File currentDir = session.getLocalDir();

    String newDirStr = (String)getArgs().get(0);
    File newDir = new File(newDirStr);

    if ( !newDir.isAbsolute() )
      currentDir = new File( currentDir, newDirStr );
    else
      currentDir = newDir;

    if ( currentDir.exists() ) {
      out.println("Directory exists.");
    }
    else if ( !currentDir.mkdir() ) {
      out.println("Permission denied.");
      result.setCode( SecureFTPError.MKDIR_FAILED );
    }
    else {
      out.println("Directory created.");
    }

    return result;
  }
}

