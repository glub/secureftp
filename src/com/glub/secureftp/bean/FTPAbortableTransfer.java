
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FTPAbortableTransfer.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

/**
 * The <code>FTPAbortableTransfer</code> class is responsible for helping 
 * to abort a data transfer.
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.0
 */

public class FTPAbortableTransfer {
  /** 
   * This value holds the current FTPData object for the current data transfer.
   */
  private FTPData currentData = null;

  /** 
   * Get the <code>FTPData</code> object for the current data transfer.
   *
   * @return the <code>FTPData</code> object for the current data transfer.
   */
  public FTPData getFTPData() { return currentData; }

  /** 
   * Set the <code>FTPData</code> object for the current data transfer.
   *
   * @param currentData    the <code>FTPData</code> object for the current 
   *                       data transfer.
   */
  public void setFTPData( FTPData currentData ) {
    this.currentData = currentData;
  }
}
