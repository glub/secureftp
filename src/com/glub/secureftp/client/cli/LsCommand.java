
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: LsCommand.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;

import java.io.*;
import java.text.*;
import java.util.*;

public class LsCommand extends NetworkCommand {
  public LsCommand() {
    super("ls", CommandID.LS_COMMAND_ID, 0, 9999, 
          "[-lf] [remote-file ...]",
          "list contents of remote directory");
    setBeepWhenDone( false );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    ArrayList args = getArgs();
    String itemToList = null;

    FTPSession session = SecureFTP.getFTPSession();

    boolean wideList = false;
    boolean fullList = false;

    if ( args != null && args.size() > 0 &&
         ((String)args.get(0)).equalsIgnoreCase("-l") ) {
      wideList = true;
      args.remove(0);
    }
    else if ( args != null && args.size() > 0 &&
         ((String)args.get(0)).equalsIgnoreCase("-f") ) {
      fullList = true;
      args.remove(0);
    }

    if ( args != null && args.size() > 0 ) {
      boolean outputDisabled = false;

      for ( int i = 0; i < args.size(); i++ ) {
        if ( i > 0 ) {
          session.getFTPBean().setRecvCmdStream(null);
          session.getFTPBean().setSendCmdStream(null);
          outputDisabled = true;
        }

        doList( (String)args.get(i), session, wideList, fullList );
      }

      if ( outputDisabled ) {
        session.getFTPBean().setSendCmdStream(session.getOutputStream());
        session.getFTPBean().setRecvCmdStream(session.getOutputStream());
      }
    }
    else {
      doList( null, session, wideList, fullList );
    }

    return result;
}

private void doList( String itemToList, FTPSession session, 
                     boolean wideList, boolean fullList ) {
    PrintStream out = session.getPrintStream();
    FTP ftp = session.getFTPBean();

    try {
      RemoteFileList dirList = ftp.list( itemToList );
      boolean skipGroup = false;

      if ( dirList.size() == 0 ) {
        out.println( "File not found." );
	return;
      }

      if ( fullList ) {
        for ( int i = 0; i < dirList.size(); i++ ) {
          RemoteFile currentFile = dirList.getFile(i);

          if ( currentFile.getFileName().equals(".") ) { 
            continue;
          }

          out.println( currentFile.getFullLine() );
        }
      }
      else if ( wideList ) {
        for ( int i = 0; i < dirList.size(); i++ ) {
          RemoteFile currentFile = dirList.getFile(i);

          if ( currentFile.getFileName().equals(".") ) { 
            continue;
          }

          if ( session.isDebugOn() ) {
            out.println( "DEBUG: " + currentFile.getFullLine() );
          }

	  if ( currentFile.getFileSize() >= 0 ) {
            String perms = currentFile.getPermissions();
            if ( perms.indexOf("?") < 0 ) {
              out.print( currentFile.getPermissions() + " " );
            }
            else if ( perms.startsWith("d") ) {
              out.print( "<DIR>" + " " );
            }
	  }
          
	  if ( currentFile.getFileSize() >= 0 ) {
            if ( currentFile.getUser().length() > 0 ) {
              out.print( currentFile.getUser() ); 
              for ( int j = currentFile.getUser().length(); j <= 8; j++ ) {
                out.print(" ");
              }
            }
	  }

          if ( !skipGroup && currentFile.getGroup().trim().length() > 0 ) {
            if ( currentFile.getGroup().length() > 0 ) {
              out.print( currentFile.getGroup() ); 
              for ( int j = currentFile.getGroup().length(); j <= 8; j++ ) {
                out.print(" ");
              }
            }
          }
          else {
            if ( currentFile.getFileSize() != -1 ) {
              skipGroup = true;
            }
          }

          String sFileSize = (new Long(currentFile.getFileSize())).toString();
	  if ( currentFile.getFileSize() >= 0 ) {
            for ( int j = sFileSize.length(); j <= 10; j++ ) {
              out.print(" ");
            }
            out.print( currentFile.getFileSize() + " " ); 
          }

          Calendar date = currentFile.getDate();
          SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
	  if ( currentFile.getFileSize() >= 0 ) {
            out.print(sdf.format(date.getTime()) + " ");
	  }

          if ( currentFile.getFileSize() < 0 && 
               session.getFTPBean().getListStyle() == 
               FTPServerInfo.LIST_STYLE_UNIX ) {
            out.println("");
            currentFile.setFileName( currentFile.getFileName() + ":" );
          }
          else if ( currentFile.isDirectory() ) {
            //out.print("<d> ");
          }
          else if ( currentFile.isLink() ) {
            //out.print("<l> ");
          }
          else {
            //out.print("    ");
          }

          out.println( currentFile.getFileName() );
        }
      }
      else {
        int row = 0;
        int col = 0;
        int count = 1;

        // get max length of a filename to be listed
        int maxFileNameWidth = 0;
        for ( int i = 0; i < dirList.size(); i++ ) {
          if ( dirList.getFile(i).getFileName().equals(".") ) {
            dirList.remove(i);
            continue;
          }

          if ( dirList.getFile(i).getFileName().length() > maxFileNameWidth )
            maxFileNameWidth = dirList.getFile(i).getFileName().length(); 
        }

        int numOfCols = 1;
        int padding = 0;

        if ( maxFileNameWidth < 10 ) {
          numOfCols = 7;
          padding = 11;
        }
        else if ( maxFileNameWidth < 12 ) {
          numOfCols = 6;
          padding = 13;
        }
        else if ( maxFileNameWidth < 15 ) {
          numOfCols = 5;
          padding = 16;
        }
        else if ( maxFileNameWidth < 19 ) {
          numOfCols = 4;
          padding = 20;
        }
        else if ( maxFileNameWidth < 25 ) {
          numOfCols = 3;
          padding = 26;
        }
        else if ( maxFileNameWidth < 39 ) {
          numOfCols = 2;
          padding = 40;
        }

        double fNumOfCols = numOfCols * 1.0;
  
        int maxRows = (int)Math.ceil(dirList.size() / fNumOfCols);

        for ( int i = 0; i < maxRows * numOfCols; i++ ) {
          int item = row + (col * maxRows);

          String file = "";
  
          if ( item < dirList.size() ) {
            file = dirList.getFile(item).getFileName();
            if ( dirList.getFile(item).isDirectory() ) {
              if ( !dirList.getFile(item).getFileName().endsWith("/") ) {
                file += "/";
              }
            }
          }

          if ( count % numOfCols == 0 ) {
            out.println(file);
            row++;
            col = 0;
          }
          else {
            out.print(file);
            for ( int j = 0; j < padding - file.length(); j++ ) {
              out.print(" ");
            }
            col++;
          }
          count++;
        }
      }
    }
    catch ( IOException ioe ) {
      out.println(ioe.getMessage());
    }
    catch ( FTPPermissionDeniedException fpde ) {
      out.println("Permission denied.");
    }
    catch ( FTPException fe ) {
      out.println(fe.getMessage());
    }
  }
}

