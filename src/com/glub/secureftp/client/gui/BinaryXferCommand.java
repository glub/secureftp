
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: BinaryXferCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;
import com.glub.util.*;

public class BinaryXferCommand extends NetworkCommand {
  public BinaryXferCommand() {
    this("binary", CommandID.BINARY_COMMAND_ID);
  }

  public BinaryXferCommand( String commandName, short id ) {
    super(commandName, id, "binary transfer type");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = new SecureFTPError();

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
    if ( null != session ) {
      FTP bean = session.getFTPBean();
      if ( null != bean ) {
        try {
          bean.binary();
          Client.setTransferMode( FTP.BINARY_TRANSFER_MODE );
        }
        catch ( FTPConnectionLostException fcle ) {
          SecureFTP.getCommandDispatcher().fireCommand( this,
            new CloseCommand() );
          ErrorDialog.showDialog( new LString("Common.connection_lost",
                                              "Connection lost.") );
          result.setCode( SecureFTPError.NOT_CONNECTED );
        }
        catch ( FTPException fe ) {}
      }
    }

    return new SecureFTPError();
  }
}

