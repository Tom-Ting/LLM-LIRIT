package cn.iselab.mooctest.device.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseWrapper<VO, DATA> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public abstract VO wrap(DATA data);

    public abstract DATA unwrap(VO data);

    public List<VO> wrap(List<DATA> data) {
        return data.stream().map(this::wrap).collect(Collectors.toList());
    }

    public List<DATA> unwrap(List<VO> vos) {
        return vos.stream().map(this::unwrap).collect(Collectors.toList());
    }

}
