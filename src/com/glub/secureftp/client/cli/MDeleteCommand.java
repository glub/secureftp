
//*****************************************************************************
//*
//* (c) Copyright 2004. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: MDeleteCommand.java 118 2009-12-02 09:58:24Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.secureftp.bean.*;
import com.glub.secureftp.client.framework.*;
import com.glub.util.*;

import org.apache.regexp.*;

import java.io.*;
import java.util.*;

public class MDeleteCommand extends NetworkCommand {
  public MDeleteCommand() {
    super("mdelete", CommandID.MDELETE_COMMAND_ID, 1, 9999, 
          "remote-file1 [remote-file2 ...]",
          "delete multiple files and/or directories");
    setBeepWhenDone( true );
  }

  public SecureFTPError doIt() throws CommandException {
    SecureFTPError result = super.doIt();

    FTPSession session = SecureFTP.getFTPSession();
    PrintStream out = session.getPrintStream();

    String pwd = "";
    boolean changedDir = false;

    if ( ! SecureFTP.getFTPSession().isDebugOn() ) {
      session.getFTPBean().setRecvCmdStream(null);
      session.getFTPBean().setSendCmdStream(null);
    }

    try {
      pwd = session.getFTPBean().pwd();
    }
    catch ( Exception e ) {}

    if ( ! SecureFTP.getFTPSession().isDebugOn() ) {
      session.getFTPBean().setSendCmdStream(session.getOutputStream());
      session.getFTPBean().setRecvCmdStream(session.getOutputStream());
    }

    // if we have a path separator, we need to change to that directory
    for ( int arg = 0; arg < getArgs().size(); arg++ ) {
      String fileName = (String)getArgs().get(arg);

      if ( ! SecureFTP.getFTPSession().isDebugOn() ) {
        session.getFTPBean().setRecvCmdStream(null);
        session.getFTPBean().setSendCmdStream(null);
      }

      // and then try and delete the file(s)
      if ( fileName.indexOf("/") >= 0 && 
           fileName.indexOf("/") != fileName.length() - 1 ) {
        fileName = parseForFile( fileName );
        changedDir = true;
      }
      
      RemoteFileList fileList = new RemoteFileList();

      if ( session.isGlobOn() && fileName.indexOf("*") >= 0 ) {
        // if we are globbing and we have a glob, check to see if that file
        // is in this directory
        try {
          //fileList = session.getFTPBean().list();
          // do a list all to get hidden files
          fileList = session.getFTPBean().listAll();
        }
        catch ( Exception e ) {}
        
        String fileTest = Util.searchAndReplace(fileName, "*", ".*", true);
        RECompiler compiler = new RECompiler();
        RE fileRegex = new RE();
        fileRegex.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
        REProgram pattern = compiler.compile("^" + fileTest + "$");
        fileRegex.setProgram(pattern);
 
        MDeleteCommand filesToDelete = new MDeleteCommand();
        ArrayList args = new ArrayList();

        boolean foundMatch = false;
        for ( int j = 0; j < fileList.size(); j++ ) {
          if ( fileRegex.match(fileList.getFile(j).getFileName()) ) {
            foundMatch = true;
            fileName = fileList.getFile(j).getFileName();
            args.add(fileName);            
          }
        }

        if ( foundMatch ) {
          filesToDelete.setArgs(args);
          result = 
            SecureFTP.getCommandDispatcher().fireCommand(this, filesToDelete);
          continue;
        }

        fileList.clear();
      }

      try {
        //fileList = session.getFTPBean().list(fileName);
        // do a list all to get hidden files
        fileList = session.getFTPBean().list(fileName, null, true);
      }
      catch ( Exception e ) {
      }

      boolean fileIsDir = false;

      if ( fileList.size() >= 2 ) {
        try {
          ByteArrayOutputStream baosSend = new ByteArrayOutputStream();
          ByteArrayOutputStream baosRecv = new ByteArrayOutputStream();

          if ( ! SecureFTP.getFTPSession().isDebugOn() ) {
            session.getFTPBean().setSendCmdStream(baosSend);
            session.getFTPBean().setRecvCmdStream(baosRecv);
          }

          session.getFTPBean().chdir(fileName);

          if ( ! SecureFTP.getFTPSession().isDebugOn() ) {
            session.getOutputStream().write(baosSend.toByteArray());
            session.getOutputStream().write(baosRecv.toByteArray());
          }

          fileIsDir = true;
        }
        catch ( Exception e ) {
          fileIsDir = false;
        }
      }

      if ( ! SecureFTP.getFTPSession().isDebugOn() ) {
        session.getFTPBean().setSendCmdStream(session.getOutputStream());
        session.getFTPBean().setRecvCmdStream(session.getOutputStream());
      }

      if ( fileList.size() == 0 ) {
        out.println("No such file or directory.");
        result.setCode( SecureFTPError.NO_SUCH_FILE );
      }

      for ( int i = 0; i < fileList.size(); i++ ) {
        if ( fileList.getFile(i).getFileName().equals(".") ||
             fileList.getFile(i).getFileName().equals("..") ) {
          continue;
        }
        // TODO: if we don't know the file props, we should try and change dir
        // on that file to test if it's a dir
        else if ( fileList.getFile(i).isDirectory() ) {
          try {
            MDeleteCommand mdel = new MDeleteCommand();
            ArrayList args = new ArrayList(1);
            args.add(fileList.getFile(i).getFileName());
            mdel.setArgs( args );
            result = SecureFTP.getCommandDispatcher().fireCommand(this, mdel);
          }
          catch ( Exception e ) {}
        }
        else {
          DeleteCommand del = new DeleteCommand();
          ArrayList args = new ArrayList(1);
          args.add(fileList.getFile(i).getFileName());
          del.setArgs( args );
          result = SecureFTP.getCommandDispatcher().fireCommand(this, del);
        }
      }

      if ( fileIsDir ) {
        try {
          session.getFTPBean().cdup();
          session.getFTPBean().rmdir(fileName);
        }
        catch ( Exception e ) {}
      }
    }

    if ( changedDir ) {
      CDCommand cd = new CDCommand();
      ArrayList args = new ArrayList(1);
      args.add(pwd);
      cd.setArgs(args);
      SecureFTP.getCommandDispatcher().fireCommand(this, cd);
    }

    return result;
  }

  private String parseForFile( String path ) {
    StringTokenizer tok = new StringTokenizer(path, "/", true);
    String dirToChangeTo = "";
    String result = "";

    do {
      result = tok.nextToken();
      if ( tok.hasMoreTokens() ) {
        dirToChangeTo += result;
      }
    } while( tok.hasMoreTokens() );

    CDCommand cd = new CDCommand();
    ArrayList args = new ArrayList(1);
    args.add(dirToChangeTo);
    cd.setArgs(args);
    
    SecureFTP.getCommandDispatcher().fireCommand(this, cd);
    return result;
  }
}

