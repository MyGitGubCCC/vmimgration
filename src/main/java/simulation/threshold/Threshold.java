package simulation.threshold;

import simulation.utils.ToArrayByFileReader;

/**
 * @program: vmimgration
 * @description: 求ed的阈值
 * @author: 杨翎
 * @createDate: 2020-04-05 15:46
 */
public class Threshold {
    public static void main(String[] args) {
        double ed[] = ToArrayByFileReader.test(
                "D:\\ideaProject\\vmimgration\\src\\file\\DATA\\TEST\\ed",
                0);
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for(int i=0;i<ed.length;i++){
            if(ed[i] > max){
                max = ed[i];
            }else if (ed[i] < min){
                min = ed[i];
            }
        }
        System.out.println("ed的最大值为：" + (int)max);
        System.out.println("ed的最小值为：" + (int)min);
        int a = (int) (max-min)/3;
        int up = (int)max - a;
        int down = (int)min + a;
        System.out.println("ed上阈值为：" + up);
        System.out.println("ed下阈值为：" + down);
    }

}
