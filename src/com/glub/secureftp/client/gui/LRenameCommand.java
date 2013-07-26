
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LRenameCommand.java 124 2009-12-06 00:47:34Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.awt.*;
import java.io.*;
import javax.swing.*;

public class LRenameCommand extends LocalCommand {
  public LRenameCommand() {
    super("lrename", CommandID.LRENAME_COMMAND_ID, 1, 1, "from",
          "rename remote-file");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    final File file = (File)getArgs().get(0);

    JLabel name = 
      new JLabel( LString.getString("RenameDialog.label.name", "Name:") );

    String title = LString.getString("Common.button.rename", "Rename");

    Object r = 
      JOptionPane.showInputDialog( SecureFTP.getBaseFrame(), 
                                   name, 
                                   title,
                                   JOptionPane.PLAIN_MESSAGE, 
                                   null /* icon */, 
                                   null /* options */, 
                                   file.getName() );

    if ( null != r && !file.getName().equals(r.toString().trim()) ) {
      File newFile = new File(file.getParentFile(), r.toString().trim());
      try {
        if ( !file.renameTo(newFile) ) {
          ErrorDialog.showDialog( new LString("MkDirDialog.permission_denied", 
                                              "Permission denied.") );
        } 
        Client.getLocalView().refresh();
        Client.getLocalView().selectFile( newFile );
        Client.getLocalView().setFocus();
      }
      catch ( IllegalArgumentException iae ) {
        throw new CommandException( getUsage() );
      }
    }

    return result;
  }
}

