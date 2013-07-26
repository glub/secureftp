
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CDCommand.java 124 2009-12-06 00:47:34Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.awt.*;
import javax.swing.*;

public class CDCommand extends NetworkCommand {
  public CDCommand() {
    super("cd", CommandID.CD_COMMAND_ID, 0, 1,  
          "[remote-directory]", "change remote working directory");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();
    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();

    RemoteFile dirToChangeTo = null;

    if ( null != getArgs() && getArgs().size() == 1 ) {
      dirToChangeTo = (RemoteFile)getArgs().get(0);
    }
    else {
      JLabel goTo =
        new JLabel( LString.getString("CDCommand.chdir", "Go to:") );

      final String defaultName = 
        ((RemotePanel)session.getRemoteUI()).getCurrentDirectory();

      String title = LString.getString("Common.button.chdir", "Go to Folder");

      Object r = 
        JOptionPane.showInputDialog( SecureFTP.getBaseFrame(), 
                                     goTo, 
                                     title,
                                     JOptionPane.PLAIN_MESSAGE, 
                                     null /* icon */, 
                                     null /* options */, 
                                     defaultName );

      if ( null != r && r.toString().trim().length() > 0 && 
           ! r.toString().trim().equals( defaultName ) ) {
        dirToChangeTo = new RemoteFile( r.toString().trim() );
        return ((RemotePanel)session.getRemoteUI()).changeDirectory( 
                                                         dirToChangeTo, false );
      }
      else {
        return result;
      }
    }

    try {
      session.getFTPBean().chdir( dirToChangeTo.getFileName() );
    }
    catch ( IllegalArgumentException iae ) {
      throw new CommandException( getUsage() );
    }
    catch ( FTPNoSuchFileException fnsfe ) {
      LString lmsg = new LString("CDCommand.noSuchDir",
                                 "[^0]: No such directory");
      lmsg.replace(0, dirToChangeTo.getFileName());

      if ( !suppressErrors() ) {
        ErrorDialog.showDialog( lmsg );
      }
      result.setCode( SecureFTPError.NOT_A_DIRECTORY );
      result.setMessage( lmsg.getString() );
    }
    catch ( FTPNotADirectoryException fnde ) {
      LString lmsg = new LString("CDCommand.noSuchDir",
                                 "[^0]: No such directory.");
      lmsg.replace(0, dirToChangeTo.getFileName());

      if ( !suppressErrors() ) {
        ErrorDialog.showDialog( lmsg );
      }
      result.setCode( SecureFTPError.NOT_A_DIRECTORY );
      result.setMessage( lmsg.getString() );
    }
    catch ( FTPPermissionDeniedException fpde ) {
      LString lmsg = new LString("CDCommand.permission_denied",
                                 "[^0]: Permission denied.");
      lmsg.replace(0, dirToChangeTo.getFileName());

      if ( !suppressErrors() ) {
        ErrorDialog.showDialog( lmsg );
      }

      result.setCode( SecureFTPError.PERMISSION_DENIED );
      result.setMessage( lmsg.getString() );
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

