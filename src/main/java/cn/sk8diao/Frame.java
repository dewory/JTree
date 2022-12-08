package cn.sk8diao;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * 作者: 刁旺睿
 * 时间: 2022/12/6 10:50
 */
public class Frame extends JFrame {

    JSplitPane jSplitPane;
    DefaultMutableTreeNode root;
    JTree jTree;
    String path;
    JScrollPane jScrollPane;
    JTextArea jTextArea;
    JPopupMenu jPopupMenu;
    JMenuItem addItem;
    JMenuItem deleteItem;

    public Frame() {

        //初始化图形界面
        super("JTree By 刁旺睿");//设置窗口标题
        setSize(800, 600);//设置宽高
        setLocationRelativeTo(null);//设置居中
        setDefaultCloseOperation(EXIT_ON_CLOSE);//设置关闭
        setResizable(false);//设置窗体不可改变大小
        setLayout(new FlowLayout());//设置布局

        //创建JTree来显示文件夹结构
        root = new DefaultMutableTreeNode("古诗词");//创建根节点
        addNode(root, "src/古诗词");//获取指定目录下的文件结构并添加到根节点
        jTree = new JTree(root);
        jTree.setFont(new Font("黑体", Font.BOLD, 20));//设置JTree字体
        jTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                path = "src/" + e.getPath().toString().replace("[", "").replace("]", "").replace(", ", "/");
                System.out.println(path);
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
                    StringBuffer string = new StringBuffer();
                    String readLine;
                    while ((readLine = bufferedReader.readLine()) != null) {
                        string.append("\n").append(readLine);
                    }
                    jTextArea.setText(string.toString());
                    bufferedReader.close();//关闭BufferedReader
                } catch (IOException ex) {
                    //throw new RuntimeException(ex);
                    System.out.println("java.io.FileNotFoundException");
                }
            }
        });//为JTree添加节点选中监听事件
        jTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TreePath path = jTree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }
                jTree.setSelectionPath(path);
                if (e.getButton() == 3) {
                    jPopupMenu.show(jTree, e.getX(), e.getY());
                }
            }
        });//为JTree节点添加右击事件监听器
        jTree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                String path = event.getPath().toString().replace(", ", "=>");
                System.out.println(path + "展开了");
                jTextArea.setText(path + "展开了");
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                String path = event.getPath().toString().replace(", ", "=>");
                System.out.println(path + "关闭了");
                jTextArea.setText(path + "关闭了");
            }
        });//为JTree节点添加展开与关闭事件监听器

        //创建JTextArea来显示文件内容
        jTextArea = new JTextArea();
        jTextArea.setLineWrap(true);//设置JTextArea自动换行
        jTextArea.setFont(new Font("黑体", Font.PLAIN, 15));//设置JTextArea字体
        jTextArea.setEditable(false);//设置JTextArea不可编辑

        //创建JScrollPane让JTextArea内容可以上下滚动
        jScrollPane = new JScrollPane(jTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //创建分割面板
        jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, jTree, jScrollPane);
        jSplitPane.setEnabled(false);//设置分割线不可移动
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                jSplitPane.setDividerLocation(0.3);
            }
        });//设置监听器 获取当前窗口大小 按比例分配

        //添加节点新建与删除的右键
        jPopupMenu = new JPopupMenu();
        addItem = new JMenuItem("新建文本文档");
        addItem.setFont(new Font("黑体", Font.BOLD, 15));
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("添加节点");
                System.out.println(path);
                try {
                    File file = new File(path + "/新建文本文档.txt");
                    if (file.createNewFile()) {
                        System.out.println("文件创建成功");
                        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                        currentNode.add(new DefaultMutableTreeNode("新建文本文档.txt"));
                        jTree.updateUI();
                    } else System.out.println("出错了，该文件已经存在。");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });//添加事件
        jPopupMenu.add(addItem);
        deleteItem = new JMenuItem("删除文本文档");
        deleteItem.setFont(new Font("黑体", Font.BOLD, 15));
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("删除节点");
                System.out.println(path);
                try {
                    File file = new File(path);
                    if (file.delete()) {
                        System.out.println(file.getName() + "文件已被删除");
                        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
                        DefaultMutableTreeNode currentNodeParent = (DefaultMutableTreeNode) currentNode.getParent();
                        currentNodeParent.remove(currentNode);
                        jTree.updateUI();
                    } else System.out.println("文件删除失败");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });//删除事件
        jPopupMenu.add(deleteItem);

        //把分隔面板作为内容面板添加到窗口并显示
        setContentPane(jSplitPane);
        setVisible(true);

    }

    //将文件路径中的内容添加到节点中
    private void addNode(DefaultMutableTreeNode node, String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            System.out.println("正在读取 " + path + " 目录中...");
            String[] list = file.list();
            assert list != null;
            for (String item : list) {
                File file2 = new File(path + "/" + item);
                if (file2.isDirectory()) {
                    System.out.println("文件夹 " + item);
                    DefaultMutableTreeNode folder = new DefaultMutableTreeNode(item);
                    node.add(folder);
                    addNode(folder, path + "/" + item);
                } else {
                    System.out.println("文件 " + item);
                    node.add(new DefaultMutableTreeNode(item));
                }
            }
        }
    }

}
