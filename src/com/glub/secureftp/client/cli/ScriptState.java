
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ScriptState.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.client.framework.*;

import java.io.*;

public class ScriptState {

  private BufferedReader input = null;
  private boolean isScripted = false;
  private boolean isInteractiveOn = false;
  private boolean showProgress = false;
  private boolean beepWhenDone = false; 

  public ScriptState( BufferedReader is, boolean isScripted, 
                      FTPSession session ) {
    setInput( is );
    setIsScripted( isScripted );
    setInteractiveOn( session.isInteractiveOn() );
    setShowProgress( session.showProgress() );
    setBeepWhenDone( session.getBeepWhenDone() );
  }

  public BufferedReader getInput() { return input; }
  public void           setInput( BufferedReader input ) { this.input = input; }

  public boolean isScripted() { return isScripted; }
  public void    setIsScripted( boolean isScripted ) { 
    this.isScripted = isScripted; 
  }

  public boolean isInteractiveOn() { return isInteractiveOn; }
  public void    setInteractiveOn( boolean isInteractiveOn ) {
    this.isInteractiveOn = isInteractiveOn;
  }

  public boolean showProgress() { return showProgress; }
  public void    setShowProgress( boolean showProgress ) {
    this.showProgress = showProgress;
  }

  public boolean getBeepWhenDone() { return beepWhenDone; }
  public void    setBeepWhenDone( boolean beepWhenDone ) {
    this.beepWhenDone = beepWhenDone;
  }
}
