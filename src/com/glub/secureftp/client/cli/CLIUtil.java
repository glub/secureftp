
//*****************************************************************************
//*
//* (c) Copyright 2002. Glub Tech, Incorporated. All Rights Reserved.
//*
//* $Id: CLIUtil.java 37 2009-05-11 22:46:15Z gary $
//*
//*****************************************************************************

package com.glub.secureftp.client.cli;

import com.glub.util.*;
import com.glub.secureftp.bean.*;

import java.io.*;
import java.text.*;
import java.util.*;
import org.apache.regexp.*;

public class CLIUtil {

  /**
   * This method builds and returns the first valid path substituting globs
   * for files when needed.
   */
  public static File globLocalPathForDir( String absDirPath ) 
                     throws FileNotFoundException {

    boolean directoryOnly = true;

    absDirPath = Util.searchAndReplace( absDirPath, "*", ".*", true );
    absDirPath = Util.searchAndReplace( absDirPath, "?", ".*", true );

    File result = new File( absDirPath );

    if ( !result.exists() ) {
      StringTokenizer tok = 
        new StringTokenizer( absDirPath, System.getProperty("file.separator") );

      result = null;

      // for each token, find matching files/folders
      while( tok.hasMoreTokens() ) {
        String token = tok.nextToken();

        File parentDir = null;

        if ( null == result ) {
          // if we're on windows, handle slightly differently
          if ( token.length() > 1 && token.charAt(1) == ':' ) {
            result = new File( token + File.separator );
            if ( tok.hasMoreTokens() ) {
              token = tok.nextToken();
            }
            parentDir = result;
          }
          else {
            result = new File( File.separator + token );
            parentDir = new File(File.separator);
          }
        }
        else {
          parentDir = (new File(result, token)).getParentFile();
        } 

        if ( token.equals(".*") && !tok.hasMoreTokens() ) {
          return parentDir;
        }
        else if ( token.equals("..") ) {
          result = result.getParentFile();
          if ( null != result.getParentFile() ) {
            parentDir = result.getParentFile(); 
            token = result.getName();
          }
          else {
            parentDir = result;
            if ( tok.hasMoreTokens() ) {
              token = tok.nextToken();
            }
          }
        }
        else if ( token.equals(".") ) {
          if ( tok.hasMoreTokens() ) {
            token = tok.nextToken();
          }
        }

        File[] filesInCurrentDir = parentDir.listFiles();

        try {
          RECompiler compiler = new RECompiler();
          RE fileRegex = new RE();
          fileRegex.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
          REProgram pattern = compiler.compile("^" + token + "$");
          fileRegex.setProgram(pattern);
          File[] filesFound = globLocalFiles( token, filesInCurrentDir, 
                                              fileRegex );
          if ( filesFound.length < 1 ) {
            throw new FileNotFoundException();
          }

          result = filesFound[0];

          if ( directoryOnly && !result.isDirectory() ) {
            boolean foundDir = false;
            for ( int i = 1; i < filesFound.length; i++ ) {
              if ( filesFound[i].isDirectory() ) {
                result = filesFound[i];
                foundDir = true;
                break;
              }
            }

            if ( !foundDir ) {
              throw new FileNotFoundException();
            }
          } 
        }
        catch ( FileNotFoundException fnfe ) {
          throw new FileNotFoundException(absDirPath);
        }
        catch ( RESyntaxException rese ) { 
          throw new FileNotFoundException(absDirPath);
        }

        if ( null == result )
          result = new File( result, token );

        //System.out.println(result.getAbsolutePath());
      }

    }

    return result;
  }

  public static short GLOB_ONLY_FILES = 0;
  public static short GLOB_ONLY_DIRS  = 1;
  public static short GLOB_ALL_FILES  = 2;

  /**
   * This method builds and returns all the files that match the fileGlob
   * parameter.
   */
  public static File[] globLocalPathForFiles( String fileGlob, 
		                              short fileSelectionMode ) 
                     throws FileNotFoundException {
    File[] filesFound = null;

    fileGlob = Util.searchAndReplace( fileGlob, "*", ".*", true );
    fileGlob = Util.searchAndReplace( fileGlob, "?", ".*", true );

    File simpleGuess = new File( fileGlob );

    if ( !simpleGuess.exists() ) {
      StringTokenizer tok = 
        new StringTokenizer( fileGlob, File.separator );

      simpleGuess = null;

      // for each token, find matching files/folders
      while( tok.hasMoreTokens() ) {
	    filesFound = null;

        String token = tok.nextToken();

        File parentDir = null;

        if ( null == simpleGuess ) {
          // if we're on windows, handle slightly differently
          if ( token.length() > 1 && token.charAt(1) == ':' ) {
            simpleGuess = new File( token + File.separator );
            if ( tok.hasMoreTokens() ) {
              token = tok.nextToken();
            }
            parentDir = simpleGuess;
          }
          else {
            simpleGuess = new File( File.separator + token );
            parentDir = new File(File.separator);
          }
        }
        else {
          parentDir = (new File(simpleGuess, token)).getParentFile();
        } 

        if ( token.equals(".*") && !tok.hasMoreTokens() ) {
          filesFound = parentDir.listFiles();
        }
        else if ( token.equals("..") ) {
          simpleGuess = simpleGuess.getParentFile();
          if ( null != simpleGuess.getParentFile() ) {
            parentDir = simpleGuess.getParentFile(); 
            token = simpleGuess.getName();
          }
          else {
            parentDir = simpleGuess;
            if ( tok.hasMoreTokens() ) {
              token = tok.nextToken();
            }
          }
        }
        else if ( token.equals(".") ) {
          if ( tok.hasMoreTokens() ) {
            token = tok.nextToken();
          }
        }

	if ( filesFound == null ) {
          File[] filesInCurrentDir = parentDir.listFiles();

          try {
            RECompiler compiler = new RECompiler();
            RE fileRegex = new RE();
            fileRegex.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
            REProgram pattern = compiler.compile("^" + token + "$");
            fileRegex.setProgram(pattern);
            filesFound = globLocalFiles( token, filesInCurrentDir, 
                                                fileRegex );

            if ( filesFound.length < 1 ) {
              throw new FileNotFoundException();
            }

	    simpleGuess = filesFound[0];
          }
          catch ( FileNotFoundException fnfe ) {
            throw new FileNotFoundException(fileGlob);
          }
          catch ( RESyntaxException rese ) { 
            throw new FileNotFoundException(fileGlob);
          }
        }
      }
    }

    if ( fileSelectionMode == GLOB_ONLY_FILES || 
         fileSelectionMode == GLOB_ONLY_DIRS ) {
      ArrayList updatedFilesFoundArray = new ArrayList( filesFound.length );
      for ( int i = 0; i < filesFound.length; i++ ) {
        if ( fileSelectionMode == GLOB_ONLY_FILES && 
             filesFound[i].isFile() ) {
          updatedFilesFoundArray.add( filesFound[i] );
        }
	else if ( fileSelectionMode == GLOB_ONLY_DIRS && 
                  filesFound[i].isDirectory() ) {
	  updatedFilesFoundArray.add( filesFound[i] );
	}
      }
      
      if ( updatedFilesFoundArray.size() < 1 ) {
        throw new FileNotFoundException(fileGlob);
      }

      filesFound = (File[])updatedFilesFoundArray.toArray( new File[1] );
    }

    return filesFound;
  }

  public static File[] globLocalFiles( String data,  File[] filesToGlob,
                                       RE regExp ) 
                                       throws FileNotFoundException {
    ArrayList results = new ArrayList();

    Arrays.sort( filesToGlob );

    boolean foundMatch = false;

    for ( int i = 0; i < filesToGlob.length; i++ ) {
      //System.out.println(data + " " + filesToGlob[i].getName());
      if ( data.equals(filesToGlob[i].getName()) ) {
        results.add( filesToGlob[i] );
        foundMatch = true;
      }
      else if ( regExp.match( filesToGlob[i].getName() ) ) {
        results.add( filesToGlob[i] ); 
        //System.out.println("found! " + filesToGlob[i].getName());
        foundMatch = true;
      }
    } 

    if ( !foundMatch ) {
      throw new FileNotFoundException("no files matching criteria");
    } 

    return (File[])results.toArray( new File[1] );
  } 

  public static String getPassword( String prompt, boolean pwMask ) 
                                    throws IOException {
    StringBuffer password = new StringBuffer();
    PrintStream out = SecureFTP.getFTPSession().getPrintStream();

    MaskingThread maskingThread = null;

    if ( pwMask ) {
      maskingThread = new MaskingThread(prompt);
      maskingThread.setPriority(Thread.MAX_PRIORITY);
      maskingThread.start();
    }
    else {
      out.print( prompt );
    }

    while (true) {
      char c = (char)SecureFTP.getInputStream().read();

      if ( pwMask ) {
        maskingThread.stopMasking();
      }

      if ( c == '\r' ) {
        c = (char)SecureFTP.getInputStream().read();
        if ( c == '\n' ) {
          break;
        }
        else {
          continue;
        }
      }
      else if ( c == '\n' ) {
        break;
      }
      else {
        password.append(c);
      }
    }

    return password.toString();
  }

  public static boolean yesNoPrompt( String msg ) {
    return yesNoPrompt( msg, YN_NO );
  }

  public static final short YN_NO     = 0;
  public static final short YN_YES    = 1;

  public static boolean yesNoPrompt( String msg, short defaultAction ) {
    boolean result = false;

    String defaultChoice = null;
    switch ( defaultAction ) {
      case YN_YES:
        defaultChoice = "y";
        result = true;
        break;

      case YN_NO:
      default:
        defaultChoice = "n";
        result = false;
        break;
    }

    PrintStream out = SecureFTP.getFTPSession().getPrintStream();
    out.print( msg + " [y|n](" + defaultChoice + "): " );

    try {
      BufferedReader stdin = SecureFTP.getInput();
      String input = stdin.readLine();
      if ( input == null ) {}
      else if ( input.trim().length() == 0 ) {}
      else if ( input.trim().equalsIgnoreCase("y") ) {
        result = true;
      }
      else {
        result = false;
      }
    }
    catch ( IOException ioe ) {}

    return result;
  }

  public static final short YNA_NO     = 0;
  public static final short YNA_YES    = 1;
  public static final short YNA_ALWAYS = 2;

  public static short yesNoAlwaysPrompt( String msg ) {
    return yesNoAlwaysPrompt( msg, YNA_NO );
  }

  public static short yesNoAlwaysPrompt( String msg, short defaultAction ) {
    short result = defaultAction;

    String defaultChoice = null;
    switch ( defaultAction ) {
      case YNA_YES:
        defaultChoice = "y";
        break;

      case YNA_ALWAYS:
        defaultChoice = "a";
        break;

      case YNA_NO:
      default:
        defaultChoice = "n";
        break;
    }

    PrintStream out = SecureFTP.getFTPSession().getPrintStream();
    out.print( msg + " [y|n|a](" + defaultChoice + "): " );

    try {
      BufferedReader stdin = SecureFTP.getInput();
      String input = stdin.readLine();
      if ( input == null ) {}
      else if ( input.trim().length() == 0 ) {}
      else if ( input.trim().equalsIgnoreCase("y") ) {
        result = YNA_YES;
      }
      else if ( input.trim().equalsIgnoreCase("a") ) {
        result = YNA_ALWAYS;
      }
      else {
        result = YNA_NO;
      }
    }
    catch ( IOException ioe ) {}

    return result;
  }
}

class MaskingThread extends Thread {
  private boolean stop = false;
  private String prompt;

  public MaskingThread( String prompt ) {
    this.prompt = prompt;
  }

  public void run() {
    while (!stop) {
      try {
        System.out.print("     \b\r" + prompt);
        System.out.flush();
        sleep(5);
      }
      catch (InterruptedException ie) {}
    }
  }

  public void stopMasking() { stop = true; }
}

class ProgressThread extends Thread implements Progress {
  private boolean stop = false;
  private String progress = "";
  private long total = 0;
  private int indeterminateState = -1;

  public void run() {
    while (!stop) {
      try {
        System.out.print("     \b\r" + progress);
        System.out.flush();
        sleep(5);
      }
      catch (InterruptedException ie) {}
    }
  }

  public boolean isRunning() { return !stop; }

  public void startProgress() {}

  public void finishProgress() { 
    stop = true; 

    DecimalFormat df = new DecimalFormat("#.##"); 
    df.setGroupingUsed( true );
    df.setMaximumFractionDigits( 2 );

    double kbTot = total / 1024.0;
    double mbTot = kbTot / 1024.0;
    double gbTot = mbTot / 1024.0;

    if ( kbTot >= 1 && mbTot < 1 ) {
      System.out.println("     \b\r[********************] 100% : " +
                         df.format(kbTot) + " of " + 
                         df.format(kbTot) + " KB transferred" );
    }
    else if ( mbTot >= 1 && gbTot < 1 ) {
      System.out.println("     \b\r[********************] 100% : " +
                         df.format(mbTot) + " of " + 
                         df.format(mbTot) + " MB transferred" );
    }
    else if ( gbTot >= 1 ) {
      System.out.println("     \b\r[********************] 100% : " +
                         df.format(gbTot) + " of " + 
                         df.format(gbTot) + " GB transferred" );
    }
    else if ( total > 0 ) {
      System.out.println("     \b\r[********************] 100% : " +
                         total + " of " + 
                         total + " bytes transferred" );
    }
    else if ( total < 0 ) {
      System.out.println("     \b\r[********************] 100%");
    }
  }

  public void updateProgress( long pos, long total ) {
    double percentDone = ((pos * 1.0) / total) * 100;
    this.total = pos;
    StringBuffer buf = new StringBuffer("["); 

    if ( total > 0 && percentDone <= 100 ) {
      for ( int i = 0; i < (int)percentDone / 5; i++ ) {
        buf.append("*");
      }
      for ( int i = (int)percentDone / 5; i < 20; i++ ) {
        buf.append(" ");
      }
    }

    // indeterminate
    else {
      indeterminateState++;
      switch ( indeterminateState % 3 ) {
        case 0:
           buf.append("*  *  *  *  *  *  * ");
           break;
        case 1:
           buf.append(" *  *  *  *  *  *  *");
           break;
        case 2:
           buf.append("  *  *  *  *  *  *  ");
           break;
      }
    }

    buf.append("] ");
    if ( indeterminateState < 0 ) {
      buf.append((int)Math.ceil(percentDone));
    }
    else {
      buf.append("unk");
    }
    buf.append("% : ");
    
    DecimalFormat df = new DecimalFormat("#.##"); 
    df.setGroupingUsed( true );
    df.setMaximumFractionDigits( 2 );

    double kbPos = pos / 1024.0;
    double mbPos = kbPos / 1024.0;
    double gbPos = mbPos / 1024.0;

    double kbTot = total / 1024.0;
    double mbTot = kbTot / 1024.0;
    double gbTot = mbTot / 1024.0;

    if ( (kbPos >= 1 && mbPos < 1) ||
         (kbTot >= 1 && mbTot < 1) ) {
      buf.append(df.format(kbPos));
      if ( indeterminateState < 0 ) {
        buf.append(" of ");
        buf.append(df.format(kbTot));
      }
      buf.append(" KB transferred" );
    }
    else if ( (mbPos >= 1 && gbPos < 1) ||
              (mbTot >= 1 && gbTot < 1) ) {
      buf.append(df.format(mbPos));
      if ( indeterminateState < 0 ) {
        buf.append(" of ");
        buf.append(df.format(mbTot));
      }
      buf.append(" MB transferred " );
    }
    else if ( gbPos >= 1 || gbTot >= 1 ) {
      buf.append(df.format(gbPos));
      if ( indeterminateState < 0 ) {
        buf.append(" of ");
        buf.append(df.format(gbTot));
      }
      buf.append(" GB transferred" );
    }
    else {
      buf.append(pos);
      if ( indeterminateState < 0 ) {
        buf.append(" of ");
        buf.append(total);
      }
      buf.append(" bytes transferred" );
    }

    progress = buf.toString();
  }
}
