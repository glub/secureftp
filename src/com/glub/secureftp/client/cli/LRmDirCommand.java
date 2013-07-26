
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LRmDirCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.*;

public class LRmDirCommand extends LocalCommand {
  public LRmDirCommand() {
    super("lrmdir", CommandID.LRMDIR_COMMAND_ID, 1, 1, "local-directory",
          "delete local directory");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    File currentFile = session.getLocalDir();

    String newFileStr = (String)getArgs().get(0);
    File newFile = new File(newFileStr);

    if ( !newFile.isAbsolute() )
      currentFile = new File( currentFile, newFileStr );
    else
      currentFile = newFile;

    if ( session.isGlobOn() && !currentFile.exists() ) {
      try {
        File[] fileGlob = 
          CLIUtil.globLocalPathForFiles( currentFile.getAbsolutePath(), 
                                         CLIUtil.GLOB_ONLY_DIRS );

        if ( fileGlob.length > 0 ) {
          currentFile = fileGlob[0];
        }
      }
      catch ( FileNotFoundException fne ) {}
    }

    if ( !currentFile.exists() ) {
      out.println("Directory doesn't exist.");
    }
    else if ( currentFile.isFile() ) {
      out.println("Not a directory. " +
                         "Use ldelete to remove a local file.");
    }
    else if ( session.isInteractiveOn() ) {
      if ( CLIUtil.yesNoPrompt("You are about to remove the \"" +
	                       currentFile.getName() + "\" directory." +
			       System.getProperty("line.separator") +
			       "Continue?") ) {
        if ( !currentFile.delete() ) {
          out.println("Permission denied.");
          result.setCode( SecureFTPError.RMDIR_FAILED );
	}
	else {
          out.println("Directory \"" + currentFile.getName() + "\" removed.");
	}
      }
      else {
        out.println("Local directory removal aborted.");
      }
    }
    else if ( !currentFile.delete() ) {
      out.println("Permission denied.");
      result.setCode( SecureFTPError.RMDIR_FAILED );
    }
    else {
      out.println("Directory \"" + currentFile.getName() + "\" removed.");
    }

    return result;
  }
}

