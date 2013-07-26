
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LDeleteCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.*;

public class LDeleteCommand extends LocalCommand {
  public LDeleteCommand() {
    super("ldelete", CommandID.LDELETE_COMMAND_ID, 1, 1, "local-file",
          "delete local file");
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
                                          CLIUtil.GLOB_ONLY_FILES );
        if ( fileGlob.length > 0 ) {
          currentFile = fileGlob[0];
        }
      }
      catch ( FileNotFoundException fne ) {}
    }

    if ( !currentFile.exists() ) {
      out.println("File does not exist.");
    }
    else if ( currentFile.isDirectory() ) {
      out.println("File is a directory. " +
                         "Use lrmdir to remove a local directory.");
    }
    else if ( session.isInteractiveOn() ) {
      if ( CLIUtil.yesNoPrompt("You are about to permenently delete \"" +
	                       currentFile.getName() + 
			       "\"." + System.getProperty("line.separator") +
			       "Continue?") ) {
        if ( !currentFile.delete() ) {
          out.println("Permission denied.");
          result.setCode( SecureFTPError.DELETE_FAILED );
        }
        else {
          out.println("File \"" + currentFile.getName() + "\" deleted.");
        }
      }
      else {
        out.println("Local file deletion aborted.");
      }
    }
    else if ( !currentFile.delete() ) {
      out.println("Permission denied.");
      result.setCode( SecureFTPError.DELETE_FAILED );
    }
    else {
      out.println("File \"" + currentFile.getName() + "\" deleted.");
    }

    return result;
  }
}

