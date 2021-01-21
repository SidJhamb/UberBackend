package com.uber.uberapi.designpatterns;

class S3Connection{};
class FileDescriptor{};

public class S3Manager {
    S3Connection s3Connection;
    FileDescriptor fileDescriptor;

    static private S3Manager instance;

    private S3Manager() {
        s3Connection = new S3Connection();
        fileDescriptor = new FileDescriptor();
    }
    private synchronized static void createInstance(){
        if(instance == null){
            instance = getInstance();
        }
    }
    public static S3Manager getInstance(){
        if(instance == null){
            createInstance();
        }
        return instance;
    }

    public String fetch(String uri){
        return null;

    }

    public void upload(String uri){

    }
}
