package com.glub.secureftp.client.framework;

import java.util.Locale;

public class ArgParser {

//  private static final String kOptDebug    = "-debug";

  private static final String kOptLanguage = "-language";
  private static final String kOptEnglish  = "english";
  private static final String kOptFrench   = "french";
  private static final String kOptItalian  = "italian";
  private static final String kOptJapanese = "japanese";
  private static final String kOptGerman   = "german";

  public ArgParser( String[] args ) {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];

      /* choose a language */
      if ( kOptLanguage.startsWith(arg.toLowerCase()) ) {

        if ( i + 1 < args.length ) {
          String lang = args[++i];

          if ( kOptEnglish.startsWith(lang.toLowerCase()) )
            Locale.setDefault(Locale.ENGLISH);
		  
          else if ( kOptFrench.startsWith(lang.toLowerCase()) )
            Locale.setDefault(Locale.FRENCH);

          else if ( kOptItalian.startsWith(lang.toLowerCase()) )
            Locale.setDefault(Locale.ITALIAN);

          else if ( kOptJapanese.startsWith(lang.toLowerCase()) )
            Locale.setDefault(Locale.JAPANESE);

          else if ( kOptGerman.startsWith(lang.toLowerCase()) )
            Locale.setDefault(Locale.GERMAN);

          else {
            System.err.println("The language \"" + lang + 
                               "\" is not supported by this application.");
            System.err.println("Using the default language.");
          }
        }
        else {
          System.err.println("A language was not specified.");
          System.err.println("Using the default language.");
        }

      }

      /* undefined option */
      else {
        System.err.println("The option \"" + arg + 
                           "\" is not supported by this application.");
        // do we want to print a usage error? - gary
        System.exit(0);
      }
    }
  }

}
