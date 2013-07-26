
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: BookmarkConverter.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.util;

import com.glub.secureftp.client.gui.*;
import com.glub.util.*;
import java.io.*;

public class BookmarkConverter {
  public static void main( String args[] ) {
    String fileSep = File.separator;
    String pathToBookmarks = System.getProperty( "user.home" ) + fileSep +
                             ".secureftp" + fileSep + "bookmarks";

    BookmarkManagerVersion1 v1 = new BookmarkManagerVersion1( pathToBookmarks );
    BookmarkManager v2 = BookmarkManager.getLocalInstance();

    if ( v2.hasBookmarks() ) {
      error( "Updated bookmark file already exists." ); 
    }

    try {
      v1.readFromDisk();
    }
    catch ( Exception e ) {
      v1.clear();
    }

    if ( v1.size() > 0 ) {
      for( int i = 0; i < v1.size(); i++ ) {
        BookmarkVersion1 old = (BookmarkVersion1)v1.get( i );
        String profile = old.getName();
        String hostname = old.getHostname();
        int port = Util.parseInt( old.getPort(), 21 );
        String username = old.getUsername();

        int securityMode = old.getSecurity();
        if ( securityMode > 0 ) {
          securityMode = securityMode + 1;
        }

        boolean anonymous = old.getAnon();
        boolean passive = old.getPasv();
        boolean dataEncrypt = old.getSSLData();
        boolean proxy = old.getProxy();
        String remoteFolder = old.getRemoteDir();
        String localFolder = old.getLocalDir();

        Bookmark newBook = 
          new Bookmark( profile, hostname, port, username, "", 
                        securityMode, anonymous, passive, dataEncrypt,
                        false, proxy, remoteFolder, localFolder );

         System.out.println( "Adding: " + newBook.getProfile() );
         v2.addBookmark( newBook );
      }
    }
    else { 
      error( "Couldn't find bookmark file." );
    }

    try {
      v2.writeBookmarks();
     }
     catch( Exception e ) {
      error( "Couldn't write new bookmark file." );
    }

    System.exit( 0 );
  }

  private static void error( String msg ) {
    System.err.println( msg );
    System.exit( 1 );
  }
}

