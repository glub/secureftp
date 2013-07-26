
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CDCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.*;
import java.util.*;

import org.apache.regexp.*;

public class CDCommand extends NetworkCommand {
  public CDCommand() {
    super("cd", CommandID.CD_COMMAND_ID, 1, 1,  
          "remote-directory", "change remote working directory");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    try {
      String dirToChangeTo = (String)getArgs().get(0);

      if ( session.isGlobOn() && dirToChangeTo.indexOf('*') >= 0 ) {

        // break up path and step through it one by one
        StringTokenizer tok = new StringTokenizer(dirToChangeTo, "/");
        int iter = 0;
        String fullPath = "";
        while( tok.hasMoreTokens() ) {
          String currentDir = "";

          if ( iter == 0 && dirToChangeTo.startsWith("/") ) {
            currentDir = "/";
          }
          else {
            currentDir = tok.nextToken();
          }

          iter++;

          // if we have a glob, list this current dir and search the results
          // for the pattern.
          if ( currentDir.indexOf('*') >= 0 ) {
            try {
              currentDir = Util.searchAndReplace(currentDir, "*", ".*", true);
              RECompiler compiler = new RECompiler();
              RE fileRegex = new RE();
              fileRegex.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
              if ( currentDir.length() > 1 && currentDir.endsWith("/") ) {
                currentDir = currentDir.substring(0, currentDir.length() - 1);
              }
              REProgram pattern = compiler.compile("^" + currentDir + "(/)?");
              fileRegex.setProgram(pattern);

              session.getFTPBean().setSendCmdStream(null);
              session.getFTPBean().setRecvCmdStream(null);

              RemoteFileList dirList = session.getFTPBean().list();

              session.getFTPBean().setSendCmdStream(session.getOutputStream());
              session.getFTPBean().setRecvCmdStream(session.getOutputStream());

              for ( int i = 0; i < dirList.size(); i++ ) {
                RemoteFile tempFile = dirList.getFile(i);
                if ( (tempFile.isDirectory() || tempFile.isLink()) &&
                     fileRegex.match(tempFile.getFileName()) ) {
                  currentDir = tempFile.getFileName();
                  break;
                } 
              }
            }
            catch ( RESyntaxException rese ) {}
            catch ( IOException ioe ) {}
            catch ( FTPException fe ) {}
          }

          fullPath += currentDir;
          if ( !currentDir.endsWith("/") )
            fullPath += "/";

          out.println("Changing to " + 
                       Util.searchAndReplace(fullPath, ".*", "*", true));

          session.getFTPBean().chdir( currentDir );
        }
      }
      else {
        session.getFTPBean().chdir( dirToChangeTo );
      }
    }
    catch ( IllegalArgumentException iae ) {
      throw new CommandException( getUsage() );
    }
    catch ( FTPException fe ) {
      String message = fe.getMessage();
      if ( session.isGlobOn() && message.indexOf('*') >= 0 ) {
        message = Util.searchAndReplace(message, ".*", "*", true);
      }
      result.setCode( SecureFTPError.CD_FAILED );
      //out.println(message);
    }

    return result;
  }
}

