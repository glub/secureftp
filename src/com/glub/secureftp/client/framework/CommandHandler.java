
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CommandHandler.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

import java.awt.event.ActionListener;

public interface CommandHandler extends ActionListener {

  public SecureFTPError handleCommand( Command command ) 
                                                        throws CommandException;

}
