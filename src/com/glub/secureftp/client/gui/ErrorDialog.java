
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: ErrorDialog.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.gui.*;
import com.glub.util.*;

import javax.swing.*;

public class ErrorDialog extends GTErrorDialog {
  public static final int ERROR = JOptionPane.ERROR_MESSAGE;
  public static final int WARN = JOptionPane.WARNING_MESSAGE;
  public static final int INFO = JOptionPane.INFORMATION_MESSAGE;
  public static final int NONE = JOptionPane.PLAIN_MESSAGE;

  public static void showDialog( LString message ) {
    LString title = new LString("Common.dialogTitle.error", "Secure FTP Error");
    showDialog( title, message );
  }

  public static void showDialog( LString title, LString message ) {
    showDialog( SecureFTP.getBaseFrame(), title, message, ERROR );
  }

  public static void showDialog( LString title, LString message, 
                                 int errorType ) {
    showDialog( SecureFTP.getBaseFrame(), title, message, errorType );
  }
}

