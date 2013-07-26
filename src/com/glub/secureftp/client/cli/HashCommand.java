
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: HashCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class HashCommand extends LocalCommand {
  public HashCommand() {
    super("hash", CommandID.HASH_COMMAND_ID, 
          "toggle printing `*' for each 5% transferred");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    session.setShowProgress( !session.showProgress() );
    if ( session.showProgress() ) {
      out.println("Hash mark printing on.");
    }
    else {
      out.println("Hash mark printing off.");
    }

    return result;
  }
}

