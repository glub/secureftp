
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: Util.java 87 2009-09-22 22:45:33Z gary $
//*
//*****************************************************************************

package com.glub.util;

import javax.swing.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.swing.filechooser.*;

public class Util {

  public static void close( InputStream is ) {
    if (is != null) {
      try {
        is.close();
      }
      catch (Exception e) {
        // Ignore all exceptions
      }
    }
  }

  public static void close( OutputStream os ) {
    if (os != null) {
      try {
        os.close();
      }
      catch (Exception e) {
        // Ignore all exceptions
      }
    }
  }

  public static void close( Reader r ) {
    if (r != null) {
      try {
        r.close();
      }
      catch (Exception e) {
        // Ignore all exceptions
      }
    }
  }

  public static void close( Writer w ) {
    if (w != null) {
      try {
        w.close();
      }
      catch (Exception e) {
        // Ignore all exceptions
      }
    }
  }

  public static void close( Socket socket ) {
    if (socket != null) {
      try {
        socket.close();
      }
      catch (Exception e) {
        // Ignore all exceptions
      }
    }
  }

  public static void close( ServerSocket ss ) {
    if (ss != null) {
      try {
        ss.close();
      }
      catch (Exception e) {
        // Ignore all exceptions
      }
    }
  }

  public static int parseInt( String str, int defaultValue ) {
    int ret = defaultValue;

    if (str != null) {
      try {
        ret = Integer.parseInt( str );
      }
      catch (NumberFormatException nfe) {
      }
    }

    return ret;
  }

  private static final int ZEROS_LEN = 1024;
  private static byte[] zeros = new byte[ZEROS_LEN];

  public static void clearByteArray( byte[] buf ) {
    clearByteArray( buf, 0, buf.length );
  }

  public static void clearByteArray( byte[] buf, int offset, int length ) {
    if ( length <= ZEROS_LEN ) {
      System.arraycopy( zeros, 0, buf, offset, length );
    } 
    else {
      System.arraycopy( zeros, 0, buf, offset, ZEROS_LEN );
      int halflength = length/2;
      for ( int i = ZEROS_LEN; i < length; i += i ) {
        System.arraycopy( buf, offset, buf, offset + i,
                          (i <= halflength) ? i : length - i );
      }
    }
  }
  
  public static boolean useJSSE() {
    boolean result = System.getProperty("java.version").startsWith("1.2") ||
                     System.getProperty("java.version").startsWith("1.3");
    return result;
  }

  public static void setPreferredLookAndFeel() {
    UIManager.LookAndFeelInfo[] lfi = UIManager.getInstalledLookAndFeels();
    String osName = System.getProperty("os.name");
    String defaultName = "Metal";
    String defaultClassName = "javax.swing.plaf.metal.MetalLookAndFeel";
    boolean found = false;

    if ( osName.startsWith("Windows") ) {
      defaultName = "Windows"; 
    }
    else if ( osName.startsWith("Mac") ) {
      defaultName = "MacOS Adaptive";
    }
    else if ( osName.equals("Solaris") || osName.equals("Linux") ||
	      osName.equals("HP-UX") || osName.equals("Irix") ||
	      osName.equals("AIX") ) {
      defaultName = "CDE/Motif";
    }
    for ( int i = 0; i < lfi.length; i++ ) {
      if ( lfi[i].getName().equals(defaultName) ) {
        defaultClassName = lfi[i].getClassName();
	found = true;
      }
    }

    try {
      if ( found )
        UIManager.setLookAndFeel( defaultClassName );
    }
    catch ( Exception e ) {
    	if (GTOverride.getBoolean("glub.debug"))
    		e.printStackTrace(); 
    }
  }

  /*
   * Converts a byte array to a hex string
   */
  public static String byteArrayToHex( byte[] block ) {
    StringBuffer buf = new StringBuffer();

    int len = block.length;

    for ( int i = 0; i < len; i++ ) {
      byteToHex( block[i], buf );
    } 

    return buf.toString();
  }

  /*
  * Converts a byte to hex digit and writes to the supplied buffer
  */
  private static void byteToHex( byte b, StringBuffer buf ) {
    char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                        '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    int high = ( (b & 0xf0) >> 4 );
    int low = ( b & 0x0f );
    buf.append( hexChars[high] );
    buf.append( hexChars[low] );
  }

  public static String searchAndReplace( String data, String search,
                                         String replacement,
                                         boolean replaceAll ) {
    int start = data.indexOf(search);
    int end = start + search.length();

    StringBuffer buf = new StringBuffer( data );

    if ( start >= 0 ) {
      buf.replace( start, end, replacement );

      if ( replaceAll ) {
        while ( end <= buf.length() ) {
          start = buf.toString().indexOf(search, end + 1);
          end = start + search.length();
          if ( start == -1 )
            break;
          buf.replace( start, end, replacement );
        }
      }
    }

    return buf.toString();
  }

  public static Calendar getDate( String year, String month, String day ) {
    Calendar cal = new GregorianCalendar(Locale.US);
    String hour = "00";
    String min  = "00";

    SimpleDateFormat sdf = 
      new SimpleDateFormat("MMM dd yyyy hh:mm", Locale.US);

    int index = 0;
    if ( (index = year.indexOf(":")) > 0 ) {
      hour = year.substring(0, index);
      min  = year.substring(index+1, year.length());

      // if the current month is less than the month passed in, it must
      // be from the last year
      int curMonth = cal.get(Calendar.MONTH);
      int curYear = cal.get(Calendar.YEAR);
      int iMonth = -1;
      try {
        Calendar c = new GregorianCalendar();
        String dateStr = month + " 1 2000 00:00";
        c.setTime(sdf.parse(dateStr));
        iMonth = c.get(Calendar.MONTH);
      }
      catch ( ParseException pe ) {}
      
      if ( curMonth < iMonth )
        curYear--;

      year = Integer.toString(curYear); 
    }

    String newDate = month + " " + day + " " + year + " " + hour + ":" + min;
    try {
      cal.setTime( sdf.parse(newDate) );
    }
    catch ( ParseException pe ) {}
    return cal;
  }

  public static Calendar getWindowsDate( String windowsDate ) {
    Calendar cal = new GregorianCalendar();

    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy  hh:mm a");
    try {
      cal.setTime( sdf.parse(windowsDate) );
    }
    catch ( ParseException pe ) {
      // try alternate listing format
      sdf = new SimpleDateFormat("MM-dd-yy hh:mma");
      try {
        cal.setTime( sdf.parse(windowsDate) );
      }
      catch ( ParseException pe2 ) {}
    }
    return cal;
  }

  public static String getLanguage() {
    return Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
  }

  public static String getLCLanguage() {
    return Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry().toLowerCase();
  }

  public static String getOS() {
    String os = System.getProperty("os.name"); 
    if ( isMacOS() ) {
      os = "Macintosh";
    }
    else if ( isWinOS() ) {
      os = "Windows";
    }

    return os;
  }

  public static boolean isMacOS() {
    return isMacintosh();
  }

  public static boolean isMacintosh() {
    return System.getProperty("os.name").startsWith("Mac");
  }

  public static boolean isWinOS() {
    return isWindows();
  }

  public static boolean isWindows() {
    return System.getProperty("os.name").startsWith("Win");
  }

  public static boolean isSunOS() {
    return System.getProperty("os.name").startsWith("Sun") ||
           System.getProperty("os.name").startsWith("Solaris");
  }

  public static boolean isLinux() {
	    return System.getProperty("os.name").startsWith("Linux");
  }
  
  public static String getPlatform() {
    String platform = "";

    if ( Util.isMacOS() ) {
      platform = "macosx";
    }
    else if ( Util.isWindows() ) {
      platform = "windows";
    }
    else if ( Util.isLinux() ) {
      platform = "linux";
    }
    else {
      platform = "unix";
    }

    return platform;
  }

  public static boolean isPackage( File file ) {
    boolean result = false;

    if ( isMacOS() && file.isDirectory() ) {
      if ( file.getName().endsWith(".app") ||
           file.getName().endsWith(".uamx") ||
           file.getName().endsWith(".pkg") ||
           file.getName().endsWith(".mpkg") ) {
        result = true;
      }
    }

    return result;
  }

  public static boolean isHiddenFile( File file ) {
    boolean result = false;

    if ( !isWinOS() ) {
      result = file.getName().startsWith(".");
    }
    else {
      result = FileSystemView.getFileSystemView().isHiddenFile( file );
    }

    return result;
  }

  public static boolean supportsFont( String fontName, Locale locale ) {
    boolean result = true;

    if ( "Verdana".equals(fontName) ) {
      result = !locale.toString().toLowerCase().equals("ja_jp"); 
    }

    return result;
  }

  public static String convertToUTF8( String orig ) {
    String result = orig;

    try {
      result = new String(result.getBytes(), "UTF8");
    }
    catch ( UnsupportedEncodingException uee ) {}

    return result;
  }

  public static void outputStreamPrintln( OutputStream os, String msg, 
                                          boolean flush ) 
                                        throws IOException {
    outputStreamPrint( os, msg, true, flush );
  }

  public static void outputStreamPrint( OutputStream os, String msg, 
                                        boolean withNewLine, boolean flush ) 
                                        throws IOException {
    if (os != null && msg != null) {
      os.write(msg.getBytes());
      if (withNewLine)
        os.write(System.getProperty("line.separator").getBytes());
      if (flush)
        os.flush();
    }
  }

  public static void openURL( String url ) {
    try {
      if ( isMacOS() ) {
        Class fileMgr = Class.forName("com.apple.eio.FileManager");
        Method openURL = fileMgr.getDeclaredMethod("openURL",
          new Class[] {String.class});
        openURL.invoke(null, new Object[] {url});
      }
      else if ( isWinOS() ) {
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
      }
      else {
        String[] browsers = {
          "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
        String browser = null;
        for (int count = 0; count < browsers.length && browser == null; count++) {
          if (Runtime.getRuntime().exec( new String[] {"which", browsers[count]}).waitFor() == 0)
              browser = browsers[count];
        }
  
        if (browser == null)
          throw new Exception("Could not find web browser");
        else
          Runtime.getRuntime().exec(new String[] {browser, url});
      }
    }
    catch ( Exception e ) {
    }
  }
}

