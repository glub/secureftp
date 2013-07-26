
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: MacMenuAdapter.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.apple.eawt.*;

public class MacMenuAdapter extends ApplicationAdapter {
  public void handleAbout( ApplicationEvent ae ) {
    ae.setHandled( true );
    SecureFTP.getCommandDispatcher().fireCommand( this, new AboutCommand() );
  }

  public void handleOpenApplication( ApplicationEvent ae ) {}
  public void handleOpenFile( ApplicationEvent ae ) {}
  public void handlePreferences( ApplicationEvent ae ) {
    ae.setHandled( true );
    SecureFTP.getCommandDispatcher().fireCommand( this, 
                                                  new PreferencesCommand() );
  }
  public void handlePrintFile( ApplicationEvent ae ) {}
  public void handleQuit( ApplicationEvent ae ) {
    ae.setHandled( true );
    SecureFTP.getCommandDispatcher().fireCommand( this, new ExitCommand() );
  }

  public void handleReOpenApplication( ApplicationEvent ae ) {} 
}
