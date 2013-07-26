
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: BookmarkManagerVersion1.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.util;

import java.io.*;
import java.util.*;

public class BookmarkManagerVersion1 extends Vector {
  protected final static long serialVersionUID = 1L;
  String file;

  public BookmarkManagerVersion1(String f) {
    super();

    file = f;
  }

  public int size() {
    return super.size();
  }

  public void clear() {
    super.clear();
  }

  public Object get( int i ) {
    return super.get(i);
  }

  public void addBookmark(BookmarkVersion1 b) {
    addElement(b);
  }

  public void readFromDisk() throws IOException, SecurityException {
    BookmarkVersion1 bookmark;

    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));

      Hashtable table = XMLParser.parse(reader, "bookmark");

      while (! table.isEmpty()) {
        bookmark = parseBookmark(table);

        if (bookmark != null)
          addElement(bookmark);

        table = XMLParser.parse(reader, "bookmark");
      }

      reader.close();
    }
    catch (FileNotFoundException fnfe) {
      // Bookmarks file does not exist, no bookmarks to read
    }

  }

  private BookmarkVersion1 parseBookmark(Hashtable table) {
    try {
      String profile = (String) table.get("profile");

      // Any bookmark without a profile name is useless
      if (profile == null)
        return null;

      String hostname = (String) table.get("hostname");
      String port     = (String) table.get("port");
      String username = (String) table.get("username");
      String password = (String) table.get("password");
      int security    = Integer.parseInt((String) table.get("security"));

      boolean pasv = Boolean.valueOf((String)table.get("pasv")).booleanValue();
      boolean anon = Boolean.valueOf((String)table.get("anon")).booleanValue();
      boolean proxy =
        Boolean.valueOf((String)table.get("proxy")).booleanValue();
      boolean ssldata =
        Boolean.valueOf((String)table.get("ssldata")).booleanValue();

      String localDir  = (String) table.get("localdir");
      String remoteDir = (String) table.get("remotedir");

      return new BookmarkVersion1(profile, hostname, port, username, password,
                          security, pasv, anon, proxy, ssldata,
                          localDir, remoteDir);
    }
    catch (Exception e) {
      return null;
    }
  }

}

