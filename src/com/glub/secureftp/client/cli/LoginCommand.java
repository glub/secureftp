
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LoginCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.util.*;

public class LoginCommand extends NetworkCommand {
  public LoginCommand() {
    super("", CommandID.LOGIN_COMMAND_ID, "");
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = getSession();
    PrintStream out = session.getPrintStream();

    String user = session.getUserName();
    String pass = session.getPassword();
    String acct = session.getAccount();

    BufferedReader stdin = SecureFTP.getInput();

    if ( null == user ) {
      if ( !SecureFTP.scripted ) {
        out.print("Name (" + session.getHostName() + 
                  ":" + System.getProperty("user.name") + "): ");
      }

      try {
        user = stdin.readLine().trim();

        if ( user.length() == 0 ) {
          user = System.getProperty("user.name");
        }

        session.setUserName(user);
      }
      catch ( IOException ioe ) {
        out.println( ioe.getMessage() );
        result.setCode( SecureFTPError.IO_EXCEPTION );
      }
    }

    try {
      session.getFTPBean().sendUserName( user ); 
    }
    catch ( FTPNeedPasswordException npe ) {
      if ( null == pass ) {
        try {
          do {
            pass = CLIUtil.getPassword("Password: ", session.maskPass());
          }
          while ( null == pass || pass.trim().length() == 0 ); 

          session.setPassword( pass );
        }
        catch ( IOException ioe ) {
          out.println( ioe.getMessage() );
          result.setCode( SecureFTPError.IO_EXCEPTION );
        }
      }

      try {
       session.getFTPBean().sendPassword( pass );
      }
      catch ( FTPBadLoginException nae ) {
        out.println(nae.getMessage());
        result.setCode( SecureFTPError.LOGIN_FAILED );
      }
      catch ( FTPNeedAccountException nae ) {
        if ( null == acct ) {
          try {
            do {
              acct = CLIUtil.getPassword("Account: ", session.maskPass());
            }
            while ( null == acct || acct.trim().length() == 0 ); 

            session.setAccount( acct );
          }
          catch ( IOException ioe ) {
            out.println( ioe.getMessage() );
            result.setCode( SecureFTPError.IO_EXCEPTION );
          }
        }

        try {
         session.getFTPBean().sendAccount( acct );
        }
        catch ( FTPBadLoginException ble ) {
          out.println(ble.getMessage());
          result.setCode( SecureFTPError.LOGIN_FAILED );
        }
        catch ( FTPException fe ) {
          out.println(fe.getMessage());  
          result.setCode( SecureFTPError.UNKNOWN );
        }
      }
      catch ( FTPException fe ) {
        out.println(fe.getMessage());  
        result.setCode( SecureFTPError.UNKNOWN );
      }
      catch (IllegalArgumentException iae) {
        out.println(iae.getMessage());
        result.setCode( SecureFTPError.BAD_ARGUMENTS );
      }
    }
    catch ( FTPBadLoginException nae ) {
      out.println(nae.getMessage());
      result.setCode( SecureFTPError.LOGIN_FAILED );
    }
    catch ( FTPNeedAccountException nae ) {
      if ( null == acct ) {
        try {
          do {
            acct = CLIUtil.getPassword("Account: ", session.maskPass());
          }
          while ( null == acct || acct.trim().length() == 0 ); 

          session.setAccount( acct );
        }
        catch ( IOException ioe ) {
          out.println( ioe.getMessage() );
          result.setCode( SecureFTPError.IO_EXCEPTION );
        }
      }

      try {
       session.getFTPBean().sendAccount( acct );
      }
      catch ( FTPBadLoginException ble ) {
        out.println(ble.getMessage());
        result.setCode( SecureFTPError.LOGIN_FAILED );
      }
      catch ( FTPException fe ) {
        out.println(fe.getMessage());  
        result.setCode( SecureFTPError.UNKNOWN );
      }
    }
    catch ( FTPException fe ) {
      out.println("Connection aborted.");
      result.setCode( SecureFTPError.UNKNOWN );
      SecureFTP.getCommandDispatcher().fireCommand(this, new CloseCommand());
    }
    catch (IllegalArgumentException iae) {
      out.println(iae.getMessage());
      result.setCode( SecureFTPError.IO_EXCEPTION );
    }

    if ( SecureFTP.forceEncrypt && 
         session.isSecure() && session.isLoggedIn() ) {
      // force data encryption on
      try {
        ((SSLFTP)session.getFTPBean()).forceDataEncryptionOn( true );
      }
      catch ( FTPException fe ) {}
    }

    //session.getFTPBean().setListStyle( FTPServerInfo.LIST_STYLE_UNIX );

    if ( session.isLoggedIn() ) {
      try {
        if ( System.getProperty("file.encoding") != null &&
             System.getProperty("file.encoding").toLowerCase().equals("utf8")) {
          if ( !session.isDebugOn() ) {
            session.getFTPBean().setSendCmdStream(null);
            session.getFTPBean().setRecvCmdStream(null);
          }

          session.getFTPBean().setStringDataAsUTF8( true );

          if ( !session.isDebugOn() ) {
            session.getFTPBean().setSendCmdStream(session.getOutputStream());
            session.getFTPBean().setRecvCmdStream(session.getOutputStream());
          }
        }
      }
      catch ( Exception e ) {}

      SecureFTP.getCommandDispatcher().fireCommand(this, new AutoCommand());

      if ( session.getWorkingDir().length() > 0 ) {
        ArrayList args = new ArrayList(1);
        args.add( session.getWorkingDir() );
        CDCommand cmd = new CDCommand();
        cmd.setArgs( args );
        SecureFTP.getCommandDispatcher().fireCommand(this, cmd);
      }
    }

    return result;
  }
}

