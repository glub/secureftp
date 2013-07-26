
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: PromptCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;

public class PromptCommand extends LocalCommand {
  public PromptCommand() {
    super("prompt", CommandID.PROMPT_COMMAND_ID,
          "force interactive prompting on multiple commands");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    if ( SecureFTP.scripted ) {
      session.setInteractiveOn( false );
      return result;
    }

    session.setInteractiveOn( !session.isInteractiveOn() );
    if ( session.isInteractiveOn() ) {
      out.println("Interactive mode on.");
    }
    else {
      out.println("Interactive mode off.");
    }

    return result;
  }
}

