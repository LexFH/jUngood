package jungood;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JFileChooser;

/**
 *
 * @author LexFH
 */
public final class MainGui extends javax.swing.JFrame {

    private String[] langs;
    private static final String[] versions = new String[]{"(REV", "(V"};
    private static final String[] noGoods = new String[]{"([o)", "[hI", "(Beta, (Alpha", "[f", "[p", "[c"};
    private static final String[] goods = new String[]{"[!]", "[C]"};
    private static String output;

    /**
     * Creates new form MainGui
     */
    public MainGui() {
        this.initComponents();
        this.jtfPath.setText(System.getProperty("user.home"));
    }

    private Boolean isInteresting(final File file) {
        final String tags = getTags(file.getName());
        if (file.getName().contains(" by ")
                || file.getName().contains("-in-1")
                || file.getName().contains("BIOS")
                || tags.contains("Prototype")
                || tags.contains("Hack")
                || tags.contains("Debug")
                || tags.contains("Demo")
                || tags.contains("[b")
                || tags.contains("[h")
                || tags.contains("[t")
                || (jcbKeepPD.isSelected() && tags.contains("(PD)"))) {
            if (jcbVerbose.isSelected()) {
                output += "\n  - rejecting " + file.getName();
            }
            return Boolean.FALSE;
        }
        if (jcbVerbose.isSelected()) {
            output += "\n  + retaining " + file.getName();
        }
        return Boolean.TRUE;
    }

    private static String getGroupName(final String string) {
        final int par = string.indexOf("(");
        final int brack = string.indexOf("[");
        if (par > 0 && brack > 0) {
            return string.substring(0, Math.min(par, brack) - 1);
        }
        if (par > 0) {
            return string.substring(0, par - 1);
        }
        try {
            return string.substring(0, brack - 1);
        } catch (final StringIndexOutOfBoundsException ex) {
            return string;
        }
    }

    private static String getTags(final String string) {
        final int par = string.indexOf("(");
        final int brack = string.indexOf("[");
        if (par > 0 && brack > 0) {
            return string.substring(Math.min(par, brack) - 1);
        }
        if (par > 0) {
            return string.substring(par - 1);
        }
        try {
            return string.substring(brack - 1);
        } catch (final IndexOutOfBoundsException ex) {
            return "";
        }
    }

    private final class GroupComparator implements Comparator<File> {

        @Override
        public int compare(final File a, final File b) {
            final String aName = a.getName();
            final String bName = b.getName();

            if (jcbVerbose.isSelected()) {
                output += "\n  comparing " + aName;
                output += "\n         to " + bName;
            }

            //compares language
            for (final String crit : langs) {
                if (aName.contains(crit) && !bName.contains(crit)) {
                    if (jcbVerbose.isSelected()) {
                        output += "\n         -> " + aName;
                    }
                    return -1;
                }
                if (bName.contains(crit) && !aName.contains(crit)) {
                    if (jcbVerbose.isSelected()) {
                        output += "\n         -> " + bName;
                    }
                    return 1;
                }
                //prefers newer version of a translation
                if (aName.contains("[T+Fre") && bName.contains("[T+Fre")) {
                    if (jcbVerbose.isSelected()) {
                        output += "\n         -> " + Math.negateExact(aName.compareTo(bName));
                    }
                    return Math.negateExact(aName.compareTo(bName));
                }
                if (aName.contains("[T+Eng") && bName.contains("[T+Eng")) {
                    final int index = Math.negateExact(aName.compareTo(bName));
                    if (jcbVerbose.isSelected()) {
                        output += "\n         -> " + ((index < 0) ? (aName) : (bName));
                    }
                    return index;
                }
                //Tries to get a non japanese or chinese version
                if (aName.contains("[J]") && bName.contains("[J]")) {
                    if (aName.contains("[T+") && !bName.contains("[T+")) {
                        if (jcbVerbose.isSelected()) {
                            output += "\n         -> " + aName;
                        }
                        return -1;
                    }
                    if (bName.contains("[T+") && !aName.contains("[T+")) {
                        if (jcbVerbose.isSelected()) {
                            output += "\n         -> " + bName;
                        }
                        return 1;
                    }
                }
                if (aName.contains("[Ch]") && bName.contains("[Ch]")) {
                    if (aName.contains("[T+") && !bName.contains("[T+")) {
                        if (jcbVerbose.isSelected()) {
                            output += "\n         -> " + aName;
                        }
                        return -1;
                    }
                    if (bName.contains("[T+") && !aName.contains("[T+")) {
                        if (jcbVerbose.isSelected()) {
                            output += "\n         -> " + bName;
                        }
                        return 1;
                    }
                }
                //selects newest version
                if (aName.contains("[T+Fre") && bName.contains("[T+Fre")) {
                    if (jcbVerbose.isSelected()) {
                        output += "\n         -> " + Math.negateExact(aName.compareTo(bName));
                    }
                    return Math.negateExact(aName.compareTo(bName));
                }
            }

            //compares version
            for (final String verTag : versions) {
                if (aName.contains(verTag) && bName.contains(verTag)) {
                    final int index = Math.negateExact(aName.substring(aName.indexOf(verTag), aName.length()).compareTo(bName.substring(bName.indexOf(verTag), bName.length())));
                    if (jcbVerbose.isSelected()) {
                        output += "\n         -> " + ((index < 0) ? (aName) : (bName));
                    }
                    return index;
                }
            }

            //downgrades unpleasant factors
            for (final String crit : noGoods) {
                if (aName.contains(crit) && !bName.contains(crit)) {
                    if (jcbVerbose.isSelected()) {
                        output += "\n         -> " + bName;
                    }
                    return 1;
                }
                if (bName.contains(crit) && !aName.contains(crit)) {
                    if (jcbVerbose.isSelected()) {
                        output += "\n         -> " + aName;
                    }
                    return -1;
                }
            }

            //upgrades good factors
            for (final String crit : goods) {
                if (aName.contains(crit) && !bName.contains(crit)) {
                    if (jcbVerbose.isSelected()) {
                        output += "\n         -> " + aName;
                    }
                    return -1;
                }
                if (bName.contains(crit) && !aName.contains(crit)) {
                    if (jcbVerbose.isSelected()) {
                        output += "\n         -> " + bName;
                    }
                    return 1;
                }
            }
            if (jcbVerbose.isSelected()) {
                output += "\n      equality";
            }
            return 0;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jtfPath = new javax.swing.JTextField();
        jbExplore = new javax.swing.JButton();
        jbGo = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtaConsole = new javax.swing.JTextArea();
        jcbKeepPD = new javax.swing.JCheckBox();
        jcbVerbose = new javax.swing.JCheckBox();
        jcbDelete = new javax.swing.JCheckBox();
        jpLanguage = new javax.swing.JPanel();
        jrbEnglish = new javax.swing.JRadioButton();
        jrbFrancais = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JunGood");

        jbExplore.setText("...");
        jbExplore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbExploreActionPerformed(evt);
            }
        });

        jbGo.setText("Go");
        jbGo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbGoActionPerformed(evt);
            }
        });

        jtaConsole.setEditable(false);
        jtaConsole.setColumns(20);
        jtaConsole.setFont(new java.awt.Font("FreeMono", 0, 12)); // NOI18N
        jtaConsole.setRows(5);
        jScrollPane1.setViewportView(jtaConsole);

        jcbKeepPD.setSelected(true);
        jcbKeepPD.setText("Keep public domain");

        jcbVerbose.setText("Verbose");

        jcbDelete.setForeground(new java.awt.Color(255, 0, 102));
        jcbDelete.setText("Apply filtering and delete from disk");

        jpLanguage.setBorder(javax.swing.BorderFactory.createTitledBorder("Language"));

        jrbEnglish.setSelected(true);
        jrbEnglish.setText("English");
        jrbEnglish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrbEnglishActionPerformed(evt);
            }
        });

        jrbFrancais.setText("Français");
        jrbFrancais.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jrbFrancaisActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpLanguageLayout = new javax.swing.GroupLayout(jpLanguage);
        jpLanguage.setLayout(jpLanguageLayout);
        jpLanguageLayout.setHorizontalGroup(
            jpLanguageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpLanguageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jrbEnglish)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jrbFrancais)
                .addContainerGap())
        );
        jpLanguageLayout.setVerticalGroup(
            jpLanguageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpLanguageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jrbEnglish)
                .addComponent(jrbFrancais))
        );

        jLabel1.setText("Roms :");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jpLanguage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jbGo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtfPath)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbExplore))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jcbDelete)
                        .addGap(0, 321, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jcbKeepPD)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jcbVerbose)))
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtfPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbExplore, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jpLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcbKeepPD)
                    .addComponent(jcbVerbose))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jcbDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jbGo)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jbGoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbGoActionPerformed
        try {
            if (this.jrbEnglish.isSelected()) {
                langs = new String[]{"(U)", "[T+Eng", "(M#)", "(UE)", "(UEB)", "(JU)", "(JUE)", "(W)", "(JUE)", "(UEB)", "(E)"};
            } else {
                langs = new String[]{"(F)", "[T+Fre", "(CF)", "(M#)", "(E)", "(UE)", "(UEB)", "(JE)", "(JUE)", "(EB)", "(UEB)", "(EBK)", "(W)", "(U)", "(JU)", "[T+Eng"};
            }
            final String inputPath = (this.jtfPath.getText().endsWith("/")) ? (this.jtfPath.getText()) : (this.jtfPath.getText() + "/");

            final List<File> retained = new ArrayList();
            final File[] files = new File(inputPath).listFiles();
            this.jtaConsole.setText("Computing things...");
            try {
                output = "Filtering " + files.length + " files";
            } catch (final NullPointerException ex) {
                this.jtaConsole.setText("Path does not exist.");
                return;
            }
            Arrays.sort(files);
            for (final File file : files) {
                if (file.isFile() && isInteresting(file)) {
                    retained.add(file);
                } else if (this.jcbDelete.isSelected()) {
                    file.delete();
                }
            }

            output += "\nGrouping " + retained.size() + " files";
            final List<List<File>> groupedResults = new ArrayList<>();
            try {
                String currentGroup = getGroupName(retained.get(0).getName());
                if (jcbVerbose.isSelected()) {
                    output += "\n  +Creating group " + currentGroup;
                }
                groupedResults.add(new ArrayList<>());
                for (final File file : retained) {
                    final String fileGroup = getGroupName(file.getName());
                    if (!fileGroup.equals(currentGroup)) {
                        currentGroup = fileGroup;
                        if (jcbVerbose.isSelected()) {
                            output += "\n  +Creating group " + currentGroup;
                        }
                        groupedResults.add(new ArrayList<>());
                    }
                    if (jcbVerbose.isSelected()) {
                        output += "\n    Adding " + file.getName();
                    }
                    groupedResults.get(groupedResults.size() - 1).add(file);
                }
            } catch (final IndexOutOfBoundsException ex) {
                this.jtaConsole.setText("Empty directory");
                return;
            }

            output += "\nSelecting best version in " + groupedResults.size() + " groups";
            for (final List<File> group : groupedResults) {
                final GroupComparator languageComparator = new GroupComparator();
                Collections.sort(group, languageComparator);
                output += "\n  + Retaining " + group.get(0).getName();
                for (int i = 1; i < group.size(); i++) {
                    if (jcbVerbose.isSelected()) {
                        output += "\n   - Deleting " + group.get(i).getName();
                    }
                    if (this.jcbDelete.isSelected()) {
                        group.get(i).delete();
                    }
                }
            }
            this.jtaConsole.setText(output);
            this.jtaConsole.setCaretPosition(0);
        } catch (final Exception ex) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            this.jtaConsole.setText(sw.toString());
        }
    }//GEN-LAST:event_jbGoActionPerformed

    private void jrbEnglishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrbEnglishActionPerformed
        this.jrbFrancais.setSelected(!this.jrbEnglish.isSelected());
    }//GEN-LAST:event_jrbEnglishActionPerformed

    private void jrbFrancaisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jrbFrancaisActionPerformed
        this.jrbEnglish.setSelected(!this.jrbFrancais.isSelected());
    }//GEN-LAST:event_jrbFrancaisActionPerformed

    private void jbExploreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbExploreActionPerformed
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.jtfPath.setText(fileChooser.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_jbExploreActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainGui().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbExplore;
    private javax.swing.JButton jbGo;
    private javax.swing.JCheckBox jcbDelete;
    private javax.swing.JCheckBox jcbKeepPD;
    private static javax.swing.JCheckBox jcbVerbose;
    private javax.swing.JPanel jpLanguage;
    private javax.swing.JRadioButton jrbEnglish;
    private javax.swing.JRadioButton jrbFrancais;
    private javax.swing.JTextArea jtaConsole;
    private javax.swing.JTextField jtfPath;
    // End of variables declaration//GEN-END:variables
}
