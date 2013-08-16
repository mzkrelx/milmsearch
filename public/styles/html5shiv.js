

  /*

  html5shiv.js
  ============

  Description
    Gets Internet Explorer 8 (and earlier) to apply CSS rules to new HTML5 elements.

  Copyright
    2012 New Vintage Media Ltd (http://www.newvintagemedia.com/)
    2009 Remy Sharp (http://remysharp.com/2009/01/07/html5-enabling-script/)
    2008 John Resig (http://ejohn.org/blog/html5-shiv/)

  Licenses
    http://opensource.org/licenses/mit-license.php
    http://www.gnu.org/licenses/gpl-2.0.html

  */


  var elements
    = [ 'abbr', 'article', 'aside', 'audio', 'bdi', 'canvas', 'data', 'datalist',
      'details', 'dialog', 'figcaption', 'figure', 'footer', 'header', 'hgroup', 'main',
      'mark', 'nav', 'output', 'progress', 'section', 'summary', 'time', 'video' ];

  for ( var i in elements ) {

    window.top.document.createElement( elements[ i ] );

  }

