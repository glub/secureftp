
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CommandException.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

public class CommandException extends Exception {
  protected static final long serialVersionUID = 1L;
	
  public CommandException() {
    super();
  }

  public CommandException( String message ) {
    super( message );
  }
}

