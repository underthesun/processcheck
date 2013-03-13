/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sim;

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import main.NodePanel;
import processcheck.Node;
import processcheck.NodeProcess;
import util.DataPOJO;
import util.ItemPOJO;
import util.PackagePOJO;

/**
 *
 * @author shuai
 */
public class Check {

    private NodePanel nodePanel;
    private int packNum = 10;
    private int itemNum = 100;
    private ArrayList<ArrayList<ItemPOJO>> itemsList;
    private ArrayList<ArrayList<ItemPOJO>> itemsOrientedList;
    private ArrayList<ArrayList<PackagePOJO>> packagesList;
    private ArrayList<ArrayList<PackagePOJO>> packagesOrientedList;
    private ArrayList<NodeProcess> nodeProcess;
    private DataPOJO data;

    public Check(NodePanel nodePanel) {
        this.nodePanel = nodePanel;
        this.nodeProcess = nodePanel.getNodeProcess();
    }

    public void startCheck() {
        nodePanel.colorResest();
        boolean flag = true;
        if (!checkSize()) {
            showWarning("数据与结构图不匹配", "报警", JOptionPane.WARNING_MESSAGE);
            flag = false;
        } else {
            for (NodeProcess np : nodeProcess) {
                if (!checkAmountIntegrity(np)) {
                    np.setBackground(Color.red);
                    showWarning("过程 " + np.getSerial() + " 检测到数目错误", "报警", JOptionPane.WARNING_MESSAGE);
                    flag = false;
                }
                for (Node node : np.getNodes()) {
                    if (!checkContainmentIntegrity(np, node)) {
                        node.setBackground(Color.red);
                        showWarning("进程" + node.getId() + "检测到包含关系错误", "报警", JOptionPane.WARNING_MESSAGE);
                        flag = false;
                    }
                    if (!checkOrderIntegrity(np, node)) {
                        node.setBackground(Color.red);
                        showWarning("进程" + node.getId() + "检测到时间顺序错误", "报警", JOptionPane.WARNING_MESSAGE);
                        flag = false;
                    }
                }
            }
        }
        if (flag) {
            showWarning("检查通过", "提示", JOptionPane.PLAIN_MESSAGE);
        }
    }

    public boolean checkAmountIntegrity(NodeProcess np) {
        boolean flag = true;
        int id = np.getSerial();
        if (itemsList.get(id).size() != itemsOrientedList.get(id).size()) {
            flag = false;
        }
        if (packagesOrientedList.get(id).size() != packagesList.get(id).size()) {
            flag = false;
        }
        return flag;
    }

    public boolean checkContainmentIntegrity(NodeProcess np, Node node) {
        boolean flag = true;
        ArrayList<ItemPOJO> itemsRecv = getItemPOJO(np.getSerial(), node);
        ArrayList<PackagePOJO> packsRecv = getPackPOJO(np.getSerial(), node);
        ArrayList<ItemPOJO> itemsOriented = getItemOrientedPOJO(np.getSerial(), node);
        ArrayList<PackagePOJO> packsOriented = getPackOrientedPOJO(np.getSerial(), node);
        ArrayList<ItemPOJO> itemsContained = null;
//        for(int i=0;i<itemsRecv.size();i++){
//            System.out.println(itemsRecv.get(i).getId()+"|"+itemsOriented.get(i).getId());
//        }
        if (!itemsRecv.containsAll(itemsOriented)) {
            flag = false;
        }
        for (PackagePOJO pack : packsOriented) {
            if (!packsRecv.contains(pack)) {
                flag = false;
                break;
            } else {
                itemsContained = getContainedItemPOJO(np, pack.getId());
                if (!itemsRecv.containsAll(itemsContained)) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

    public boolean checkOrderIntegrity(NodeProcess np, Node node) {
        boolean flag = true;
        System.out.println("node " + node.getId() + ":" + getItemPOJO(np.getSerial(), node).size());
        for (ItemPOJO item : getItemPOJO(np.getSerial(), node)) {
            long time = getLastCheckTime(np, node, item);
            System.out.println(time + "|" + item.getLastTimeChecked());
            if (time > item.getLastTimeChecked()) {
                flag = false;
                break;
            }
        }
        for (PackagePOJO pack : getPackOrientedPOJO(np.getSerial(), node)) {
            long time = getLastCheckTime(np, node, pack);
            if (time > pack.getLastTimeChecked()) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public ArrayList<ItemPOJO> getItemPOJO(int npId, Node node) {
        ArrayList<ItemPOJO> pojo = new ArrayList<ItemPOJO>();
        for (ItemPOJO item : itemsList.get(npId)) {
            if (item.getDstNodeId() == node.getId()) {
                pojo.add(item);
            }
        }
        return pojo;
    }

    public ArrayList<PackagePOJO> getPackPOJO(int npId, Node node) {
        ArrayList<PackagePOJO> pojo = new ArrayList<PackagePOJO>();
        for (PackagePOJO pack : packagesList.get(npId)) {
            if (pack.getDstNodeId() == node.getId()) {
                pojo.add(pack);
            }
        }
        return pojo;
    }

    public ArrayList<ItemPOJO> getItemOrientedPOJO(int npId, Node node) {
        ArrayList<ItemPOJO> pojo = new ArrayList<ItemPOJO>();
        for (ItemPOJO item : itemsOrientedList.get(npId)) {
            if (item.getDstNodeId() == node.getId()) {
                pojo.add(item);
            }
        }
        return pojo;
    }

    public ArrayList<PackagePOJO> getPackOrientedPOJO(int npId, Node node) {
        ArrayList<PackagePOJO> pojo = new ArrayList<PackagePOJO>();
        for (PackagePOJO pack : packagesOrientedList.get(npId)) {
            if (pack.getDstNodeId() == node.getId()) {
                pojo.add(pack);
            }
        }
        return pojo;
    }

    public ArrayList<ItemPOJO> getContainedItemPOJO(NodeProcess np, int packId) {
        ArrayList<ItemPOJO> items = new ArrayList<ItemPOJO>();
        for (ItemPOJO it : itemsOrientedList.get(np.getSerial())) {
            if (it.getPackId() == packId) {
                items.add(it);
            }
        }
        return items;
    }

    public long getLastCheckTime(NodeProcess np, Node node, ItemPOJO itemPOJO) {
        long lastTime = -1;
        for (ItemPOJO item : getItemOrientedPOJO(np.getSerial(), node)) {
            if (item.getId() == itemPOJO.getId()) {
                lastTime = item.getLastTimeChecked();
            }
        }
        return lastTime;
    }

    public long getLastCheckTime(NodeProcess np, Node node, PackagePOJO packPOJO) {
        long lastTime = -1;
        for (PackagePOJO pack : getPackOrientedPOJO(np.getSerial(), node)) {
            if (pack.getId() == packPOJO.getId()) {
                lastTime = pack.getLastTimeChecked();
            }
        }
        return lastTime;
    }

    public boolean checkSize() {
        boolean flag = true;
        int lenItem = itemsList.size();
        int lenPack = packagesList.size();
        int lenProcess = nodeProcess.size();
        if (lenItem != lenPack || lenItem != lenProcess || lenPack != lenProcess) {
            flag = false;
        }
        return flag;
    }

    public void intiCheckData(DataPOJO data) {
        this.data = data;
        this.itemNum = data.getItemNum();
        this.packNum = data.getPackNum();
        this.itemsList = data.getItemPOJO();
        this.packagesList = data.getPackPOJO();
        this.itemsOrientedList = data.getItemOrientedPOJO();
        this.packagesOrientedList = data.getPackOrientedPOJO();

//        System.out.println(itemsList.get(0).size()+"|"+packagesList.get(0).size());
    }

    public void showWarning(String message, String title, int mesType) {
        JOptionPane.showOptionDialog(null, message, title, JOptionPane.OK_CANCEL_OPTION, mesType, null, new Object[]{"确定"}, "确定");
    }

    public NodePanel getNodePanel() {
        return nodePanel;
    }

    public void setNodePanel(NodePanel nodePanel) {
        this.nodePanel = nodePanel;
    }

    public int getPackNum() {
        return packNum;
    }

    public void setPackNum(int packNum) {
        this.packNum = packNum;
    }

    public int getItemNum() {
        return itemNum;
    }

    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }

    public ArrayList<ArrayList<ItemPOJO>> getItemsList() {
        return itemsList;
    }

    public void setItemsList(ArrayList<ArrayList<ItemPOJO>> itemsList) {
        this.itemsList = itemsList;
    }

    public ArrayList<ArrayList<PackagePOJO>> getPackagesList() {
        return packagesList;
    }

    public void setPackagesList(ArrayList<ArrayList<PackagePOJO>> packagesList) {
        this.packagesList = packagesList;
    }

    public ArrayList<NodeProcess> getNodeProcess() {
        return nodeProcess;
    }

    public void setNodeProcess(ArrayList<NodeProcess> nodeProcess) {
        this.nodeProcess = nodeProcess;
    }

    public DataPOJO getData() {
        return data;
    }

    public void setData(DataPOJO data) {
        this.data = data;
    }
}
