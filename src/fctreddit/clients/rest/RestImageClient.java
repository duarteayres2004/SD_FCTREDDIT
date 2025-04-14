package fctreddit.clients.rest;


import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.api.rest.RestImage;
import fctreddit.clients.GeneralizationClient;
import fctreddit.clients.java.ImageClient;
import fctreddit.clients.java.UsersClient;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import jakarta.ws.rs.client.Invocation.Builder;

import java.net.URI;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RestImageClient extends ImageClient {

    private static final Logger Log = Logger.getLogger(RestImageClient.class.getName());

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 5;
    protected static final int RETRY_SLEEP = 5000;
    final URI serverURI;
    final Client client;
    final ClientConfig config;
    final WebTarget target;


    public RestImageClient( URI serverURI ) {
        this.serverURI = serverURI;
        this.config = new ClientConfig();
        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);


        this.client = ClientBuilder.newClient(config);
        target = client.target( serverURI ).path( RestImage.PATH );
    }

    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Log.info("Uploading image");

        return repeated(() -> {
            WebTarget imageTarget = target.path(userId).queryParam("password", password);
            Builder request = imageTarget.request(MediaType.APPLICATION_JSON);

            Response response = target.request().post(Entity.entity(imageContents, MediaType.APPLICATION_OCTET_STREAM));

            if (response == null){
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
            if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
                return Result.ok(response.readEntity(String.class));
            } else {
                return Result.error(getErrorCodeFrom(response.getStatus()));
            }

        });
    }


    public Result<byte[]> getImage(String userId, String imageId) {
        Log.info("Fetching image");

        return repeated(() -> {
            WebTarget imageTarget = target.path(userId).path(imageId);
            Builder request = imageTarget.request(MediaType.APPLICATION_OCTET_STREAM);
            Response response = target.request().get();

            if (request == null) {
                Result.error(ErrorCode.INTERNAL_ERROR);
            }
            if (response.getStatus() == Response.Status.OK.getStatusCode() && response.hasEntity()) {
                return Result.ok(response.readEntity(byte[].class));
            } else {
                return Result.error(getErrorCodeFrom(response.getStatus()));
            }
        });
    }


    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Log.info("Deleting image for user: " + userId);

        return repeated(() -> {
            WebTarget imageTarget = target.path(userId).path(imageId).queryParam("password", password);
            Builder request = imageTarget.request();
            Response response = target.request().delete();
            if (request == null) {
                Result.error(ErrorCode.INTERNAL_ERROR);
            }
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return Result.ok();
            } else {
                return Result.error(getErrorCodeFrom(response.getStatus()));
            }
        });
    }



    private <T> Result<T> repeated(Supplier<Result<T>> f) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return f.get();
            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);

    }




    public static ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> ErrorCode.OK;
            case 409 -> ErrorCode.CONFLICT;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            case 501 -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

}
