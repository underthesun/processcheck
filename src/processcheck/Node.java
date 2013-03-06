/*
 * 原子进程，图中节点
 */
package processcheck;

import components.DraggableButton;
import components.NodeConfDialog;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import main.NodePanel;
import rfid.Item;

/**
 *
 * @author shuai
 */
public class Node extends DraggableButton {

    private static int count = 0;
    private int id = 0;
    private int proID;
    private ArrayList<Node> preNodes;
    private ArrayList<Node> postNodes;
    private NodePanel nodePanel;
    private NodeConfDialog dialog;
    private ArrayList<Item> items = null;
    private ArrayList<Item> itemsOriented = null;

//    public Node(String name, Node preNode, Point p, NodePanel nodePanel) {
//        super(name, p);
//        this.nodePanel = nodePanel;
//        this.dialog = nodePanel.getNodeDialog();
//        this.preNodes = new ArrayList<Node>();
//        this.postNodes = new ArrayList<Node>();
//        addListener();
//    }
    
    public static void resetCounter(){
        Node.count = 0;
    }
    public Node(String text, Point p, int pid, NodePanel np) {
        super(text, p);
        this.id = count++;
        this.proID = pid;
        this.nodePanel = np;
        this.dialog = np.getNodeDialog();
        this.preNodes = new ArrayList<Node>();
        this.postNodes = new ArrayList<Node>();
        addListener(this);
    }

    public void addListener(final Node self) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                ArrayList<Node> pNodes = getNodesOfPostProcess();
                dialog.setNodes(pNodes);
                ArrayList<JCheckBox> children = dialog.getChildren();
                if (children != null) {
                    postNodes.clear();
                    int len = children.size();
                    for (int i = 0; i < len; i++) {
                        JCheckBox cb = children.get(i);
                        Node pn = pNodes.get(i);
                        if (cb.isSelected()) {// add post nodes and add itself to post nodes' pre-node list
                            postNodes.add(pn);
                            pn.addPreNode(self);
                            pn.checkAssociation();
                        }else{
                            ArrayList<Node> postPre = pn.getPreNodes();
                            if(postPre.contains(self)){
                                postPre.remove(self);
                            }
                        }
                    }
                    checkAssociation();
                    nodePanel.repaint();
                    dialog.reset();
                }
            }
        });
    }

    public ArrayList<Node> getNodesOfPostProcess() {
        ArrayList<Node> pNodes = new ArrayList<Node>();
        for (Node n : nodePanel.getNodes()) {
            if (n.getProID() == proID + 1) {
                pNodes.add(n);
            }
        }
        return pNodes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProID() {
        return proID;
    }

    public void setProID(int proID) {
        this.proID = proID;
    }

    public ArrayList<Node> getPreNodes() {
        return preNodes;
    }

    public void setPreNodes(ArrayList<Node> preNodes) {
        this.preNodes = preNodes;
    }

    public ArrayList<Node> getPostNodes() {
        return postNodes;
    }

    public void setPostNodes(ArrayList<Node> postNodes) {
        this.postNodes = postNodes;
    }

    public NodePanel getNodePanel() {
        return nodePanel;
    }

    public void setNodePanel(NodePanel nodePanel) {
        this.nodePanel = nodePanel;
    }

    public NodeConfDialog getDialog() {
        return dialog;
    }

    public void setDialog(NodeConfDialog dialog) {
        this.dialog = dialog;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public ArrayList<Item> getItemsOriented() {
        return itemsOriented;
    }

    public void setItemsOriented(ArrayList<Item> itemRelations) {
        this.itemsOriented = itemRelations;
    }

    public boolean hasPosts() {
        return postNodes.isEmpty() ? false : true;
    }

    public void addPreNode(Node node) {
        boolean flag = true;
        for (Node n : preNodes) {
            if (n == node) {
                flag = false;
                break;
            }
        }
        if (flag) {
            preNodes.add(node);
        }
    }

    public boolean checkAssociation() {
        boolean flag = true;
        if (proID == 0) {
            if (postNodes.isEmpty()) {
                flag = false;
            }
        } else if (proID == nodePanel.getNodeProcess().size() - 1) {
            if (preNodes.isEmpty()) {
                flag = false;
            }
        } else {
            if (preNodes.isEmpty() || postNodes.isEmpty()) {
                flag = false;
            }
        }
        if (flag) {
            setBackground(UIManager.getColor("Button.ground"));
        } else {
            setBackground(Color.red);
        }
        return flag;
    }
//    public HashSet<Item> getPreContainedItems(int packageId) {
//        HashSet<Item> containedItems = new HashSet<Item>();
//        for (Item item : itemsOriented) {
//            if (item.getPackageId() == packageId) {
//                containedItems.add(item);
//            }
//        }
//        return containedItems;
//    }
//    
//    public boolean checkIntegrity() {
//        boolean flag = true;
//        return flag;
//    }
//
//    public void checkAmountIntegrity() {
//    }
//
//    public void checkContainmentIntegrity() {
//    }
//
//    public void checkOrderIntegrity() {
//    }
}
