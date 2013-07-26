<?xml version='1.0' encoding='ISO-8859-1' ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<?SecureFTP this is data for SecureFTP ?>

<helpset version="1.0">

  <!-- title -->
  <title>Aide sur Secure FTP</title>

  <!-- maps -->
  <maps>
     <homeID>intro</homeID>
     <mapref location="Map_fr.jhm"/>
  </maps>

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Contenu de l'aide</label>
    <type>javax.help.TOCView</type>
    <data>SecureFTPTOC_fr.xml</data>
  </view>

  <view>
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>SecureFTPIndex_fr.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Recherche</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch_fr
    </data>
  </view>

  <presentation default=true>
    <size width="700" height="550" />
    <location x="100" y="100" />
  </presentation>

</helpset>
