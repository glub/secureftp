
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: PutCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.util.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.util.*;

public class PutCommand extends NetworkCommand {
  private static boolean supportsSIZE = true;
  private static boolean supportsMDTM = true;
  private static boolean testForREST = true;
  private static boolean supportsREST = false;
  private boolean isSyncing = false;

  public PutCommand() {
    this( "put", CommandID.PUT_COMMAND_ID );
  }

  public PutCommand( boolean sync ) {
    this();
    isSyncing = sync;
  }

  public PutCommand( String commandName, short id ) {
    super(commandName, id, 1, 2, "local-file [remote-file]", "send one file");
    setBeepWhenDone( true );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    ProgressThread pt = null;

    try {
      File currentDir = session.getLocalDir();

      String newFileStr = (String)getArgs().get(0);
      String fileName = null;

      File newFile = new File(newFileStr);
      if ( !newFile.isAbsolute() )
        newFile = new File( currentDir, newFileStr );

      if ( GTOverride.getBoolean("glub.zerobyte.put.ignore") ) {
        if (newFile.length() == 0) {
          out.println("Skipping empty file.");
          return result;   
        } 
      }

      if ( session.isGlobOn() && !newFile.exists() ) {
        try {
          File[] fileGlob =
            CLIUtil.globLocalPathForFiles( newFile.getAbsolutePath(),
                                           CLIUtil.GLOB_ONLY_FILES );

	  if ( fileGlob.length > 0 ) {
            newFile = fileGlob[0];
	  }
	}
	catch ( FileNotFoundException fne ) {}
      }

      if ( getArgs().size() > 1 ) {
        fileName = (String)getArgs().get(1); 
      }
      else {
        fileName = newFile.getName();
      } 

      boolean remoteFileExists = false;

      if ( !session.isDebugOn() ) {
        session.getFTPBean().setSendCmdStream(null);
        session.getFTPBean().setRecvCmdStream(null);
      }

      long fileSize = 0;
      if ( supportsSIZE ) {
        short transferMode = session.getFTPBean().getTransferMode();

        if ( transferMode != FTP.BINARY_TRANSFER_MODE ) {
          session.getFTPBean().binary();
        }

        try {
          fileSize = session.getFTPBean().size( fileName );
          remoteFileExists = true;

          if ( fileSize < 0 ) {
            fileSize = 0;
          }
        }
        catch ( FTPNoSuchFileException remoteFileNotFound ) {
          remoteFileExists = false;
        }
        catch ( FTPException noSize ) {
          supportsSIZE = false;
        }

        if ( transferMode == FTP.ASCII_TRANSFER_MODE ) {
          session.getFTPBean().ascii();
        }
        else if ( transferMode == FTP.EBCDIC_TRANSFER_MODE ) {
          session.getFTPBean().ebcdic();
        }
        else if ( transferMode == FTP.AUTO_TRANSFER_MODE ) {
          session.getFTPBean().auto();
        }
      }

     Date remoteModTime = null;

     try {
       if ( supportsMDTM ) {
         remoteModTime = session.getFTPBean().time( fileName );
         remoteFileExists = true;
       }
     }
     catch ( FTPNoSuchFileException nsfe ) {
       remoteFileExists = false;
     }
     catch ( FTPException fe ) {
       supportsMDTM = false;
     }

     RemoteFile remoteFile = null;

     if ( !supportsMDTM ) {
       RemoteFileList listing = new RemoteFileList();

       try {
         listing = session.getFTPBean().list( fileName );
       }
       catch ( FTPException listException ) {
         remoteFileExists = false;
       }

       for ( int i = 0; i < listing.size(); i++ ) {
         RemoteFile tempFile = listing.getFile(i);
         if ( tempFile.getFileName().equals(fileName) ) {
           remoteFile = tempFile;
           remoteFileExists = true;
           break;
         }
       }
     }

      if ( testForREST ) {
        testForREST = false;
        supportsREST = session.getFTPBean().isTransferRestartable();
      }

      session.getFTPBean().setSendCmdStream(session.getOutputStream());
      session.getFTPBean().setRecvCmdStream(session.getOutputStream());

      boolean resumeUpload = false;

      if ( isSyncing && (remoteFile != null || remoteFileExists) ) {
        if ( remoteModTime != null ) {
          // if the local file is older or the same
          if (newFile.lastModified() <= remoteModTime.getTime()) {
            // skip it
            return result;
          }
        }
        else if ( remoteFile != null ) {
          // if the local file is older or the same
          Calendar c = remoteFile.getDate();
          if ( c == null || newFile.lastModified() <= c.getTime().getTime() ) {
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
      else if ( session.isInteractiveOn() && remoteFileExists ) {
        if ( supportsREST && fileSize > 0 && newFile.length() > fileSize &&
             (session.getFTPBean().getTransferMode() ==
              FTP.BINARY_TRANSFER_MODE ||
              (session.getFTPBean().getTransferMode() ==
               FTP.AUTO_TRANSFER_MODE &&
               !FileTypeDecider.isAscii(newFile.getName()))) ) {
          out.println("\"" + newFile.getName() + "\" exists.");

          if ( !GTOverride.getBoolean("glub.resume_xfer.disabled") ) {
            String msg = 
              "The remote file is smaller than local file. Resume transfer?";
            resumeUpload = CLIUtil.yesNoPrompt( msg, CLIUtil.YN_YES );
          }

          if ( !resumeUpload ) {
            if (!CLIUtil.yesNoPrompt("Do you want to replace it?")) {
              out.println("Upload aborted.");
              return result;
            }
          }
        }
        else if (!CLIUtil.yesNoPrompt("\"" + newFile.getName() +
                                      "\" exists. Replace?")) {
          out.println("Upload aborted.");
          return result;
        }
      }
      else if ( !session.isInteractiveOn() && remoteFileExists &&
                GTOverride.getBoolean("glub.default.prompt.no") ) {
        out.println("\"" + newFile.getName() + "\" exists. Skipping...");
        return result;
      }

      if ( session.showProgress() ) {
        pt = new ProgressThread();
        pt.start();
      }

      session.getFTPBean().store( newFile, fileName, resumeUpload, pt );
    }
    catch ( IOException ioe ) {
      out.println(ioe.getMessage());
      result.setCode( SecureFTPError.UPLOAD_FAILED );
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
      result.setCode( SecureFTPError.UPLOAD_FAILED );
    }
    finally {
      if ( session.showProgress() && null != pt && pt.isRunning() ) {
        pt.finishProgress();
      }
    }

    return result;
  }
}

class SendCommand extends PutCommand {
  public SendCommand() {
    super("send", CommandID.SEND_COMMAND_ID);
  }
}

