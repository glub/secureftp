
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: MPutCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.*;
import java.util.*;

public class MPutCommand extends NetworkCommand {
  private PutCommand putCommand = null;

  public MPutCommand() {
    super("mput", CommandID.MPUT_COMMAND_ID, 3, 4, 
          "local-file1 session progress [PutCommand]",
          "send multiple files and/or directories");
    setBeepWhenDone( false );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    File localFile = (File)getArgs().get(0);
    FTPSession session = (FTPSession)getArgs().get(1);
    DataTransferDialog progress = (DataTransferDialog)getArgs().get(2);

    if ( getArgs().size() == 4 ) {
      putCommand = (PutCommand)getArgs().get(3);
    }

    if ( null ==  session.getLocalDir() ) {
      return result;
    }

    ArrayList fileList = new ArrayList();

    // if this is null, get all the files in the local dir
    if ( null == localFile ) {
      File[] files = session.getLocalDir().listFiles();
      for ( int i = 0; i < files.length; i++ ) {
        boolean add = true;

        // don't transfer these files
        if ( Util.isMacOS() ) {
          if ( files[i].getName().equals(".DS_Store") ) {
            add = false;
          }
        }

        if ( !Client.showHiddenFiles() ) {
          if ( Util.isHiddenFile(files[i]) ) {
            add = false;
          }
        }

        if ( add ) {
          fileList.add( files[i] );
        }
      }
    }
    else {
      fileList.add( localFile );
    }

    try {
      for( int i = 0; i < fileList.size(); i++ ) {
        if ( ((File)fileList.get(i)).isDirectory() ) {
          File oldLocalDir = session.getLocalDir();
	  String oldRemoteDir = session.getFTPBean().pwd();

          try {
	    session.setLocalDir( (File)fileList.get(i) );
          }
          catch ( FileNotFoundException fnfe ) {}

          String newDirName = ((File)fileList.get(i)).getName();

          try {
            session.getFTPBean().mkdir( newDirName );
          }
          catch ( FTPAccessDeniedException fde ) {
            LString msg = 
              new LString("MkDirDialog.permission_denied_verbose",
                          "The directory \"[^0]\" could not be created. Permission denied." );
            msg.replace( 0, newDirName );
            ErrorDialog.showDialog( msg );
            result.setCode( SecureFTPError.TRANSFER_ABORTED );
            return result;
          }
          catch ( FTPException fe ) {
            // we may have already created a dir there, but if we have,
            // let it go.
          }

	  session.getFTPBean().chdir( oldRemoteDir + "/" + newDirName );

          ArrayList args = null;

          if ( null == putCommand ) {
            putCommand = new PutCommand();
            args = new ArrayList(3);
            putCommand.setArgs( args );
          }

          MPutCommand mpc = new MPutCommand();
          ArrayList mpcArgs = new ArrayList(4);
	  mpcArgs.add( (File)null );
          mpcArgs.add( session );
          mpcArgs.add( progress ); 
          mpcArgs.add( putCommand );
          mpc.setArgs( mpcArgs );
	  result = SecureFTP.getCommandDispatcher().fireCommand( this, mpc );

          if ( SecureFTPError.NOT_CONNECTED == result.getCode() ) {
            break;
          }

          try {
	    session.setLocalDir( oldLocalDir );
          }
          catch ( FileNotFoundException fnfe ) {}

	  session.getFTPBean().chdir( oldRemoteDir );

          if ( SecureFTPError.TRANSFER_ABORTED == result.getCode() ) {
            return result;
          }
        }
        else {
          ArrayList args = null;

          if ( null == putCommand ) {
            putCommand = new PutCommand();
            args = new ArrayList(3);
            putCommand.setArgs( args );
          }

          args = putCommand.getArgs();
          args.clear();
          args.add( (File)fileList.get(i) );
          args.add( session );
          args.add( progress );
          putCommand.setArgs( args );
          result = 
            SecureFTP.getCommandDispatcher().fireCommand( this, putCommand );

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
      result.setCode( SecureFTPError.UPLOAD_FAILED );
      SecureFTP.getCommandDispatcher().fireCommand(this, new LsCommand());
    }

    return result;
  }

  public PutCommand getLastCommand() { return putCommand; }
}

