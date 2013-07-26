
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: Version.java 146 2009-12-19 21:52:49Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

import com.glub.secureftp.bean.*;

public interface Version {
  public final static String COPYRIGHT     = "Copyright (c) 1999-2010 " + 
                                             "Glub Tech, Inc.";
  public final static String COPYRIGHT_GUI = "Copyright " + '\u00a9' + 
                                             " 1999-2010 " + "Glub Tech, Inc.";
  public final static String PROGRAM_NAME  = "Glub Tech Secure FTP";
  public final static String UPDATE_ID     = "secureftp_2_5";
  public       static String VERSION       = "2.6.1";
  public       static int    MAJOR_VERSION = 2;
  public       static String SHORT_VERSION = "2.6";
  public       static String BEAN_VERSION  = FTP.getVersion();
  public       static String BEAN_DATE     = FTP.getDateStamp();
  public final static String BETA          = "";
}
