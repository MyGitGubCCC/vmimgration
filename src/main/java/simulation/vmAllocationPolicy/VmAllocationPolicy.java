package simulation.vmAllocationPolicy;

import simulation.core.Host;
import simulation.core.Vm;
import java.util.List;

/**
 * @program: migration
 * @description: 虚拟机放置策略抽象类
 * @author: 杨翎
 * @createDate: 2020-01-28 13:09
 */
public abstract class VmAllocationPolicy {
    //获取要放置的主机
    public abstract Host getHost(List<Host> hostList, Vm vm);
}
