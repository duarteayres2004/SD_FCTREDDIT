package fctreddit.impl.server.grpc;

import fctreddit.impl.server.grpc.generated_java.ImageGrpc;
import fctreddit.impl.server.grpc.generated_java.ImageProtoBuf;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;

public class GrpcImageServerStub implements ImageGrpc.AsyncService, BindableService {

    @Override
    public void deleteImage(ImageProtoBuf.DeleteImageArgs request, StreamObserver<ImageProtoBuf.DeleteImageResult> responseObserver) {
        ImageGrpc.AsyncService.super.deleteImage(request, responseObserver);
    }

    @Override
    public ServerServiceDefinition bindService() {
        return null;
    }
}
