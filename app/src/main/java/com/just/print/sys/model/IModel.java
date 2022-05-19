package com.just.print.sys.model;

import com.alibaba.fastjson.annotation.JSONField;

public interface IModel {
    @JSONField(serialize = false)
    String getService();

    @JSONField(serialize = false)
    int getInnerType();

}
