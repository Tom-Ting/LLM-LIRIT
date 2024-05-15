package cn.iselab.mooctest.lit.vo;

import java.util.List;

public class QueryData {
    public QueryData(){

    }
    public QueryData(List list){
        this.list=list;
        this.pageTotal=list.size();
    }

    public List list;
    public int pageTotal;

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public int getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(int pageTotal) {
        this.pageTotal = pageTotal;
    }
}
