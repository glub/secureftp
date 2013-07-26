
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: MPutCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.util.*;

public class MPutCommand extends NetworkCommand {
  private boolean isSyncing = false;

  public MPutCommand() {
    super("mput", CommandID.MPUT_COMMAND_ID, 1, 9999, 
          "local-file1 [local-file2 ...]",
          "send multiple files and/or directories");
    setBeepWhenDone( true );
  }

  public MPutCommand(boolean sync) {
    this();
    isSyncing = sync;
    setBeepWhenDone( !sync );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    for ( int arg = 0; arg < getArgs().size(); arg++ ) {
      String fileGlobStr = (String)getArgs().get(arg);
      File fileGlob = new File( fileGlobStr );

      if ( !fileGlob.isAbsolute() ) {
        fileGlob = new File( session.getLocalDir(), fileGlobStr );
      }

      out.println( fileGlob.getAbsolutePath() );

      File[] fileList = null;
      String initialRemoteDir = null;

      try {
        if ( !fileGlob.exists() && session.isGlobOn() ) {
          fileList = 
            CLIUtil.globLocalPathForFiles( fileGlob.getAbsolutePath(), 
                                           CLIUtil.GLOB_ALL_FILES );
        }
        else if ( fileGlob.exists() && fileGlob.isDirectory() ) {
          // attempt to change into the dir
          try {
            initialRemoteDir = session.getFTPBean().pwd();
            session.getFTPBean().chdir( fileGlob.getName() );
          }
          catch ( FTPNotADirectoryException fnde ) {
            out.println("File is not a directory on the server.");
            result.setCode( SecureFTPError.NOT_A_DIRECTORY );
            return result;
          }
          catch ( FTPException fnsfe ) {
            boolean mkDir = true;

	    if ( session.isInteractiveOn() ) {
	      mkDir = 
                CLIUtil.yesNoPrompt( "The \"" +
                                     fileGlob.getName() + "\" directory " +
                                     "does not exist. Create it?",
                                     CLIUtil.YN_YES );
            }
            else {
              //out.println( "Upload aborted." );
              //return;
              mkDir = true;
            }

            if ( mkDir ) {
              try {
                session.getFTPBean().mkdir( fileGlob.getName() );
                session.getFTPBean().chdir( fileGlob.getName() );
	      }
	      catch ( FTPException fe ) {
                out.println(fe.getMessage());
                result.setCode( SecureFTPError.UNKNOWN );
	        return result;
              }
            }
          }

          fileList = fileGlob.listFiles();
        }
        else if ( fileGlob.exists() ) {
          fileList = new File[] { fileGlob };
        }
        else {
          out.println("No such file: " + fileGlob);
        }

        for ( int i = 0; i < fileList.length; i++ ) {
	  try {
            if ( fileList[i].isDirectory() ) {
	      boolean changeDir = true;
	      if ( session.isInteractiveOn() ) {
	        changeDir = 
                  CLIUtil.yesNoPrompt( "Do you want to change into the \"" +
                                       fileList[i].getName() + "\" directory?",
				       CLIUtil.YN_YES );
              }

	      if ( changeDir ) {
	        File oldLocalDir = session.getLocalDir();
	        String oldRemoteDir = session.getFTPBean().pwd();
	        boolean wasGlobOn = session.isGlobOn();
	        session.setLocalDir( fileList[i].getAbsolutePath() );
	        session.setGlobOn( true );

                try {
	          session.getFTPBean().chdir( oldRemoteDir + "/" + 
                                              fileList[i].getName() );
                }
                catch ( FTPException fe ) {
                  session.getFTPBean().mkdir( fileList[i].getName() );
	          session.getFTPBean().chdir( oldRemoteDir + "/" + 
                                              fileList[i].getName() );
                }

                MPutCommand mpc = new MPutCommand(isSyncing);
                ArrayList args = new ArrayList(1);
	        args.add( "*" );
                mpc.setArgs( args );
	        SecureFTP.getCommandDispatcher().fireCommand( this, mpc );

	        session.setLocalDir( oldLocalDir );
	        session.setGlobOn( wasGlobOn );
	        session.getFTPBean().chdir( oldRemoteDir );
              }
            }
	    else {
	      boolean putFile = true;
	      if ( session.isInteractiveOn() ) {
	        putFile = CLIUtil.yesNoPrompt( "Upload \"" +
			                       fileList[i].getName() + "\"?",
					       CLIUtil.YN_YES );
	      }

	      if ( putFile ) {
	        PutCommand pc = new PutCommand(isSyncing);
	        ArrayList args = new ArrayList(1);
	        args.add( fileList[i].getAbsolutePath() );
	        pc.setArgs( args );

                result = SecureFTP.getCommandDispatcher().fireCommand( this, pc );

                if ( result.getCode() == SecureFTPError.UPLOAD_FAILED ) {
                  break;
                }
	      }
            }
	  }
          catch ( FTPException fe ) {
            out.println( "An upload error occured: " + fe.getMessage() );
            result.setCode( SecureFTPError.UPLOAD_FAILED );
          }
        }

        if ( initialRemoteDir != null ) {
          try {
            session.getFTPBean().chdir( initialRemoteDir );
          }
          catch ( FTPException fe ) {}
        }
      }
      catch ( FileNotFoundException fne ) {
        out.println("No such file: " + fileGlob);
        result.setCode( SecureFTPError.NO_SUCH_FILE );
      }
    }

    return result;
  }
}

