
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: MDeleteCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.gui.*;
import com.glub.util.*;

import javax.swing.*;
import java.awt.Cursor;
import java.io.*;
import java.util.*;

public class MDeleteCommand extends NetworkCommand {
  private boolean displayedWarning = false;
  private static int level = 0;

  public MDeleteCommand() {
    super("mdelete", CommandID.MDELETE_COMMAND_ID, 2, 2, 
          "remote-file session",
          "delete multiple files and/or directories");
    //setBeepWhenDone( true );
  }

  public MDeleteCommand( boolean displayedWarning ) {
    this();
    this.displayedWarning = displayedWarning;
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    if ( level == 0 ) {
      SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.WAIT_CURSOR) );
    }

    RemoteFileList files = (RemoteFileList)getArgs().get(0);
    FTPSession session = (FTPSession)getArgs().get(1);

    //String pwd = ((RemotePanel)session.getRemoteUI()).getCurrentDirectory();

    if ( !displayedWarning ) {
      displayedWarning = true;

      LString msg = null;

      if ( files.size() == 1 && !files.getFile(0).isDirectory() ) {
        msg = new LString("DeleteDialog.confirmation.one_file",
                          "You are about to permanently delete: [^0]");
        msg.replace( 0, files.getFile(0).getFileName() );
      }
      else {
        msg = 
          new LString("DeleteDialog.confirmation.mult_files",
                      "You are about to permanently delete multiple files.");
      }

      LString title = new LString("DeleteDialog.confirmation.title",
                                  "Delete Confirmation");

      Object[] options = { LString.getString("Common.button.ok", "OK"),
                          LString.getString("Common.button.cancel", "Cancel") };
      int r = JOptionPane.showOptionDialog( SecureFTP.getBaseFrame(),
                                            new GTLabel(msg, 400),
                                            title.getString(),
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.WARNING_MESSAGE,
                                            null, options, options[1] );

      if ( JOptionPane.YES_OPTION == r ) {
      }
      else {
        result.setCode( SecureFTPError.DELETE_ABORTED );
        level = 0;
        SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
        return result;
      }
    }

    for( int i = 0; i < files.size(); i++ ) {
      RemoteFile fileName = files.getFile(i);

      if ( fileName.isDirectory() || fileName.isLink() ||
           !fileName.isKnownFileType() ) {
        RemoteFileList fileList = new RemoteFileList();

        session.getFTPBean().setSendCmdStream(null);
        session.getFTPBean().setRecvCmdStream(null);

        boolean fileIsDir = fileName.isDirectory();

        boolean changedDir = false;

        try {
          ByteArrayOutputStream baosSend = new ByteArrayOutputStream();
          ByteArrayOutputStream baosRecv = new ByteArrayOutputStream();
          session.getFTPBean().setSendCmdStream(baosSend);
          session.getFTPBean().setRecvCmdStream(baosRecv);
          session.getFTPBean().chdir(fileName);
          changedDir = true;
          session.getOutputStream().write(baosSend.toByteArray());
          session.getOutputStream().write(baosRecv.toByteArray());
          fileIsDir = true;
        }
        catch ( Exception e ) {
          fileIsDir = false;
        }

        if ( fileIsDir ) {
          try {
            fileList.clear();
            fileList = session.getFTPBean().listAll();
          }
          catch ( Exception e ) {
          }
        }

        session.getFTPBean().setSendCmdStream(session.getOutputStream());
        session.getFTPBean().setRecvCmdStream(session.getOutputStream());

        if ( fileList.size() == 0 ) {
          ErrorDialog.showDialog( new LString("DeleteDialog.no_file",
                                              "No such file or directory.") );
          result.setCode( SecureFTPError.NO_SUCH_FILE );
        }

        for ( int j = 0; j < fileList.size(); j++ ) {
          if ( fileList.getFile(j).getFileName().equals(".") ||
               fileList.getFile(j).getFileName().equals("..") ) {
            continue;
          }
          else {
            try {
              MDeleteCommand mdel = new MDeleteCommand( true );
              level++;
              ArrayList args = new ArrayList(1);
              RemoteFileList newList = new RemoteFileList();
              newList.add( fileList.getFile(j) );
              args.add( newList );
              args.add(session);
              mdel.setArgs( args );
              result = SecureFTP.getCommandDispatcher().fireCommand(this, mdel);

              if ( SecureFTPError.OK != result.getCode() ) {
                break;
              }
            }
            catch ( Exception e ) {
            }
            finally {
              level--;
            }
          }
        }

        if ( fileIsDir ) {
          try {
            if ( changedDir ) {
              session.getFTPBean().cdup();
            }
            session.getFTPBean().rmdir(fileName);
          }
          catch ( FTPConnectionLostException fcle ) {
            SecureFTP.getCommandDispatcher().fireCommand(this, 
                                                         new CloseCommand());
            ErrorDialog.showDialog( new LString("Common.connection_lost",
                                                "Connection lost.") );
            result.setCode( SecureFTPError.NOT_CONNECTED ); 
            return result;
          } 
          catch ( FTPException fe ) {
            LString msg = new LString("Common.unknown.error",
                                      "An error has occurred: [^0]");
            msg.replace( 0, fe.getMessage() );
            ErrorDialog.showDialog( msg );

            result.setCode( SecureFTPError.UNKNOWN );
          }
          catch ( Exception e ) {}
        }
      }
      else {
        DeleteCommand del = new DeleteCommand();
        ArrayList args = new ArrayList(1);
        args.add( fileName );
        args.add( session );
        del.setArgs( args );
        result = SecureFTP.getCommandDispatcher().fireCommand(this, del);
      }
    }

    if ( 0 == level ) {
      SecureFTP.getCommandDispatcher().fireCommand(this, new LsCommand()); 
      SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
    }

    return result;
  }
}

