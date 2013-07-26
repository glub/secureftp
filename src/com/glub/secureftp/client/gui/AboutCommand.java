
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: AboutCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

public class AboutCommand extends LocalCommand {
  public AboutCommand() {
    this("about", CommandID.ABOUT_COMMAND_ID);
  }

  public AboutCommand( String commandName, short id ) {
    super(commandName, id, "about");
  }

  public SecureFTPError doIt() throws CommandException {
    new SplashScreen(true);
    
    return new SecureFTPError();
  }
}

