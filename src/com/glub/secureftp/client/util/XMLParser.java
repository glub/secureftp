
//*****************************************************************************
//*
//* (c) Copyright 2005. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: BookmarkVersion1.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.util;

import java.io.BufferedReader;
import java.lang.String;
import java.util.Hashtable;

public class XMLParser
{
  public static Hashtable parse(BufferedReader reader, String doc)
                                        throws java.io.IOException {
    // A really poor way to parse XML.  Configuration files are written
    // in XML notation so they can be easily extended in future releases.
    // However, the XML parsers for Java made by Sun and IBM apparently
    // cannot be distributed, even with free software.  So, until we
    // find a suitable XML parser for use in this program, we will have
    // to use this quick hack.

    String line;
    Hashtable table = new Hashtable();
    boolean foundDocName = false;

    while ((line = reader.readLine()) != null) {
      int start = line.indexOf('<') + 1;

      if (start < 1)
        continue;

      int end = line.indexOf('>', start);

      if (end < 0)
        continue;

      String tagName = line.substring(start, end);

      if (tagName.equals(doc))
        foundDocName = true;
      else if (foundDocName && tagName.equals('/' + doc))
        break;
      else if (tagName.startsWith("/"))
        continue;

      if (! foundDocName)
        continue;

      int close = line.indexOf("</" + tagName + ">", end + 1);

      if (close < 0)
        continue;

      table.put(tagName, line.substring(end + 1, close));
    }

    return table;
  }
}

