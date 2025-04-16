package fctreddit.impl.grpc;

import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.impl.grpc.util.DataModelAdaptor;
import fctreddit.impl.grpc.generated_java.ImageGrpc;
import fctreddit.impl.grpc.generated_java.ImageProtoBuf;
import fctreddit.impl.java.JavaImages;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class GrpcImageServerStub implements ImageGrpc.AsyncService, BindableService {

   UriInfo u;
    String URI = u.getAbsolutePath().toString()+"image/";
    Image impl = new JavaImages(URI);

    @Override
    public ServerServiceDefinition bindService() {
        return ImageGrpc.bindService(this);
    }
    //Isto está mal para já
    /**
    @Override
    public void createImage(ImageProtoBuf.CreateImageArgs request, StreamObserver<ImageProtoBuf.CreateImageResult> responseObserver) {
        Result<String> res = impl.createImage( DataModelAdaptor.GrpcUser_to_User(request.getUser()));
        if( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else {
            responseObserver.onNext( ImageProtoBuf.CreateImageResult.newBuilder().setUserId( res.value() ).build());
        }
    }
     **/

    @Override
    public void getImage(ImageProtoBuf.GetImageArgs request, StreamObserver<ImageProtoBuf.GetImageResult> responseObserver) {
        ImageGrpc.AsyncService.super.getImage(request, responseObserver);
    }

    @Override
    public void deleteImage(ImageProtoBuf.DeleteImageArgs request, StreamObserver<ImageProtoBuf.DeleteImageResult> responseObserver) {
        ImageGrpc.AsyncService.super.deleteImage(request, responseObserver);
    }

    protected static Throwable errorCodeToStatus( Result.ErrorCode error ) {
        var status =  switch( error) {
            case NOT_FOUND -> io.grpc.Status.NOT_FOUND;
            case CONFLICT -> io.grpc.Status.ALREADY_EXISTS;
            case FORBIDDEN -> io.grpc.Status.PERMISSION_DENIED;
            case NOT_IMPLEMENTED -> io.grpc.Status.UNIMPLEMENTED;
            case BAD_REQUEST -> io.grpc.Status.INVALID_ARGUMENT;
            default -> io.grpc.Status.INTERNAL;
        };

        return status.asException();
    }

}
