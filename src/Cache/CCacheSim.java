﻿package Cache;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;


public class CCacheSim extends JFrame implements ActionListener{

	private JPanel panelTop, panelLeft, panelRight, panelBottom;
	private JButton execStepBtn, execAllBtn, fileBotton;
	private JComboBox csBox, bsBox, wayBox, replaceBox, prefetchBox, writeBox, allocBox;
	private JFileChooser fileChoose;
	
	private JLabel labelTop,labelLeft,rightLabel,bottomLabel,fileLabel,fileAddrBtn, stepLabel1, stepLabel2,
		    csLabel, bsLabel, wayLabel, replaceLabel, prefetchLabel, writeLabel, allocLabel;
	private JLabel results[];


    //参数定义
	private String cachesize[] = { "2KB", "8KB", "32KB", "128KB", "512KB", "2MB" };
	private int[] csize = { 2048, 8192, 32768, 131072, 524288, 2097152 };
	
	private String blocksize[] = { "16B", "32B", "64B", "128B", "256B" };
	private int[] bsize = { 16, 32, 64, 128, 256 };
	
	private String way[] = { "直接映象", "2路", "4路", "8路", "16路", "32路" };
	private int[] ways = { 1, 2, 4, 8, 16, 32};
	
	private String replace[] = { "LRU", "FIFO", "RAND" };
	private String pref[] = { "不预取", "不命中预取" };
	private String write[] = { "写回法", "写直达法" };
	private String alloc[] = { "按写分配", "不按写分配" };
	private String typename[] = { "读数据", "写数据", "读指令" };
	private String hitname[] = {"不命中", "命中" };
	
	//右侧结果显示
	private String rightLable[]={"访问总次数：","读指令次数：","读数据次数：","写数据次数："};
	
	//打开文件
	private File file;
	
	//分别表示左侧几个下拉框所选择的第几项，索引从 0 开始
	private int csIndex, bsIndex, wayIndex, replaceIndex, prefetchIndex, writeIndex, allocIndex;
	
	//其它变量定义
	//...
	private ArrayList<String> instruction;  
	private char instructionType;
	private String instructionAddress;
	Cache cache;
	private int readInstruction;//读指令次数
	private int readData;		//读数据次数
	private int writeData;		//写数据次数
	private int sum;			//总次数
	private int readInstructionHit;//读指令命中
	private int readInstructionMiss;//读指令不命中
	private int readDataHit;//读数据命中
	private int readDataMiss;//读数据不命中
	private int writeDataHit;//写数据命中
	private int writeDataMiss;//写数据不命中
	private int sumMiss;		//总不命中次数
	private double readInstructionMissRate;//读指令不命中率
	private double writeDataMissRate;//写数据不命中率
	private double readDataMissRate;//读数据不命中率
	private double missRate;//不命中率
	private boolean isExecStep = false;
	private int instructionStart;
	/*
	 * 构造函数，绘制模拟器面板
	 */
	public CCacheSim(){
		super("Cache Simulator");
		fileChoose = new JFileChooser();
		draw();
	}
	
	
	//响应事件，共有三种事件：
	//   1. 执行到底事件
	//   2. 单步执行事件
	//   3. 文件选择事件
	public void actionPerformed(ActionEvent e){
				
		if (e.getSource() == execAllBtn) {
			initCache();
			simExecAll();
			consoleOutput();
			refreshUI();
			initCache();
		}
		if (e.getSource() == execStepBtn) {
			simExecStep();
			consoleOutput();
			refreshUI();
		}
		if (e.getSource() == fileBotton){
			int fileOver = fileChoose.showOpenDialog(null);
			if (fileOver == 0) {
				   String path = fileChoose.getSelectedFile().getAbsolutePath();
				   fileAddrBtn.setText(path);
				   file = new File(path);
				   try {
					readFile();
				} catch (IOException e1) {
					// TODO 自动生成的 catch 块
					e1.printStackTrace();
				}
			}
			initCache();
		}
	}
	
	/*
	 * 初始化 Cache 模拟器
	 */
	public void initCache() {
		readInstruction = 0;//读指令次数
		readData = 0;		//读数据次数
		writeData = 0;		//写数据次数
		sum = 0;			//总次数
		readInstructionHit = 0;//读指令命中
		readInstructionMiss = 0;//读指令不命中
		readDataHit = 0;//读数据命中
		readDataMiss = 0;//读数据不命中
		writeDataHit = 0;//写数据命中
		writeDataMiss = 0;//写数据不命中
		sumMiss = 0;		//总不命中次数
		readInstructionMissRate =  0.0;//读指令不命中率
		writeDataMissRate = 0.0;//写数据不命中率
		readDataMissRate = 0.0;//读数据不命中率
		missRate = 0.0;//不命中率
		instructionStart = 0;
		cache = new Cache(csize[csIndex],bsize[bsIndex],ways[wayIndex]);
	}
	
	/*
	 * 将指令和数据流从文件中读入
	 */
	public void readFile() throws IOException {
	     InputStream f = new FileInputStream(file);
		 DataInputStream in = new DataInputStream(f); 
		 BufferedReader bf = new BufferedReader(new InputStreamReader(in));
		 instruction = new ArrayList<String>();
		 String rd;
		 while((rd = bf.readLine()) != null) {
			 instruction.add(rd);		 
		 }
	}
	
	/*
	 * 模拟单步执行
	 */
	public void simExecStep() {
		isExecStep = true;
		simExecAll();
		instructionStart++;
	}
	
	/*
	 * 模拟执行到底
	 */
	public void simExecAll() {
		int tag = 0;
		int index = 0;
		int instructionEnd = 0;
		try {
			if(isExecStep) {//单步
				if(instructionStart >= instruction.size())
					return;
				instructionEnd = instructionStart + 1;
			}
			else {
				instructionEnd = instruction.size();
			}
		}catch(NullPointerException e) {
			e.printStackTrace();
		}
		
		for(int i = instructionStart;i < instructionEnd;i++) {
			instructionType = instruction.get(i).charAt(0);
			instructionAddress = instruction.get(i).substring(2);
			try {
				tag = instructionProcess.gettag(instructionAddress, 
						cache.groupOffset,cache.blockOffset);
				index = instructionProcess.getindex(instructionAddress,
						cache.groupOffset,cache.blockOffset);
			}catch(StringIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			if(instructionType == '0') {//读数据
				readData++;
					if(!cache.read(tag,index)) {//读不命中
						readDataMiss++;
						cache.replace(tag, index, replaceIndex, writeIndex);//0:LRU,1:FIFO,2:Random
						if(prefetchIndex == 0) {//不预取
							
						}
						else if(prefetchIndex == 1) {//预取
							readData++;
							if(!cache.prefetch(tag, index, replaceIndex, writeIndex)) {
								readDataMiss++;
							}
							else {
								readDataHit++;
							}
						}
					}
					else {
						readDataHit++;
					}
				}	
			else if(instructionType == '1') {//写不命中
				writeData++;
					if(!cache.write(tag,index,writeIndex)) {//写失败
						writeDataMiss++;
						if(allocIndex == 0) {//写分配
							cache.replace(tag, index, replaceIndex, writeIndex);
							cache.write(tag,index,writeIndex);
						}
						else if(allocIndex == 1) {
							
						}
					}
					else {
						writeDataHit++;
					}
				}
			else if(instructionType == '2') {
				readInstruction++;//读指令
					if(!cache.read(tag,index)) {
						readInstructionMiss++;
						cache.replace(tag, index, replaceIndex, writeIndex);
						if(prefetchIndex == 0) {//不预取
							
						}
						else if(prefetchIndex == 1) {//预取
							readInstruction++;
							if(!cache.prefetch(tag, index, replaceIndex, writeIndex)) {
								readInstructionMiss++;
							}
							else {
								readInstructionHit++;
							}
						}
					}
					else {
						readInstructionHit++;
					}
				}
			}
		isExecStep = false;
	}
	
	public void caculateMissRate() {
		readInstructionMissRate = (double) readInstructionMiss/readInstruction;//读指令不命中率
		writeDataMissRate = (double) writeDataMiss/writeData;//写数据不命中率
		readDataMissRate = (double) readDataMiss/readData;//读数据不命中率
		sum = readInstruction + writeData + readData;
		sumMiss = readInstructionMiss + writeDataMiss + readDataMiss;
		missRate = (double) sumMiss/sum;
	}

	public void consoleOutput() {
		caculateMissRate();
		DecimalFormat df = new DecimalFormat("0.00%");
		System.out.println(rightLable[0] + sum + "总不命中次数" + sumMiss 
				+ "总不命中率" + df.format(missRate));
		System.out.println(rightLable[1] + readInstruction + "不命中次数：" + readInstructionMiss 
				+ "不命中率：" + df.format(readInstructionMissRate));
		System.out.println(rightLable[2] + readData + "不命中次数：" + readDataMiss 
				+ "不命中率：" + df.format(readDataMissRate));
		System.out.println(rightLable[3] + writeData + "不命中次数：" + writeDataMiss 
				+ "不命中率：" + df.format(writeDataMissRate));
	}
	
	public void refreshUI(){
		DecimalFormat df = new DecimalFormat("0.00%");
		results[0].setText(rightLable[0] + sum + "总不命中次数" + sumMiss 
				+ "总不命中率" + df.format(missRate));
		
		results[1].setText(rightLable[1] + readInstruction + "不命中次数：" + readInstructionMiss 
				+ "不命中率：" + df.format(readInstructionMissRate));
		
		results[2].setText(rightLable[2] + readData + "不命中次数：" + readDataMiss 
				+ "不命中率：" + df.format(readDataMissRate));
		
		results[3].setText(rightLable[3] + writeData + "不命中次数：" + writeDataMiss 
				+ "不命中率：" + df.format(writeDataMissRate));
	}
	
	public static void main(String[] args) {
		new CCacheSim();
	}
	
	/**
	 * 绘制 Cache 模拟器图形化界面
	 * 无需做修改
	 */
	public void draw() {
		//模拟器绘制面板
		setLayout(new BorderLayout(5,5));
		panelTop = new JPanel();
		panelLeft = new JPanel();
		panelRight = new JPanel();
		panelBottom = new JPanel();
		panelTop.setPreferredSize(new Dimension(800, 50));
		panelLeft.setPreferredSize(new Dimension(300, 450));
		panelRight.setPreferredSize(new Dimension(500, 450));
		panelBottom.setPreferredSize(new Dimension(800, 100));
		panelTop.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelLeft.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelRight.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelBottom.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//*****************************顶部面板绘制*****************************************//
		labelTop = new JLabel("Cache Simulator");
		labelTop.setAlignmentX(CENTER_ALIGNMENT);
		panelTop.add(labelTop);

		
		//*****************************左侧面板绘制*****************************************//
		labelLeft = new JLabel("Cache 参数设置");
		labelLeft.setPreferredSize(new Dimension(300, 40));
		
		//cache 大小设置
		csLabel = new JLabel("总大小");
		csLabel.setPreferredSize(new Dimension(120, 30));
		csBox = new JComboBox(cachesize);
		csBox.setPreferredSize(new Dimension(160, 30));
		csBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				csIndex = csBox.getSelectedIndex();
			}
		});
		
		//cache 块大小设置
		bsLabel = new JLabel("块大小");
		bsLabel.setPreferredSize(new Dimension(120, 30));
		bsBox = new JComboBox(blocksize);
		bsBox.setPreferredSize(new Dimension(160, 30));
		bsBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				bsIndex = bsBox.getSelectedIndex();
			}
		});
		
		//相连度设置
		wayLabel = new JLabel("相联度");
		wayLabel.setPreferredSize(new Dimension(120, 30));
		wayBox = new JComboBox(way);
		wayBox.setPreferredSize(new Dimension(160, 30));
		wayBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				wayIndex = wayBox.getSelectedIndex();
			}
		});
		
		//替换策略设置
		replaceLabel = new JLabel("替换策略");
		replaceLabel.setPreferredSize(new Dimension(120, 30));
		replaceBox = new JComboBox(replace);
		replaceBox.setPreferredSize(new Dimension(160, 30));
		replaceBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				replaceIndex = replaceBox.getSelectedIndex();
			}
		});
		
		//预取策略设置
		prefetchLabel = new JLabel("预取策略");
		prefetchLabel.setPreferredSize(new Dimension(120, 30));
		prefetchBox = new JComboBox(pref);
		prefetchBox.setPreferredSize(new Dimension(160, 30));
		prefetchBox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				prefetchIndex = prefetchBox.getSelectedIndex();
			}
		});
		
		//写策略设置
		writeLabel = new JLabel("写策略");
		writeLabel.setPreferredSize(new Dimension(120, 30));
		writeBox = new JComboBox(write);
		writeBox.setPreferredSize(new Dimension(160, 30));
		writeBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				writeIndex = writeBox.getSelectedIndex();
			}
		});
		
		//调块策略
		allocLabel = new JLabel("写不命中调块策略");
		allocLabel.setPreferredSize(new Dimension(120, 30));
		allocBox = new JComboBox(alloc);
		allocBox.setPreferredSize(new Dimension(160, 30));
		allocBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				allocIndex = allocBox.getSelectedIndex();
			}
		});
		
		//选择指令流文件
		fileLabel = new JLabel("选择指令流文件");
		fileLabel.setPreferredSize(new Dimension(120, 30));
		fileAddrBtn = new JLabel();
		fileAddrBtn.setPreferredSize(new Dimension(210,30));
		fileAddrBtn.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		fileBotton = new JButton("浏览");
		fileBotton.setPreferredSize(new Dimension(70,30));
		fileBotton.addActionListener(this);
		
		panelLeft.add(labelLeft);
		panelLeft.add(csLabel);
		panelLeft.add(csBox);
		panelLeft.add(bsLabel);
		panelLeft.add(bsBox);
		panelLeft.add(wayLabel);
		panelLeft.add(wayBox);
		panelLeft.add(replaceLabel);
		panelLeft.add(replaceBox);
		panelLeft.add(prefetchLabel);
		panelLeft.add(prefetchBox);
		panelLeft.add(writeLabel);
		panelLeft.add(writeBox);
		panelLeft.add(allocLabel);
		panelLeft.add(allocBox);
		panelLeft.add(fileLabel);
		panelLeft.add(fileAddrBtn);
		panelLeft.add(fileBotton);
		
		//*****************************右侧面板绘制*****************************************//
		//模拟结果展示区域
		rightLabel = new JLabel("模拟结果");
		rightLabel.setPreferredSize(new Dimension(500, 40));
		results = new JLabel[4];
		results[0] = new JLabel(rightLable[0] + sum + "总不命中次数" + sumMiss + "总不命中率" + missRate);
		results[0].setPreferredSize(new Dimension(500, 40));
		
		results[1] = new JLabel(rightLable[1] + readInstruction + "不命中次数：" + readInstructionMiss 
				+ "不命中率：" + readInstructionMissRate);
		results[1].setPreferredSize(new Dimension(500, 40));
		
		results[2] = new JLabel(rightLable[2] + readData + "不命中次数：" + readDataMiss 
				+ "不命中率：" + readDataMissRate);
		results[2].setPreferredSize(new Dimension(500, 40));
		
		results[3] = new JLabel(rightLable[3] + writeData + "不命中次数：" + writeDataMiss 
				+ "不命中率：" + writeDataMissRate);
		results[3].setPreferredSize(new Dimension(500, 40));
		
		stepLabel1 = new JLabel();
		stepLabel1.setVisible(false);
		stepLabel1.setPreferredSize(new Dimension(500, 40));
		stepLabel2 = new JLabel();
		stepLabel2.setVisible(false);
		stepLabel2.setPreferredSize(new Dimension(500, 40));
		
		
		panelRight.add(rightLabel);
		for (int i=0; i<4; i++) {
			panelRight.add(results[i]);
		}
		
		panelRight.add(stepLabel1);
		panelRight.add(stepLabel2);
		

		//*****************************底部面板绘制*****************************************//
		
		bottomLabel = new JLabel("执行控制");
		bottomLabel.setPreferredSize(new Dimension(800, 30));
		execStepBtn = new JButton("步进");
		execStepBtn.setLocation(100, 30);
		execStepBtn.addActionListener(this);
		execAllBtn = new JButton("执行到底");
		execAllBtn.setLocation(300, 30);
		execAllBtn.addActionListener(this);
		
		panelBottom.add(bottomLabel);
		panelBottom.add(execStepBtn);
		panelBottom.add(execAllBtn);

		add("North", panelTop);
		add("West", panelLeft);
		add("Center", panelRight);
		add("South", panelBottom);
		setSize(820, 620);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
