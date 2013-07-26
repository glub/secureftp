
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: RemoteFile.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.bean;

import com.glub.util.*;

import java.io.*;
import java.util.*;

import org.apache.regexp.*;

/**
 * The <code>RemoteFile</code> class is an abstract representation of a file
 * on the FTP server.
 *
 * @author Gary Cohen
 * @version $Revision: 47 $, $Date: 2009-05-16 10:10:12 -0700 (Sat, 16 May 2009) $
 * @since 2.1.3
 */

public class RemoteFile implements Serializable {
  private static final long serialVersionUID = 1L;

  /** Used for getting the filename from an alias. */
  private transient RE matcher = null;
  private transient RECompiler compiler = null;
  private transient REProgram pattern = null;

  /** The permissions of the file. */
  private String permissions = null;

  /** The reference count of the file. */
  private int linkCount = 0;

  /** The user owner of the file. */
  private String user = null;

  /** The group owner of the file. */
  private String group = null;

  /** The size of the file. */
  private long fileSize = -1;

  /** The date of the file. */
  private Calendar date = null;

  /** The filename of the file. */
  private String fileName = null;

  /** The alias of the file. */
  private String alias = null;

  /** The full unparsed line listing */
  private String fullLine = null;

  /** Is this file an alias? */
  private boolean isLink = false;

  /** Is this file a directory? */
  private boolean isDirectory = false;

  /** Is this file a pipe? */
  private boolean isPipe = false;

  /** Is this file a door? */
  private boolean isDoor = false;

  /** Is this file a special block file? */
  private boolean isBlockSpecial = false;

  /** Is this file a character block file? */
  private boolean isCharSpecial = false;

  /** Is this file a socket? */
  private boolean isSocket = false;

  /** Is known file type? */
  private boolean isKnownFileType = true;

  //** Place to store metadata */
  private HashMap metaData = new HashMap(1);

  /** Create an empty <code>RemoteFile</code> object. */
  public RemoteFile() {
    this( null, 0, null, null, -1, null, null, null );
  }

  /**
   * Create a <code>RemoteFile</code> object.
   *
   * @param fileName  the name of the remote file.
   */
  public RemoteFile( String fileName ) {
    this( null, 0, null, null, -1, null, fileName, fileName );
  }

  /**
   * Create a <code>RemoteFile</code> object.
   *
   * @param permissions   the permissions of the remote file.
   * @param linkCount     the reference count of the remote file.
   * @param user          the user owner of the remote file.
   * @param group         the group owner of the remote file.
   * @param fileSize      the size of the remote file.
   * @param date          the modification date of the remote file.
   * @param fileName      the name of the remote file.
   * @param fullLine      the (unparsed) line as returned from the server.
   */
  public RemoteFile( String permissions, int linkCount, String user, 
                     String group, long fileSize, Calendar date, 
                     String fileName, String fullLine ) {
    setPermissions( permissions );
    setLinkCount( linkCount );
    setUser( user );
    setGroup( group );
    setFileSize( fileSize );
    setDate( date );
    setFileName( fileName );
    setAlias( null );
    setFullLine( fullLine );
  }

  /**
   * Simple display of remote file.
   *
   * @return the remote file as a String.
   */
  public String toString() { 
    String fileName = getFileName();
    if ( null != getMetaData("pwd") ) {
      fileName = getMetaData("pwd") + fileName;
    }
    return fileName;
  }

  /**
   * Get the permissions of the remote file.
   *
   * @return the permissions. 
   */
  public String getPermissions() { return this.permissions; }

  /**
   * Set the permissions of the remote file.
   *
   * @param permissions  the permissions.
   */
  public void setPermissions( String permissions ) {
    if ( permissions == null )
      permissions = "";

    this.permissions = permissions.trim();

    if ( this.permissions.length() > 0 ) {
      if ( this.permissions.startsWith("d") ) {
        isDirectory = true;
      }
      else if ( this.permissions.startsWith("D") ) {
        isDoor = true;
      }
      else if ( this.permissions.startsWith("l") ) {
        isLink = true;
      }
      else if ( this.permissions.startsWith("b") ) {
        isBlockSpecial = true;
      }
      else if ( this.permissions.startsWith("c") ) {
        isCharSpecial = true;
      }
      else if ( this.permissions.startsWith("p") ) {
        isPipe = true;
      }
      else if ( this.permissions.startsWith("s") ) {
        isSocket = true;
      }
    }
    else {
      isKnownFileType = false;
    }
  }

  /**
   * Get the link (or reference) count of the remote file.
   *
   * @return the link count. 
   */
  public int getLinkCount() { return this.linkCount; }

  /**
   * Set the link (or reference) count of the remote file.
   *
   * @param linkCount the link count. 
   */
  public void setLinkCount( int linkCount ) { 
    if ( linkCount < 0 )
      linkCount = 0;

    this.linkCount = linkCount; 
  }

  /**
   * Get the user owner of the remote file.
   *
   * @return the user owner.
   */
  public String getUser() { return this.user; }

  /**
   * Set the user owner of the remote file.
   *
   * @param user the user owner.
   */
  public void setUser( String user ) { 
    if ( user == null )
      user = "";

    this.user = user; 
  }

  /**
   * Get the group owner of the remote file.
   *
   * @return the group owner.
   */
  public String getGroup() { return this.group; }

  /**
   * Set the group owner of the remote file.
   *
   * @param group the user owner.
   */
  public void setGroup( String group ) { 
    if ( group == null )
      group = "";

    this.group = group; 
  }

  /**
   * Get the file size of the remote file.<p>
   * Note: if the fileSize == -1, the other metadata may be inaccurate.
   *
   * @return the file size owner.
   */
  public long getFileSize() { return this.fileSize; }

  /**
   * Set the file size of the remote file.
   *
   * @param fileSize the size of the file.
   */
  public void setFileSize( long fileSize ) { 
    if ( fileSize < -1 )
      fileSize = -1;

    this.fileSize = fileSize; 
  }

  /**
   * Get the modification date of the remote file.
   *
   * @return the modification date.
   */
  public Calendar getDate() { return this.date; }

  /**
   * Set the modification date of the remote file.
   *
   * @param date the modification date.
   */
  public void setDate( Calendar date ) { 
    if ( date == null )
      date = new GregorianCalendar();

    this.date = date; 
  }

  /**
   * Get the file name of the remote file.
   *
   * @return the file name.
   */
  public String getFileName() { return this.fileName; }

  /**
   * Set the file name of the remote file.
   *
   * @param fileName the file name.
   */
  public void setFileName( String fileName ) { 
    if ( fileName == null )
      fileName = "";

    //this.fileName = fileName.trim(); 
    this.fileName = fileName; 

    try {
      if ( compiler == null || matcher == null || pattern == null ) {
        try {
          compiler = new RECompiler();
          matcher  = new RE();
          //pattern = compiler.compile( "^(\\S+) -> (\\S+)$" );
          pattern = compiler.compile( "^(\\S.*) -> (.*)$" );
        }
        catch ( RESyntaxException rese ) {}
        matcher.setProgram(pattern);
      }
    }
    catch ( Exception e ) {
      // something went wrong while building the regexp
      // AIX is known to have this problem
      compiler = null;
      matcher = null;
    
      // do the regexp by hand
      int offsetLinkBegin = 0;
      while ( offsetLinkBegin >= 0 ) {
        offsetLinkBegin = this.fileName.indexOf('-', offsetLinkBegin);
        if ( this.fileName.charAt(offsetLinkBegin + 1) == '>' ) {
          // if this is true, check to see if we have a space before and
          // after the link
          char ch = this.fileName.charAt(offsetLinkBegin - 1);
          if ( ch == ' ' && 
               ch == this.fileName.charAt(offsetLinkBegin + 2) ) {

            // we've got it.

            this.alias = 
              this.fileName.substring(offsetLinkBegin + 3, 
                                      this.fileName.length());
            this.fileName =
              this.fileName.substring(0, offsetLinkBegin-1);
            break;
          }
        }
      } 
    }

    if ( null != matcher && matcher.match(this.fileName) ) {
      this.fileName = matcher.getParen(1);
      this.alias = matcher.getParen(2);
    } 
  }

  /**
   * Get the alias name of the remote file.
   *
   * @return the alias name.
   */
  public String getAlias() { return this.alias; }

  /**
   * Set the alias name of the remote file.
   *
   * @param alias the name of the alias.
   */
  public void setAlias( String alias ) {
    if ( alias == null ) 
      alias = "";

    this.alias = alias;
  }

  /**
   * Get the full (unparsed) line listing from the FTP server.
   *
   * @return the full (unparsed) line.
   */
  public String getFullLine() { return this.fullLine; }

  /**
   * Set the full (unparsed) line listing from the FTP server.
   *
   * @param fullLine the full (unparsed) line.
   */
  public void setFullLine( String fullLine ) {
    if ( fullLine == null ) {
      fullLine = "";
    }

    this.fullLine = fullLine;
  }

  /**
   * A place to store your own metadata.
   */
  public void setMetaData( Object key, Object value ) { 
    metaData.put( key, value );
  }

  /**
   * Get stored metadata.
   *
   * @return stored matadata.
   */
  public Object getMetaData( Object key ) { return metaData.get( key ); }

  /**
   * Is this a known file type?
   *
   * @return true if file type was parsed.
   */
  public boolean isKnownFileType() { return isKnownFileType; }

  /**
   * Is this file a directory?
   *
   * @return true if a directory.
   */
  public boolean isDirectory() { return isDirectory; }

  /**
   * Is this file a door?
   *
   * @return true if a door.
   */
  public boolean isDoor() { return isDoor; }

  /**
   * Is this file a symbolic link?
   *
   * @return true if a symbolic link.
   */
  public boolean isLink() { return isLink; }

  /**
   * Is this file a block special file?
   *
   * @return true if a block special file.
   */
  public boolean isBlockSpecial() { return isBlockSpecial; }

  /**
   * Is this file a character special file?
   *
   * @return true if a character special file.
   */
  public boolean isCharSpecial() { return isCharSpecial; }

  /**
   * Is this file a pipe?
   *
   * @return true if a pipe.
   */
  public boolean isPipe() { return isPipe; }

  /**
   * Is this file a socket?
   *
   * @return true if a socket.
   */
  public boolean isSocket() { return isSocket; }

  /**
   * Is this file an ordinary file?
   *
   * @return true if an ordinary file?
   */
  public boolean isFile() { return !(isDirectory || isDoor || isLink ||
                                     isBlockSpecial || isCharSpecial ||
                                     isPipe || isSocket); }
}
