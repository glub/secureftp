
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: Progress.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

/**
 * The <code>Progress</code> interface is responsible for reporting the progress
 * of a data transfer from the FTP server.
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.0
 */

public interface Progress {

  /** Called at the beginning of the data transfer. */
  public void startProgress();

  /** 
   * Called repeatedly during the data transfer. 
   *
   * @param current    the current amount of bytes transferred.
   * @param total      the total amount of bytes in the transfer.
   */
  public void updateProgress( long current, long total );

  /** Called at the end of the data transfer. */
  public void finishProgress();

}
