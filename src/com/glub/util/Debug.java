
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: Debug.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.util;

public class Debug {

  // during development, debug is on by default
  private static boolean sEnabled = true;
  private static int sLevel = 5;

  public static void enable( boolean enable ) {
    sEnabled = enable;
  }

  public static boolean isEnabled() {
    return sEnabled;
  }

  public static int getLevel() {
    return sLevel;
  }

  public static void setLevel( int level ) {
    if ( level < 0 )
      level = 0;

    sLevel = level;
  }

  public static final DebugPrintStream out = new DebugPrintStream();

}
