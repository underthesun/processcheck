/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sim;

import java.awt.Color;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import main.MainPanel;
import processcheck.Node;
import processcheck.NodeProcess;
import rfid.Item;
import rfid.Package;

/**
 *
 * @author shuai
 */
public class Simulation {

    private final static int packNum = 2;
    private final static int itemNum = 4;
    private ArrayList<NodeProcess> nodeProcess = null;
    private ArrayList<Item> items = null;
    private ArrayList<Item> itemsOriented = null;

    public Simulation() {
    }

    public void start() {
        boolean flag = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (NodeProcess np : nodeProcess) {
            if (!checkAmountIntegrity(np)) {
                np.setBackground(Color.red);
                showWarning("过程" + np.getSerial() + "检测到数目错误", "报警", JOptionPane.WARNING_MESSAGE);
                flag = false;
                break;
            } else {
                for (Node node : np.getNodes()) {
                    if (!checkContainmentIntegrity(node)) {
                        node.setBackground(Color.red);
                        showWarning("进程" + node.getId() + "检测到包含关系错误", "报警", JOptionPane.WARNING_MESSAGE);
                        flag = false;
                        break;
                    }
                    if (!checkOrderIntegrity(node)) {
                        node.setBackground(Color.red);
                        showWarning("进程" + node.getId() + "检测到时间顺序错误", "报警", JOptionPane.WARNING_MESSAGE);
                        flag = false;
                        break;
                    }
                    //set items attr to the next node
                    nodeTransfer(node);
                }
            }
            //set items attr to the next NodeProcess
            if (flag) {
                errorGen(np);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                break;
            }
        }
        if (flag) {
            showWarning("检查通过", "提示", JOptionPane.PLAIN_MESSAGE);
        }
    }

    public boolean checkAmountIntegrity(NodeProcess np) {
        boolean flag = true;
        if (items.size() != itemsOriented.size()) {
            flag = false;
        }
        return flag;
    }

    public boolean checkContainmentIntegrity(Node node) {
        boolean flag = true;
        ArrayList<Item> itemsRecv = getItems(node);
        ArrayList<Item> itemsSent = getItemsOriented(node);
        ArrayList<Item> itemsContained = null;

        for (Item item : itemsSent) {
            if (!itemsRecv.contains(item)) {
                flag = false;
                break;
            } else {
                int id = item.getId();
                if (id <= packNum) {// is a package
                    itemsContained = getPreContainedItems(id);
                    if (!itemsRecv.containsAll(itemsContained)) {
                        flag = false;
                        break;
                    }
                }
            }
        }
        return flag;
    }

    public boolean checkOrderIntegrity(Node node) {
        boolean flag = true;
        long time = System.currentTimeMillis();
        for (Item item : getItems(node)) {
            for (Item it : getItemsOriented(node)) {
                if (item.getId() == it.getId()) {
                    if (it.getLastTimeChecked() >= time) {
                        flag = false;
                        break;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * init data for the whole process
     */
    public void init() {
        items = new ArrayList<Item>();
        itemsOriented = new ArrayList<Item>();
        //set items' attrs
        for (int i = 0; i < itemNum; i++) {
            if (i < packNum) {
                Package p = new Package(i);
                p.setLastTimeChecked(System.currentTimeMillis());
                items.add(p);
            } else {
                Item item = new Item(i);
                item.setPackageId(i % packNum);
                item.setLastTimeChecked(System.currentTimeMillis());
                items.add(item);
            }
        }
        //set items for every package; set dst for i  
        ArrayList<Node> nodes = nodeProcess.get(0).getNodes();
        int len = nodes.size();
        for (int i = 0; i < packNum; i++) {
            Package p = (Package) items.get(i);
            Node node = nodes.get(i % len);
            p.setDstNode(node);
            for (Item item : items) {
                if (item.getPackageId() == p.getId()) {
                    item.setDstNode(node);
                }
            }
        }
        //clone items set as itemsoriented
        for (Item item : items) {
            itemsOriented.add((Item) item.clone());
//            System.out.println("item:" + item.getId() + " dst:" + item.getDstNode().getId());
        }
    }

    public ArrayList<Item> getItems(Node node) {
        ArrayList<Item> its = new ArrayList<Item>();
        for (Item it : items) {
            if (it.getDstNode().getId() == node.getId()) {
                its.add(it);
            }
        }
        return its;
    }

    public ArrayList<Item> getItemsOriented(Node node) {
        ArrayList<Item> its = new ArrayList<Item>();
        for (Item it : itemsOriented) {
            if (it.getDstNode().getId() == node.getId()) {
                its.add(it);
            }
        }
        return its;
    }

    public ArrayList<Item> getPreContainedItems(int packageId) {
        ArrayList<Item> containedItems = new ArrayList<Item>();
        for (Item item : itemsOriented) {
            if (item.getPackageId() == packageId) {
                containedItems.add(item);
            }
        }
        return containedItems;
    }

    public void nodeTransfer(Node node) {
        ArrayList<Node> nodes = node.getPostNodes();
        int nodeSize = nodes.size();
        ArrayList<Item> itemsRecv = getItems(node);
        int itemSize = itemsRecv.size();
        if (nodeSize > 0 && itemSize > 0) {
            long time = System.currentTimeMillis();
            for (int i = 0; i < itemSize; i++) {
                Item item = itemsRecv.get(i);
                if (item.getId() < packNum) {
                    item.setDstNode(node);
                    for (Item it : items) {
                        if (it.getPackageId() == item.getId()) {
                            it.setDstNode(node);
                        }
                    }
                }
                item.setLastTimeChecked(time);
            }
        }
        itemsOriented.clear();
        for (Item item : items) {
            itemsOriented.add((Item) item.clone());
        }
    }

    public void nodeProcessTransfer(NodeProcess nodeProcess) {
    }

    public void errorGen(Node node) {
    }

    public void errorGen(NodeProcess nodeProcess) {
    }

    public ArrayList<NodeProcess> getNodeProcess() {
        return nodeProcess;
    }

    public void setNodeProcess(ArrayList<NodeProcess> nodeProcess) {
        this.nodeProcess = nodeProcess;
    }

    public void showWarning(String message, String title, int mesType) {
        JOptionPane.showOptionDialog(null, message, title, JOptionPane.OK_CANCEL_OPTION, mesType, null, new Object[]{"确定"}, "确定");
    }

    public static void main(String[] args) {
        Simulation sim = new Simulation();
        sim.init();
    }
}
