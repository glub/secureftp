
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LLsCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.util.*;

public class LLsCommand extends LocalCommand {
  public LLsCommand() {
    super("lls", CommandID.LLS_COMMAND_ID, 0, 1, "[local-directory]",
          "list contents of local directory");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    File dirToList = session.getLocalDir();

    if ( getArgs().size() == 1 ) {
      String newDirToListStr = (String)getArgs().get(0);
      File newDirToList = new File(newDirToListStr);

      if ( !newDirToList.isAbsolute() )
        dirToList = new File( dirToList, newDirToListStr );
      else
        dirToList = newDirToList;

      try {
        if ( session.isGlobOn() ) {
          dirToList = 
            CLIUtil.globLocalPathForDir( dirToList.getAbsolutePath() );
          out.println(dirToList.getAbsolutePath() + ":");
        }
      }
      catch (FileNotFoundException fnfe) {
        out.println("No such directory: " + newDirToListStr); 
      }

    }

    if ( !dirToList.exists() ) {
      out.println("Directory does not exist: " + dirToList.getName()); 
      return result;
    }
    else if ( !dirToList.isDirectory() ) {
      out.println("File is not a directory: " + dirToList.getName()); 
      return result;
    }

    File[] filesInDir = dirToList.listFiles();

    if ( null == filesInDir ) {
      out.println("Permission denied.");
      return result;
    }

    Arrays.sort(filesInDir);

    int row = 0;
    int col = 0;
    int count = 1;

    // get max length of a filename to be listed
    int maxFileNameWidth = 0;
    for ( int i = 0; i < filesInDir.length; i++ ) {
      if ( filesInDir[i].getName().length() > maxFileNameWidth )
        maxFileNameWidth = filesInDir[i].getName().length(); 
    }

    int numOfCols = 1;
    int padding = 0;

    if ( maxFileNameWidth < 10 ) {
      numOfCols = 5;
      padding = 10;
    }
    else if ( maxFileNameWidth < 20 ) {
      numOfCols = 4;
      padding = 20;
    }
    else if ( maxFileNameWidth < 25 ) {
      numOfCols = 3;
      padding = 25;
    }
    else if ( maxFileNameWidth < 40 ) {
      numOfCols = 2;
      padding = 40;
    }

    double fNumOfCols = numOfCols * 1.0;

    int maxRows = (int)Math.ceil(filesInDir.length / fNumOfCols);

    for ( int i = 0; i < maxRows * numOfCols; i++ ) {
      int item = row + (col * maxRows);

      String file = "";

      if ( item < filesInDir.length ) {
        file = filesInDir[item].getName();
        if ( filesInDir[item].isDirectory() )
          file += System.getProperty("file.separator");
      }

      if ( count % numOfCols == 0 ) {
        out.println(file);
        row++;
        col = 0;
      }
      else {
        out.print(file);
        for ( int j = 0; j < padding - file.length(); j++ ) {
          out.print(" ");
        }
        col++;
      }
      count++;
    }

    return result;
  }
}

