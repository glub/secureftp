
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LMDeleteCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;
import java.io.*;
import java.util.*;

public class LMDeleteCommand extends LocalCommand {
  private boolean displayedWarning = false;

  public LMDeleteCommand() {
    super("lmdelete", CommandID.LMDELETE_COMMAND_ID, 1, 9999, 
          "local-file1 [local-file2 ...]",
          "delete multiple local files and/or directories");
  }

  public LMDeleteCommand( boolean displayedWarning ) {
    this();
    this.displayedWarning = displayedWarning;
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    if ( !displayedWarning ) {
      displayedWarning = true;

      if ( session.isInteractiveOn() ) {
        if ( !CLIUtil.yesNoPrompt("You are about to permenently delete " +
                                  "multiple files. Continue?") ) {
          out.println("Local files deletion aborted.");
          return result; 
        }
      }
    }

    for ( int i = 0; i < getArgs().size(); i++ ) {
      File currentFile = session.getLocalDir();
    
      String newFileStr = (String)getArgs().get(i);
      File newFile = new File(newFileStr);

      if ( !newFile.isAbsolute() ) {
        currentFile = new File( currentFile, newFileStr );
      }
      else {
        currentFile = newFile;
      }

      // resend items that have patterns in them
      if ( session.isGlobOn() && !currentFile.exists() ) {
        try {
          File[] fileGlob =
            CLIUtil.globLocalPathForFiles( currentFile.getAbsolutePath(),
                                           CLIUtil.GLOB_ALL_FILES );

          if ( fileGlob.length == 0 ) {
            out.println("File and/or directory not found.");
            result.setCode( SecureFTPError.NO_SUCH_FILE );
            return result;
          }

          ArrayList globList = new ArrayList(fileGlob.length);
          for ( int j = 0; j < fileGlob.length; j++ ) {
            globList.add( fileGlob[j].getAbsolutePath() ); 
          }

          LMDeleteCommand cmd = new LMDeleteCommand( true );
          cmd.setArgs( globList );
          result = SecureFTP.getCommandDispatcher().fireCommand( this, cmd );
          if ( SecureFTPError.PERMISSION_DENIED == result.getCode() ) {
            break;
          }

          continue;
        }
        catch ( FileNotFoundException fne ) {}
      }

      if ( !currentFile.exists() ) {
        out.println("File and/or directory does not exist.");
        result.setCode( SecureFTPError.NO_SUCH_FILE );
      }

      // at this point we should have a valid file/directory
      else if ( currentFile.isDirectory() ) {
        File[] files = currentFile.listFiles();
        if ( files == null ) {
          out.println("Permission denied: Deletion of \"" +
                      currentFile.getName() + "\" failed.");
          continue;
        }
        else {
          ArrayList list = new ArrayList( files.length );
          for ( int k = 0; k < files.length; k++ ) {
            list.add( files[k].getAbsolutePath() );  
          }
          LMDeleteCommand cmd = new LMDeleteCommand( true );
          cmd.setArgs( list );
          result = SecureFTP.getCommandDispatcher().fireCommand( this, cmd );
          if ( SecureFTPError.PERMISSION_DENIED == result.getCode() ) {
            break;
          }

          LRmDirCommand rmCmd = new LRmDirCommand();
          ArrayList args = new ArrayList(1);
          args.add( currentFile.getAbsolutePath() );
          rmCmd.setArgs( args ); 
          result = SecureFTP.getCommandDispatcher().fireCommand( this, rmCmd ); 
        }
      }

      else {
        LDeleteCommand ldc = new LDeleteCommand();
        ArrayList args = new ArrayList(1);
        args.add( currentFile.getAbsolutePath() );
        ldc.setArgs( args );
  
        result = SecureFTP.getCommandDispatcher().fireCommand( this, ldc );  

        if ( SecureFTPError.PERMISSION_DENIED == result.getCode() ) {
          out.println("Permission denied: Deletion of \"" +
                      currentFile.getName() + "\" failed.");
          result.setCode( SecureFTPError.PERMISSION_DENIED );
          return result;
        }
      }
    }

    return result;
  }
}

