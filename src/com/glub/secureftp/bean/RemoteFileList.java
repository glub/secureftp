
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RemoteFileList.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import java.util.*;

/**
 * The <code>RemoteFileList</code> class contains a list of 
 * <code>RemoteFiles</code>.
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.1.3
 */

public class RemoteFileList extends ArrayList {
  private static final long serialVersionUID = 1L;

  /**
   * Create a <code>RemoteFileList</code> object.
   */
  public RemoteFileList() {
    super();
  }

  /**
   * Create a <code>RemoteFileList</code> object.
   *
   * @param list a convience to create a RemoteFileList from an existing List. 
   */
  public RemoteFileList( List list ) {
    super();
    for( int i = 0; i < list.size(); i++ ) {
      add( list.get(i) );
    }
  }

  /**
   * Returns a <code>RemoteFile</code> at the specified position in this list.
   *
   * @param index index of the <code>RemoteFile</code> to return.
   *
   * @return the <code>RemoteFile</code> at the specified position 
   *         in this list.
   */
  public RemoteFile getFile( int index ) {
    return (RemoteFile)get( index );
  }
}

