
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: MkDirCommand.java 124 2009-12-06 00:47:34Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.awt.*;
import javax.swing.*;

public class MkDirCommand extends NetworkCommand {
  public MkDirCommand() {
    super("mkdir", CommandID.MKDIR_COMMAND_ID,
          "make directory on the remote machine");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    JLabel name = 
      new JLabel( LString.getString("MkDirDialog.label.name", "Directory:") );

    String title = LString.getString("Common.button.new_folder", "New Folder");

    Object r = 
      JOptionPane.showInputDialog( SecureFTP.getBaseFrame(), 
                                   name, 
                                   title,
                                   JOptionPane.PLAIN_MESSAGE, 
                                   null /* icon */, 
                                   null /* options */, 
                                   null /* default text */ );

    if ( null != r && r.toString().trim().length() > 0 ) {
      try {
        session.getFTPBean().mkdir( r.toString().trim() );
        DataTransferManager.getInstance().list( session );
        //RemoteFile rf = new RemoteFile( dirName );
        //((RemotePanel)session.getRemoteUI()).getTableView().selectFile( rf );
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

