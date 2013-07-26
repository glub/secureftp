//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: PreferencesDispatcher.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

import java.util.*;
import java.util.prefs.Preferences;

public class PreferencesDispatcher {
  private static ArrayList prefsList = new ArrayList();

  public static Preferences getPreferences( PreferenceHandler ph ) {
    addPreferenceHandler( ph );
    return Preferences.userNodeForPackage( ph.getClass() );
  }

  private static void addPreferenceHandler( PreferenceHandler ph ) {
    prefsList.add( ph );
  }

  public static void doReadPrefs() {
    PreferenceHandler ph = null;
    for ( int i = 0; i < prefsList.size(); i++ ) {
      ph = (PreferenceHandler)prefsList.get(i);
      ph.readPreferences();
    }
  }

  public static void doWritePrefs() {
    PreferenceHandler ph = null;
    for ( int i = 0; i < prefsList.size(); i++ ) {
      ph = (PreferenceHandler)prefsList.get(i);
      ph.writePreferences();
    }
  }
}

