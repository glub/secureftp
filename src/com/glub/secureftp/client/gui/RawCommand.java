
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RawCommand.java 141 2009-12-16 03:53:52Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.awt.*;
import javax.swing.*;

public class RawCommand extends NetworkCommand {
  public RawCommand() {
    super("raw", CommandID.RAW_COMMAND_ID, 0, 1,  
          "[command]", "send raw command");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    if ( !Client.getAllowRaw() ) {
      result.setCode( SecureFTPError.PERMISSION_DENIED );
      return result;
    }

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    String cmd = "";

    if ( null != getArgs() && getArgs().size() == 1 ) {
      cmd = (String)getArgs().get(0);
    }
    else {
      JLabel label =
        new JLabel( LString.getString("RawCommand.label", "Command:") );

      String title = LString.getString("RawCommand.title", "Send Raw Command");

      Object r = 
        JOptionPane.showInputDialog( SecureFTP.getBaseFrame(), 
                                     label,
                                     title,
                                     JOptionPane.PLAIN_MESSAGE, 
                                     null /* icon */, 
                                     null /* options */, 
                                     null );


      if ( null != r && r.toString().trim().length() > 0 ) {
        cmd = r.toString().trim();
      }
    }

    try {
      if ( cmd.length() > 0 )
        session.getFTPBean().raw( cmd );
    }
    catch ( IllegalArgumentException iae ) {
      throw new CommandException( getUsage() );
    }
    catch ( FTPConnectionLostException fcle ) {
      SecureFTP.getCommandDispatcher().fireCommand( this, new CloseCommand() );
      ErrorDialog.showDialog( new LString("Common.connection_lost",
                                          "Connection lost.") );
      result.setCode( SecureFTPError.NOT_CONNECTED );
    }
    catch ( FTPException fe ) {
      if ( !suppressErrors() ) {
        LString lmsg = LString.getLocalizedString( fe.getMessage() );
        ErrorDialog.showDialog( lmsg );
      }

      result.setCode( SecureFTPError.UNKNOWN );
      result.setMessage( fe.getMessage() );
    }

    return result;
  }
}

