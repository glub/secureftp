
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: MGetCommand.java 99 2009-10-10 01:00:36Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.util.*;

public class MGetCommand extends NetworkCommand {
  private boolean isSyncing = false;

  public MGetCommand() {
    super("mget", CommandID.MGET_COMMAND_ID, 1, 9999, 
          "remote-file1 [remote-file2 ...]",
          "get multiple files and/or directories");
    setBeepWhenDone( true );
  }

  public MGetCommand( boolean sync ) {
    this();
    isSyncing = sync;
    setBeepWhenDone( !sync );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    for ( int arg = 0; arg < getArgs().size(); arg++ ) {
      String remoteFileStr = (String)getArgs().get(arg);

      RemoteFileList filesToGet = null;

      if ( ! SecureFTP.getFTPSession().isDebugOn() ) {
        session.getFTPBean().setSendCmdStream(null);
        session.getFTPBean().setRecvCmdStream(null);
      }

      try {
        filesToGet = session.getFTPBean().list( remoteFileStr );
        if ( filesToGet != null && filesToGet.size() > 1 ) {
          RemoteFileList tempList = session.getFTPBean().list();
	  for ( int i = 0; i < tempList.size(); i++ ) {
            if ( remoteFileStr.equals(tempList.getFile(i).getFileName()) ) {
              filesToGet.clear();

              if ( SecureFTP.getFTPSession().isDebugOn() ) {
                System.out.println( "DEBUG: Add " + tempList.getFile(i) );
              }

	      filesToGet.add( tempList.getFile(i) );
	      break;
	    }
	  }
        }
      }
      catch ( FTPException fe ) {}
      catch ( IOException ioe ) {}

      if ( ! SecureFTP.getFTPSession().isDebugOn() ) {
        session.getFTPBean().setSendCmdStream(session.getOutputStream());
        session.getFTPBean().setRecvCmdStream(session.getOutputStream());
      }

      if ( filesToGet.size() == 0 ) {
        out.println("No such file or directory.");
        result.setCode( SecureFTPError.NO_SUCH_FILE );
      }

      for ( int i = 0; filesToGet != null && i < filesToGet.size(); i++ ) {
        if ( filesToGet.getFile(i).getFileName().equals(".") ||
             filesToGet.getFile(i).getFileName().equals("..") ) {
          continue;
        }

        try {
          RemoteFile currentFile = filesToGet.getFile(i);
          boolean linkIsFile = false;
          boolean linkIsDir  = false;

          // if we are a link, see if it's a directory by listing it's contents
          // if we have something, it's a dir
          if ( currentFile.isLink() || currentFile.getFileSize() < 0 ) {
            if ( ! SecureFTP.getFTPSession().isDebugOn() ) {
              session.getFTPBean().setSendCmdStream(null);
              session.getFTPBean().setRecvCmdStream(null);
            }

            RemoteFileList rfl = null;
		
            try {
              String linkList = currentFile.getFileName() + "/*";
              rfl = session.getFTPBean().list( linkList );

            }
            catch ( FTPException fe ) {}
            catch ( IOException ioe ) {}

            if ( ! SecureFTP.getFTPSession().isDebugOn() ) {
              session.getFTPBean().setSendCmdStream(session.getOutputStream());
              session.getFTPBean().setRecvCmdStream(session.getOutputStream());
            }

	    if ( rfl != null && rfl.size() > 0 ) {
              linkIsDir = true;
	    }
	    else {
              linkIsFile = true;
	    }
          }

          if ( (currentFile.isFile() && currentFile.getFileSize() >= 0) || 
               linkIsFile ) {
            boolean getFile = true;

            if ( session.isInteractiveOn() ) {
              getFile = CLIUtil.yesNoPrompt( "Download \"" + 
                                             currentFile.getFileName() + "\"?",
                                             CLIUtil.YN_YES );
            }

            if ( getFile ) {
              GetCommand gc = new GetCommand(isSyncing);
	      ArrayList args = new ArrayList(1);
	      //args.add( currentFile.getFileName() );
	      args.add( currentFile );
	      gc.setArgs( args );
              SecureFTPError err = 
                SecureFTP.getCommandDispatcher().fireCommand( this, gc );	
              if ( isSyncing &&
                 err.getCode() == SecureFTPError.SYNC_NOT_SUPPORTED ) {
                return err;
              }
            }
          }
          else if ( currentFile.isDirectory() || linkIsDir ) {
            ArrayList args = new ArrayList(1);

	    boolean getDir = true;
            if ( session.isInteractiveOn() ) {
              getDir =
                CLIUtil.yesNoPrompt( "Do you want to download the contents " +
                                     "of the \"" + currentFile.getFileName() +
				     "\" directory?", CLIUtil.YN_YES );
	    }

	    if ( getDir ) {
              File oldLocalDir = session.getLocalDir();
	      String oldRemoteDir = session.getFTPBean().pwd();
        
              File newDir = new File( oldLocalDir, currentFile.getFileName() );

	      if ( !newDir.exists() ) {
	        LMkDirCommand lmdc = new LMkDirCommand();
	        args.clear();
	        args.add( currentFile.getFileName() );
	        lmdc.setArgs( args );
                SecureFTP.getCommandDispatcher().fireCommand( this, lmdc );	
	      }

	      session.setLocalDir( newDir );
	      session.getFTPBean().chdir( oldRemoteDir + "/" + 
                                          currentFile.getFileName() );

	      MGetCommand mgc = new MGetCommand(isSyncing);
	      args.clear();
	      args.add( "" );
	      mgc.setArgs( args );
              SecureFTP.getCommandDispatcher().fireCommand( this, mgc );	
          
              session.setLocalDir( oldLocalDir );
              session.getFTPBean().chdir( oldRemoteDir );
            }
          }
        }
        catch ( FTPException fe ) {
          out.println( "A download error occured: " + fe.getMessage() );
          result.setCode( SecureFTPError.DOWNLOAD_FAILED );
        }
        catch ( FileNotFoundException fnfe ) {
          out.println( "A download error occured: " + fnfe.getMessage() );
          result.setCode( SecureFTPError.DOWNLOAD_FAILED );
        }
      }
    }

    return result;
  }
}

