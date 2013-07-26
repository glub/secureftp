
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: PutCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.*;
import java.util.*;

public class PutCommand extends NetworkCommand {
  private boolean resumeAll = false;
  private boolean replaceAll = false;
  private boolean skipAll = false;

  public PutCommand() {
    this( "put", CommandID.PUT_COMMAND_ID );
  }

  public PutCommand( String commandName, short id ) {
    super(commandName, id, 3, 3, "local-file session progress", 
          "send one file");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    if ( !Client.getAllowUpload() ) {
      result.setCode( SecureFTPError.PERMISSION_DENIED );
      return result;
    }

    File newFile = (File)getArgs().get(0);
    FTPSession session = (FTPSession)getArgs().get(1);
    DataTransferDialog progress = (DataTransferDialog)getArgs().get(2); 

    if ( GTOverride.getBoolean("glub.zerobyte.put.ignore") ) {
      if (newFile.length() == 0) {
        return result;
      }
    }

    try {
      File currentDir = session.getLocalDir();

      if ( null == currentDir ) {
        return result;
      }

      String fileName = newFile.getName();

      boolean remoteFileExists = false;
      Date modTime = null;

      session.getFTPBean().setSendCmdStream(null);
      session.getFTPBean().setRecvCmdStream(null);

      long fileSize = 0;
      try {
        if ( session.supportsSIZE() ) {
          short transferMode = session.getFTPBean().getTransferMode();

          if ( transferMode != FTP.BINARY_TRANSFER_MODE ) {
            session.getFTPBean().binary();
          }

          fileSize = session.getFTPBean().size( fileName );

          if ( fileSize >= 0 ) {
            remoteFileExists = true;
          }
          else {
            result.setCode( SecureFTPError.TRANSFER_ABORTED );
            return result;
          }

          if ( transferMode == FTP.ASCII_TRANSFER_MODE ) {
            session.getFTPBean().ascii();
          }
          else if ( transferMode == FTP.AUTO_TRANSFER_MODE ) {
            session.getFTPBean().auto();
          }
        }

        if ( fileSize < 0 ) {
          fileSize = 0;
        }
      }
      catch ( FTPNoSuchFileException remoteFileNotFound ) {
        remoteFileExists = false;
      }
      catch ( FTPException noSize ) {
        session.setSupportsSIZE( false );
      }

      if ( !session.supportsSIZE() ) {
        try {
          RemoteFileList rfl = session.getFTPBean().list( fileName );
          if ( rfl.size() > 0 ) {
            RemoteFile rf = rfl.getFile(0);
            fileSize = rf.getFileSize(); 
            remoteFileExists = true;
          }
        }
        catch ( FTPNoSuchFileException remoteFileNotFound ) {
          remoteFileExists = false;
        }
      }

      if ( remoteFileExists ) {
        try {
          if ( session.supportsMDTM() ) {
            modTime = session.getFTPBean().time( fileName );
          }
         }
        catch ( FTPException noTime ) {
          session.setSupportsMDTM( false );
        }
      }

      if ( session.testForREST() ) {
        session.setTestForREST( false );
        session.setSupportsREST( session.getFTPBean().isTransferRestartable() );
      }

      session.getFTPBean().setSendCmdStream(session.getOutputStream());
      session.getFTPBean().setRecvCmdStream(session.getOutputStream());

      boolean resumeUpload = false;

      if ( remoteFileExists ) {
        boolean resumable = false;

        if ( session.supportsREST() && fileSize > 0 && 
             newFile.length() > fileSize &&
             (session.getFTPBean().getTransferMode() ==
                                                  FTP.BINARY_TRANSFER_MODE ||
             (session.getFTPBean().getTransferMode() ==
                                                  FTP.AUTO_TRANSFER_MODE &&
             !FileTypeDecider.isAscii(fileName))) ) {
           resumable = true;
         }

        if ( GTOverride.getBoolean("glub.resume_xfer.disabled") ) 
          resumable = true;

        int r = FileExistsDialog.SKIP;

        if ( !resumeAll && !replaceAll && !skipAll ) {
          r = 
            FileExistsDialog.showDialog( FileExistsDialog.DIRECTION_PUT,
                                         fileName,
                                         modTime, newFile.lastModified(),
                                         fileSize, newFile.length(),
                                         resumable );
        }
        else if ( resumeAll ) {
           r = FileExistsDialog.RESUME_ALL;
        }
        else if ( replaceAll ) {
          r = FileExistsDialog.REPLACE_ALL;
        }
        else if ( skipAll ) {
          return result;
        }

        if ( FileExistsDialog.CANCEL == r ) {
          result.setCode( SecureFTPError.TRANSFER_ABORTED );
          return result;
        }
        else if ( FileExistsDialog.SKIP == r ) { 
          return result;
        }
        else if ( FileExistsDialog.SKIP_ALL == r ) { 
          skipAll = true;
          return result;
        }

        resumeUpload = ( FileExistsDialog.RESUME == r ||
                         FileExistsDialog.RESUME_ALL == r );

        resumeAll = FileExistsDialog.RESUME_ALL == r;
        replaceAll = FileExistsDialog.REPLACE_ALL == r;
      }

      FTPAbortableTransfer abort = new FTPAbortableTransfer();

      progress.setFileName( fileName );
      progress.setAbortableTransfer( abort );

      session.getFTPBean().store( newFile, fileName, 
                                  resumeUpload, progress, abort );
    }
    catch ( IOException ioe ) {
      LString msg = new LString("DataTransfer.transfer_failed",
                                "The data transfer failed: [^0]");
      msg.replace( 0, ioe.getMessage() );
      ErrorDialog.showDialog( msg );

      result.setCode( SecureFTPError.IO_EXCEPTION );
    }
    catch ( FTPAbortException fae ) {
      result.setCode( SecureFTPError.TRANSFER_ABORTED );
    }
    catch ( FTPConnectionLostException fcle ) {
      SecureFTP.getCommandDispatcher().fireCommand( this, new CloseCommand() );
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
    }

    return result;
  }
}

