
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LMkDirCommand.java 124 2009-12-06 00:47:34Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.awt.*;
import java.io.*;
import javax.swing.*;

public class LMkDirCommand extends LocalCommand {
  public LMkDirCommand() {
    super("lmkdir", CommandID.LMKDIR_COMMAND_ID, 0, 2, "[directory session]",
          "make directory on the loacl machine");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    String dirName = "";

    File currentDir = Client.getLocalView().getCurrentDirectory();

    if ( null == currentDir ) {
      return result;
    }

    if ( null != getArgs() && getArgs().size() == 1 ) {
      dirName = (String)getArgs().get(0);
    }
    else if ( null != getArgs() && getArgs().size() == 2 ) {
      dirName = (String)getArgs().get(0);
      FTPSession session = (FTPSession)getArgs().get(1);
      currentDir = session.getLocalDir();
    }
    else {
      JLabel name = 
        new JLabel( LString.getString("MkDirDialog.label.name", "Directory:") );

      String title = LString.getString("Common.button.new_folder",
                                       "New Folder");
      Object r = 
        JOptionPane.showInputDialog( SecureFTP.getBaseFrame(), 
                                     name, 
                                     title,
                                     JOptionPane.PLAIN_MESSAGE, 
                                     null /* icon */, 
                                     null /* options */, 
                                     null /* default text */ );

      if ( null != r && r.toString().trim().length() > 0 ) {
        dirName = r.toString().trim();
      }
    }

    if ( (null != getArgs() && getArgs().size() >= 1) ||  
         dirName.length() > 0 ) {
      try {
        File newDir = new File( currentDir, dirName );

        if ( newDir.exists() ) {
          result.setCode( SecureFTPError.DIRECTORY_EXISTS );
          ErrorDialog.showDialog( 
            new LString("MkDirDialog.dir_exists", "Directory exists.") );
        }
        else if ( !newDir.mkdir() ) {
          result.setCode( SecureFTPError.PERMISSION_DENIED );
          ErrorDialog.showDialog( 
            new LString("MkDirDialog.permission_denied", "Permission denied."));
        }

        Client.getLocalView().refresh();
        Client.getLocalView().selectFile( newDir );
        Client.getLocalView().setFocus();
      }
      catch ( IllegalArgumentException iae ) {
        throw new CommandException( getUsage() );
      }
    }

    return result;
  }
}

