
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LDeleteCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

import java.io.*;

public class LDeleteCommand extends LocalCommand {
  public LDeleteCommand() {
    super("ldelete", CommandID.LDELETE_COMMAND_ID, 1, 1, "local-file",
          "delete local file");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    File fileToDel = (File)getArgs().get(0);

    if ( !fileToDel.exists() ) {
      result.setCode( SecureFTPError.NO_SUCH_FILE );
    }
    else {
      if ( !fileToDel.delete() ) {
        result.setCode( SecureFTPError.PERMISSION_DENIED );
      }
    }

    return result;
  }
}

