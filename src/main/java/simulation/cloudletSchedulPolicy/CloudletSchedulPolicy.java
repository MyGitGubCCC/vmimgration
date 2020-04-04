package simulation.cloudletSchedulPolicy;

import simulation.cloudletPlacement.CloudletPlacement;
import simulation.core.Cloudlet;
import simulation.core.Vm;

import java.util.List;

/**
 * @program: migration
 * @description: 任务调度的抽象类，任务调度=任务执行顺序+放置算法
 * @author: 杨翎
 * @createDate: 2020-01-28 12:57
 */
public abstract class CloudletSchedulPolicy {
    /**
     * 放置容器到虚拟机中的算法
     * @param cloudletPlacement 容器放置策略
     * @param vmList 虚拟机列表
     * @param cloudletListQueue 任务队列
     * @param cloudletList 任务列表
     * @param starSimTime 目前的模拟时间
     * @param removeCloudletList 放置完成从队列中要删除的任务列表
     */
    public abstract void cloudletPutInVm(CloudletPlacement cloudletPlacement, List<Vm> vmList, List<Cloudlet> cloudletListQueue,
                                         List<Cloudlet> cloudletList, int starSimTime, List<Cloudlet> removeCloudletList);

}
