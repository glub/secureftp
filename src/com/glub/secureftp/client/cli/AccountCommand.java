
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: AccountCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.util.*;

public class AccountCommand extends NetworkCommand {
  public AccountCommand() {
    super("account", CommandID.ACCOUNT_COMMAND_ID, 0, 1, 
          "[passwd]", "send account command to remote server");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();
    ArrayList args = getArgs();
    String acct = null;

    if ( null == args || args.size() < 1 ) {
      try {
        do {
          acct = CLIUtil.getPassword("Account: ", session.maskPass());
        }
        while ( null == acct || acct.trim().length() == 0 ); 

        session.setAccount(acct);
      }
      catch ( IOException ioe ) {
        out.println( ioe.getMessage() );
      }
    }
    else {
      session.setAccount( ((String)args.get(0)).trim() );
    }

    try {
     session.getFTPBean().sendAccount( session.getAccount() );
    }
    catch ( FTPBadLoginException nae ) {
      out.println(nae.getMessage());
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
    }
    catch ( IllegalArgumentException iae ) {
      throw new CommandException( getUsage() );
    }

    return result;
  }
}

