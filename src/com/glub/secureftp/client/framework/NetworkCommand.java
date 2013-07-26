
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: NetworkCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

public abstract class NetworkCommand extends Command {
  private FTPSession session;

  public NetworkCommand( String commandName, short id, String helpMessage ) {
    this( commandName, id, 0, 0, "", helpMessage, null );
  }

  public NetworkCommand( String commandName, short id, String helpMessage,
                         FTPSession currentSession ) {
    this( commandName, id, 0, 0, "", helpMessage, null );
  }

  public NetworkCommand( String commandName, short id, 
                         int minNumOfArgs, int maxNumOfArgs, 
                         String usage, String helpMessage ) { 
    super( commandName, id, minNumOfArgs, maxNumOfArgs, usage, helpMessage );
    session = FTPSessionManager.getInstance().getCurrentSession();
  }

  public NetworkCommand( String commandName, short id, 
                         int minNumOfArgs, int maxNumOfArgs, 
                         String usage, String helpMessage, 
                         FTPSession currentSession ) { 
    super( commandName, id, minNumOfArgs, maxNumOfArgs, usage, helpMessage );
    session = currentSession;
  }

  public FTPSession getSession() { 
    if ( null == session ) {
      session = FTPSessionManager.getInstance().getCurrentSession();
    }

    return session; 
  }

  public SecureFTPError doIt() throws CommandException {
    if ( !getSession().isConnected() ) {
      throw new NotConnectedCommandException();
    }

    getSession().setLastCommandSent( System.currentTimeMillis() );

    verifyArgs();
    return new SecureFTPError();
  }

  public boolean getBeepWhenDone() {
    if ( FTPSessionManager.getInstance().getCurrentSession().isConnected() )
      return super.getBeepWhenDone();
    else
      return false;
  }
}

