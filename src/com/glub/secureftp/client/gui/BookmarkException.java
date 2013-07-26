
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: BookmarkException.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

public class BookmarkException extends Exception {
  protected static final long serialVersionUID = 1L;
  public BookmarkException() {
    super();
  }

  public BookmarkException( String message ) {
    super( message );
  }
}

