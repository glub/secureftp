//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPSessionManager.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

import java.util.*;

public class FTPSessionManager extends ArrayList {
  protected static final long serialVersionUID = 1L;	
  private int currentSessionIndex = -1;
  private static FTPSessionManager mgr = new FTPSessionManager();

  private FTPSessionManager() {
    super();
  }

  public static FTPSessionManager getInstance() {
    return mgr;
  }

  public void addSession( FTPSession session ) {
    add( session );
    setCurrentSession( size() - 1 );
  }

  public FTPSession getSession( int index ) {
    if ( size() == 0 ) {
      return null;
    }
    else {
      return (FTPSession)get( index );
    }
  }

  public void setCurrentSession( int index ) {
    currentSessionIndex = index;
  }

  public FTPSession getCurrentSession() {
    if ( currentSessionIndex < 0 ) {
      return null;
    }
    else if ( currentSessionIndex >= size() ) {
      currentSessionIndex--;
    }

    return getSession( currentSessionIndex );
  }

  public int getCurrentSessionIndex() {
    return currentSessionIndex;
  }

  public void removeCurrentSession() {
    if ( size() > 0 ) {
      remove( currentSessionIndex );
      setCurrentSession( currentSessionIndex );
     }
  }

  public boolean hasOpenSessions() {
    return ( size() > 0 );
  }

  public int getNumberOfOpenSessions() {
    return size();
  }
}

