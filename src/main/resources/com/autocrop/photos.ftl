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
    </div>
	</body>
</html>
