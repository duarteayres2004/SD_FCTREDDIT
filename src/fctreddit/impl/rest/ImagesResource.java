package fctreddit.impl.rest;

import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestImage;
import fctreddit.impl.java.JavaImages;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import java.util.Arrays;
import java.util.logging.Logger;

import static fctreddit.impl.rest.UsersResource.errorCodeToStatus;

public class ImagesResource implements RestImage {
    private static final Logger Log = Logger.getLogger(ImagesResource.class.getName());
    final Image impl;
    public String URI;

    public ImagesResource(@Context UriInfo UriInfo) {
        URI = UriInfo.getAbsolutePath().toString()+"image/";
        impl = new JavaImages(URI);
    }

    @Override
    public String createImage(String userId, byte[] imageContents, String password) {
        Log.info("createImage : " + Arrays.toString(imageContents));

        Result<String> res = impl.createImage(userId, imageContents, password);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public byte[] getImage(String userId, String imageId) {
        Log.info("getImage : " + imageId);

        Result<byte[]> res = impl.getImage(userId, imageId);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public void deleteImage(String userId, String imageId, String password) {
        Log.info("deleteImage : " + imageId);

        Result<Void> res = impl.deleteImage(userId, imageId,password);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
    }
}
