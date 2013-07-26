
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: UID.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.jni;

public class UID {
  public static final int SUCCESS = 0;
  public static final int FAILURE = -1;

  public static native int setuid(int uid);
  public static native int seteuid(int uid);
  public static native int setgid(int gid);
  public static native int setegid(int gid);

  static {
    try {
      System.loadLibrary("uid");
    }
    catch (UnsatisfiedLinkError ule) {
      System.err.println("The library \"uid\" could not be found.\n" +
                         "Try setting the LD_LIBRARY_PATH to the shared lib." +
                         "\n");                     
      System.err.println("Note: Changing the user/group cannot be done on " +
                         "Microsoft Windows.");
      System.exit(1);
    }
  }
}
