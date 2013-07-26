
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LMDeleteCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.gui.*;
import com.glub.util.*;

import javax.swing.*;
import java.awt.Cursor;
import java.io.*;
import java.util.*;

public class LMDeleteCommand extends LocalCommand {
  private boolean displayedWarning = false;
  private static int level = 0;

  public LMDeleteCommand() {
    super("lmdelete", CommandID.LMDELETE_COMMAND_ID, 1, 1, "local-file",
          "delete multiple local files or directories");
  }

  public LMDeleteCommand( boolean displayedWarning ) {
    this();
    this.displayedWarning = displayedWarning;
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    if ( 0 == level ) {
      SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.WAIT_CURSOR) );
    }

    ArrayList filesToDel = (ArrayList)getArgs().get(0);

    if ( !displayedWarning ) {
      displayedWarning = true;

     LString msg = null;

      if ( filesToDel.size() == 1 && 
           !((File)filesToDel.get(0)).isDirectory() ) {
        msg = new LString("DeleteDialog.confirmation.one_file",
                          "You are about to permanently delete \"[^0]\".");
        msg.replace( 0, ((File)filesToDel.get(0)).getName() );
      }
      else {
        msg = new LString("DeleteDialog.confirmation.mult_files",
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

    for( int i = 0; i < filesToDel.size(); i++ ) {
      File fileToDel = (File)filesToDel.get(i);

      if ( !fileToDel.exists() ) {
        ErrorDialog.showDialog( new LString("DeleteDialog.no_file",
                                            "No such file or directory.") );
        result.setCode( SecureFTPError.NO_SUCH_FILE );
      }
      else if ( fileToDel.isDirectory() ) {
        File[] files = fileToDel.listFiles();
        if ( files == null ) {
          LString msg = 
            new LString("DeleteDialog.permission_denied",
                        "Permission denied: Deletion of \"[^0]\" failed.");
          msg.replace( 0, fileToDel.getName() );
          ErrorDialog.showDialog( msg );
          continue;      
        }

        for ( int j = 0; j < files.length; j++ ) {
          LMDeleteCommand ldc = new LMDeleteCommand( true );
          level++;
          ArrayList args = new ArrayList(1);
          ArrayList newDir = new ArrayList(1);
          newDir.add( files[j] );
          args.add( newDir );
          ldc.setArgs( args );

          result = SecureFTP.getCommandDispatcher().fireCommand( this, ldc );  
          level--;
 
          if ( SecureFTPError.PERMISSION_DENIED == result.getCode() ) {
            break;
          }
        }

        if ( !fileToDel.delete() ) {
          LString msg = 
            new LString("DeleteDialog.permission_denied",
                        "Permission denied: Deletion of \"[^0]\" failed.");
          msg.replace( 0, fileToDel.getName() );
          ErrorDialog.showDialog( msg );
          result.setCode( SecureFTPError.PERMISSION_DENIED );
        }
      }
      else {
        LDeleteCommand ldc = new LDeleteCommand();
        ArrayList args = new ArrayList(1);
        args.add( fileToDel );
        ldc.setArgs( args );
  
        result = SecureFTP.getCommandDispatcher().fireCommand( this, ldc );  

        if ( SecureFTPError.PERMISSION_DENIED == result.getCode() ) {
          LString msg = 
            new LString("DeleteDialog.permission_denied",
                        "Permission denied: Deletion of \"[^0]\" failed.");
          msg.replace( 0, fileToDel.getName() );
          ErrorDialog.showDialog( msg );
          result.setCode( SecureFTPError.PERMISSION_DENIED );
          return result;
        }
      }
    }

    if ( 0 == level ) {
      Client.getLocalView().refresh();
      Client.getLocalView().setFocus();
      SecureFTP.getBaseFrame().setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );
    }

    return result;
  }
}

