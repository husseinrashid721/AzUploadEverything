package UploadFile.FunctionApp;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;


/**
 * Azure Functions with HTTP Trigger.
 */
public class UploadFile {
    /**
     * This function listens at endpoint "/api/UploadFile". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/UploadFile 2. curl {your host}/api/UploadFile?name=HTTP%20Query
     * 
     * @throws Exception
     */
    @FunctionName("UploadFile")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws Exception {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);

        String destinationStorage = System.getenv("destinationStorage");
        String destinationContainer = System.getenv("destinationContainer");

        String StorageConnectionString = destinationStorage;//System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        context.getLogger().info("connection string:" + StorageConnectionString);

        // Create a local file in the ./data/ directory for uploading and downloading
       
        String fileName = "quickstart" + java.util.UUID.randomUUID() + ".txt";

        byte[] b = name.getBytes();

      
        // Create a BlobServiceClient object which will be used to create a container client
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(StorageConnectionString).buildClient();

       // destinationContainer
        String containerName = destinationContainer;//"triggerfilecont";

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
      
        // Get a reference to a blob
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        System.out.println("\nUploading to Blob storage as blob:\n\t" + blobClient.getBlobUrl());

        // Upload the blob
        //blobClient.uploadFromFile(localPath + fileName);

        CloudStorageAccount storageAccount;
         try { 
             storageAccount = CloudStorageAccount.parse(StorageConnectionString); 
         } 
        catch (IllegalArgumentException|URISyntaxException e) { 
             System.out.println("\nConnection string specifies an invalid URI."); 
             System.out.println("Please confirm the connection string is in the Azure connection string format."); 
             throw e; 
         } 
         catch (InvalidKeyException e) { 
             System.out.println("\nConnection string specifies an invalid key."); 
             System.out.println("Please confirm the AccountName and AccountKey in the connection string are valid."); 
             throw e; 
         } 
 
 
          CloudBlobClient client = storageAccount.createCloudBlobClient(); 
      
          CloudBlobContainer container = client.getContainerReference(containerName);
         
         // BlobContainerPermissions containerPermissions = new BlobContainerPermissions() {     PublicAccess = BlobContainerPublicAccessType.Blob };
        //  container.SetPermissions(containerPermissions);
          CloudBlockBlob photo = container.getBlockBlobReference(fileName);
          photo.uploadFromByteArray(b, 0, b.length);
          

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }
}
