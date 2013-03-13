/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sim;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import main.MainPanel;
import main.NodePanel;
import processcheck.Node;
import processcheck.NodeProcess;
import rfid.Item;
import rfid.Package;
import util.DataPOJO;
import util.ItemPOJO;
import util.PackagePOJO;

/**
 *
 * @author shuai
 */
public class Simulation {

    private int packNum = 5;
    private int itemNum = 10;
    private ArrayList<NodeProcess> nodeProcess = null;
    private ArrayList<Item> items = null;
    private ArrayList<Item> itemsOriented = null;
    private NodePanel nodePanel;
    /**
     * check dataPOJO
     */
    private DataPOJO dataPOJO;

    public Simulation(NodePanel nodePanel) {
        this.nodePanel = nodePanel;
        nodeProcess = nodePanel.getNodeProcess();
        /**
         * check dataPOJO
         */
        dataPOJO = new DataPOJO();
        dataPOJO.setItemNum(itemNum);
        dataPOJO.setPackNum(packNum);
    }

    public void startSim() {
        nodePanel.colorResest();
        boolean flag = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
        }

        /**
         * check dataPOJO
         */
        ArrayList<ArrayList<ItemPOJO>> itemPOJOList = new ArrayList<ArrayList<ItemPOJO>>();
        ArrayList<ArrayList<PackagePOJO>> packPOJOList = new ArrayList<ArrayList<PackagePOJO>>();
        ArrayList<ArrayList<ItemPOJO>> itemOrientedPOJOList = new ArrayList<ArrayList<ItemPOJO>>();
        ArrayList<ArrayList<PackagePOJO>> packOrientedPOJOList = new ArrayList<ArrayList<PackagePOJO>>();

        for (NodeProcess np : nodeProcess) {
//            errorGen(np);// generate random NodeProcess error
            /**
             * check dataPOJO
             */
            ArrayList<ItemPOJO> itemPOJO = new ArrayList<ItemPOJO>();
            ArrayList<PackagePOJO> packPOJO = new ArrayList<PackagePOJO>();
            ArrayList<ItemPOJO> itemOrientedPOJO = new ArrayList<ItemPOJO>();
            ArrayList<PackagePOJO> packOrientedPOJO = new ArrayList<PackagePOJO>();
            for (Item item : items) {
                int itemId = item.getId();
                if (itemId < packNum) {
                    PackagePOJO packPojo = new PackagePOJO();
                    packPojo.setDstNodeId(item.getDstNode().getId());
                    packPojo.setId(itemId);
                    packPojo.setLastTimeChecked(item.getLastTimeChecked());
                    packPojo.setPackId(item.getPackageId());
                    packPojo.setPackIds(new int[]{});
                    ArrayList<Item> itemContained = getPreContainedItems(itemId);
                    int[] ids = new int[itemContained.size()];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = itemContained.get(i).getId();
                    }
                    packPOJO.add(packPojo);
                } else {
                    ItemPOJO itemPojo = new ItemPOJO();
                    itemPojo.setDstNodeId(item.getDstNode().getId());
                    itemPojo.setId(itemId);
                    itemPojo.setLastTimeChecked(item.getLastTimeChecked());
                    itemPojo.setPackId(item.getPackageId());
                    itemPOJO.add(itemPojo);
                }
            }
            for (Item itemOriented : itemsOriented) {
                int itemId = itemOriented.getId();
                if (itemId < packNum) {
                    PackagePOJO packOrientedPojo = new PackagePOJO();
                    packOrientedPojo.setDstNodeId(itemOriented.getDstNode().getId());
                    packOrientedPojo.setId(itemId);
                    packOrientedPojo.setLastTimeChecked(itemOriented.getLastTimeChecked());
                    packOrientedPojo.setPackId(itemOriented.getPackageId());
                    packOrientedPojo.setPackIds(new int[]{});
                    ArrayList<Item> itemContained = getPreContainedItems(itemId);
                    int[] ids = new int[itemContained.size()];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = itemContained.get(i).getId();
                    }
                    packOrientedPOJO.add(packOrientedPojo);
                } else {
                    ItemPOJO itemOrientedPojo = new ItemPOJO();
                    itemOrientedPojo.setDstNodeId(itemOriented.getDstNode().getId());
                    itemOrientedPojo.setId(itemId);
                    itemOrientedPojo.setLastTimeChecked(itemOriented.getLastTimeChecked());
                    itemOrientedPojo.setPackId(itemOriented.getPackageId());
                    itemOrientedPOJO.add(itemOrientedPojo);
                }
            }
            itemPOJOList.add(itemPOJO);
            packPOJOList.add(packPOJO);
            itemOrientedPOJOList.add(itemOrientedPOJO);
            packOrientedPOJOList.add(packOrientedPOJO);

            long time = System.currentTimeMillis();
            if (!checkAmountIntegrity(np)) {
                np.setBackground(Color.red);
                showWarning("过程 " + np.getSerial() + " 检测到数目错误", "报警", JOptionPane.WARNING_MESSAGE);
                flag = false;
                break;
            } else {
                for (Node node : np.getNodes()) {
//                    errorGen(node);// generate random Node error
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
//                    nodeTransfer(node,time);
                }
            }
            //set items attr to the next NodeProcess
            if (flag) {
                transfer(np, time);
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
            /**
             * check dataPOJO
             */
            dataPOJO.setItemOrientedPOJO(itemOrientedPOJOList);
            dataPOJO.setItemPOJO(itemPOJOList);
            dataPOJO.setPackOrientedPOJO(packOrientedPOJOList);
            dataPOJO.setPackPOJO(packPOJOList);
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(nodePanel) == JFileChooser.APPROVE_OPTION) {
                File saveFile = fc.getSelectedFile();
                if (!saveFile.exists()) {
                    try {
                        saveFile.createNewFile();
                    } catch (IOException ex) {
                        Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(saveFile));
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(dataPOJO, dataPOJO.getClass(), bw);
                } catch (Exception ex) {
                } finally {
                    try {
                        bw.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Simulation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
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
                if (id < packNum) {// is a package
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
     * initSimData dataPOJO for the whole process
     */
    public void initSimData() {
        items = new ArrayList<Item>();
        itemsOriented = new ArrayList<Item>();
        //set items' attrs
        long time = System.currentTimeMillis();
        for (int i = 0; i < itemNum; i++) {
            if (i < packNum) {
                Package p = new Package(i);
                p.setLastTimeChecked(time);
                items.add(p);
            } else {
                Item item = new Item(i);
                item.setPackageId(i % packNum);
                item.setLastTimeChecked(time);
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
            if (item.getId() < packNum) {
                Package p = (Package) item;
                itemsOriented.add((Package) p.clone());
            } else {
                itemsOriented.add((Item) item.clone());
            }
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

    public void transfer(NodeProcess np, long time) {
        for(Node node:np.getNodes()){
            nodeTransfer(node, time);
        }        
        itemsOriented.clear();
        for (Item item : items) {
            if (item.getId() < packNum) {
                Package p = (Package) item;
                itemsOriented.add((Package) p.clone());
            } else {
                itemsOriented.add((Item) item.clone());
            }
        }
    }

    /**
     * set items' attrs which will be transfered to next adjacent nodes
     *
     * @param node
     */
    public void nodeTransfer(Node node, long time) {
        ArrayList<Node> nodes = node.getPostNodes();
        int nodeSize = nodes.size();
        ArrayList<Item> itemsRecv = getItems(node);
        ArrayList<Item> packsRecv = new ArrayList<Item>();
        for (Item item : itemsRecv) {
            if (item.getId() < packNum) {
                packsRecv.add(item);
            }
        }
        int packSize = packsRecv.size();
        if (nodeSize > 0 && packSize > 0) {
            for (int i = 0; i < packSize; i++) {
                Item item = packsRecv.get(i);
                item.setDstNode(nodes.get(i % nodeSize));
                for (Item it : itemsRecv) {
                    if (it.getPackageId() == item.getId()) {
                        it.setDstNode(nodes.get(i % nodeSize));
                        it.setLastTimeChecked(time);
                    }
                }
                item.setLastTimeChecked(time);
            }
        }
    }

    public void errorGen(Node node) {
        Random rdm = new Random();
        int errorNodeId = rdm.nextInt(nodePanel.getNodes().size());
        int errorType = rdm.nextInt(2);
        System.out.println("nodeId:" + node.getId() + " errorId:" + errorNodeId);
        if (node.getId() == errorNodeId) {
            if (errorType == 0) {// containment error
                for (Item item : itemsOriented) {
                    if (item.getDstNode().getId() == node.getId()) {
                        itemsOriented.remove(item);
                        System.out.println("nodeId:" + node.getId() + " errorNodeId:" + errorNodeId + " error: containment");
                        break;
                    }
                }
            } else {// order error
                for (Item item : itemsOriented) {
                    if (item.getDstNode().getId() == node.getId()) {
                        item.setLastTimeChecked(System.currentTimeMillis() + 10000);// set a 10-second long delay
                        System.out.println("nodeId:" + node.getId() + " errorNodeId:" + errorNodeId + " error: order");
                        break;
                    }
                }
            }
        }
    }

    public void errorGen(NodeProcess np) {
        Random rdm = new Random();
        int errorNpId = rdm.nextInt(nodeProcess.size());
        if (np.getSerial() == errorNpId) {
            itemsOriented.clear();
        }
        System.out.println("npId:" + np.getSerial() + " errorId:" + errorNpId);
    }

    public ArrayList<NodeProcess> getNodeProcess() {
        return nodeProcess;
    }

    public void setNodeProcess(ArrayList<NodeProcess> nodeProcess) {
        this.nodeProcess = nodeProcess;
    }

    public void showWarning(String message, String title, int mesType) {
        JOptionPane.showOptionDialog(null, message, title, JOptionPane.OK_CANCEL_OPTION, mesType, null, new Object[]{"确定"}, "确定");
//        nodePanel.colorResest();
    }
}
