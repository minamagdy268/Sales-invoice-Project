package com.sales.controller;

import com.sales.model.Invoice_Hed;
import com.sales.model.InvoiceHedTable;
import com.sales.model.LineClass;
import com.sales.model.LinesTable;
import com.sales.view.InvoiceHeaderD;
import com.sales.view.InvoiceFrame;
import com.sales.view.InvoiceLineD;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class salesInvController implements ActionListener, ListSelectionListener {

    private InvoiceFrame frm;
    private InvoiceHeaderD invoiceDialog;
    private InvoiceLineD lineDialog;

    public salesInvController(InvoiceFrame frame) {
        this.frm = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        System.out.println("Action: " + actionCommand);
        switch (actionCommand) {
            case "Load File":
                loadFile();
                break;
            case "Create New Invoice":
                createNewInvoice();
                break;
            case "Delete Invoice":
                deleteInvoice();
                break;
            case "Create New Item":
                createNewItemLine();
                break;
            case "Delete Item":
                deleteItemLine();
                break;
            case "createInvoiceCancel":
                createInvoiceCancel();
                break;
            case "createInvoiceAdd":
                createInvoiceOK();
                break;
            case "createLineAdd":
                createLineOK();
                break;
            case "createLineCancel":
                createLineCancel();
                break;
            case "Save File":
                saveFile();
                break;
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int selectedIndex = frm.getInvoiceTable().getSelectedRow();
        if (selectedIndex != -1) {
            System.out.println("You have selected row: " + selectedIndex);
            Invoice_Hed currentInvoice = frm.getInvoices().get(selectedIndex);
            frm.getInvoiceNumLabel().setText("" + currentInvoice.getIdNumber());
            frm.getInvoiceDateLabel1().setText("" + currentInvoice.getInvoiceDate());
            frm.getCustomerNameLabel().setText(currentInvoice.getCustomerName());
            frm.getInvoiceTotalLabel().setText("" + currentInvoice.getInvoiceTotal());
            LinesTable linesTableModel = new LinesTable(currentInvoice.getLines());
            frm.getLineTable().setModel(linesTableModel);
            linesTableModel.fireTableDataChanged();
        }
    }

    private void loadFile() {
        JFileChooser fc = new JFileChooser();

        try {
            JOptionPane.showMessageDialog(frm, "Select Invoice Header File",
                    "Information Message", JOptionPane.INFORMATION_MESSAGE);
            int result = fc.showOpenDialog(frm);
            if (result == JFileChooser.APPROVE_OPTION) {
                File headerFile = fc.getSelectedFile();
                Path headerPath = Paths.get(headerFile.getAbsolutePath());
                List<String> headerLines = Files.readAllLines(headerPath);
                System.out.println("Invoices have been read");
                ArrayList<Invoice_Hed> invoicesArray = new ArrayList<>();
                for (String headerLine : headerLines) {
                    try {
                        String[] headerParts = headerLine.split(",");
                        int invoiceNum = Integer.parseInt(headerParts[0]);
                        String invoiceDate = headerParts[1];
                        String customerName = headerParts[2];

                        Invoice_Hed invoice = new Invoice_Hed(invoiceNum, invoiceDate, customerName);
                        invoicesArray.add(invoice);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frm, "Error in line format", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                System.out.println("Check point");
                JOptionPane.showMessageDialog(frm, "Select Invoice Line File",
                        "Information Message", JOptionPane.INFORMATION_MESSAGE);
                result = fc.showOpenDialog(frm);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File lineFile = fc.getSelectedFile();
                    Path linePath = Paths.get(lineFile.getAbsolutePath());
                    List<String> lineLines = Files.readAllLines(linePath);
                    System.out.println("Lines have been read");
                    for (String lineLine : lineLines) {
                        try {
                            String lineParts[] = lineLine.split(",");
                            int invoiceNum = Integer.parseInt(lineParts[0]);
                            String itemName = lineParts[1];
                            double itemPrice = Double.parseDouble(lineParts[2]);
                            int count = Integer.parseInt(lineParts[3]);
                            Invoice_Hed inv = null;
                            for (Invoice_Hed invoice : invoicesArray) {
                                if (invoice.getIdNumber() == invoiceNum) {
                                    inv = invoice;
                                    break;
                                }
                            }

                            LineClass line = new LineClass(itemName, itemPrice, count, inv);
                            inv.getLines().add(line);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(frm, "Error in line format", "Error", JOptionPane.ERROR_MESSAGE);
                    
                        }
                    }
                    System.out.println("Check point");
                }
                frm.setInvoices(invoicesArray);
                InvoiceHedTable invoicesTableModel = new InvoiceHedTable(invoicesArray);
                frm.setInvoicesTableModel(invoicesTableModel);
                frm.getInvoiceTable().setModel(invoicesTableModel);
                frm.getInvoicesTableModel().fireTableDataChanged();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frm, "Cannot read file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveFile() {
        ArrayList<Invoice_Hed> invoices = frm.getInvoices();
        String headers = "";
        String lines = "";
        for (Invoice_Hed invoice : invoices) {
            String invCSV = invoice.getAsCSV();
            headers += invCSV;
            headers += "\n";

            for (LineClass line : invoice.getLines()) {
                String lineCSV = line.getAsCSV();
                lines += lineCSV;
                lines += "\n";
            }
        }
        System.out.println("Check point");
        
        try {
            JFileChooser fc = new JFileChooser();
            int result = fc.showSaveDialog(frm);
            if (result == JFileChooser.APPROVE_OPTION) {
                File headerFile = fc.getSelectedFile();
                FileWriter hfw = new FileWriter(headerFile);
                hfw.write(headers);
                hfw.flush();
                hfw.close();
                result = fc.showSaveDialog(frm);
                JOptionPane.showMessageDialog(frm, "File saved successfully",
           "Information Message", JOptionPane.INFORMATION_MESSAGE);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File lineFile = fc.getSelectedFile();
                    FileWriter lfw = new FileWriter(lineFile);
                    lfw.write(lines);
                    lfw.flush();
                    lfw.close();
                }
            }
        } catch (Exception ex) {
            

        }
    }

    private void createNewInvoice() {
        invoiceDialog = new InvoiceHeaderD(frm);
        invoiceDialog.setVisible(true);
    }
    //Create InvoiceDialog method

    private void deleteInvoice() {
        int selectedRow = frm.getInvoiceTable().getSelectedRow();
        if (selectedRow != -1) {
            frm.getInvoices().remove(selectedRow);
            frm.getInvoicesTableModel().fireTableDataChanged();
        }
    }

    private void createNewItemLine() {
        lineDialog = new InvoiceLineD(frm);
        lineDialog.setVisible(true);
    }

    private void deleteItemLine() {
        int selectedRow = frm.getLineTable().getSelectedRow();

        if (selectedRow != -1) {
            LinesTable linesTableModel = (LinesTable) frm.getLineTable().getModel();
            linesTableModel.getLines().remove(selectedRow);
            linesTableModel.fireTableDataChanged();
            frm.getInvoicesTableModel().fireTableDataChanged();
        }
    }

    private void createInvoiceCancel() {
        invoiceDialog.setVisible(false);
        invoiceDialog.dispose();
        invoiceDialog = null;
    }

    private void createInvoiceOK() {
        String date = invoiceDialog.getInvDateField().getText();
        String customer = invoiceDialog.getCustNameField().getText();
        int num = frm.getNextInvoiceNum();
        try {
            String[] dateParts = date.split("-");  // 
            if (dateParts.length < 3) {
                JOptionPane.showMessageDialog(frm, "Wrong date format", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int year = Integer.parseInt(dateParts[2]);
                if (day > 31 || month > 12) {
                    JOptionPane.showMessageDialog(frm, "Wrong date format", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    Invoice_Hed invoice = new Invoice_Hed(num, date, customer);
                    frm.getInvoices().add(invoice);
                    frm.getInvoicesTableModel().fireTableDataChanged();
                    invoiceDialog.setVisible(false);
                    invoiceDialog.dispose();
                    invoiceDialog = null;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frm, "Wrong date format", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void createLineOK() {
        String item = lineDialog.getItemNameField().getText();
        String countStr = lineDialog.getItemCountField().getText();
        String priceStr = lineDialog.getItemPriceField().getText();
        int count = Integer.parseInt(countStr);
        double price = Double.parseDouble(priceStr);
        int selectedInvoice = frm.getInvoiceTable().getSelectedRow();
        if (selectedInvoice != -1) {
            Invoice_Hed invoice = frm.getInvoices().get(selectedInvoice);
            LineClass line = new LineClass(item, price, count, invoice);
            invoice.getLines().add(line);
            LinesTable linesTableModel = (LinesTable) frm.getLineTable().getModel();
            linesTableModel.fireTableDataChanged();
            frm.getInvoicesTableModel().fireTableDataChanged();
        }
        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null;
    }

    private void createLineCancel() {
        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null;
    }

}
