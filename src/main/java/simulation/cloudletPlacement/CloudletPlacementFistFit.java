package simulation.cloudletPlacement;

import simulation.core.Cloudlet;
import simulation.core.Vm;
import simulation.utils.ExampleUtils;
import java.util.List;

public class CloudletPlacementFistFit extends CloudletPlacement{
    @Override
    public Vm getVm(List<Vm> vmList, Cloudlet cloudlet) {
        Vm selectVm = null;
        for(Vm vm : vmList){
            if (!ExampleUtils.cloudletIsAvailableVm(cloudlet, vm)){
                continue;
            }
            selectVm = vm;
            break;
        }
        return selectVm;
    }
}
