package antiSpamFilter.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import antiSpamFilter.api.AntiSpamFilterAutomaticConfiguration;
import antiSpamFilter.api.AntiSpamFilterProblem;
import antiSpamFilter.api.FileEmail;
import antiSpamFilter.api.FileRule;
import antiSpamFilter.api.HVFile;
import antiSpamFilter.api.LatexFile;
import antiSpamFilter.api.Rule;

/**
 * Gui da janela de processamento automatico
 * 
 */
public class RunGuiAuto {

	private JFrame frmAuto;
	private JTable table;
	private FileRule fileRules;
	private FileEmail fileHam;
	private FileEmail fileSpam;
	private JTextField textField;
	private DefaultTableModel model;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private boolean jmetalalreadyrunned = false;

	/**
	 * Inicializador Cria a gui para mostrar a janela de conigura��o automatica
	 */
	public RunGuiAuto(FileRule fileRules, FileEmail fileHam, FileEmail fileSpam) {
		this.fileRules = fileRules;
		this.fileHam = fileHam;
		this.fileSpam = fileSpam;
		initialize();
		frmAuto.setVisible(true);
	}

	private void initialize() {
		
		/**
		 * Initialize Gui Visuals
		 */
		
		frmAuto = new JFrame();
		frmAuto.setTitle("Auto");
		frmAuto.setBounds(300, 300, 450, 300);
		frmAuto.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JSplitPane splitPane = new JSplitPane();
		frmAuto.getContentPane().add(splitPane, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));

		JButton btnNewButton_1 = new JButton("Run & Find best weights");
		panel.add(btnNewButton_1);

		lblNewLabel = new JLabel("FP: ");
		panel.add(lblNewLabel);

		lblNewLabel_1 = new JLabel("FN: ");
		panel.add(lblNewLabel_1);

		JButton btnSaveRulesTo = new JButton("Save Rules To File");

		JButton btnCompileHv = new JButton("Compile Hv");
		panel.add(btnCompileHv);
		btnCompileHv.setEnabled(false);

		JButton btnCompileLatex = new JButton("Compile Latex");
		panel.add(btnCompileLatex);
		btnCompileLatex.setEnabled(false);

		panel.add(btnSaveRulesTo);

		JButton btnBack = new JButton("Back");
		panel.add(btnBack);

		JButton btnExit = new JButton("Exit");
		panel.add(btnExit);

		JPanel panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		// Setup table

		model = new DefaultTableModel();
		model.addColumn("Rules");
		model.addColumn("Weight [-5;5]");
		table = new JTable(model);
		table.setEnabled(false);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
		table.setRowSorter(sorter);
		tabelUpdate();

		//

		panel_1.add(new JScrollPane(table), BorderLayout.CENTER);

		JButton btnNewButton = new JButton("Configure Rules");
		panel_1.add(btnNewButton, BorderLayout.SOUTH);

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.NORTH);

		textField = new JTextField();

		JLabel lblSearch = new JLabel("Search");
		panel_2.add(lblSearch);
		panel_2.add(textField);
		textField.setColumns(10);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(true);

		/**
		 * Action Listeners
		 */

		btnSaveRulesTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileRules.replaceFileContent();
				JOptionPane.showMessageDialog(null, "File edited!");
			}
		});

		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainGui mainGui = new MainGui(fileRules, fileHam, fileSpam);
				frmAuto.setVisible(false);
			}
		});

		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		btnCompileHv.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				HVFile hvfile = new HVFile();

				try {
					hvfile.compile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});

		btnCompileLatex.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				LatexFile latexfile = new LatexFile();

				try {
					latexfile.compile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});

		textField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sorter.setRowFilter(RowFilter.regexFilter("(?i)" + textField.getText()));
			}
		});

		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ConfGui confGui = new ConfGui(fileRules, fileHam, fileSpam, 1);
				frmAuto.setVisible(false);
			}
		});

		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Run With Auto config
				JOptionPane.showMessageDialog(null, "Running experiment please wait till the message appears!");
				runExperiment();
				jmetalalreadyrunned = true;

				btnCompileHv.setEnabled(true);
				btnCompileLatex.setEnabled(true);

			}
		});
	}

	private void tabelUpdate() {
		// Called When a change occurs in the table to update its data
		model.setRowCount(0);
		for (Entry<String, Rule> entry : fileRules.getHmapRules().entrySet()) {
			model.addRow(new Object[] { entry.getKey(), entry.getValue().getRuleWeight() });
		}
	}

	public void runExperiment() {
		// Run The experiment with the auto settings
		AntiSpamFilterProblem problem = new AntiSpamFilterProblem(fileHam, fileSpam, fileRules);

		AntiSpamFilterAutomaticConfiguration antiSpamConfig = new AntiSpamFilterAutomaticConfiguration(problem);
		try {
			antiSpamConfig.runSolution();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		double FP = fileHam.calculateFPorFN(fileRules.getHmapRules());
		double FN = fileSpam.calculateFPorFN(fileRules.getHmapRules());
		lblNewLabel.setText("FP: " + FP); // actualizar os valores
		lblNewLabel_1.setText("FN: " + FN); // actualizar os valores
		tabelUpdate();

		JOptionPane.showMessageDialog(null, "Experiment complete!");
	}

}
