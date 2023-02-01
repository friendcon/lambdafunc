import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.apache.commons.fileupload.MultipartStream;
import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

// RequestHandler<Input type, Output type)
public class UploadFunctionHandler implements RequestHandler<Map<String, Object>, ApiResponse> {

    // lambda 함수의 요청을 다루는 메서드
    @Override
    public ApiResponse handleRequest(Map<String, Object> input, Context context) {

        Regions region = Regions.AP_NORTHEAST_2;
        String bucketName = "myshop-img-upload";

        String fileObKeyName = "";
        Map<String, String> response = new HashMap<>();
        ApiResponse apiResponse = null;
        String contentType = "";

        try {
            byte[] bI = Base64.getDecoder().decode(input.get("body").toString().getBytes());
            Map<String, String> requestHeader = (Map<String, String>) input.get("headers");
            if(requestHeader != null) {
                contentType = requestHeader.get("Content-Type");
            }
            String[] boundaryArray = contentType.split("=");
            byte[] boundary = boundaryArray[1].getBytes();
            ByteArrayInputStream content = new ByteArrayInputStream(bI);
            MultipartStream multipartStream = new MultipartStream(content, boundary, bI.length);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            boolean nextPart = multipartStream.skipPreamble();
            while (nextPart) {
                String header = multipartStream.readHeaders();

                fileObKeyName = getFileName(header, "filename");
                multipartStream.readBodyData(outputStream);
                nextPart = multipartStream.readBoundary();
            }
            InputStream fileInputStream = new ByteArrayInputStream(outputStream.toByteArray());

            AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(outputStream.toByteArray().length);

            PutObjectResult result = s3.putObject(bucketName, fileObKeyName, fileInputStream, metadata);
            response.put("Status", "File stored in s3");
            String resBodyString = new JSONObject(response).toJSONString();
            String url = s3.getUrl(bucketName, fileObKeyName).toString();
            apiResponse = new ApiResponse(url,resBodyString, 200);

        } catch (Exception e) {
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", e.getMessage());
            apiResponse = new ApiResponse("error", errorObj.toString(), 400);
        }

        return apiResponse;
    }

    private String getFileName(String str, String field) {
        String result = null;
        int index = str.indexOf(field);
        if(index >= 0) {
            int first = str.indexOf("\"", index);
            int second = str.indexOf("\"", first+1);
            result = str.substring(first+1, second);
        }
        return result;
    }
}
