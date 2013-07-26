
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: Log.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.util;

import org.apache.log4j.*;
import java.util.Properties;

public class Log {

  private Category cat = null;

  public Log( Class myClass, Properties logConfig ) {
    cat = Category.getInstance( myClass );
    PropertyConfigurator.configure( logConfig );
  }

  public void shutdown() {
    if ( cat != null )
      Category.shutdown();
  }

/*
  public void assert( boolean assertion, String msg ) {
    if ( cat != null )
      cat.assert( assertion, msg );
  }
*/

  public void debug( Object message ) {
    if ( cat != null )
      cat.debug( message );
    else
      System.err.println(message);
  }

  public void debug( Object message, Throwable t ) {
    if ( cat != null )
      cat.debug( message, t );
    else
      System.err.println(message);
  }

  public void error( Object message ) {
    if ( cat != null )
      cat.error( message );
    else
      System.err.println(message);
  }

  public void error( Object message, Throwable t ) {
    if ( cat != null )
      cat.error( message, t );
    else
      System.err.println(message);
  }

  public void fatal( Object message ) {
    if ( cat != null )
      cat.fatal( message );
    else
      System.err.println(message);
  }

  public void fatal( Object message, Throwable t ) {
    if ( cat != null )
      cat.fatal( message, t );
    else
      System.err.println(message);
  }

  public void info( Object message ) {
    if ( cat != null )
      cat.info( message );
    else
      System.err.println(message);
  }

  public void info( Object message, Throwable t ) {
    if ( cat != null )
      cat.info( message, t );
    else
      System.err.println(message);
  }

  public void warn( Object message ) {
    if ( cat != null )
      cat.warn( message );
    else
      System.err.println(message);
  }

  public void warn( Object message, Throwable t ) {
    if ( cat != null )
      cat.warn( message, t );
    else
      System.err.println(message);
  }

}

