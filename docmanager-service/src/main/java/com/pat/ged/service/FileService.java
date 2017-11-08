package com.pat.ged.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.pat.ged.exception.FileDocumentException;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by patricou on 08/11/2017.
 */
@Repository
public class FileService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    public static final Logger logger = LoggerFactory.getLogger( FileService.class );

    // save the file in MongoDB
    public String saveFile(MultipartFile multipartFile) throws IOException {

        DBObject metaData = new BasicDBObject();
        metaData.put("FileName", multipartFile.getOriginalFilename());

        // Save the doc ( all type ) in  MongoDB
        ObjectId objectId =
                gridFsTemplate.store(
                        multipartFile.getInputStream(),
                        multipartFile.getOriginalFilename(),
                        multipartFile.getContentType(),
                        metaData);

        if (logger.isInfoEnabled()) logger.info("Doc created id : "+objectId.toString());

        return objectId.toString();
    }

    // retrieve the file from MongoDB
    public GridFsResource getResource(String fileName){
        // with MongoDB 2.0.0.Release, we get the file like this  ( not with GridFSDBFile ) :
        Optional<GridFsResource> gridFsResource = Optional.ofNullable(gridFsTemplate.getResource(fileName));
        gridFsResource.orElseThrow(()->new FileDocumentException(fileName+ " Not Found"));

        return gridFsResource.orElse(null);
    }

}
