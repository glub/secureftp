
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SyncCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.PrintStream;
import java.util.*;

public class SyncCommand extends NetworkCommand {
  public SyncCommand() {
    super("sync", CommandID.SYNC_COMMAND_ID,
          "sync local and remote data for the current directory");
    setBeepWhenDone( true );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    boolean interactive = session.isInteractiveOn();
    session.setInteractiveOn( false );

    ArrayList args = new ArrayList();

    // get files down first
    MGetCommand mgc = new MGetCommand(true);
    args.clear();
    args.add( "" );
    mgc.setArgs( args );
    result = SecureFTP.getCommandDispatcher().fireCommand( this, mgc );

    if ( result.getCode() == SecureFTPError.SYNC_NOT_SUPPORTED ) {
      out.println("The remote system does not have enough capabilities to support syncing");
      return result;
    }

    // the push files up

    MPutCommand mpc = new MPutCommand(true);
    args.clear();
    args.add( "*" );
    mpc.setArgs( args );
    result = SecureFTP.getCommandDispatcher().fireCommand( this, mpc );

    if ( result.getCode() == SecureFTPError.SYNC_NOT_SUPPORTED ) {
      out.println("The remote system does not have enough capabilities to support syncing");
      return result;
    }

    session.setInteractiveOn( interactive );

    return result;
  }
}

