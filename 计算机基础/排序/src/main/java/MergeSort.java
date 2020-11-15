import java.util.Arrays;

/**
 * @Description: 归并排序 二分左右寻找最小值 时间复杂度 O(nlogn)
 * @Author: maoqitian
 * @CreateDate: 2020/11/9 22:49
 */
public class MergeSort {

    public static void main(String[] args) {

        //测试数组
        int a[] = { 51, 46, 20, 18, 65, 97, 82, 30, 77, 50 };
        mergeSort(a,0,a.length-1);

        System.out.println("排序结果：" + Arrays.toString(a));

    }


    public static void mergeSort(int [] a,int low,int high){
        //获取中位数 index
        int mid = low + (high-low)/2;
        //如果 地位index 小于高位 index
        if(low < high) {
            //mind左边部分
            mergeSort(a,low,mid);
            //mid右边部分
            mergeSort(a,mid+1,high);

            //左右归并
            merge(a,low,mid,high);
            System.out.println("一次排序结果 ："+Arrays.toString(a));
        }
    }

    public static void merge(int[] a, int low, int mid, int high) {
        //存储中间结果数组
        int [] temp = new int[high - low +1];
        //左右指针
        int left = low,right = mid+1;
        //中间结果数组index
        int index = 0;

        //把较小的数移动到新数组
        while(left <= mid && right<=high){
          if(a[left] < a[right]){
              //记录左边小数值
              temp[index++] = a[left++];
          }else{

              temp[index++] = a[right++];
          }
        }

        //把左边剩余的数组放入temp数组
        while(left<=mid){
             temp[index++] = a[left++];
        }
        //右边剩余部分放入temp数组
        while(right<=high){
            temp[index++] = a[right++];
        }
        //新数组覆盖老数组
        for (int i = 0; i < temp.length; i++) {
            a[i+low] = temp[i];
        }
    }


}
