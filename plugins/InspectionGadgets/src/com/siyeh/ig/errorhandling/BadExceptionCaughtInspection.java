package com.siyeh.ig.errorhandling;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BadExceptionCaughtInspection extends ExpressionInspection {
    /** @noinspection PublicField*/
    public String exceptionCheckString =
            "java.lang.NullPointerException," +
            "java.lang.IllegalMonitorStateException," +
            "java.lang.ArrayOutOfBoundsException";

    private final List<Object> exceptionsList = new ArrayList<Object>(32);

    {
        parseExceptionsString();
    }

    public void readSettings(Element element) throws InvalidDataException {
        super.readSettings(element);
        parseExceptionsString();
    }

    private void parseExceptionsString() {
        exceptionsList.clear();
        final String[] strings = exceptionCheckString.split(",");
        for(String string : strings){
            exceptionsList.add(string);
        }
    }

    public void writeSettings(Element element) throws WriteExternalException {
        formatCallCheckString();
        super.writeSettings(element);
    }

    private void formatCallCheckString() {
        final StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for(Object aExceptionsList : exceptionsList){
            if(first){
                first = false;
            } else{
                buffer.append(',');
            }
            final String exceptionName = (String) aExceptionsList;
            buffer.append(exceptionName);
        }
        exceptionCheckString = buffer.toString();
    }

    public String getID(){
        return "ProhibitedExceptionCaught";
    }
    public String getDisplayName() {
        return "Prohibited exception caught";
    }

    public String getGroupDisplayName() {
        return GroupNames.ERRORHANDLING_GROUP_NAME;
    }

    public JComponent createOptionsPanel() {
        final Form form = new Form();
        return form.getContentPanel();
    }

    public String buildErrorString(PsiElement location) {
        return "Prohibited exception '#ref' caught. #loc ";
    }

    public BaseInspectionVisitor buildVisitor() {
        return new BadExceptionCaughtVisitor();
    }

    private class BadExceptionCaughtVisitor extends BaseInspectionVisitor {

        public void visitTryStatement(@NotNull PsiTryStatement statement) {

            super.visitTryStatement(statement);
            final PsiParameter[] catchBlockParameters = statement.getCatchBlockParameters();
            for(final PsiParameter parameter : catchBlockParameters){
                if(parameter == null){
                    continue;
                }
                final PsiType type = parameter.getType();
                if(type == null){
                    continue;
                }
                final String text = type.getCanonicalText();
                if(text == null){
                    continue;
                }
                for(Object aExceptionsList : exceptionsList){
                    final String exceptionClass = (String) aExceptionsList;
                    if(text.equals(exceptionClass)){
                        final PsiTypeElement typeElement = parameter.getTypeElement();
                        registerError(typeElement);
                    }
                }
            }
        }
    }

    /** @noinspection PublicInnerClass*/
    public class Form {
        private JPanel contentPanel;
        private JButton addButton;
        private JButton deleteButton;
        private JTable table;

        public Form() {
            super();
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.setRowSelectionAllowed(true);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.setEnabled(true);
            final ReturnCheckSpecificationTableModel model =
                    new ReturnCheckSpecificationTableModel();
            table.setModel(model);
            addButton.setEnabled(true);
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exceptionsList.add("");
                    model.fireTableStructureChanged();
                }
            });
            deleteButton.setEnabled(true);
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final int[] selectedRows = table.getSelectedRows();
                    Arrays.sort(selectedRows);
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        exceptionsList.remove(selectedRows[i]);
                    }
                    model.fireTableStructureChanged();
                }
            });
        }

        public JComponent getContentPanel() {
            return contentPanel;
        }
    }

    private class ReturnCheckSpecificationTableModel extends AbstractTableModel {

        public int getRowCount() {
            return exceptionsList.size();
        }

        public int getColumnCount() {
            return 1;
        }

        public String getColumnName(int columnIndex) {
            return "Exception class";
        }

        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return exceptionsList.get(rowIndex);
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            exceptionsList.set(rowIndex, aValue);
        }
    }

}
