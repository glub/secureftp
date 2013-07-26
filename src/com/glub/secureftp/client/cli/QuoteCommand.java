
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: QuoteCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;
import java.util.*;

public class QuoteCommand extends NetworkCommand {
  public QuoteCommand() {
    super("quote", CommandID.QUOTE_COMMAND_ID, 1, 9999, 
          "arg1 [arg2 ...]", "send arbitrary ftp command");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    ArrayList args = getArgs();

    try {
      StringBuffer rawCommands = new StringBuffer();
      for ( int i = 0; i < args.size(); i++ ) {
        rawCommands.append( (String)args.get(i) );
        rawCommands.append( " " );
      }
      session.getFTPBean().raw( rawCommands.toString().trim() );
    }
    catch ( FTPException fe ) {
      out.println( fe.getMessage() );
    } 

    return result;
  }
}

