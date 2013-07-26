
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPKeyStoreException.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

/**
 * Thrown if there is a problem with the KeyStore 
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.0.3
 */

public class FTPKeyStoreException extends FTPException {
  private static final long serialVersionUID = 1L;
  public FTPKeyStoreException() { super(); }
  public FTPKeyStoreException(String s) { super(s); }
}

