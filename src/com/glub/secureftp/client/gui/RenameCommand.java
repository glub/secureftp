
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RenameCommand.java 124 2009-12-06 00:47:34Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.gui.*;
import com.glub.util.*;

import java.awt.*;
import javax.swing.*;

public class RenameCommand extends NetworkCommand {
  public RenameCommand() {
    super("rename", CommandID.RENAME_COMMAND_ID, 1, 1, "from",
          "rename remote-file");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    final RemoteFile file = (RemoteFile)getArgs().get(0);

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
                                   file.getFileName() );

    if ( null != r && !file.getFileName().equals(r.toString().trim()) ) {
      try {
        session.getFTPBean().rename( file.getFileName(), r.toString().trim() );
        DataTransferManager.getInstance().list( session );
      }
      catch ( IllegalArgumentException iae ) {
        throw new CommandException( getUsage() );
      }
      catch ( FTPConnectionLostException fcle ) {
        SecureFTP.getCommandDispatcher().fireCommand(this, new CloseCommand());
        ErrorDialog.showDialog( new LString("Common.connection_lost",
                                            "Connection lost.") );
        result.setCode( SecureFTPError.NOT_CONNECTED ); 
      } 
      catch ( FTPException fe ) {
        LString msg = new LString("Common.unknown.error",
                                  "An error has occurred: [^0]");
        msg.replace( 0, fe.getMessage() );
        ErrorDialog.showDialog( msg );

        result.setCode( SecureFTPError.UNKNOWN );
      }
    }

    return result;
  }
}

