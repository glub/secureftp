
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: MGetCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.*;
import java.util.*;

public class MGetCommand extends NetworkCommand {
  private GetCommand getCommand = null;

  public MGetCommand() {
    super("mget", CommandID.MGET_COMMAND_ID, 3, 4, 
          "remote-file session progress [GetCommand]",
          "get multiple files and/or directories");
    setBeepWhenDone( false );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    RemoteFile remoteFile = (RemoteFile)getArgs().get(0);
    FTPSession session = (FTPSession)getArgs().get(1);
    DataTransferDialog progress = (DataTransferDialog)getArgs().get(2);

    if ( getArgs().size() == 4 ) {
      getCommand = (GetCommand)getArgs().get(3);
    }

    RemoteFileList filesToGet = null;

    if ( !remoteFile.isKnownFileType() ) {
      session.getFTPBean().setSendCmdStream(null);
      session.getFTPBean().setRecvCmdStream(null);

      try {
        filesToGet = session.getFTPBean().list( remoteFile );
        if ( filesToGet != null && filesToGet.size() > 1 ) {
          RemoteFileList tempList = session.getFTPBean().list();
          for ( int i = 0; i < tempList.size(); i++ ) {
            if ( remoteFile == tempList.getFile(i) ) {
              filesToGet.clear();
              filesToGet.add( tempList.getFile(i) );
              break;
            }
          }
        }
      }
      catch ( FTPException fe ) {}
      catch ( IOException ioe ) {}

      session.getFTPBean().setSendCmdStream(session.getOutputStream());
      session.getFTPBean().setRecvCmdStream(session.getOutputStream());
    }
    else {
      filesToGet = new RemoteFileList();
      filesToGet.add( remoteFile );
    }

    for ( int i = 0; filesToGet != null && i < filesToGet.size(); i++ ) {
      if ( !Client.showHiddenFiles() ) {
        if ( filesToGet.getFile(i).getFileName().startsWith(".") ) {
          continue;
        }
      }
      else if ( filesToGet.getFile(i).getFileName().equals(".") ||
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
          session.getFTPBean().setSendCmdStream(null);
          session.getFTPBean().setRecvCmdStream(null);

          RemoteFileList rfl = null;
		
          try {
            String linkList = currentFile.getFileName() + "/*";
            rfl = session.getFTPBean().list( linkList );
          }
          catch ( FTPException fe ) {}
          catch ( IOException ioe ) {}

          session.getFTPBean().setSendCmdStream(session.getOutputStream());
          session.getFTPBean().setRecvCmdStream(session.getOutputStream());

          if ( rfl != null && rfl.size() > 0 ) {
            // this is for a stupid case where a server responds in properly
            if ( rfl.size() == 1 && rfl.getFile(0).getFileName().equals(".") )
              linkIsFile = true;
            else 
              linkIsDir = true;
          }
          else {
            linkIsFile = true;
          }
        }

        if ( (currentFile.isFile() && currentFile.getFileSize() >= 0) || 
             linkIsFile ) {
          boolean getFile = true;

          if ( getFile ) {
            ArrayList args = null;

            if ( null == getCommand ) {
              getCommand = new GetCommand();
              args = new ArrayList(3);
              getCommand.setArgs( args );
            }

            args = getCommand.getArgs();
            args.clear();
            args.add( currentFile );
	    args.add( session );
	    args.add( progress );
            getCommand.setArgs( args );
            result = 
              SecureFTP.getCommandDispatcher().fireCommand( this, getCommand );	

            if ( SecureFTPError.TRANSFER_ABORTED == result.getCode() ) {
              return result;
            }
          }
        }
        else if ( currentFile.isDirectory() || linkIsDir ) {
          ArrayList args = new ArrayList(1);

	  boolean getDir = true;

	  if ( getDir ) {
            File oldLocalDir = session.getLocalDir();

            if ( null == oldLocalDir ) {
              return result;
            }

	    String oldRemoteDir = session.getFTPBean().pwd();
        
            File newDir = new File( oldLocalDir, currentFile.getFileName() );

	    if ( !newDir.exists() ) {
	      LMkDirCommand lmdc = new LMkDirCommand();
	      args.clear();
	      args.add( currentFile.getFileName() );
              args.add( session );
	      lmdc.setArgs( args );
              SecureFTP.getCommandDispatcher().fireCommand( this, lmdc );	
	    }

	    session.setLocalDir( newDir );

	    session.getFTPBean().chdir( oldRemoteDir + "/" + 
                                        currentFile.getFileName() );

	    MGetCommand mgc = new MGetCommand();
	    args.clear();
            args.add( new RemoteFile() );
	    args.add( session );
	    args.add( progress );
            args.add( getCommand );
            mgc.setArgs( args );
            result = SecureFTP.getCommandDispatcher().fireCommand( this, mgc );	

            if ( SecureFTPError.NOT_CONNECTED == result.getCode() ) {
              break;
            } 
          
            session.setLocalDir( oldLocalDir );
            session.getFTPBean().chdir( oldRemoteDir );

            if ( SecureFTPError.TRANSFER_ABORTED == result.getCode() ) {
              return result;
            }
          }
        }
      }
      catch ( FTPConnectionLostException fcle ) {
        SecureFTP.getCommandDispatcher().fireCommand(this, new CloseCommand());
        ErrorDialog.showDialog( new LString("Common.connection_lost",
                                            "Connection lost.") );
        if ( progress != null ) {
          progress.dispose();
        }
        result.setCode( SecureFTPError.NOT_CONNECTED );
        return result;
      }
      catch ( FTPException fe ) {
        if ( progress == null || progress.abortAttempted() ) {
          result.setCode( SecureFTPError.TRANSFER_ABORTED );
          return result;
        }

        LString msg = new LString("DataTransfer.transfer_failed",
                                  "The data transfer failed: [^0]");
        msg.replace( 0, fe.getMessage() );
        ErrorDialog.showDialog( msg );
        result.setCode( SecureFTPError.DOWNLOAD_FAILED );
        return result;
      }
      catch ( FileNotFoundException fnfe ) {
        ErrorDialog.showDialog( new LString("DataTransfer.file_not_found",
                                            "File not found.") );
        result.setCode( SecureFTPError.NO_SUCH_FILE );
        return result;
      }
    }

    return result;
  }

  public GetCommand getLastCommand() { return getCommand; }
}

