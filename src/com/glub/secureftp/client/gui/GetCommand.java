
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: GetCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.*;
import java.util.*;

public class GetCommand extends NetworkCommand {
  private boolean resumeAll = false;
  private boolean replaceAll = false;
  private boolean skipAll = false;

  public GetCommand() {
    this( "get", CommandID.GET_COMMAND_ID );
  }

  public GetCommand( String commandName, short id ) {
    super(commandName, id, 3, 3, "remote-file session progress", 
          "receive file");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    if ( !Client.getAllowDownload() ) {
      result.setCode( SecureFTPError.PERMISSION_DENIED );
      return result;
    }

    RemoteFile remoteFile = (RemoteFile)getArgs().get(0);
    String remoteFileStr = remoteFile.toString();
    FTPSession session = (FTPSession)getArgs().get(1);
    DataTransferDialog progress = (DataTransferDialog)getArgs().get(2);

    boolean weCreatedTheFile = false;
    File newFile = null;
    Date modTime = null;

    if ( null == session.getLocalDir() ) {
      return result;
    }

    try {
      String newFileStr = remoteFileStr;

      StringTokenizer tok = new StringTokenizer( newFileStr, "/" );
      while ( tok.hasMoreTokens() ) {
        newFileStr = tok.nextToken();
      }

      // sanitize filename if needed
      newFileStr = Util.searchAndReplace( newFileStr, "\\", "_", true );

      newFile = new File( newFileStr );

      if ( !newFile.isAbsolute() ) {
        newFile = new File( session.getLocalDir(), newFileStr );
      }

      weCreatedTheFile = !newFile.exists();

      session.getFTPBean().setSendCmdStream(null);
      session.getFTPBean().setRecvCmdStream(null);

      try {
        // there seems to be a bug with at least one server where
        // the time command doesn't properly handle a time query with
        // a utf8 file name
        if ( session.supportsMDTM() && 
             !session.getFTPBean().stringDataAsUTF8() ) {
          modTime = session.getFTPBean().time( remoteFile );
        }
        else {
          Calendar remoteFileCal = remoteFile.getDate();
          if ( remoteFileCal != null ) {
            modTime = remoteFileCal.getTime();
          }
        }
      }
      catch ( FTPNoSuchFileException remoteFileNotFound ) {
        ErrorDialog.showDialog( new LString("DataTransfer.file_not_found",
                                            "File not found.") );
        result.setCode( SecureFTPError.NO_SUCH_FILE );
        return result;
      }
      catch ( FTPException noTime ) {
        session.setSupportsMDTM( false );
      }

      long fileSize = remoteFile.getFileSize();
      try {
        if ( fileSize < 0 && session.supportsSIZE() ) {
          fileSize = session.getFTPBean().size( remoteFile );
        }

        if ( fileSize < 0 ) {
          fileSize = 0;
        }
      }
      catch ( FTPNoSuchFileException remoteFileNotFound ) {
        ErrorDialog.showDialog( new LString("DataTransfer.file_not_found",
                                            "File not found.") );
        result.setCode( SecureFTPError.NO_SUCH_FILE );
        return result;
      }
      catch ( FTPException noSize ) {
        session.setSupportsSIZE( false );
      }
 
      if ( session.testForREST() ) {
        session.setTestForREST( false );
        session.setSupportsREST( session.getFTPBean().isTransferRestartable() );
      }

      session.getFTPBean().setSendCmdStream(session.getOutputStream());
      session.getFTPBean().setRecvCmdStream(session.getOutputStream());

      boolean resumeDownload = false;

      if ( !weCreatedTheFile ) {
        boolean resumable = false;

        // we can only resume a transfer during a binary mode
        if ( session.supportsREST() && fileSize > 0 && 
             fileSize > newFile.length() &&
             (session.getFTPBean().getTransferMode() == 
                                                     FTP.BINARY_TRANSFER_MODE ||
             (session.getFTPBean().getTransferMode() ==
                                                     FTP.AUTO_TRANSFER_MODE &&
             !FileTypeDecider.isAscii(newFile.getName()))) ) {
          resumable = true;
         }

        if ( GTOverride.getBoolean("glub.resume_xfer.disabled") )
          resumable = false;

        int r = FileExistsDialog.SKIP;

        if ( !resumeAll && !replaceAll && !skipAll ) {
          r = 
            FileExistsDialog.showDialog( FileExistsDialog.DIRECTION_GET,
                                         newFile.getName(), 
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

        resumeDownload = ( FileExistsDialog.RESUME == r || 
                           FileExistsDialog.RESUME_ALL == r );

        resumeAll = FileExistsDialog.RESUME_ALL == r;
        replaceAll = FileExistsDialog.REPLACE_ALL == r;
      }

      FTPAbortableTransfer abort = new FTPAbortableTransfer();

      progress.setFileName( remoteFileStr );
      progress.setAbortableTransfer( abort );

      session.getFTPBean().retrieve( remoteFile, newFile, 
                                     resumeDownload, progress, abort );
    }
    catch ( IOException ioe ) {
      if ( weCreatedTheFile ) {
        //newFile.delete(); 
      }

      LString msg = new LString("DataTransfer.transfer_failed",
                                "The data transfer failed: [^0]");
      msg.replace( 0, ioe.getMessage() );
      ErrorDialog.showDialog( msg );

      result.setCode( SecureFTPError.IO_EXCEPTION );
    }
    catch ( FTPAbortException fe ) {
      if ( weCreatedTheFile ) {
        //newFile.delete(); 
      }

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

      if ( weCreatedTheFile ) {
        //newFile.delete(); 
      }

      LString msg = new LString("DataTransfer.transfer_failed",
                                "The data transfer failed: [^0]");
      msg.replace( 0, fe.getMessage() );
      ErrorDialog.showDialog( msg );
      result.setCode( SecureFTPError.DOWNLOAD_FAILED );
    }
    finally {
      if ( modTime != null ) {
          newFile.setLastModified( modTime.getTime() );
      }
    }

    return result;
  }
}

