package com.joshondesign.appbundler.simpletest;

import javax.swing.*;
import java.awt.event.*;

public class Main {
    public static void main(String ... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setup();
            }
        });
    }
    
    public static void setup() {
        JFrame frame = new JFrame("A Simple Test");
        JButton button = new JButton("click to quit");
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
        });
        frame.add(button);
        frame.pack();
        frame.setSize(600,400);
        frame.show();
    }
}
