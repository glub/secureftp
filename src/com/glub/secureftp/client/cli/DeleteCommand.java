
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: DeleteCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import org.apache.regexp.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class DeleteCommand extends NetworkCommand {
  public DeleteCommand() {
    super("delete", CommandID.DELETE_COMMAND_ID, 1, 1, 
          "remote-file", "delete remote file");
    setBeepWhenDone( false );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    try {
      boolean deleteIt = true;
      boolean foundFile = true;

      String pwd = "";
      boolean changedDir = false;

      String fileToDelete = (String)getArgs().get(0);

      if ( session.isGlobOn() && fileToDelete.indexOf('*') >= 0 ) {
        session.getFTPBean().setRecvCmdStream(null);
        session.getFTPBean().setSendCmdStream(null);

        try {
          pwd = session.getFTPBean().pwd();
        }
        catch ( Exception e ) {}

        session.getFTPBean().setSendCmdStream(session.getOutputStream());
        session.getFTPBean().setRecvCmdStream(session.getOutputStream());

        // if we have a path separator, we need to change to that directory
        // and then try and delete the file(s)
        if ( fileToDelete.indexOf("/") >= 0 ) {
          fileToDelete = parseForFile( fileToDelete );
          changedDir = true;         
        }

        fileToDelete = Util.searchAndReplace(fileToDelete, "*", ".*", true);
        RECompiler compiler = new RECompiler();
        RE fileRegex = new RE();
        fileRegex.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        REProgram pattern = compiler.compile("^" + fileToDelete);
        fileRegex.setProgram(pattern);

        session.getFTPBean().setSendCmdStream(null);
        session.getFTPBean().setRecvCmdStream(null);

        RemoteFileList dirList;

        if ( fileToDelete.startsWith("/") ) {
          StringTokenizer tok = new StringTokenizer(fileToDelete, "/", true);
          String pathToDelete = "";

          while ( tok.hasMoreTokens() ) {
            String newItem = tok.nextToken();
            if ( tok.hasMoreTokens() ) {
              pathToDelete += newItem;
            }
          }

          pathToDelete = Util.searchAndReplace(pathToDelete, ".*", "*", true);
          pathToDelete += "*";

          dirList = session.getFTPBean().list(pathToDelete);

          for ( int i = 0; i < dirList.size(); i++ ) {
            String fn = dirList.getFile(i).getFileName();
            String dirCheck = 
              fn.substring(pathToDelete.length()-1, fn.length());
            if ( dirCheck.indexOf("/") >= 0 ) {
              dirList.set(i, new RemoteFile());
            }
          }
        }
        else {
          dirList = session.getFTPBean().list();
        }

        session.getFTPBean().setSendCmdStream(session.getOutputStream());
        session.getFTPBean().setRecvCmdStream(session.getOutputStream());

        foundFile = false;

        for ( int i = 0; i < dirList.size(); i++ ) {
          RemoteFile tempFile = dirList.getFile(i);

          boolean linkToFile = false;
          if ( tempFile.isLink() && 
               fileRegex.match(tempFile.getFileName()) ) {
            session.getFTPBean().setSendCmdStream(null);
            session.getFTPBean().setRecvCmdStream(null);

            RemoteFileList tl = session.getFTPBean().list(tempFile + "/*");

            if ( tl != null && tl.size() > 0 ) {
              linkToFile = false;
            }

            session.getFTPBean().setSendCmdStream(session.getOutputStream());
            session.getFTPBean().setRecvCmdStream(session.getOutputStream());
          }

          if ( (tempFile.isFile() || linkToFile) && 
               fileRegex.match(tempFile.getFileName()) ) {
            fileToDelete = tempFile.getFileName();
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
          CLIUtil.yesNoPrompt("You are about to permenently delete \"" +
                              fileToDelete + "\"." +
			      System.getProperty("line.separator") +
			      "Continue?");
      }

      if ( deleteIt ) {
        session.getFTPBean().delete( fileToDelete );
	out.println("File deleted.");
      }
      else if ( !foundFile ) {
        out.println("File not found.");
        result.setCode( SecureFTPError.NO_SUCH_FILE );
      }
      else {
        out.println("File deletion aborted.");
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
    catch ( RESyntaxException rese ) {
      result.setCode( SecureFTPError.BAD_ARGUMENTS );
    }
    catch ( IOException ioe ) {
      result.setCode( SecureFTPError.DELETE_FAILED );
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
      result.setCode( SecureFTPError.DELETE_FAILED );
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

