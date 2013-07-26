
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LString.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.util;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

public class LString {

  private static String resource = "com.glub.resources.index";
  private static ResourceBundle rb = null;
  private String data;

  private LString() {
    this.data = null;
  }

  public LString( String key, String defaultStr ) {
    this( key, defaultStr, rb );
  }

  public LString( String key, String defaultStr, ResourceBundle bundle ) {

    data = defaultStr;

    try {
      if ( bundle == null ) {
        bundle = getResourceBundle();
      }

      data = rb.getString(key);
    }  
    catch ( MissingResourceException e ) {
      Debug.out.println( "WARNING: " + key + " not found in " + resource +
                         ". Using " + defaultStr );
    }

  }

  public static LString getLocalizedString( String data ) {
    LString result = new LString();
    result.setData( data );
    return result;
  }

  public static LString concat( LString left, LString right ) {
    LString concat = LString.getLocalizedString( "[^0][^1]" );
    concat.replace( 0, left.getString() ); 
    concat.replace( 1, right.getString() );
    return concat; 
  }

  public static LString getNewLine() {
    return LString.getLocalizedString( "\n" );
  }
 
  protected void setData( String data ) {
    this.data = data;
  }

  private static ResourceBundle getResourceBundle() {
    if ( rb == null ) {
      if ( System.getProperty("glub.resourceBundle") != null ) {
        resource = System.getProperty( "glub.resourceBundle" );
      }

      rb = ResourceBundle.getBundle( resource );
    }

    return rb;
  }

  public static String getString( String key, String defaultStr ) {
    return getString( key, defaultStr, false );
  }

  public static String getString( String key, String defaultStr, 
                                  boolean isMenuItem ) {
    return (new LString(key, defaultStr)).getString(isMenuItem);
  }

  public String getString() {
    return getString(false);
  }

  public String getString( boolean isMenuItem ) {
    // on menu items for Japanese we put the English mnemonic in parens
    // at the back (e.g. File (F)), but on Mac we don't show the mnemonic
    String tempData = data;
    boolean hasEllipsis = false;
    if ( tempData.endsWith("...") ) {
      hasEllipsis = true;
      tempData = tempData.substring(0, tempData.length()-3);
    }

    if ( Util.isMacOS() && isMenuItem ) {
      StringBuffer buf = new StringBuffer( tempData );
      if ( tempData.length() >= 4 &&
           buf.charAt(tempData.length()-4) == ' ' &&
           buf.charAt(tempData.length()-3) == '(' && 
           buf.charAt(tempData.length()-1) == ')' ) {
        data = tempData.substring(0, tempData.length()-4);
        if ( hasEllipsis )
          data += "...";
      }
    }

    return data;
  }

  public void replace( int index, String replacement ) {
    String search = "[^" + index + "]";
   
    int start = data.indexOf(search);
    int end = start + search.length(); 
   
    StringBuffer buf = new StringBuffer( data );

    buf.replace( start, end, replacement );
 
    data = buf.toString();
  }

  public char getFirstChar() {
    return ( data.length() == 0 ) ? ' ' : data.charAt(0);
  }
}
