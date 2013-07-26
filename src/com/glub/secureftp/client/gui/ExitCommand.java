
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ExitCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

public class ExitCommand extends Command {
  public ExitCommand() {
    this("exit", CommandID.EXIT_COMMAND_ID);
  }

  public ExitCommand( String commandName, short id ) {
    super(commandName, id, "terminate ftp session and exit");
  }

  public SecureFTPError doIt() throws CommandException {
    while( FTPSessionManager.getInstance().hasOpenSessions() ) {
      SecureFTP.getCommandDispatcher().fireCommand( this, new CloseCommand() );
    }
    
    PreferencesDispatcher.doWritePrefs();
    System.exit( 0 );
    return new SecureFTPError();
  }
}

