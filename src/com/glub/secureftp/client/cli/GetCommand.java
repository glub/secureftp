
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: GetCommand.java 99 2009-10-10 01:00:36Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.util.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.util.*;

public class GetCommand extends NetworkCommand {
  private static boolean supportsMDTM = true;
  private static boolean supportsSIZE = true;
  private static boolean testForREST = true;
  private static boolean supportsREST = false;
  private boolean isSyncing = false;

  public GetCommand() {
    this( "get", CommandID.GET_COMMAND_ID );
  }

  public GetCommand( boolean sync ) {
    this();
    isSyncing = sync;
    setBeepWhenDone( !sync );
  }

  public GetCommand( String commandName, short id ) {
    super(commandName, id, 1, 2, "remote-file [local-file]", "receive file");
    setBeepWhenDone( true );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    ProgressThread pt = null;

    boolean weCreatedTheFile = false;
    File newFile = null;

    try {
      Object r = getArgs().get(0);

      RemoteFile remoteFile = null;
      String remoteFileStr = null;

      if ( r instanceof RemoteFile ) {
        remoteFile = (RemoteFile)r;
        remoteFileStr = remoteFile.getFileName();
      }
      else if ( r instanceof String )
        remoteFileStr = (String)r;

      if ( session.isGlobOn() && remoteFileStr != null && 
    	   remoteFileStr.indexOf('*') >= 0 ) {
        if ( ! session.isDebugOn() ) {
          session.getFTPBean().setSendCmdStream(null);
          session.getFTPBean().setRecvCmdStream(null);
        }

        RemoteFileList dirList = new RemoteFileList();
        boolean remoteFileFound = true;

        try {
          dirList = session.getFTPBean().list( remoteFileStr );
        }
        catch ( FTPException listException ) {
          remoteFileFound = false;
        }

        if ( ! session.isDebugOn() ) {
          session.getFTPBean().setSendCmdStream(session.getOutputStream());
          session.getFTPBean().setRecvCmdStream(session.getOutputStream());
        }

        for ( int i = 0; i < dirList.size(); i++ ) {
          RemoteFile tempFile = dirList.getFile(i);
          if ( (tempFile.isFile() || tempFile.isLink()) ) {
              remoteFileStr = tempFile.getFileName();
              break;
          } 
        }

        if ( !remoteFileFound || remoteFileStr.equals(".") ) {
          throw new FTPException("File not found.");
        }
      }

      String newFileStr = null;
      if ( getArgs().size() == 2 ) {
        newFileStr = (String)getArgs().get(1);
      }
      else {
        newFileStr = remoteFileStr;
        StringTokenizer tok = new StringTokenizer(newFileStr, "/");
        while (tok.hasMoreTokens()) {
          newFileStr = tok.nextToken(); 
        }
      }

      // sanitize filename if needed
      newFileStr = Util.searchAndReplace( newFileStr, "\\", "_", true );

      newFile = new File( newFileStr );

      if ( !newFile.isAbsolute() ) {
        newFile = new File( session.getLocalDir(), newFileStr );
      }

      weCreatedTheFile = !newFile.exists();

      Date modTime = null;

      if ( !session.isDebugOn() ) {
        session.getFTPBean().setSendCmdStream(null);
        session.getFTPBean().setRecvCmdStream(null);
      }

      try {
        if ( supportsMDTM ) {
          modTime = session.getFTPBean().time( remoteFileStr );
        }
      }
      catch ( FTPNoSuchFileException remoteFileNotFound ) {
/*
        out.println("File not found.");
        return;
*/
      }
      catch ( FTPException noTime ) {
        supportsMDTM = false;
      }

      long fileSize = 0;
      try {
        if ( supportsSIZE ) {
          fileSize = session.getFTPBean().size( remoteFileStr );
        }

        if ( remoteFile != null && fileSize <= 0 ) {
          fileSize = remoteFile.getFileSize();
        }
        else if ( fileSize < 0 ) {
          fileSize = 0;
        }
      }
      catch ( FTPNoSuchFileException remoteFileNotFound ) {
/*
        out.println("File not found.");
        return;
*/
      }
      catch ( FTPException noSize ) {
        supportsSIZE = false;
      }
 
      if ( testForREST ) {
        testForREST = false;
        supportsREST = session.getFTPBean().isTransferRestartable();
      }

      if ( ! session.isDebugOn() ) {
        session.getFTPBean().setSendCmdStream(session.getOutputStream());
        session.getFTPBean().setRecvCmdStream(session.getOutputStream());
      }

      boolean resumeDownload = false;

      if ( isSyncing && !weCreatedTheFile ) {
        if ( modTime != null ) {
          // if the remote file is older or the same
          if (modTime.getTime() <= newFile.lastModified()) {
            // skip it
            return result; 
          }
        } 
        else if ( remoteFile != null ) {
          // if the remote file is older or the same
          Calendar c = remoteFile.getDate();
          if ( c == null || c.getTime().getTime() <= newFile.lastModified() ) {
            // skip it
            return result; 
          }
        }
        else {
          // we can't find a remote date/time
          // skip it
          result.setCode( SecureFTPError.SYNC_NOT_SUPPORTED );
          return result;
        }
      }
      else if ( session.isInteractiveOn() && !weCreatedTheFile ) {
        if ( supportsREST && fileSize > 0 && fileSize > newFile.length() &&
             (session.getFTPBean().getTransferMode() == 
              FTP.BINARY_TRANSFER_MODE ||
              (session.getFTPBean().getTransferMode() ==
               FTP.AUTO_TRANSFER_MODE && 
               !FileTypeDecider.isAscii(newFile.getName()))) ) {
          out.println(newFile.getName() + " exists.");

          String msg = "The remote file is larger than the local file.";
          if ( modTime != null ) {
            msg = "The remote file is older than the local file.";
            if ( modTime.getTime() > newFile.lastModified() ) {
              msg = "The remote file is newer than the local file.";
            }
          }

          if ( !GTOverride.getBoolean("glub.resume_xfer.disabled") ) 
            resumeDownload = CLIUtil.yesNoPrompt( msg + " Resume transfer?" );

          if ( !resumeDownload ) {
            if (!CLIUtil.yesNoPrompt("Do you want to replace it?")) {
              out.println("Download aborted.");
              return result;
            }
          }
        }
        else if (!CLIUtil.yesNoPrompt(newFile.getName() + 
                                      " exists. Replace?")) {
          out.println("Download aborted.");
          return result; 
        }
      }
      else if ( !session.isInteractiveOn() && !weCreatedTheFile &&
                GTOverride.getBoolean("glub.default.prompt.no") ) {
        out.println("\"" + newFile.getName() + "\" exists. Skipping...");
        return result;
      }

      if ( session.showProgress() ) {
        pt = new ProgressThread();
        pt.start();
      }

      if ( remoteFile == null ) {
        remoteFile = new RemoteFile( remoteFileStr );
        if ( fileSize > 0 ) {
          remoteFile.setFileSize( fileSize );
        }
      }

      session.getFTPBean().retrieve( remoteFile, newFile, 
                                     resumeDownload, pt );

      if ( modTime != null ) {
          newFile.setLastModified( modTime.getTime() );
      }
    }
    catch ( IOException ioe ) {
      if ( weCreatedTheFile ) {
        newFile.delete(); 
      }

      result.setCode( SecureFTPError.DOWNLOAD_FAILED );
      out.println(ioe.getMessage());
    }
    catch ( FTPException fe ) {
      if ( weCreatedTheFile ) {
        newFile.delete(); 
      }

      result.setCode( SecureFTPError.DOWNLOAD_FAILED );
      out.println(fe.getMessage());
    }
    finally {
      if ( session.showProgress() && null != pt && pt.isRunning() ) {
        pt.finishProgress();
      }
    }

    return result;
  }
}

class RecvCommand extends GetCommand {
  public RecvCommand() {
    super("recv", CommandID.RECV_COMMAND_ID);
  }
}

