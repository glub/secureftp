
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CommandID.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

public interface CommandID {
  public final static short NOOP_COMMAND_ID            = 0;
  public final static short AMBIGUOUS_COMMAND_ID       = 1;
  public final static short ACCOUNT_COMMAND_ID         = 2;
  public final static short APPEND_COMMAND_ID          = 3;
  public final static short ASCII_COMMAND_ID           = 4;
  public final static short BELL_COMMAND_ID            = 5;
  public final static short BINARY_COMMAND_ID          = 6;
  public final static short EXIT_COMMAND_ID            = 7;
  public final static short CD_COMMAND_ID              = 8;
  public final static short CDUP_COMMAND_ID            = 9;
  public final static short CLOSE_COMMAND_ID           = 10;
  public final static short DELETE_COMMAND_ID          = 11;
  public final static short DEBUG_COMMAND_ID           = 12;
  public final static short DIR_COMMAND_ID             = 13;
  public final static short DISCONNECT_COMMAND_ID      = 14;
  public final static short GET_COMMAND_ID             = 15;
  public final static short GLOB_COMMAND_ID            = 16;
  public final static short HASH_COMMAND_ID            = 17;
  public final static short HELP_COMMAND_ID            = 18;
  public final static short LCD_COMMAND_ID             = 19;
  public final static short LS_COMMAND_ID              = 20;
  public final static short LLS_COMMAND_ID             = 21;
  public final static short MGET_COMMAND_ID            = 22;
  public final static short MKDIR_COMMAND_ID           = 23;
  public final static short MPUT_COMMAND_ID            = 24;
  public final static short OPEN_COMMAND_ID            = 25;
  public final static short PROMPT_COMMAND_ID          = 26;
  public final static short PUT_COMMAND_ID             = 27;
  public final static short PWD_COMMAND_ID             = 28;
  public final static short QUIT_COMMAND_ID            = 29;
  public final static short QUOTE_COMMAND_ID           = 30;
  public final static short RECV_COMMAND_ID            = 31;
  public final static short REMOTEHELP_COMMAND_ID      = 32;
  public final static short RENAME_COMMAND_ID          = 33;
  public final static short RMDIR_COMMAND_ID           = 34;
  public final static short SEND_COMMAND_ID            = 35;
  public final static short PASV_COMMAND_ID            = 36;
  public final static short PORT_COMMAND_ID            = 37;
  public final static short STATUS_COMMAND_ID          = 38;
  public final static short USER_COMMAND_ID            = 39;
  public final static short VERBOSE_COMMAND_ID         = 40;
  public final static short QMARK_COMMAND_ID           = 41;
  public final static short LPWD_COMMAND_ID            = 42;
  public final static short LOGIN_COMMAND_ID           = 43;
  public final static short TYPE_COMMAND_ID            = 44;
  public final static short LMKDIR_COMMAND_ID          = 45;
  public final static short LDELETE_COMMAND_ID         = 46;
  public final static short LRMDIR_COMMAND_ID          = 47;
  public final static short LRENAME_COMMAND_ID         = 48;
  public final static short DATAENCRYPT_COMMAND_ID     = 49;
  public final static short AUTO_COMMAND_ID            = 50;
  public final static short REFRESH_UI_COMMAND_ID      = 51;
  public final static short BOOKMARK_ADD_COMMAND_ID    = 52;
  public final static short BOOKMARK_EDIT_COMMAND_ID   = 53;
  public final static short BOOKMARK_LAUNCH_COMMAND_ID = 54;
  public final static short CONNECT_COMMAND_ID         = 55;
  public final static short ABOUT_COMMAND_ID           = 56;
  public final static short UPDATE_COMMAND_ID          = 57;
  public final static short SSL_CERTIFICATE_COMMAND_ID = 58;
  public final static short LMDELETE_COMMAND_ID        = 59;
  public final static short MDELETE_COMMAND_ID         = 60;
  public final static short PREFS_COMMAND_ID           = 61;
  public final static short REGISTRATION_COMMAND_ID    = 62;
  public final static short RAW_COMMAND_ID             = 63;
  public final static short INFO_COMMAND_ID            = 64;
  public final static short CERTIFICATE_MGR_COMMAND_ID = 65;
  public final static short EBCDIC_COMMAND_ID          = 66;
}
