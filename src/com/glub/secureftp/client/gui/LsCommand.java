
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LsCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

public class LsCommand extends NetworkCommand {
  public LsCommand() {
    super("ls", CommandID.LS_COMMAND_ID, 0, 1, 
          "[remote-file]", "list contents of remote directory");
    setBeepWhenDone( false );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    RemoteFile file = null;

    if ( null != getArgs() &&  getArgs().size() > 0 ) {
      file = (RemoteFile)getArgs().get(0);
    }

    DataTransferManager.getInstance().list( getSession(), file );

    ((RemotePanel)getSession().getRemoteUI()).setFocus();

    return result;
  }
}

