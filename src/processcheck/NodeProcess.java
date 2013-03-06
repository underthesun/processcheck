/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package processcheck;

import components.DraggableButton;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import main.NodePanel;
import rfid.Item;

/**
 *
 * @author shuai
 */
public class NodeProcess extends DraggableButton {

    private int serial;
    private ArrayList<Node> nodes;
    private NodePanel nodePanel;
    private ArrayList<Item> items = null;
    private ArrayList<Item> itemsOriented = null;

    public NodeProcess(String s, Point p, int serial, NodePanel np) {
        super(s, p);
        this.serial = serial;
        this.nodePanel = np;
        this.nodes = new ArrayList<Node>();
        setBorder(new LineBorder(Color.green));
        addListener(this);
    }

    public void addListener(final NodeProcess nodeProcess) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (!hasNodes()) {
                    String input = JOptionPane.showInputDialog("输入过程包含的进程数");
                    if (input != null) {
                        int num = Integer.parseInt(input);
                        Point p = getLocation();
                        for (int i = 0; i < num; i++) {
                            Node node = new Node("(" + serial + ")过程" + i, new Point(p.x, p.y + (i + 1) * 100), serial, nodePanel);
                            nodes.add(node);
                            nodePanel.addNode(node);
                        }
                        setBackground(UIManager.getColor("Button.ground"));
                    }
                    nodePanel.repaint();
                }
            }
        });
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public NodePanel getNodePanel() {
        return nodePanel;
    }

    public void setNodePanel(NodePanel nodePanel) {
        this.nodePanel = nodePanel;
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

    public void setItemsOriented(ArrayList<Item> itemsOriented) {
        this.itemsOriented = itemsOriented;
    }

    public boolean hasNodes() {
        return !nodes.isEmpty();
    }

    public void reset() {
        nodes.clear();
    }

    public NodeProcess getPreNodePreocess() {
        NodeProcess nodeProcess = null;
        if (serial > 1) {
            nodeProcess = nodePanel.getNodeProcess().get(serial - 1);
        }
        return nodeProcess;
    }

    public NodeProcess getPostNodeProcess() {
        NodeProcess nodeProcess = null;
        if (serial < nodePanel.getNodeProcess().size()) {
            nodeProcess = nodePanel.getNodeProcess().get(serial + 1);
        }
        return nodeProcess;
    }
}
