package simulation.threshold;

import simulation.utils.ToArrayByFileReader;

import java.util.ArrayList;

public class KmeansTest {

    public static void main(String[] args) {

        //初始化一个Kmean对象，将k置为2
        Kmeans k=new Kmeans(3);
        ArrayList<double[]> dataSet = new ArrayList<double[]>();
        /*double load2[] = ToArrayByFileReader.historyUtilzationToDouble(
                "D:\\ideaProject\\vmimgration\\src\\file\\DATA\\TEST\\load2",
                0);

        double net[] = ToArrayByFileReader.historyUtilzationToDouble(
                "D:\\ideaProject\\vmimgration\\src\\file\\DATA\\TEST\\netEnger",
                1);*/

        double ed[] = ToArrayByFileReader.historyUtilzationToDouble(
                "D:\\ideaProject\\vmimgration\\src\\file\\DATA\\TEST\\ed");

        /*for(int i=0;i<load2.length;i++){
            for(int j=i;j<net.length;j++){
                for(int z=i;z<ed.length;z++){
                    dataSet.add(new double[]{load2[i],net[j],ed[z]});
                    break;
                }
                break;
            }
        }*/
        for(int i=0;i<ed.length;i++)dataSet.add(new double[]{ed[i]});
        //设置原始数据集
        k.setDataSet(dataSet);

        //执行算法
        k.execute();

        //得到聚类结果
        ArrayList<ArrayList<double[]>> cluster=k.getCluster();

        //查看结果
        for(int i=0;i<cluster.size();i++)

        {
            k.printDataArray(cluster.get(i), "cluster["+i+"]");
        }

    }

}

