package simulation.vmAllocationPolicy;

import simulation.core.Host;
import simulation.core.Vm;
import simulation.utils.ExampleUtils;
import java.util.List;

/**
 * @program: migration
 * @description: 虚拟机放置策略最简单实现Simple，采用fistfit
 * @author: 杨翎
 * @createDate: 2020-01-28 13:09
 */
public class VmAllocationPolicySimple extends VmAllocationPolicy {
    /**
     * 第一个满足虚拟机的主机进行放置
     */
    @Override
    public Host getHost(List<Host> hostList, Vm vm) {
        Host selectHost = null;
        for(Host host : hostList){
            if(ExampleUtils.vmIsAvailableHost(vm, host)){
                selectHost = host;
                break;
            }
            else continue;
        }
        return selectHost;
    }
}
