
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: AutoXferCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.secureftp.bean.*;

public class AutoXferCommand extends NetworkCommand {
  public AutoXferCommand() {
    this("auto", CommandID.AUTO_COMMAND_ID);
  }

  public AutoXferCommand( String commandName, short id ) {
    super(commandName, id, "auto transfer type");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = new SecureFTPError();

    FTPSession session = FTPSessionManager.getInstance().getCurrentSession();
    if ( null != session ) {
      FTP bean = session.getFTPBean();
      if ( null != bean ) {
        bean.auto();
        Client.setTransferMode( FTP.AUTO_TRANSFER_MODE );
      }
    }

    return result;
  }
}

