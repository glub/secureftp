
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RmDirCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.*;
import java.util.*;

import org.apache.regexp.*;

public class RmDirCommand extends NetworkCommand {
  public RmDirCommand() {
    super("rmdir", CommandID.RMDIR_COMMAND_ID, 1, 1, "directory-name",
          "remove directory on the remote machine");
    setBeepWhenDone( false );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    try {
      boolean deleteIt = true;
      boolean foundFile = true;
      boolean changedDir = false;

      String dirToRemove = (String)getArgs().get(0);

      String pwd = "";

      if ( session.isGlobOn() && dirToRemove.indexOf("*") >= 0 ) {
        RemoteFileList rfl = new RemoteFileList();

        session.getFTPBean().setRecvCmdStream(null);
        session.getFTPBean().setSendCmdStream(null);

        try {
           pwd = session.getFTPBean().pwd();
        }
        catch ( Exception e ) {}

        // if we have a path separator, we need to change to that directory
        // and then try and delete the file(s)
        if ( dirToRemove.indexOf("/") >= 0 ) {
          dirToRemove = parseForFile( dirToRemove );
          changedDir = true;
        }

        String globToRemove = 
          Util.searchAndReplace(dirToRemove, "*", ".*", true);

        RECompiler compiler = new RECompiler();
        RE fileRegex = new RE();
        fileRegex.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        REProgram pattern = compiler.compile("^" + globToRemove);
        fileRegex.setProgram(pattern);

        if ( dirToRemove.startsWith("/") ) {
          StringTokenizer tok = new StringTokenizer(dirToRemove, "/", true);
          String pathToDelete = "";

          while ( tok.hasMoreTokens() ) {
            String newItem = tok.nextToken();
            if ( tok.hasMoreTokens() ) {
              pathToDelete += newItem;
            }
          }

          pathToDelete += "*";

          rfl = session.getFTPBean().list(pathToDelete); 
        }
        else {
          rfl = session.getFTPBean().list();
        }

        session.getFTPBean().setSendCmdStream(session.getOutputStream());
        session.getFTPBean().setRecvCmdStream(session.getOutputStream());
 
        foundFile = false;

        for ( int i = 0; i < rfl.size(); i++ ) {
          RemoteFile tempFile = rfl.getFile(i);
          if ( (tempFile.isDirectory() || tempFile.isLink()) && 
               fileRegex.match(tempFile.getFileName()) ) {
            dirToRemove = tempFile.getFileName();
            foundFile = true;
            break;
          }
        }

        if ( !foundFile ) {
          deleteIt = false;
        }
      }

      if ( session.isInteractiveOn() && foundFile ) {
        deleteIt =
	  CLIUtil.yesNoPrompt("You are about to remove the \"" +
			      dirToRemove + "\" directory." +
			      System.getProperty("line.separator") +
			      "Continue?");
      }

      if ( deleteIt ) {
        session.getFTPBean().rmdir( dirToRemove );
	out.println("Directory removed.");
      }
      else {
        out.println("Directory removal aborted.");
      }

      if ( changedDir ) {
        CDCommand cd = new CDCommand();
        ArrayList args = new ArrayList(1);
        args.add(pwd);
        cd.setArgs(args);
        SecureFTP.getCommandDispatcher().fireCommand(this, cd);
      }
    }
    catch ( IllegalArgumentException iae ) {
      throw new CommandException( getUsage() ); 
    }
    catch ( IOException ioe ) {
      result.setCode( SecureFTPError.RMDIR_FAILED );
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
      result.setCode( SecureFTPError.RMDIR_FAILED );
    }

    return result;
  }

  private String parseForFile( String path ) {
    StringTokenizer tok = new StringTokenizer(path, "/", true);
    String dirToChangeTo = "";
    String result = "";

    do {
      result = tok.nextToken();
      if ( tok.hasMoreTokens() ) {
        dirToChangeTo += result;
      }
    } while( tok.hasMoreTokens() );

    CDCommand cd = new CDCommand();
    ArrayList args = new ArrayList(1);
    args.add(dirToChangeTo);
    cd.setArgs(args);

    SecureFTP.getCommandDispatcher().fireCommand(this, cd);

    return result;
  }
}

