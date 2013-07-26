
//*****************************************************************************
//*
//* (c) Copyright 2003. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: BookmarkManager.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.gui;

import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jaxen.*;
import org.jaxen.jdom.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class BookmarkManager {
  private static BookmarkManager instance = null;
  public static final String VERSION = "2.0";
  private static String pathToBookmarks = null;
  private static final String bookmarkFile = "bookmarks.xml";
  private Document doc = null;
  private ArrayList bookmarks = null;
  private ArrayList localBookmarks = new ArrayList();
  private ArrayList globalBookmarks = new ArrayList();

  private static boolean lookForGlobal = true;

  private BookmarkManager() {
    this( null );    
  }

  private BookmarkManager( String ptb ) {
    pathToBookmarks = ptb;

    if ( null != pathToBookmarks ) {
      // check to see if the bookmark folder exists, if not create it
      File bookmarkFolder = new File( pathToBookmarks );
      if ( !bookmarkFolder.exists() ) {
        bookmarkFolder.mkdirs();
      }

      // check to see if the bookmark file exists, if not create it
      File bookFile = new File( bookmarkFolder, bookmarkFile );
      if ( bookFile.exists() ) {
	try {
          loadBookmarks( bookFile );
	}
	catch ( Exception e ) {
          if (SecureFTP.debug) {
            e.printStackTrace();
          }
        }
      }
    }

    Thread bt = new Thread() {
      public void run() {
        try {
          if ( loadGlobalBookmarks() ) {
            Client.getMenus().updateBookmarks();
          }
        }
        catch ( Exception e ) {}
      }
    };

    if ( lookForGlobal ) {
      if ( Client.startWithOpenDialog() ) {
        try {
          loadGlobalBookmarks();
        }
        catch ( Exception e ) { e.printStackTrace(); }
      }
      else {
        bt.start();
      }
    }
  }

  public static BookmarkManager getLocalInstance() {
    String fileSep = File.separator;
    String pathToBookmarks = System.getProperty( "user.home" ) + fileSep +
			     ".secureftp" + fileSep;
    if ( null == instance ) {
      lookForGlobal = false;
      instance = new BookmarkManager( pathToBookmarks );
    }

    return instance;
  }

  public static BookmarkManager getInstance() {
    String fileSep = File.separator;
    String pathToBookmarks = System.getProperty( "user.home" ) + fileSep +
			     ".secureftp" + fileSep;
    if ( null == instance ) {
      instance = new BookmarkManager( pathToBookmarks );
    }

    return instance;
  }
						     
  public static BookmarkManager getInstance( String pathToBookmarks ) {
    if ( null == instance ) {
      instance = new BookmarkManager( pathToBookmarks );
    }

    return instance;
  }

  public void loadBookmarks( File bookFile ) 
                                        throws BookmarkException, IOException {
    try {
      //File bookFile = new File( pathToBookmarks + bookmarkFile );
      doc = new SAXBuilder().build( bookFile );
      XPath path = new JDOMXPath( "/booklist/bookmark" );
      List results = path.selectNodes( doc );
      Iterator iter = results.iterator();
      localBookmarks.clear();

      while( iter.hasNext() ) {
        Element element = (Element)iter.next();
	Bookmark book = new Bookmark();

        book.setProfile( element.getChildTextNormalize("profile") ); 
        book.setHostName( element.getChildTextNormalize("hostname") ); 
	book.setPort( Util.parseInt(element.getChildTextNormalize("port"),
                      Constants.DEF_EXPLICIT_SSL_PORT) );
	book.setUserName( element.getChildTextNormalize("username") );
	book.setEncryptedPassword( element.getChildTextNormalize("password") );
	book.setSecurityMode( Util.parseInt(
                                element.getChildTextNormalize("security"), 
                                ConnectionDialog.EXPLICIT_SSL) );
	book.setPassiveConnection( 
          (new Boolean(element.getChildTextNormalize("pasv"))).booleanValue() );
	book.setAnonymous( 
          (new Boolean(element.getChildTextNormalize("anon"))).booleanValue() );
	book.setDataEncrypt( 
          (new Boolean(
            element.getChildTextNormalize("ssldata"))).booleanValue() );
	book.setCCCEnabled( 
          (new Boolean(
            element.getChildTextNormalize("ccc"))).booleanValue() );
	book.setRemoteFolder( element.getChildTextNormalize("remotedir") );
        book.setLocalFolder( element.getChildTextNormalize("localdir") );
        book.setProxy( 
          (new Boolean(
            element.getChildTextNormalize("proxy"))).booleanValue() );

	localBookmarks.add( book );
      }
    }
    catch ( JaxenException je ) {
      throw new BookmarkException( je.getMessage() );
    }
    catch ( JDOMException jdome ) {
      throw new BookmarkException( jdome.getMessage() );
    }
  }

  public boolean loadGlobalBookmarks() throws BookmarkException, IOException {
    URL globalBookURL = null;

    globalBookURL = Client.getGlobalBookmarksURL();

    if ( null == globalBookURL ) {
      return false;
    }

    try {
      Document doc2 = new SAXBuilder().build( globalBookURL );
      XPath path = new JDOMXPath( "/booklist/bookmark" );
      List results = path.selectNodes( doc2 );
      Iterator iter = results.iterator();
      globalBookmarks.clear();

      while( iter.hasNext() ) {
        Element element = (Element)iter.next();
	Bookmark book = new Bookmark();

        book.setProfile( element.getChildTextNormalize("profile") ); 
        book.setHostName( element.getChildTextNormalize("hostname") ); 
	book.setPort( Util.parseInt(element.getChildTextNormalize("port"),
                      Constants.DEF_EXPLICIT_SSL_PORT) );
	book.setUserName( element.getChildTextNormalize("username") );
	book.setEncryptedPassword( element.getChildTextNormalize("password") );
	book.setSecurityMode( Util.parseInt(
                                element.getChildTextNormalize("security"), 
                                ConnectionDialog.EXPLICIT_SSL) );
	book.setPassiveConnection( 
          (new Boolean(element.getChildTextNormalize("pasv"))).booleanValue() );
	book.setAnonymous( 
          (new Boolean(element.getChildTextNormalize("anon"))).booleanValue() );
	book.setDataEncrypt( 
          (new Boolean(
            element.getChildTextNormalize("ssldata"))).booleanValue() );
	book.setCCCEnabled( 
          (new Boolean(
            element.getChildTextNormalize("ccc"))).booleanValue() );
	book.setRemoteFolder( element.getChildTextNormalize("remotedir") );
        book.setLocalFolder( element.getChildTextNormalize("localdir") );
        book.setProxy( 
          (new Boolean(
            element.getChildTextNormalize("proxy"))).booleanValue() );

	globalBookmarks.add( book );
      }
    }
    catch ( JaxenException je ) {
      throw new BookmarkException( je.getMessage() );
    }
    catch ( JDOMException jdome ) {
      throw new BookmarkException( jdome.getMessage() );
    }
    catch ( UnknownHostException uhe ) {
      LString msg =
        new LString("ConnectThread.connectionFailed1", "Connection failed: [^0]");
      msg.replace( 0, uhe.getMessage() );
      ErrorDialog.showDialog( msg );
      return false;
    }

    return true;
  }

  public boolean hasBookmarks() {
    return size() > 0;
  }

  public int size() {
    return globalBookmarks.size() + localBookmarks.size();
  }

  public void addBookmark( Bookmark book ) {
    localBookmarks.add( book ); 
    buildBookmarkList();
  }

  public Bookmark getBookmark( int index ) {
    Bookmark result = null;

    // if we have global bookmarks... put them up top
    if ( globalBookmarks.size() > 0 && isGlobalBookmark(index) ) {
      result = (Bookmark)globalBookmarks.get( index );
    }
    else {
      result = (Bookmark)localBookmarks.get( index - globalBookmarks.size() );
    }

    return result;
  }

  public Bookmark getBookmarkCopy( int index ) {
    Bookmark newBook = new Bookmark();
    Bookmark oldBook = getBookmark( index );

    newBook.setProfile( oldBook.getProfile() );
    newBook.setHostName( oldBook.getHostName() );
    newBook.setPort( oldBook.getPort() );
    newBook.setUserName( oldBook.getUserName() );
    newBook.setPassword( oldBook.getPassword() );
    newBook.setAnonymous( oldBook.isAnonymous() );
    newBook.setSecurityMode( oldBook.getSecurityMode() );
    newBook.setDataEncrypt( oldBook.isDataEncrypted() );
    newBook.setCCCEnabled( oldBook.isCCCEnabled() );
    newBook.setRemoteFolder( oldBook.getRemoteFolder() );
    newBook.setLocalFolder( oldBook.getLocalFolder() );
    newBook.setPassiveConnection( oldBook.isPassiveConnection() );
    newBook.setProxy( oldBook.usesProxy() );

    return newBook;
  }

  public void deleteBookmark( int index ) {
    localBookmarks.remove( index );
    buildBookmarkList();
  }

  protected void addBookmarkToDOM( Bookmark book ) {
    Element root = doc.getRootElement();

    Element bookmark = new Element( "bookmark" );

    addElement( "profile", book.getProfile(), bookmark );
    addElement( "hostname", book.getHostName(), bookmark );
    addElement( "port", 
                new Integer(book.getPort()).toString(), bookmark );
    addElement( "username", book.getUserName(), bookmark );
    addElement( "password", book.getEncryptedPassword(), bookmark );
    addElement( "security", 
                new Integer(book.getSecurityMode()).toString(), bookmark );
    addElement( "pasv", new Boolean(book.isPassiveConnection()).toString(),
                bookmark );
    addElement( "anon", new Boolean(book.isAnonymous()).toString(), 
                bookmark );
    addElement( "proxy", new Boolean(book.usesProxy()).toString(), 
                bookmark );
    addElement( "ssldata", new Boolean(book.isDataEncrypted()).toString(), 
                 bookmark );
    addElement( "ccc", new Boolean(book.isCCCEnabled()).toString(), 
                 bookmark );

    if ( book.getRemoteFolder() != null ) { 
      addElement( "remotedir", book.getRemoteFolder().trim(), bookmark );
    }

    if ( book.getLocalFolder() != null ) {
      addElement( "localdir", book.getLocalFolder().trim(), bookmark );
    }

    root.addContent( bookmark );
  }

  private void addElement( String name, String content, Element bookmark ) {
    Element e = new Element( name );
    e.addContent( content );
    bookmark.addContent( e );
  }

  public List getGlobalBookmarks() {
    return globalBookmarks;
  }

  public List getLocalBookmarks() {
    return localBookmarks;
  }

  private void buildBookmarkList() {
    bookmarks = new ArrayList();

    for( int i = 0; i < globalBookmarks.size(); i++ ) {
      bookmarks.add( globalBookmarks.get(i) );
    }

    for( int i = 0; i < localBookmarks.size(); i++ ) {
      bookmarks.add( localBookmarks.get(i) );
    }
  }

  public boolean isGlobalBookmark( int index ) {
     return index < globalBookmarks.size();
  }

  public boolean hasGlobalBookmarks() {
    return globalBookmarks.size() > 0;
  }

  public boolean hasLocalBookmarks() {
    return localBookmarks.size() > 0;
  }

  public List getBookmarks() {
    if ( null == bookmarks ) {
      buildBookmarkList();
    }

    return bookmarks;
  }

  public void writeBookmarks() throws IOException {
    doc = new Document();
    Element root = new Element( "booklist" );
    doc.setRootElement( root );

    Element version = new Element( "version" );
    version.addContent( VERSION );
    root.addContent( version );

    Iterator iter = localBookmarks.iterator();
    while( iter.hasNext() ) {
      Bookmark book = (Bookmark)iter.next();
      addBookmarkToDOM( book );
    }

    File bookFile = new File( pathToBookmarks + bookmarkFile );
    FileOutputStream fos = new FileOutputStream( bookFile );

    XMLOutputter output = new XMLOutputter( "  ", true );
    output.output( doc, fos );

    fos.close();
  }
}

