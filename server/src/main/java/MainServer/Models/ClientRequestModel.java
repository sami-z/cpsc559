package MainServer.Models;

import java.util.ArrayList;

/**

 This class represents a model for a client request.
 It contains fields that can be used to build and send requests to a server.
 The requestType field indicates the type of request that the client is making.
 The currentUser field represents the current user making the request.
 The userName field represents the name of the user whose file is being requested.
 The fileName field represents the name of the file being requested.
 The fileType field represents the type of file being requested.
 The bytes field represents the file content in bytes.
 The shared field is an ArrayList of usernames that the file will be shared with.
 The password field represents the password of the file, for authentication purposes.
 */
public class ClientRequestModel {
    public String requestType;
    public String currentUser;
    public String userName;
    public String fileName;
    public String fileType;
    public String bytes;
    public ArrayList<String> shared;

    public String password;
}
