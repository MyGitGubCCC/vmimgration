package simulation.core;

import java.util.List;
import java.util.Map;

public class Cloudlet {
    //任务id
    private int id;
    //任务长度
    private int length;
    //任务消耗内存
    private int ram;
    //任务所在虚拟机
    private Vm vm;
    //任务开始时间
    private int execStartTime;
    //预计完成任务的时间
    private int expectFinishTime;
    //有关联任务的键值对
    private Map<List<Cloudlet>, Integer> relatedCloulet;

    /**
     * 任务构造器
     */
    public Cloudlet(int id, int length, int ram) {
        this.id = id;
        this.length = length;
        this.ram = ram;
        Map<List<Cloudlet>, Integer> relatedCloulet;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Vm getVm() {
        return vm;
    }

    public void setVm(Vm vm) {
        this.vm = vm;
    }

    public int getExecStartTime() {
        return execStartTime;
    }

    public void setExecStartTime(int execStartTime) {
        this.execStartTime = execStartTime;
    }

    public int getExpectFinishTime() {
        return expectFinishTime;
    }

    public void setExpectFinishTime(int expectFinishTime) {
        this.expectFinishTime = expectFinishTime;
    }

    public Map<List<Cloudlet>, Integer> getRelatedCloulet() {
        return relatedCloulet;
    }

    public void setRelatedCloulet(Map<List<Cloudlet>, Integer> relatedCloulet) {
        this.relatedCloulet = relatedCloulet;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("任务Id:" + getId());
        return stringBuilder.toString();
    }


}
