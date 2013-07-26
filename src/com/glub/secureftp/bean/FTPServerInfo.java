
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPServerInfo.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

/**
 * The <code>FTPServerInfo</code> class is responsible for determining the 
 * type of FTP server the user is currently connected to.
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-12-15 19:53:52 -0800 (Tue, 15 Dec 2009) $
 * @since 2.5.11
 */

public class FTPServerInfo {

  /** Used to set the FTP server type to <code>unknown</code>. */
  public static final short LIST_STYLE_UNKNOWN = 0;

  /** Used to set the FTP server type to <code>UNIX</code>. */
  public static final short LIST_STYLE_UNIX    = 1;

  /** Used to set the FTP server type to <code>Windows</code>. */
  public static final short LIST_STYLE_WINDOWS = 2;

  /** Used to set the FTP server type to <code>Netware</code>. */
  public static final short LIST_STYLE_NETWARE = 3;

  /** Used to set the FTP server type to <code>Engin</code>. */
  public static final short LIST_STYLE_ENGIN = 4;

  // This file is ugly, deal with it

  /**
   * Try to determine what type of FTP server the user is connected to based
   * on the login banner.
   *
   * @param banner   the banner sent from the FTP server.
   *
   * @return         the code that specifies the server type.
   *
   * @see #LIST_STYLE_UNKNOWN
   * @see #LIST_STYLE_UNIX
   * @see #LIST_STYLE_WINDOWS
   * @see #LIST_STYLE_NETWARE
   */
  public static short lookupListStyleByBanner(String banner) {
    if (banner == null)
      return LIST_STYLE_UNKNOWN;

    if (banner.indexOf("FTP server (Version wu-") >= 0)
      return LIST_STYLE_UNIX;
    else if (banner.indexOf("xTrade FTP") >= 0)
      return LIST_STYLE_UNIX;
    else if (banner.indexOf("UNIX") >= 0)
      return LIST_STYLE_UNIX;
    else if (banner.indexOf("NETWARE") >= 0)
      return LIST_STYLE_NETWARE;
    else if (banner.indexOf("Microsoft FTP Service (Version 4.0)") >= 0)
      return LIST_STYLE_WINDOWS;
    else if (banner.indexOf("Microsoft FTP Service (Version 5.0)") >= 0)
      return LIST_STYLE_UNIX;
    else if (banner.indexOf("Microsoft FTP Service") >= 0)
      return LIST_STYLE_UNIX;
    else
      return LIST_STYLE_UNKNOWN;
  }

  /**
   * Try to determine what type of FTP server the user is connected to based
   * on the response from the <code>SYST</code> command.
   *
   * @param syst     the SYST response from the FTP server.
   *
   * @return         the code that specifies the server type.
   *
   * @see FTPCommand#syst()
   * @see #LIST_STYLE_UNKNOWN
   * @see #LIST_STYLE_UNIX
   * @see #LIST_STYLE_WINDOWS
   * @see #LIST_STYLE_NETWARE
   */
  public static short lookupListStyleBySyst(String syst) {
    // NOTE: This method is reliable only for Unix systems.  Some servers
    //       report Windows system type but use the "Unix style" list format.
    //       First try by banner, then use this if banner fails.
    if (syst == null)
      return LIST_STYLE_UNKNOWN;

    syst = syst.toLowerCase();

    if (syst.startsWith("unix"))
      return LIST_STYLE_UNIX;
    else if (syst.startsWith("netware"))
      return LIST_STYLE_NETWARE;
    else if (syst.startsWith("windows"))
      return LIST_STYLE_WINDOWS;
    else
      return LIST_STYLE_UNKNOWN;
  }
}

