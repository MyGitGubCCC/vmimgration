package simulation.cloudletSchedulPolicy;

import simulation.cloudletPlacement.CloudletPlacement;
import simulation.core.Cloudlet;
import simulation.core.Vm;
import simulation.utils.ExampleUtils;
import java.util.List;

/**
 * @program: migration
 * @description: FCFS任务调度算法，即先来先服务
 * @author: 杨翎
 * @createDate: 2020-01-28 12:58
 */
public class CloudletSchedulPolicyFCFS extends CloudletSchedulPolicy {
    /**
     *
     * @param cloudletPlacement 容器放置策略
     * @param vmList 虚拟机列表
     * @param cloudletListQueue 任务队列
     * @param cloudletList 任务列表
     * @param starSimTime 目前的模拟时间
     * @param removeCloudletList 放置完成从队列中要删除的任务列表
     */
    @Override
    public void cloudletPutInVm(CloudletPlacement cloudletPlacement, List<Vm> vmList, List<Cloudlet> cloudletListQueue,
                                List<Cloudlet> cloudletList, int starSimTime, List<Cloudlet> removeCloudletList) {
        //循环进行任务放置
        for (Cloudlet cloudlet : cloudletListQueue) {
            Vm vm = cloudletPlacement.getVm(vmList, cloudlet);
            if(vm != null){
                vm.setStartTime(starSimTime);
                vm.setFinishTime((int)(vm.getFinishTime() + cloudlet.getLength()/vm.getMips()));
                boolean vmNotNull = ExampleUtils.updateAfterCloudletInVm(vm, cloudlet, cloudletList, starSimTime, removeCloudletList);
                if (vmNotNull) continue;
                else break;
            }else continue;
        }
    }
}
