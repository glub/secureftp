
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPConnectionLostException.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

/**
 * Thrown if the FTP connection was dropped (or timed-out).
 *
 * @author Brian Knight
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.0
 */

public class FTPConnectionLostException extends FTPException {
  private static final long serialVersionUID = 1L;
  public FTPConnectionLostException() { super(); }
  public FTPConnectionLostException(String s) { super(s); }
}

