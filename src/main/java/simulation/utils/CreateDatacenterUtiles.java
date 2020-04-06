package simulation.utils;

import simulation.core.Cloudlet;
import simulation.core.Host;
import simulation.core.Vm;
import simulation.powerModel.PowerModel;
import java.util.*;

/**
 * @program: migration
 * @description: 创建数据中心工具类
 * @author: 杨翎
 * @createDate: 2020-01-28 13:18
 */
public class CreateDatacenterUtiles {
    /**
     * 创建任务
     */
    public static void createCloudletList(List<Cloudlet> cloudletList) {
        int index = 0;
        for(int i = 0; i < ExampleConstant.CLOUDLET_NUMBER; i++){
            int id = i;  //任务ID
            int length = ExampleConstant.CLOUDLET_LENGTH[index % ExampleConstant.CLOUDLET_LENGTH.length]; //任务长度
            int fileSize = ExampleConstant.CLOUDLET_FILESIZE[index % ExampleConstant.CLOUDLET_FILESIZE.length]; //文件大小，影响传输的带宽花销
            Cloudlet cloudlet = new Cloudlet(id, length, fileSize);
            cloudletList.add(cloudlet);
            index ++;
        }
    }

    /**
     * 创建虚拟机
     */
    public static void createVmList(List<Vm> vmList) {
        int vmNumber = ExampleConstant.VMS_NUMBER;
        for (int i = 0; i < vmNumber; i++) {
            int type = i % ExampleConstant.VM_TYPES;
            int vmId = i;
            double mips = ExampleConstant.VM_MIPS[type];
            double ram = ExampleConstant.VM_RAM[type];
            double bw = ExampleConstant.VM_BW[type];
            vmList.add(new Vm(vmId,mips,ram,bw));
        }
    }

    /**
     * 创建主机
     */
    public static void createHostList(List<Host> hostList) {
        int hostsNumber = ExampleConstant.HOSTS_NUMBER;
        for (int i = 0; i < hostsNumber; i++) {
            int hostId = i;
            int type = i % ExampleConstant.HOST_TYPES;
            double mips = ExampleConstant.HOST_MIPS[type];
            double ram = ExampleConstant.HOST_RAM[type];
            double bw = ExampleConstant.HOST_BW[type];
            PowerModel powerModel = ExampleConstant.HOST_POWER[0];
            hostList.add(new Host(hostId,mips,ram,bw,powerModel));
        }
    }

    /**
     * 创建任务之间的相关性
     * @param cloudletList
     */
    public static void createClouletRelated(List<Cloudlet> cloudletList) {
        for(int i = 0; i<cloudletList.size(); i++) {
            int relatedNumber = ExampleConstant.NETWORK_NUMBER[i%ExampleConstant.NETWORK_MAXNUMBER];
            if(relatedNumber==0)continue;
            int relatedMaxDistance = ExampleConstant.NETWORK_DISTANCE[i%ExampleConstant.NETWORK_MAXDISTANCENUMBER];
            for(int j = 0; j < relatedNumber; j++) {
                List<Cloudlet> related = new ArrayList<Cloudlet>();
                List<Cloudlet> relatedopposite = new ArrayList<Cloudlet>();
                related.add(cloudletList.get(i));
                if((i + relatedMaxDistance) >= cloudletList.size()) continue;
                related.add(cloudletList.get(i + relatedMaxDistance));
                Map<List<Cloudlet>, Integer> relatedCloulet = new HashMap<List<Cloudlet>, Integer>();
                Map<List<Cloudlet>, Integer> relatedClouletopposite = new HashMap<List<Cloudlet>, Integer>();
                relatedCloulet.put(related,ExampleConstant.CLOUDLET_FILESIZE[relatedMaxDistance]);
                relatedopposite.add(cloudletList.get(i));
                relatedClouletopposite.put(relatedopposite,ExampleConstant.CLOUDLET_FILESIZE[relatedMaxDistance]);
                cloudletList.get(i).setRelatedCloulet(relatedCloulet);
                cloudletList.get((i + relatedMaxDistance)).setRelatedCloulet(relatedCloulet);
            }
        }
    }

    /**
     * 创建虚拟机之间的相关性
     * @param vmList
     */
    public static void createVmRelated(List<Vm> vmList) {
        for (Vm vm : vmList) {
            for(Cloudlet cloudlet : vm.getCloudletList()) {
                if(cloudlet.getRelatedCloulet() != null) {
                    Set<List<Cloudlet>> set = cloudlet.getRelatedCloulet().keySet();
                    Iterator<List<Cloudlet>> it = set.iterator();
                    List<Vm> relatedVm = new ArrayList<Vm>();
                    while (it.hasNext()) {
                        List<Cloudlet> cloudlets = it.next();
                        relatedVm.add(cloudlets.get(1).getVm());
                        vm.setNetRelatedVm(relatedVm);
                    }
                }
            }
        }
    }
}
