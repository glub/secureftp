
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: SecureFTPError.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

public class SecureFTPError {
  private int errorCode = OK;
  private String msg = null;

  public final static int OK = 0;
  public final static int JAVA_VERSION_INCORRECT = 1;
  public final static int IO_EXCEPTION = 2;
  public final static int BAD_ARGUMENTS = 3;
  public final static int NOT_CONNECTED = 4;
  public final static int NOT_A_DIRECTORY = 5;
  public final static int NO_SUCH_FILE = 6;
  public final static int UPLOAD_FAILED = 7;
  public final static int DOWNLOAD_FAILED = 8;
  public final static int LOGIN_FAILED = 9;
  public final static int CD_FAILED = 10;
  public final static int MKDIR_FAILED = 11;
  public final static int RMDIR_FAILED = 12;
  public final static int DELETE_FAILED = 13;
  public final static int RENAME_FAILED = 14;
  public final static int CHMOD_FAILED = 15;
  public final static int PERMISSION_DENIED = 50;
  public final static int PROBLEM_SAVING_BOOKMARK = 51;
  public final static int ACCEPT_CERTIFICATE_ONCE = 52;
  public final static int ACCEPT_CERTIFICATE_ALWAYS = 53;
  public final static int DENY_CERTIFICATE = 54;
  public final static int TRANSFER_ABORTED = 55;
  public final static int DELETE_ABORTED = 56;
  public final static int DIRECTORY_EXISTS = 57;
  public final static int SYNC_NOT_SUPPORTED = 58;
  public final static int UNKNOWN = 255;

  public SecureFTPError() {
    this( OK, null );
  }

  public SecureFTPError( int errorCode ) {
    this( errorCode, null );
  }

  public SecureFTPError( int errorCode, String msg ) {
    setCode( errorCode );
    setMessage( msg );
  }

  public int getCode() { return errorCode; }
  public void setCode( int errorCode ) { this.errorCode = errorCode; }

  public String getMessage() { return msg; }
  public void setMessage( String msg ) {
    this.msg = ( null == msg ) ? "" : msg;
  }

  public String toString() {
    return getCode() + ": " + getMessage();
  }
}

