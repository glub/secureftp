
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LocalCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

public abstract class LocalCommand extends Command {
  public LocalCommand( String commandName, short id, String helpMessage ) {
    this( commandName, id, 0, 0, "", helpMessage );
  }

  public LocalCommand( String commandName, short id, 
                         int minNumOfArgs, int maxNumOfArgs, 
                         String usage, String helpMessage ) { 
    super( commandName, id, minNumOfArgs, maxNumOfArgs, usage, helpMessage );
  }

  public SecureFTPError doIt() throws CommandException {
    verifyArgs();
    return new SecureFTPError();
  }
}

