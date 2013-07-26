<?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<?SecureFTP this is data for SecureFTP ?>

<helpset version="1.0">

  <!-- title -->
  <title>Secure FTP Help</title>

  <!-- maps -->
  <maps>
     <homeID>intro</homeID>
     <mapref location="Map.jhm"/>
  </maps>

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Contents</label>
    <type>javax.help.TOCView</type>
    <data>SecureFTPTOC.xml</data>
  </view>

  <view>
    <name>Index</name>
    <label>Index</label>
    <type>javax.help.IndexView</type>
    <data>SecureFTPIndex.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch
    </data>
  </view>

  <presentation default=true>
    <size width="700" height="550" />
    <location x="100" y="100" />
  </presentation>

</helpset>
