import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @Description:
 * @Author: maoqitian
 * @CreateDate: 2020/8/30 10:42
 */
public class Test {

     public class TreeNode {
          int val;
         TreeNode left;
          TreeNode right;
          TreeNode(int x) { val = x; }
      }


    List<List<Integer>> res = new ArrayList<>();
    List<Integer> path = new ArrayList<>();

    public List<List<Integer>> pathSum(TreeNode root, int sum) {

        //方法一  dfs 时间复杂度 O(n) 前序遍历

        // dfs(root, sum);
        // return res;

        //方法二 BFS 队列先进先出 时间复杂度 O(n)

        if(root == null) return res;

        //两个队列 一个队列存储遍历节点 一个存储从根结点到当前节点的路径
        LinkedList<TreeNode> queue = new LinkedList<>();
        LinkedList<List<Integer>> queueList = new LinkedList<>();


        queue.addFirst(root);

        //路径集合入队
        path.add(root.val);
        queueList.addFirst(path);

        while(!queue.isEmpty()){
            //当前节点出队
            TreeNode node = queue.removeLast();
            //当前节点的路径出队
            List<Integer> temp = queueList.removeLast();
            //如果满足条件 存储当前路径
            if(node.val == sum && node.left == null && node.right == null){
                res.add(temp);
            }
            //左子树不为空
            if(node.left != null){
                //保存当前路径值
                temp.add(node.left.val);
                queueList.addFirst(new ArrayList<>(temp));
                //保存左子树节点
                node.left.val += node.val;
                queue.addFirst(node.left);
                //去处 temp 保存值
                temp.remove(temp.size()-1);
            }
            if(root.left != null){
                //保存当前路径值
                temp.add(node.right.val);
                queueList.addFirst(new ArrayList<>(temp));
                //保存右子树节点
                node.right.val += node.val;
                queue.addFirst(node.right);
            }

        }
        return res;
    }

    public static void main(String[] args) {

        String s = "Let's take LeetCode contest";

        List<String> strList = new ArrayList<>(Arrays.asList(s.split(" ")));

        StringBuilder stringBuilder = new StringBuilder();

        for(String str : strList){

            stringBuilder.append(new StringBuffer(str).reverse());
            stringBuilder.append(" ");
        }

        System.out.println(stringBuilder.toString().trim());

        
    }
}
