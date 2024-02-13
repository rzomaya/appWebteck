# appWebteck


#usage

to retrive the locationX call 

         window.Android.getLocation();
      
      
once the locationX is available this function will be called : value is locationX object 

       window.locationGotFromAndroid = function (value) {
              const locationX = JSON.parse(value);
               console.log(locationX);
       };
        
        
        
to capture Image call this function

       window.Android.captureImage("directoryName","imageName.jpg");
       
after the image is captured this function will be called value is in base64 

       window.imgGotFromAndroid = function (value) {
             console.log(value);
         };
         
         
         
         
to record Video call this function

        window.Android.recordVideo("directoryName","videoName.mp4");
        
after the video is captured this function will be called value is in base64 

        window.videoGotFromAndroid = function (value) {
            console.log(value);
          };       
        
        
        
 to select Image call this function
 
        window.Android.selectImage();
        
    
after selecting the image this function will be called value is in base64

       window.imgGotFromAndroid = function (value) {
                   console.log(value);
               };
        
        

to load all images of an order

      let value = window.Android.loadOrdersPhotos("directoryName", "orderId");
          let array = JSON.parse(value);
          let imagesNamesArray = Object.keys(array);// this will get all images names from the array
 
          
        
to create a File

     window.Android.createFile("directoryName","fileName.json","content");
     
     
to load one image / video / json
 
       let value = window.Android.load("directoryName","fileName.json/jpg/png/mp4");
       
             console.log(value);
    
        // if file is json it will return the content else base64
        
        
 to save files like pdfs 
     
        window.Android.saveFiles(fileName, base64);
        //files will be saved in android download folder 
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        


