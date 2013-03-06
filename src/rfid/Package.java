/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rfid;

import java.util.ArrayList;

/**
 *
 * @author shuai
 */
public class Package extends Item {

    private ArrayList<Item> items = null;
    private ArrayList<Package> packages = null;

    public Package(int id) {
        super(id);
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public ArrayList<Package> getPackages() {
        return packages;
    }

    public void setPackages(ArrayList<Package> packages) {
        this.packages = packages;
    }

    public static void main(String[] args) {
        ArrayList<Package> ps = new ArrayList<Package>();
        Package p1 = new Package(1);
        Package p2 = new Package(2);
        ps.add(p1);
        ps.add(p2);

        ArrayList<Package> ps2 = new ArrayList<Package>();
        ps2.add((Package) p1.clone());
        ps2.add((Package) p2.clone());

        for (Package p : ps) {
            System.out.println(p.getId());
        }
        for (Package p : ps2) {
            System.out.println(p.getId());
        }
        System.out.println("---------------");
        p1.setId(5);
        for (Package p : ps) {
            System.out.println(p.getId());
        }
        for (Package p : ps2) {
            System.out.println(p.getId());
        }
    }
}
