<?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<?SecureFTP this is data for SecureFTP ?>

<helpset version="1.0">

  <!-- title -->
  <title>Ayuda de FTP seguro</title>

  <!-- maps -->
  <maps>
     <homeID>intro</homeID>
     <mapref location="Map_es.jhm"/>
  </maps>

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Contenidos</label>
    <type>javax.help.TOCView</type>
    <data>SecureFTPTOC_es.xml</data>
  </view>

  <view>
    <name>Index</name>
    <label>Indice</label>
    <type>javax.help.IndexView</type>
    <data>SecureFTPIndex_es.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Búsqueda</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch_es
    </data>
  </view>

  <presentation default=true>
    <size width="700" height="550" />
    <location x="100" y="100" />
  </presentation>

</helpset>
