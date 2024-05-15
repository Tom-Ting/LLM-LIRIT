package cn.iselab.mooctest.device.model;

import cn.iselab.mooctest.device.common.enums.Schema;

public class Command {

    private Schema schema;

    private String content;

    public Command(String command) {
        int splitIndex = command.indexOf("://");
        if (splitIndex > -1) {
            String schemaStr = command.substring(0, splitIndex);
            switch (schemaStr) {
                case "keyevent":
                    schema = Schema.KEYEVENT;
                    break;
                case "minitouch":
                    schema = Schema.MINITOUCH;
                    break;
                default:
                    schema = Schema.UNDEFINED;
                    break;
            }
            content = command.substring(splitIndex + 3);
        }
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
