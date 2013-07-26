
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: DebugPrintStream.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.util;

public class DebugPrintStream {

  public void print( boolean b ) {
    if ( Debug.isEnabled() )
      System.err.print( b );
  }

  public void print( boolean b, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.print( b );
  }

  public void print( char c ) {
    if ( Debug.isEnabled() )
      System.err.print( c );
  }

  public void print( char c, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.print( c );
  }

  public void print( char[] s ) {
    if ( Debug.isEnabled() )
      System.err.print( s );
  }

  public void print( char[] s, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.print( s );
  }

  public void print( double d ) {
    if ( Debug.isEnabled() )
      System.err.print( d );
  }

  public void print( double d, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.print( d );
  }

  public void print( float f ) {
    if ( Debug.isEnabled() )
      System.err.print( f );
  }

  public void print( float f, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.print( f );
  }

  public void print( int i ) {
    if ( Debug.isEnabled() )
      System.err.print( i );
  }

  public void print( int i, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.print( i );
  }

  public void print( long l ) {
    if ( Debug.isEnabled() )
      System.err.print( l );
  }

  public void print( long l, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.print( l );
  }

  public void print( Object obj ) {
    if ( Debug.isEnabled() )
      System.err.print( obj );
  }

  public void print( Object obj, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.print( obj );
  }

  public void print( String s ) {
    if ( Debug.isEnabled() )
      System.err.print( s );
  }

  public void print( String s, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.print( s );
  }

  public void println( boolean b ) {
    if ( Debug.isEnabled() )
      System.err.println( b );
  }

  public void println( boolean b, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.println( b );
  }

  public void println( char c ) {
    if ( Debug.isEnabled() )
      System.err.println( c );
  }

  public void println( char c, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.println( c );
  }

  public void println( char[] s ) {
    if ( Debug.isEnabled() )
      System.err.println( s );
  }

  public void println( char[] s, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.println( s );
  }

  public void println( double d ) {
    if ( Debug.isEnabled() )
      System.err.println( d );
  }

  public void println( double d, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.println( d );
  }

  public void println( float f ) {
    if ( Debug.isEnabled() )
      System.err.println( f );
  }

  public void println( float f, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.println( f );
  }

  public void println( int i ) {
    if ( Debug.isEnabled() )
      System.err.println( i );
  }

  public void println( int i, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.println( i );
  }

  public void println( long l ) {
    if ( Debug.isEnabled() )
      System.err.println( l );
  }

  public void println( long l, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.println( l );
  }

  public void println( Object obj ) {
    if ( Debug.isEnabled() )
      System.err.println( obj );
  }

  public void println( Object obj, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.println( obj );
  }

  public void println( String s ) {
    if ( Debug.isEnabled() )
      System.err.println( s );
  }

  public void println( String s, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.println( s );
  }

  public void write( byte[] buf, int off, int len ) {
    if ( Debug.isEnabled() )
      System.err.write( buf, off, len );
  }

  public void write( byte[] buf, int off, int len, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.write( buf, off, len );
  }

  public void write( byte b ) {
    if ( Debug.isEnabled() )
      System.err.write( b );
  }

  public void write( byte b, int level ) {
    if ( Debug.isEnabled() && Debug.getLevel() >= level )
      System.err.write( b );
  }

}
