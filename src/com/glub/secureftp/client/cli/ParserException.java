
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ParserException.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

public class ParserException extends Exception {
  protected static final long serialVersionUID = 1L;

  public ParserException() {
    super();
  }

  public ParserException( String message ) {
    super( message );
  }
}

