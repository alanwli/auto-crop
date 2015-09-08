<#-- @ftlvariable name="" type="com.autocrop.PhotosView" -->
<html>
  <head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
  </head>
	<body>
    <div class="container">
      <h2>Your autocropped photos</h2>
      <p>Save these images to your computer because they will only remain on the server for a few minutes. Click on the images to see it in full size.</p>
      <div class="row">
        <div class="col-md-4"></div>
        <div class="col-md-4" class="thumbnail">
          <script async src="//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"></script>
          <!-- crop-photos -->
          <ins class="adsbygoogle"
               style="display:block"
               data-ad-client="ca-pub-8350070372151679"
               data-ad-slot="9242079576"
               data-ad-format="auto"></ins>
          <script>
          (adsbygoogle = window.adsbygoogle || []).push({});
          </script>
        </div>
        <div class="col-md-4"></div>
      </div>
      <#assign numRows=(numPhotos/3)?ceiling>
      <#list 0 ..< numRows as i>
        <div class="row">
          <#list 0 ..< 3 as j>
            <#if (i*3 + j) < numPhotos>
              <div class="col-md-4">
                <a href="${requestId?html}/${i*3 + j}" class="thumbnail">
                  <img src="${requestId?html}/${i*3 + j}" alt=""/>
                </a>
              </div>
            </#if>
          </#list>
        </div>
      </#list>
      <div class="row col-md-12">
        <div class="center">
          <a href="/">Crop more photos</a>
        </div>
      </div>
      <div class="row col-md-12">
        <hr/>
      </div>
      <div class="row col-md-12">
        <div class="center">
          <p>Feel free to email me with any feedback, questions, or comments. <a href="mailto:feedback@crop-photos.com">feedback@crop-photos.com</a></p>
        </div>
      </div>
    </div>
    <script>
      (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
      (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
      m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
      })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

      ga('create', 'UA-65870728-1', 'auto');
      ga('send', 'pageview');

    </script>
	</body>
</html>
