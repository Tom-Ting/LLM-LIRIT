package cn.iselab.mooctest.lit.vo;


import java.util.Objects;

public class ResponseVO {
    /**
     * 调用是否成功
     */
    private Boolean success;
    /**
     * 返回的提示信息
     */
    private String message;
    /**
     * 内容
     */
    private Object content;

    public static ResponseVO buildSuccess() {
        ResponseVO response = new ResponseVO();
        response.setSuccess(true);
        return response;
    }

    public static ResponseVO buildSuccess(Object content) {
        ResponseVO response = new ResponseVO();
        response.setContent(content);
        response.setSuccess(true);
        return response;
    }

    public static ResponseVO buildFailure(String message) {
        ResponseVO response = new ResponseVO();
        response.setSuccess(false);
        response.setMessage(message);
        System.out.println(message);
        return response;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseVO that = (ResponseVO) o;
        return Objects.equals(getSuccess(), that.getSuccess()) &&
                Objects.equals(getMessage(), that.getMessage()) &&
                Objects.equals(getContent(), that.getContent());
    }

}
