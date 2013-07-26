
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPAuthNotSupportedException.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

/**
 * Thrown if the FTP server does not support an explicit SSL connection.
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.0
 */

public class FTPAuthNotSupportedException extends FTPException {
  private static final long serialVersionUID = 1L;
  public FTPAuthNotSupportedException() { super(); }
  public FTPAuthNotSupportedException(String s) { super(s); }
}

