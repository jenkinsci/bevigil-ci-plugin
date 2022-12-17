package io.jenkins.plugins.bevigilciplugin;

import java.io.File;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class GetPresignedUrlResponse {
    @JsonProperty("url")
    public String url;

    @JsonProperty("job_id")
    public String jobId;
}

class SubmitResponse {
    @JsonProperty("msg")
    public String msg;
}

class SubmitRequest {

    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    @JsonProperty("upload_url")
    public String uploadUrl;

    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    @JsonProperty("app_type")
    public String appType;
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    @JsonProperty("package_name")
    public String packageName;
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    @JsonProperty("source_ci")
    public String sourceCI;
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    @JsonProperty("job_id")
    public String jobID;
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    @JsonProperty("scan_timeout")
    public Integer scanTimeout;
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    @JsonProperty("severity_threshold")
    public String severityThreshold;
}

class StatusResponse {
    @JsonProperty("status")
    public String status;
}

class OutputResponse {
    @JsonProperty("output")
    public String output;
}

public class BeVigilCIClient {
    private String baseUrl;

    private String apiKey;
    private HttpClient client;
    BeVigilCIClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.client = HttpClient.newHttpClient();
    }

    private HttpRequest.Builder createBuilder() {
        return HttpRequest.newBuilder().setHeader("Authorization", apiKey);
    }
    
    public GetPresignedUrlResponse getPresignedUrl(String appType) throws Exception {
        URL uploadPath = new URL(new URL(baseUrl), "/upload/" + appType);
        HttpRequest req = createBuilder().uri(uploadPath.toURI()).GET().build();
        HttpResponse<String> res =  this.client.send(req, BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new Exception("getPresignedUrl request failed with status code " + res.statusCode() + " and body: " + res.body());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(res.body(), GetPresignedUrlResponse.class);
    }

    public void uploadToPresignedUrl(File buildFile, String presignedURL) throws Exception {
        URL uploadEndpoint = new URL(presignedURL);
        HttpRequest req = HttpRequest.newBuilder().uri(uploadEndpoint.toURI())
                .PUT(HttpRequest.BodyPublishers.ofFile(buildFile.toPath()))
//                .setHeader("Content-Type", "application/vnd.android.package-archive")
                .build();
        HttpResponse<String> res = this.client.send(req, BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new Exception("Could not upload to presigned URL" + res.statusCode() + " and body: " + res.body());
        }
    }

    public SubmitResponse submit(SubmitRequest request) throws Exception{
        URL uploadPath = new URL(new URL(baseUrl), "/submit");
        ObjectMapper objectMapper = new ObjectMapper();

        HttpRequest req = createBuilder().uri(uploadPath.toURI())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .setHeader("Content-Type", "application/json")
                .build();
        HttpResponse<String> res =  this.client.send(req, BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new Exception("Could not submit the app for scan" + res.statusCode() + " and body: " + res.body());
        }

        return objectMapper.readValue(res.body(), SubmitResponse.class);
    }

    public StatusResponse status(String jobID) throws Exception{
        URL uploadPath = new URL(new URL(baseUrl), "/status?job_id=" + jobID);
        HttpRequest req = createBuilder().uri(uploadPath.toURI()).GET().build();
        HttpResponse<String> res =  this.client.send(req, BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new Exception("Error occurred in status request " + res.statusCode() + " and body: " + res.body());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(res.body(), StatusResponse.class);
    }

    public OutputResponse output(String jobID) throws Exception{
        URL uploadPath = new URL(new URL(baseUrl), "/output?job_id=" + jobID);
        HttpRequest req = createBuilder().uri(uploadPath.toURI()).GET().build();
        HttpResponse<String> res =  this.client.send(req, BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new Exception("Error occurred in output request " + res.statusCode() + " and body: " + res.body());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(res.body(), OutputResponse.class);
    }
}


