<?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<?SecureFTP this is data for SecureFTP ?>

<helpset version="1.0">

  <!-- title -->
  <title>Справка по Secure FTP</title>

  <!-- maps -->
  <maps>
     <homeID>intro</homeID>
     <mapref location="Map_ru.jhm"/>
  </maps>

  <!-- views -->
  <view>
    <name>TOC</name>
    <label>Secure FTP</label>
    <type>javax.help.TOCView</type>
    <data>SecureFTPTOC_ru.xml</data>
  </view>

  <view>
    <name>Index</name>
    <label>Указатель</label>
    <type>javax.help.IndexView</type>
    <data>SecureFTPIndex_ru.xml</data>
  </view>

  <view>
    <name>Search</name>
    <label>Поиск</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch_ru
    </data>
  </view>

  <presentation default=true>
    <size width="700" height="550" />
    <location x="100" y="100" />
  </presentation>

</helpset>
