package simulation.cloudletPlacement;

import simulation.core.Cloudlet;
import simulation.core.Vm;
import java.util.List;

public abstract class CloudletPlacement {
    //获取要放置的虚拟机
    public abstract Vm getVm(List<Vm> vmList, Cloudlet cloudlet);
}
