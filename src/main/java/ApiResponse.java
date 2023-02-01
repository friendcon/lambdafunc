public class ApiResponse {
    String imgUrl;
    String body;
    int statusCode;

    public ApiResponse(String imgUrl, String body, int statusCode) {
        this.imgUrl = imgUrl;
        this.body = body;
        this.statusCode = statusCode;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
