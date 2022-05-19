package com.just.print.sys.model;

abstract
public class AbsResult extends AbsModel {
    {
        setType(2);
    }

    @Override
    final public String getService() {
        return null;
    }

    @Override
    final public int getInnerType() {
        return 0;
    }
}
