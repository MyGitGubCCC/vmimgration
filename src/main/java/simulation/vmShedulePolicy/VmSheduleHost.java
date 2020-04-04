package simulation.vmShedulePolicy;

import simulation.core.Host;
import simulation.core.Vm;

import java.io.FileNotFoundException;
import java.util.List;


/**
 * @program: VmSheduleHost
 * @description: 虚拟机调度抽象类
 */
public abstract class VmSheduleHost {
    //虚拟机调度抽象方法
    public abstract double[] getVmMigrationHost(
            List<Host> hostList, int current,
            double migEnergy, int migNumber,
            Double mipsRequest, Double mipsAllcation,List<Vm> vmList) throws FileNotFoundException;
}
