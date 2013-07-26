
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: DeleteCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

public class DeleteCommand extends NetworkCommand {
  public DeleteCommand() {
    super("delete", CommandID.DELETE_COMMAND_ID, 2, 2, 
          "remote-file session", "delete remote file");
    setBeepWhenDone( false );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    try {
      boolean deleteIt = true;

      RemoteFile fileToDelete = (RemoteFile)getArgs().get(0);
      FTPSession session = (FTPSession)getArgs().get(1);

      if ( deleteIt ) {
        session.getFTPBean().delete( fileToDelete );
      }
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

    return result;
  }
}

