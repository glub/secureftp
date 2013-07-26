
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: FileTypeDecider.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.framework;

import java.util.*;

public class FileTypeDecider {
  private static String[] textExtensions = {
    "txt", "html", "htm", "xhtml", "pl", "sh", "xml", "java", "xsd", "bat",
    "cpp", "c", "hpp", "h", "vb", "ccs", "dss", "lsp", "pgr", "htc", "rtx",
    "tsv", "wml", "wmls", "hdml", "etx", "talk", "spc", "xsl", "sgml", "sgm",
    "rtf", "asc", "bas"
  };

  private static Hashtable text = setupHash();

  private static Hashtable setupHash() {
    text = new Hashtable();

    for (int i = 0; i < textExtensions.length; i++) {
      text.put( textExtensions[i], new Boolean(true) );
    }

    return text;
  }

  private static String getFileExtension( String filename ) {
    int index = filename.lastIndexOf('.');

    if (index >= 0)
      return filename.substring(index + 1);
    else
      return null;
  }

  public static boolean isAscii( String filename ) {
    String temp = getFileExtension( filename );
    return (temp != null && text.get(temp) != null);
  }
}
