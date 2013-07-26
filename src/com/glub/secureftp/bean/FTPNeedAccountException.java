
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPNeedAccountException.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

/**
 * Thrown if the FTP server requires an account to continue the login process.
 *
 * @author Brian Knight
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.0
 */

public class FTPNeedAccountException extends FTPException {
  private static final long serialVersionUID = 1L;
  public FTPNeedAccountException() { super(); }
  public FTPNeedAccountException(String s) { super(s); }
}

