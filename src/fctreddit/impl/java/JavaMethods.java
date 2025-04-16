package fctreddit.impl.java;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.server.discovery.Discovery;

import java.net.URI;

/**
 * Classe para metermos todos os metodos que usamos em todas as classes
 */
public abstract class JavaMethods {


    protected URI tryDiscovery(String serviceName, Discovery discovery){
        URI[] uris = discovery.knownUrisOf(serviceName, 1);
        if (uris == null || uris.length == 0) {
            return null;
        }
        return uris[0];
    }


    protected Result<User> getUserError(String userId, String pwd,  Result<User> u) {

        if (u.error().equals(Result.ErrorCode.NOT_FOUND)) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        } else if (u.error().equals(Result.ErrorCode.FORBIDDEN)) {
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }
        return u;
    }


    protected boolean isValidField(String field) {
        return field != null && !field.isBlank();
    }

}
