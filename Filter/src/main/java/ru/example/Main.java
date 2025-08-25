package ru.example;

import org.apache.commons.cli.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {
    private enum Mode {NONE, SHORT, FULL}

    public static void main(String[] args) {
        if (args.length == 0) {
            launchGUI();
        } else {
            run(args);
        }
    }

    private static void launchGUI() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("File Filter Utility");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

            JPanel file1Panel = new JPanel(new FlowLayout());
            JLabel file1Label = new JLabel("Input File 1:");
            JTextField file1Path = new JTextField(30);
            JButton browse1 = new JButton("Browse");
            browse1.addActionListener(e -> chooseFile(file1Path, frame));
            file1Panel.add(file1Label);
            file1Panel.add(file1Path);
            file1Panel.add(browse1);

            JPanel file2Panel = new JPanel(new FlowLayout());
            JLabel file2Label = new JLabel("Input File 2:");
            JTextField file2Path = new JTextField(30);
            JButton browse2 = new JButton("Browse");
            browse2.addActionListener(e -> chooseFile(file2Path, frame));
            file2Panel.add(file2Label);
            file2Panel.add(file2Path);
            file2Panel.add(browse2);

            JPanel outputPanel = new JPanel(new FlowLayout());
            JLabel outputLabel = new JLabel("Output Directory:");
            JTextField outputDir = new JTextField(30);
            JButton browseDir = new JButton("Browse");
            browseDir.addActionListener(e -> chooseDirectory(outputDir, frame));
            outputPanel.add(outputLabel);
            outputPanel.add(outputDir);
            outputPanel.add(browseDir);

            JPanel prefixPanel = new JPanel(new FlowLayout());
            JLabel prefixLabel = new JLabel("Prefix:");
            JTextField prefix = new JTextField(30);
            prefixPanel.add(prefixLabel);
            prefixPanel.add(prefix);

            JPanel optionsPanel = new JPanel(new FlowLayout());
            JCheckBox appendBox = new JCheckBox("Append (-a)");
            JCheckBox shortStats = new JCheckBox("Short stats (-s)");
            JCheckBox fullStats = new JCheckBox("Full stats (-f)");
            optionsPanel.add(appendBox);
            optionsPanel.add(shortStats);
            optionsPanel.add(fullStats);

            ItemListener statsListener = e -> {
                if (e.getSource() == shortStats && shortStats.isSelected()) {
                    fullStats.setSelected(false);
                } else if (e.getSource() == fullStats && fullStats.isSelected()) {
                    shortStats.setSelected(false);
                }
            };
            shortStats.addItemListener(statsListener);
            fullStats.addItemListener(statsListener);

            JButton runButton = new JButton("Run");
            runButton.addActionListener(e -> {
                ArrayList<String> argList = new ArrayList<>();
                if (!outputDir.getText().isEmpty()) {
                    argList.add("-o");
                    argList.add(outputDir.getText());
                }
                if (!prefix.getText().isEmpty()) {
                    argList.add("-p");
                    argList.add(prefix.getText());
                }
                if (appendBox.isSelected()) {
                    argList.add("-a");
                }
                if (shortStats.isSelected()) {
                    argList.add("-s");
                }
                if (fullStats.isSelected()) {
                    argList.add("-f");
                }
                if (!file1Path.getText().isEmpty()) {
                    argList.add(file1Path.getText());
                }
                if (!file2Path.getText().isEmpty()) {
                    argList.add(file2Path.getText());
                }
                if (argList.isEmpty() || (file1Path.getText().isEmpty() && file2Path.getText().isEmpty())) {
                    JOptionPane.showMessageDialog(frame, "Please select at least one input file.");
                    return;
                }
                run(argList.toArray(new String[0]));
                JOptionPane.showMessageDialog(frame, "Processing completed.");
            });

            frame.add(file1Panel);
            frame.add(file2Panel);
            frame.add(outputPanel);
            frame.add(prefixPanel);
            frame.add(optionsPanel);
            frame.add(runButton);

            frame.setVisible(true);
        });
    }

    private static void chooseFile(JTextField textField, JFrame frame) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            textField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private static void chooseDirectory(JTextField textField, JFrame frame) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            textField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private static void run(String[] args) {
        Options options = new Options();
        options.addOption("o", true, "output path");
        options.addOption("p", true, "prefix");
        options.addOption("a", false, "append");
        options.addOption("s", false, "short stats");
        options.addOption("f", false, "full stats");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line: " + e.getMessage());
            System.exit(1);
            return;
        }

        String outputDir = cmd.getOptionValue("o", "");
        String prefix = cmd.getOptionValue("p", "");
        boolean append = cmd.hasOption("a");
        Mode mode = Mode.NONE;
        if (cmd.hasOption("s")) {
            mode = Mode.SHORT;
        }
        if (cmd.hasOption("f")) {
            if (mode != Mode.NONE) {
                System.err.println("Cannot specify both -s and -f");
                System.exit(1);
                return;
            }
            mode = Mode.FULL;
        }

        String[] inputFiles = cmd.getArgs();
        if (inputFiles.length == 0) {
            System.err.println("No input files specified");
            System.exit(1);
            return;
        }

        IntStats intStats = new IntStats();
        FloatStats floatStats = new FloatStats();
        StringStats stringStats = new StringStats();

        BufferedWriter intWriter = null;
        BufferedWriter floatWriter = null;
        BufferedWriter stringWriter = null;

        for (String inputFile : inputFiles) {
            try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    try {
                        BigInteger bi = new BigInteger(line);
                        intStats.add(bi);
                        if (intWriter == null) {
                            intWriter = createWriter(outputDir, prefix, "integers", append);
                        }
                        if (intWriter != null) {
                            try {
                                intWriter.write(line);
                                intWriter.newLine();
                            } catch (IOException e) {
                                System.err.println("Error writing to integers file: " + e.getMessage());
                                closeWriter(intWriter);
                                intWriter = null;
                            }
                        }
                    } catch (NumberFormatException e1) {
                        try {
                            BigDecimal bd = new BigDecimal(line);
                            floatStats.add(bd);
                            if (floatWriter == null) {
                                floatWriter = createWriter(outputDir, prefix, "floats", append);
                            }
                            if (floatWriter != null) {
                                try {
                                    floatWriter.write(line);
                                    floatWriter.newLine();
                                } catch (IOException e) {
                                    System.err.println("Error writing to floats file: " + e.getMessage());
                                    closeWriter(floatWriter);
                                    floatWriter = null;
                                }
                            }
                        } catch (NumberFormatException e2) {
                            stringStats.add(line);
                            if (stringWriter == null) {
                                stringWriter = createWriter(outputDir, prefix, "strings", append);
                            }
                            if (stringWriter != null) {
                                try {
                                    stringWriter.write(line);
                                    stringWriter.newLine();
                                } catch (IOException e) {
                                    System.err.println("Error writing to strings file: " + e.getMessage());
                                    closeWriter(stringWriter);
                                    stringWriter = null;
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading file " + inputFile + ": " + e.getMessage());
            }
        }

        closeWriter(intWriter);
        closeWriter(floatWriter);
        closeWriter(stringWriter);

        if (mode != Mode.NONE) {
            if (intStats.count > 0) {
                System.out.println("Integers:");
                System.out.println("count: " + intStats.count);
                if (mode == Mode.FULL) {
                    System.out.println("min: " + intStats.min);
                    System.out.println("max: " + intStats.max);
                    System.out.println("sum: " + intStats.sum);
                    System.out.println("average: " + intStats.getAverage());
                }
            }
            if (floatStats.count > 0) {
                System.out.println("Floats:");
                System.out.println("count: " + floatStats.count);
                if (mode == Mode.FULL) {
                    System.out.println("min: " + floatStats.min);
                    System.out.println("max: " + floatStats.max);
                    System.out.println("sum: " + floatStats.sum);
                    System.out.println("average: " + floatStats.getAverage());
                }
            }
            if (stringStats.count > 0) {
                System.out.println("Strings:");
                System.out.println("count: " + stringStats.count);
                if (mode == Mode.FULL) {
                    System.out.println("shortest length: " + stringStats.minLen);
                    System.out.println("longest length: " + stringStats.maxLen);
                }
            }
        }
    }

    private static BufferedWriter createWriter(String outputDir, String prefix, String type, boolean append) {
        try {
            Path path = Paths.get(outputDir, prefix + type + ".txt");
            File file = path.toFile();
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            return new BufferedWriter(new FileWriter(file, append));
        } catch (IOException e) {
            System.err.println("Error creating output file for " + type + ": " + e.getMessage());
            return null;
        }
    }

    private static void closeWriter(BufferedWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("Error closing writer: " + e.getMessage());
            }
        }
    }
}