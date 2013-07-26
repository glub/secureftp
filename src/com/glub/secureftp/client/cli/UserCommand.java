
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: UserCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import java.io.*;

public class UserCommand extends NetworkCommand {
  public UserCommand() {
    super("user", CommandID.USER_COMMAND_ID, 0, 3, 
          "user-name [password] [account]", "send new user information");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    if ( session.isSecure() && session.useCCC() ) {
      out.println("Cannot change user while command channel is in the clear.");
      throw new CommandException("CCC mode has been specified. Cannot change user.");
    }

    String user = session.getUserName();
    String pass = session.getPassword();
    String acct = session.getAccount();

    session.setUserName(null);
    session.setPassword(null);
    session.setAccount(null);

    BufferedReader stdin = SecureFTP.getInput();

    if ( getArgs().size() == 0 ) {
      out.print("(username) ");
      try {
        String newUser = stdin.readLine().trim();
        session.setUserName( newUser );
      }
      catch (IOException ioe) {
        session.setUserName( user );
        session.setPassword( pass );
        session.setAccount( acct );
        throw new CommandException( getUsage() );
      }
    }
    else if ( getArgs().size() == 1 ) {
      session.setUserName( (String)getArgs().get(0) );
    }
    else if ( getArgs().size() == 2 ) {
      session.setUserName( (String)getArgs().get(0) );

      String password = (String)getArgs().get(1);
      session.setPassword( decryptPassword(password) );
    }
    else if ( getArgs().size() == 3 ) {
      session.setUserName( (String)getArgs().get(0) );

      String password = (String)getArgs().get(1);
      session.setPassword( decryptPassword(password) );

      session.setAccount( (String)getArgs().get(2) );
    }

    result = SecureFTP.getCommandDispatcher().fireCommand(this, new LoginCommand());

    return result;
  }

  private String decryptPassword( String password ) {
    String result = password;

    if ( password.startsWith("ENCRYPTED:") ) {
      password = password.substring(10, password.length());
      result = EncryptionUtils.decryptPassword( password );
    }

    return result;
  }
}

