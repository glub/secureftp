
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: GTOverride.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.util;

public class GTOverride {
  public static String getString( String key ) {
    return System.getProperty( key ); 
  }

  public static boolean getBoolean( String key ) {
    boolean result = false;
    String str = getString(key);

    if ( null != str ) {
      Boolean value = new Boolean( getString(key) );
      result = value.booleanValue();
    }

    return result;
  }
  
  public static int getInt( String key, int defaultValue ) {
    return Util.parseInt( getString(key), defaultValue );
  }
}

