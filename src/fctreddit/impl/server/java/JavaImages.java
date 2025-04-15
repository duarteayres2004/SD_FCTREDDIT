package fctreddit.impl.server.java;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.rest.RestUsersClient;
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

    private UsersClient usersClient;
    private String imgPath;


    public URI tryDiscovery(String serviceName){
        URI[] Uris = discovery.knownUrisOf(serviceName,1);
        URI Uri = Uris[0];
        return Uri;
    }


    public JavaImages(String URI){
        this.imgPath = URI;

        try {
            discovery = new Discovery(Discovery.DISCOVERY_ADDR);
            discovery.start();
            URI usersUri = this.tryDiscovery("Users");
            if (usersUri == null) {
                Log.info("URI invalid");
                //return Result.error(Result.ErrorCode.NOT_FOUND);
            }


            //Adicionei isto, dps vê se ta tudo certo

            this.usersClient = usersUri.toString().endsWith("rest") ? new RestUsersClient(usersUri) : new GrpcUsersClient();
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
            Result<User> r = getUserError(userId,password);
            if(!r.isOK()) {
                return Result.error(r.error());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        Path path = Path.of(imgPath,userId,imageID,".png");
        File file = new File(String.valueOf(path));
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

        Path path = Path.of(imgPath,userId,imageId,".png");
        File file = new File(String.valueOf(path));
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

        try {
            Result<User> r = getUserError(userId,password);
            if(!r.isOK()) {
                return Result.error(r.error());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        Path path = Path.of(imgPath,userId,imageId,".png");
        File file = new File(String.valueOf(path));
        if(!file.exists()){
            Log.info("No image found.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        if(!file.delete()){
            Log.info("delete failed.");
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
        Log.info("Image deleted.");
        return Result.ok();
    }

    private Result<User> getUserError(String userId, String pwd) {
        Result<User> u = usersClient.getUser(userId, pwd);

        if (u.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        } else if (u.error().equals(Result.ErrorCode.FORBIDDEN)) {
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }
        return u;
    }

}
