
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LRenameCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.*;

public class LRenameCommand extends LocalCommand {
  public LRenameCommand() {
    super("lrename", CommandID.LRENAME_COMMAND_ID, 2, 2, "from to",
          "rename local file");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    File currentFile = session.getLocalDir();

    String oldFileStr = (String)getArgs().get(0);
    File oldFile = new File(oldFileStr);

    String newFileStr = (String)getArgs().get(1);
    File newFile = new File(newFileStr);

    if ( !newFile.isAbsolute() )
      newFile = new File( currentFile, newFileStr );

    if ( !oldFile.isAbsolute() )
      currentFile = new File( currentFile, oldFileStr );
    else
      currentFile = oldFile;

    if ( !currentFile.exists() ) {
      out.println("File doesn't exist.");
    }
    else if ( !currentFile.renameTo(newFile) ) {
      out.println("Permission denied.");
      result.setCode( SecureFTPError.RENAME_FAILED );
    }

    return result;
  }
}

