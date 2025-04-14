package fctreddit.impl.server.java;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.clients.java.UsersClient;
import fctreddit.server.discovery.Discovery;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

public class JavaImages implements Image {

    private static final Logger Log = Logger.getLogger(JavaUsers.class.getName());
    private final Discovery discovery;
    private final UsersClient usersClient;


    public JavaImages(UsersClient UsersClient){
        try {
            this.usersClient = UsersClient;
            discovery = new Discovery(Discovery.DISCOVERY_ADDR);
            discovery.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Discovery", e);
        }
    }

    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Log.info("create Image for user: "+userId);
        String imageID = UUID.randomUUID().toString();

        if(imageContents == null){
            Log.info("Empty Image");
             return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        try {
            Result<User> r = usersClient.getUser(userId,password);
            if(r.isOK()){
                Log.info("User authenticated.");
            } else {
                Log.info("Authentication failed. Status: " + r);
                return Result.error(Result.ErrorCode.BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

        File file = new File(String.valueOf(Path.of("fctreddit","images",userId,imageID)));
        try {
            Files.write(file.toPath(), imageContents);
        } catch(IOException e){
            Log.info("Internal error.");
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        return Result.ok();
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        Log.info("Fetching image for "+userId);
        File file = new File(String.valueOf(Path.of("fctreddit","images",userId,imageId)));
        if(!file.exists()){
            Log.info("No image found.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        try {
            byte [] image = Files.readAllBytes(file.toPath());
            Log.info("Image found.");
            return Result.ok(image);
        } catch (IOException e) {
            Log.info("Error reading file.");
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Log.info("Deleting image for "+userId);

        try{
            Result<User> r = usersClient.getUser(userId,password);
            if(r.isOK()){
                Log.info("User authenticated.");
            }
            else {
                if(r.equals(Result.error(Result.ErrorCode.FORBIDDEN))) {
                    Log.info("Authentication failed.");
                    return Result.error(Result.ErrorCode.FORBIDDEN);
                }
                Log.info("Authentication failed. Status: " + r);
                return Result.error(Result.ErrorCode.BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        File file = new File(String.valueOf(Path.of("fctreddit","images",userId,imageId)));
        if(!file.exists()){
            Log.info("No image found.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        if(!file.delete()){
            Log.info("delete failed.");
        }
        Log.info("Image deleted.");
        return Result.ok();
    }

}
