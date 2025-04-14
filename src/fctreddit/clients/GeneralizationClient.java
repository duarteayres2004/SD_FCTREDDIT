package fctreddit.clients;

import fctreddit.clients.rest.RestUsersClient;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.ProcessingException;
import java.util.logging.Logger;

public class GeneralizationClient {
    public static Logger Log = Logger.getLogger(RestUsersClient.class.getName());
    //protected static final int READ_TIMEOUT = 5000;
    //protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 5;
    protected static final int RETRY_SLEEP = 5000;


    protected Response executeOperationPost(Builder req, Entity<?> e) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.post(e);
            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }

    protected Response executeOperationGet(Builder req) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.get();
            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }

    protected Response executeOperationDelete(Builder req) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.delete();
            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }
    protected Response executeOperationPut(Builder req,Entity<?> e){
        for(int i = 0; i < MAX_RETRIES; i++){
            try{
                return req.put(e);
            } catch (ProcessingException x) {
                Log.info(x.getMessage());


                try{
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {

                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
            return null;
    }
}