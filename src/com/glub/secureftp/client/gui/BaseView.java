//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: BaseView.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;

import java.awt.*;
import javax.swing.*;
import java.util.prefs.Preferences;

public class BaseView extends JSplitPane implements PreferenceHandler {
  protected static final long serialVersionUID = 1L;
  private Preferences prefs = PreferencesDispatcher.getPreferences( this );

  private int dividerLocation     = 0;
  private int lastDividerLocation = 0;

  private static final String DIVIDER_LOCATION      = "DividerLocation";
  private static final String LAST_DIVIDER_LOCATION = "LastDividerLocation";

  public BaseView( Component left, Component right ) {
    super( JSplitPane.HORIZONTAL_SPLIT, false, left, right );
    //setOneTouchExpandable( true );
    setResizeWeight( 0 );

    Client.fixComponentColor( this );
  }

  public void readPreferences() {
    if ( Client.getClientType() == Client.APPLET ) {
      dividerLocation = Client.applet.getIntParameter("local_divider_location", 200);
      lastDividerLocation = dividerLocation;
    }
    else {
      dividerLocation = prefs.getInt( DIVIDER_LOCATION, 200 );
      lastDividerLocation = prefs.getInt( LAST_DIVIDER_LOCATION, 200 );
    }

    setDividerLocation( dividerLocation );
    setLastDividerLocation( lastDividerLocation );
  }

  public void writePreferences() {
    prefs.putInt( DIVIDER_LOCATION, getDividerLocation() );
    prefs.putInt( LAST_DIVIDER_LOCATION, getLastDividerLocation() );
  }
}

