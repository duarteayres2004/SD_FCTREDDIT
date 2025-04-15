package fctreddit.impl.grpc;

import fctreddit.impl.grpc.generated_java.ContentGrpc;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;

public class GrpcContentServerStub implements ContentGrpc.AsyncService, BindableService {


    @Override
    public ServerServiceDefinition bindService() {
        return null;
    }

    @Override
    public void createPost(ContentProtoBuf.CreatePostArgs request, StreamObserver<ContentProtoBuf.CreatePostResult> responseObserver) {
        ContentGrpc.AsyncService.super.createPost(request, responseObserver);
    }

    @Override
    public void getPosts(ContentProtoBuf.GetPostsArgs request, StreamObserver<ContentProtoBuf.GetPostsResult> responseObserver) {
        ContentGrpc.AsyncService.super.getPosts(request, responseObserver);
    }

    @Override
    public void getPost(ContentProtoBuf.GetPostArgs request, StreamObserver<ContentProtoBuf.GrpcPost> responseObserver) {
        ContentGrpc.AsyncService.super.getPost(request, responseObserver);
    }

    @Override
    public void getPostAnswers(ContentProtoBuf.GetPostAnswersArgs request, StreamObserver<ContentProtoBuf.GetPostsResult> responseObserver) {
        ContentGrpc.AsyncService.super.getPostAnswers(request, responseObserver);
    }

    @Override
    public void updatePost(ContentProtoBuf.UpdatePostArgs request, StreamObserver<ContentProtoBuf.GrpcPost> responseObserver) {
        ContentGrpc.AsyncService.super.updatePost(request, responseObserver);
    }

    @Override
    public void deletePost(ContentProtoBuf.DeletePostArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        ContentGrpc.AsyncService.super.deletePost(request, responseObserver);
    }

    @Override
    public void upVotePost(ContentProtoBuf.ChangeVoteArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        ContentGrpc.AsyncService.super.upVotePost(request, responseObserver);
    }

    @Override
    public void removeUpVotePost(ContentProtoBuf.ChangeVoteArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        ContentGrpc.AsyncService.super.removeUpVotePost(request, responseObserver);
    }

    @Override
    public void downVotePost(ContentProtoBuf.ChangeVoteArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        ContentGrpc.AsyncService.super.downVotePost(request, responseObserver);
    }

    @Override
    public void removeDownVotePost(ContentProtoBuf.ChangeVoteArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        ContentGrpc.AsyncService.super.removeDownVotePost(request, responseObserver);
    }

    @Override
    public void getUpVotes(ContentProtoBuf.GetPostArgs request, StreamObserver<ContentProtoBuf.VoteCountResult> responseObserver) {
        ContentGrpc.AsyncService.super.getUpVotes(request, responseObserver);
    }

    @Override
    public void getDownVotes(ContentProtoBuf.GetPostArgs request, StreamObserver<ContentProtoBuf.VoteCountResult> responseObserver) {
        ContentGrpc.AsyncService.super.getDownVotes(request, responseObserver);
    }
}
